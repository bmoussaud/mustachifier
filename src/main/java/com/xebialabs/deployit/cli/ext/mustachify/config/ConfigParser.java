/*
 * @(#)ConfigParser.java     22 Jul 2011
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

import static com.google.common.base.Functions.forMap;
import static com.google.common.collect.ImmutableMap.copyOf;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.filterKeys;
import static com.google.common.collect.Maps.fromProperties;
import static com.google.common.collect.Maps.transformValues;
import static org.apache.commons.lang.StringUtils.substringAfter;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.xebialabs.deployit.cli.ext.mustachify.transform.DarEntryTransformer;


public class ConfigParser implements Supplier<List<DarEntryTransformer>>{
    private static final int MAX_NUM_TRANSFORMERS = Integer.MAX_VALUE;
    
    private final List<DarEntryTransformer> transformers;
    
    public ConfigParser(@Nonnull Properties config, @Nonnull TransformParser transformParser) {
        transformers = parseTransforms(config, transformParser);
    }

    private static List<DarEntryTransformer> parseTransforms(Properties config,
            TransformParser parser) {
        List<DarEntryTransformer> matchers = newLinkedList();
        Map<String, String> configProperties = fromProperties(config);
        
        // rules are numbered beginning with 1
        for (int i = 1; i <= MAX_NUM_TRANSFORMERS; i++) {
            Map<String, String> nthTransformProperties = new RulePropertiesCollector(i).apply(configProperties);
            if (nthTransformProperties.isEmpty()) {
                // only support consecutive numbering
                break;
            }            
            matchers.add(parser.apply(nthTransformProperties));
        }
        return matchers;
    }
    
    // finds the rules for the given index and strips the "transform.N" prefix from the keys
    private static class RulePropertiesCollector implements Function<Map<String, String>, Map<String, String>> {
        private static final String RULE_PROPERTY_PREFIX = "transform.";
        
        private final String indexedRulePrefix;

        private RulePropertiesCollector(int ruleIndex) {
            indexedRulePrefix = RULE_PROPERTY_PREFIX + ruleIndex + '.';
        }
        
        @Override
        public Map<String, String> apply(Map<String, String> from) {
            KeyTransformer<String, String, String> prefixStripper = new KeyTransformer<String, String, String>(
                    new Function<String, String>() {
                        @Override
                        public String apply(String from) {
                            return substringAfter(from, indexedRulePrefix);
                        }
                    });
            // expecting non-null values
            return copyOf(prefixStripper.apply(filterKeys(from, 
                    new Predicate<String>() {
                        @Override
                        public boolean apply(String input) {
                            return input.startsWith(indexedRulePrefix);
                        }
                    })));
        }
    }
    
    // assumes the transform function returns unique values
    private static class KeyTransformer<K1, V, K2> implements Function<Map<K1, V>, Map<K2, V>> {
        private final Function<K1, K2> keyTransform;
        
        private KeyTransformer(Function<K1, K2> keyTransform) {
            this.keyTransform = keyTransform;
        }

        @Override
        public Map<K2, V> apply(Map<K1, V> input) {
            Map<K2, K1> newKeys = Maps.uniqueIndex(input.keySet(), keyTransform);
            return transformValues(newKeys, forMap(input));
        }
    }

    @Override
    public @Nonnull List<DarEntryTransformer> get() {
        return transformers;
    }
    
}
