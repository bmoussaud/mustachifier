/*
 * @(#)DarManifestBuilder.java     21 Jul 2011
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
package com.xebialabs.deployit.cli.ext.mustachify.dar;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.xebialabs.deployit.cli.ext.mustachify.dar.DarManifestParser.DarManifest;
import com.xebialabs.deployit.cli.ext.mustachify.dar.DarManifestParser.DarManifest.DarManifestEntry;


public class DarManifestParser implements Supplier<DarManifest> {
    private static final String PACKAGE_FORMAT_VERSION_ATTRIBUTE_NAME = "Deployit-Package-Format-Version";
    private static final String SUPPORTED_PACKAGE_FORMAT_VERSION = "1.3";
    private static final String APPLICATION_ATTRIBUTE_NAME = "Ci-Application";
    private static final String VERSION_ATTRIBUTE_NAME = "Ci-Version";
    
    private final DarManifest parsedManifest;
    
    public DarManifestParser(Manifest manifest) {
        parsedManifest = parse(manifest);
    }
    
    private static DarManifest parse(Manifest manifest) {
        Attributes mainAttributes = manifest.getMainAttributes();
        validate(mainAttributes);
        
        Iterable<DarManifestEntry> darEntries = filter(transform(manifest.getEntries().entrySet(), 
                new Function<Entry<String, Attributes>, DarManifestEntry>() {
                    @Override
                    public DarManifestEntry apply(Entry<String, Attributes> input) {
                        String entryName = input.getKey();
                        Attributes entryAttributes = input.getValue();
                        return (DarManifestEntry.isDarEntry(entryName, entryAttributes)
                                ? DarManifestEntry.fromEntryAttributes(entryName, entryAttributes)
                                : DarManifestEntry.NULL);
                    }
                }),
                not(new Predicate<Object>() {
                    @Override
                    public boolean apply(Object input) {
                        return (input == DarManifestEntry.NULL);
                    }
                }));
        return new DarManifest(mainAttributes.getValue(APPLICATION_ATTRIBUTE_NAME), 
                mainAttributes.getValue(VERSION_ATTRIBUTE_NAME), copyOf(darEntries));
    }
    
    private static void validate(Attributes mainAttributes) {
        checkArgument(mainAttributes.containsKey(new Name(PACKAGE_FORMAT_VERSION_ATTRIBUTE_NAME)), 
                "manifest does not contain required DAR attribute '%s'", PACKAGE_FORMAT_VERSION_ATTRIBUTE_NAME);
        checkArgument(mainAttributes.getValue(PACKAGE_FORMAT_VERSION_ATTRIBUTE_NAME).equals(SUPPORTED_PACKAGE_FORMAT_VERSION), 
                "unsupported package format version '%s', only '%s' supported", 
                mainAttributes.getValue(PACKAGE_FORMAT_VERSION_ATTRIBUTE_NAME), SUPPORTED_PACKAGE_FORMAT_VERSION);
        checkArgument(mainAttributes.containsKey(new Name(APPLICATION_ATTRIBUTE_NAME)), 
                "manifest does not contain required DAR attribute '%s'", APPLICATION_ATTRIBUTE_NAME);
        checkArgument(mainAttributes.containsKey(new Name(VERSION_ATTRIBUTE_NAME)), 
                "manifest does not contain required DAR attribute '%s'", VERSION_ATTRIBUTE_NAME);
    }
    
    @Override
    public @Nonnull DarManifest get() {
        return parsedManifest;
    }
    
    public static class DarManifest {
        public final String appName;
        public final String version;
        public final Set<DarManifestEntry> entries;
        
        public DarManifest(String appName, String version, Set<DarManifestEntry> entries) {
            this.appName = appName;
            this.version = version;
            this.entries = copyOf(entries);
        }
        
        public static class DarManifestEntry {
            // null object to indicate "no result"
            public static final DarManifestEntry NULL = new DarManifestEntry(EMPTY, null, EMPTY);
            
            @VisibleForTesting
            public static final String TYPE_ATTRIBUTE_NAME = "Ci-Type";
            private static final String NAME_ATTRIBUTE_NAME = "Ci-Name";

            public final @Nonnull String type;
            public final @Nullable String name;
            public final @Nonnull String jarEntryPath;

            private DarManifestEntry(@Nonnull String type, @Nullable String name, @Nonnull String jarEntryPath) {
                this.type = checkNotNull(type, "type");
                this.name = name;
                this.jarEntryPath = checkNotNull(jarEntryPath, "jarEntryPath");
            }
            
            private static boolean isDarEntry(@Nonnull String manifestEntryName, 
                    @Nonnull Attributes entryAttributes) {
                return isNotEmpty(entryAttributes.getValue(TYPE_ATTRIBUTE_NAME));
            }
            
            @VisibleForTesting
            public static @Nonnull DarManifestEntry fromEntryAttributes(
                    @Nonnull String manifestEntryName, @Nonnull Attributes entryAttributes) {
                checkArgument(isDarEntry(manifestEntryName, entryAttributes), 
                        "'%s' is not a valid DAR entry", manifestEntryName);
                return new DarManifestEntry(entryAttributes.getValue(TYPE_ATTRIBUTE_NAME), 
                        entryAttributes.getValue(NAME_ATTRIBUTE_NAME), manifestEntryName);
            }

            @Override
            public String toString() {
                return "DarManifestEntry [type=" + type + ", name=" + name
                        + ", jarEntryPath=" + jarEntryPath + "]";
            }
        }
    }
}