/*
 * @(#)RuleParser.java     22 Jul 2011
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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.xebialabs.deployit.cli.ext.mustachify.transform.DarEntryTransformer;
import com.xebialabs.deployit.cli.ext.mustachify.transform.DarEntryTransformer.TransformerFactory;

/**
 * @author aphillips
 * @since 22 Jul 2011
 *
 */
public class TransformParser implements Function<Map<String, String>, DarEntryTransformer>{
    private static final String TYPE_PROPERTY = "type";
    
    private final Map<String, TransformerFactory> transformerFactories;
    
    public TransformParser(Collection<TransformerFactory> transformerFactories) {
        this.transformerFactories = Maps.uniqueIndex(transformerFactories, 
                new Function<TransformerFactory, String>() {
                    @Override
                    public String apply(TransformerFactory input) {
                        return input.getTransformerType();
                    }
                });
    }

    @Override
    public @Nonnull DarEntryTransformer apply(@Nonnull Map<String, String> config) {
        String transformerType = config.get(TYPE_PROPERTY);
        checkArgument(transformerType != null, "config property '%s' is required", TYPE_PROPERTY);
        TransformerFactory transformerFactory = transformerFactories.get(transformerType);
        checkArgument(transformerFactory != null, "unknown rule type '%s'", transformerType);
        return transformerFactory.from(config);
    }

}
