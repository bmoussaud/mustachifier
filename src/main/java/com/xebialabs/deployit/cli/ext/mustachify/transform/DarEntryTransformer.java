/*
 * @(#)DarEntryTransformer.java     Jul 28, 2011
 *
 * Copyright © 2010 Andrew Phillips.
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package com.xebialabs.deployit.cli.ext.mustachify.transform;

import com.google.common.collect.ImmutableMap;
import com.xebialabs.deployit.cli.ext.mustachify.dar.DarManifestParser.DarManifest.DarManifestEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;


/**
 * @author aphillips
 * @since Jul 28, 2011
 *
 */
public abstract class DarEntryTransformer {
    private static final String MATCHED_TYPE_PROPERTY = "ci.type";
    private static final String MATCHED_ENTRY_PATH_PROPERTY = "ci.path.pattern";

    // null object - declared after private statics because it requires the MATCHED_TYPE_PROPERTY
    public static final DarEntryTransformer NULL = new DarEntryTransformer(
            ImmutableMap.of(MATCHED_TYPE_PROPERTY, "null")) {
        @Override
        public void transform(@Nonnull InputStream entryContents, 
                OutputStream newEntryContents) throws IOException {
            throw new UnsupportedOperationException("null object should never be called");
        }
    };

    protected final @Nonnull String typeToMatch;
    protected final @Nullable Pattern entryPathPatternToMatch;
    protected final boolean pathIndependentMatch;
    
    protected DarEntryTransformer(@Nonnull Map<String, String> config) {
        validate(config);
        typeToMatch = config.get(MATCHED_TYPE_PROPERTY);
        entryPathPatternToMatch = config.containsKey(MATCHED_ENTRY_PATH_PROPERTY)
                                  ? Pattern.compile(config.get(MATCHED_ENTRY_PATH_PROPERTY))
                                  : null;
        pathIndependentMatch = (entryPathPatternToMatch == null);
    }

    protected void validate(@Nonnull Map<String, String> config) {
        checkConfigProperty(config, MATCHED_TYPE_PROPERTY);
    }
    
    protected static void checkConfigProperty(@Nonnull Map<String, String> config,
            String propertyName) {
        checkArgument(config.containsKey(propertyName), 
                "config property '%s' is required", propertyName);
    }
    
    public boolean matches(@Nonnull DarManifestEntry entry) {
        return (entry.type.equalsIgnoreCase(typeToMatch)
                && (pathIndependentMatch || entryPathPatternToMatch.matcher(entry.jarEntryPath).matches()));
    }


	@Override
	public String toString() {
		return "DarEntryTransformer{" +
				"entryPathPatternToMatch=" + entryPathPatternToMatch +
				", pathIndependentMatch=" + pathIndependentMatch +
				", typeToMatch='" + typeToMatch + '\'' +
				'}';
	}

	/**
     * Streams can be used only during invocation of the method; any calls outside
     * may produce inconsistent results and/or result in exceptions.
     */
    public abstract void transform(@Nonnull InputStream entryContents, 
            @Nonnull OutputStream newEntryContents) throws IOException ;

	public  boolean canApply(File filesToTransform) {
		return true;
	}

	public static interface TransformerFactory {
        @Nonnull String getTransformerType();
        @Nonnull DarEntryTransformer from(@Nonnull Map<String, String> config);
    }
}
