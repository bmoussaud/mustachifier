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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * Unit tests for the {@link DarTextEntryTransformer}
 */
public class DarTextEntryTransformerTest {
    private static class StubTextTransformer extends DarTextEntryTransformer {
        private StubTextTransformer(Map<String, String> config) {
            super(config);
        }

        @Override
        protected void transform(Reader entryContents, Writer newEntryContents)
                throws IOException {
            // do nothing
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void requiresAvailableEncoding() {
        // Cp1047 = Latin-1 character set for EBCDIC hosts 
        new StubTextTransformer(ImmutableMap.of("type", "stub", "ci.type", "Dummy",
                "encoding", "unavailable"));
    }
}
