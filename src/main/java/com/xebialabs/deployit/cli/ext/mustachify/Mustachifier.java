package com.xebialabs.deployit.cli.ext.mustachify;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Maps.filterValues;
import static com.xebialabs.deployit.cli.ext.mustachify.base.Predicates2.is;
import static com.xebialabs.deployit.cli.ext.mustachify.collect.Maps2.fromKeys;
import static com.xebialabs.deployit.cli.ext.mustachify.collect.Maps2.transformKeys;
import static com.xebialabs.deployit.cli.ext.mustachify.dar.DarReader.checkValidDar;
import static com.xebialabs.deployit.cli.ext.mustachify.dar.DarReader.getManifest;
import static com.xebialabs.deployit.cli.ext.mustachify.io.Files2.getTempFilePath;
import static java.lang.String.format;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.xebialabs.deployit.cli.CliObject;
import com.xebialabs.deployit.cli.api.Proxies;
import com.xebialabs.deployit.cli.ext.mustachify.DarEntryTransformerApplier.DarEntry;
import com.xebialabs.deployit.cli.ext.mustachify.config.ConfigParser;
import com.xebialabs.deployit.cli.ext.mustachify.config.TransformParser;
import com.xebialabs.deployit.cli.ext.mustachify.dar.DarManifestParser;
import com.xebialabs.deployit.cli.ext.mustachify.dar.DarManifestParser.DarManifest.DarManifestEntry;
import com.xebialabs.deployit.cli.ext.mustachify.dar.DarReader;
import com.xebialabs.deployit.cli.ext.mustachify.dar.DarWriter;
import com.xebialabs.deployit.cli.ext.mustachify.transform.DarEntryTransformer;
import com.xebialabs.deployit.cli.ext.mustachify.transform.DarEntryTransformer.TransformerFactory;
import com.xebialabs.deployit.cli.ext.mustachify.transform.RegexReplaceTransformer.RegexReplaceTransformerFactory;
import com.xebialabs.deployit.cli.ext.mustachify.transform.StringReplaceTransformer.StringReplaceTransformerFactory;

import de.schlichtherle.truezip.file.TFile;

@CliObject(name = "placeholderformats")
public class Mustachifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(Mustachifier.class);
    
    private static final String CONFIG_FILE_NAME = "mustachifier.properties";
    private static final Properties CONFIG = new Properties();
    
    private static final Collection<TransformerFactory> TRANSFORMER_FACTORIES = 
        ImmutableSet.of(new StringReplaceTransformerFactory(), 
                new RegexReplaceTransformerFactory());
    
    static {
        try {
            CONFIG.load(checkNotNull(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(CONFIG_FILE_NAME), CONFIG_FILE_NAME));
        } catch (Exception exception) {
            LOGGER.error(format("Unable to load configuration file '%s' from classpath", 
                    CONFIG_FILE_NAME), exception);
        }
    }

    private final List<DarEntryTransformer> transformers;
    
    public Mustachifier(Proxies ignored) {
        this(new ConfigParser(CONFIG, new TransformParser(TRANSFORMER_FACTORIES)).get());
    }
    
    @VisibleForTesting
    protected Mustachifier(List<DarEntryTransformer> transformers) {
        this.transformers = transformers;
    }
    
    public File convert(String sourcePath) throws IOException {
        return convert(sourcePath, getTempFilePath(
                getBaseName(sourcePath), "-dar." + getExtension(sourcePath)));
    }
    
    public File convert(String sourcePath, String targetPath) throws IOException {
        File target = new File(targetPath);
        checkArgument(!target.exists(), "Target file '%s' already exists", targetPath);
        return convert(sourcePath, target);
    }
    
    private File convert(String sourcePath, final File target) throws IOException {
        TFile sourceArchive = new TFile(sourcePath);
        checkValidDar(sourceArchive);
        
        // the DAR will be a copy of the source with transformed files
        Files.copy(sourceArchive, target);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Copied source archive {} to new target {}", sourceArchive, target);
        }
        
        Set<DarManifestEntry> manifestEntries = 
            new DarManifestParser(getManifest(sourceArchive)).get().entries;
        
        // only entries with one (or more, which should be a warning condition) kept
        Map<DarManifestEntry, DarEntryTransformer> entriesToTransform = filterValues(
                fromKeys(manifestEntries, new FirstMatchingTransformerOrNull()),
                not(is(DarEntryTransformer.NULL)));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("About to transform entries {}", entriesToTransform.keySet());
        }
        
        Collection<TFile> transformedFiles = new DarEntryTransformerApplier().apply(
                transformKeys(entriesToTransform, new Function<DarManifestEntry, DarEntry>() {
                    @Override
                    public DarEntry apply(DarManifestEntry input) {
                        return new DarEntry(input, 
                                DarReader.getEntry(target, input.jarEntryPath));
                    }
                }));
        DarWriter.flush(target);
        
        if (LOGGER.isInfoEnabled()) {
            for (TFile transformedFile : transformedFiles) {
                LOGGER.info("Transformed entry '{}'", transformedFile);
            }
        }
        return target;
    }
    
    private class FirstMatchingTransformerOrNull implements Function<DarManifestEntry, DarEntryTransformer>{
        @Override
        public DarEntryTransformer apply(final DarManifestEntry input) {
            return Iterables.find(transformers, new Predicate<DarEntryTransformer>() {
                @Override
                public boolean apply(DarEntryTransformer transformer) {
                    boolean matches = transformer.matches(input);
                    if (matches && LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Matched '{}' to DAR entry '{}'", transformer, input);
                    }
                    return matches;
                }
            }, DarEntryTransformer.NULL);
        }
    }
}