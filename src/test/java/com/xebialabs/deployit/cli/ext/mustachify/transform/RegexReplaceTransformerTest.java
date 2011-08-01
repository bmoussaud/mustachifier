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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.xebialabs.deployit.cli.ext.mustachify.transform.RegexReplaceTransformer.RegexReplaceTransformerFactory;

/**
 * Unit tests for the {@link RegexReplaceTransformer}
 */
public class RegexReplaceTransformerTest {

    @Test
    public void findsPattern() {
        RegexReplaceTransformer transformer = get("^.*$", "long");
        assertEquals("long", transformer.transform("A long .* string"));
    }
    
    @Test
    public void supportsMatchGroupReplacement() {
        RegexReplaceTransformer transformer = get("(long)", "l$1g");
        assertEquals("A llongg (llongg) string", transformer.transform("A long (long) string"));

    }
    
    private static RegexReplaceTransformer get(String patternToFind, String replacement) {
        return new RegexReplaceTransformer(ImmutableMap.of(
                "type", RegexReplaceTransformerFactory.TRANSFORMER_TYPE,
                "ci.type", "Dummy",
                "encoding", Charsets.UTF_8.toString(),
                "pattern", patternToFind,
                "replacement", replacement));
    }
}
