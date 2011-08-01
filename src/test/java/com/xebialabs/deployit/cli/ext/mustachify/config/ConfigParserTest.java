/*
 * @(#)ConfigParserTest.java     Jul 31, 2011
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
package com.xebialabs.deployit.cli.ext.mustachify.config;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.xebialabs.deployit.cli.ext.mustachify.transform.DarEntryTransformer;
import com.xebialabs.deployit.cli.ext.mustachify.transform.DarEntryTransformer.TransformerFactory;
import com.xebialabs.deployit.cli.ext.mustachify.transform.DarEntryTransformerTest.StubTransformer;

/**
 * Unit tests for the {@link ConfigParser}
 */
public class ConfigParserTest {

    // matches all transformers of type 'stub'
    private static class StubTransformerFactory implements TransformerFactory {
        private static final String TRANSFORM_TYPE = "stub";
        
        @Override
        public String getTransformerType() {
            return TRANSFORM_TYPE;
        }
        
        @Override
        public DarEntryTransformer from(Map<String, String> config) {
            return new StubTransformer(config);
        }
    }
    
    @Test
    public void processesTransformsUpToFirstMissingIndex() {
        Properties config = new Properties();
        // 8 properties could be 4 rules - 'ci.type' and 'type' are the only required values
        config.put("transform.1.type", StubTransformerFactory.TRANSFORM_TYPE);
        config.put("transform.1.ci.type", "Dummy");
        config.put("transform.2.type", StubTransformerFactory.TRANSFORM_TYPE);
        config.put("transform.2.ci.type", "Dummy");
        config.put("transform.3.type", StubTransformerFactory.TRANSFORM_TYPE);
        config.put("transform.3.ci.type", "Dummy");
        config.put("transform.3.other.prop", "Dummy");
        config.put("transform.3.other.prop.2", "Dummy");
        assertEquals(3, new ConfigParser(config, 
                new TransformParser(ImmutableSet.<TransformerFactory>of(new StubTransformerFactory())))
                .get().size());
    }
}
