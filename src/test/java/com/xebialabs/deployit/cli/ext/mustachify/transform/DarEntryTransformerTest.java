/*
 * @(#)DarTextEntryTransformerTest.java     Jul 31, 2011
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.xebialabs.deployit.cli.ext.mustachify.dar.DarManifestParser.DarManifest.DarManifestEntry;

/**
 * Unit tests for the {@link DarTextEntryTransformer}
 */
public class DarEntryTransformerTest {
    // used in other test classes too
    public static class StubTransformer extends DarEntryTransformer {
        public StubTransformer(Map<String, String> config) {
            super(config);
        }

        @Override
        public void transform(InputStream entryContents,
                OutputStream newEntryContents) throws IOException {
            // do nothing
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void requiresTypeProperty() {
        new StubTransformer(ImmutableMap.<String, String>of());
    }
    
    @Test
    public void matchesOnType() {
        DarEntryTransformer transformer = 
            new StubTransformer(ImmutableMap.of("ci.type", "Dummy"));
        assertTrue("Expected transformer to match input", 
                transformer.matches(newEntry("irrelevant/path", "Dummy")));
    }
    
    @Test
    public void matchesOnPatternIfSpecified() {
        DarEntryTransformer transformer = 
            new StubTransformer(ImmutableMap.of("ci.type", "Dummy",
                    "ci.path.pattern", ".+path"));
        assertTrue("Expected transformer to match input", 
                transformer.matches(newEntry("relevant/path", "Dummy")));
    }
    
    @Test
    public void failsMatchOnNonmatchingPatternIfSpecified() {
        DarEntryTransformer transformer = 
            new StubTransformer(ImmutableMap.of("ci.type", "Dummy",
                    "ci.path.pattern", ".+path"));
        assertFalse("Expected transformer not to match input", 
                transformer.matches(newEntry("nonmatching/path/entry", "Dummy")));
    }
    
    private static DarManifestEntry newEntry(String entryPath, String ciType) {
        Attributes attributes = new Attributes(2);
        attributes.put(new Name(DarManifestEntry.TYPE_ATTRIBUTE_NAME), ciType);
        return DarManifestEntry.fromEntryAttributes(entryPath, attributes);
    }    
}
