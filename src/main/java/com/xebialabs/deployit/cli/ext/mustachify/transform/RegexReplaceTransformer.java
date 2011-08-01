/*
 * @(#)StringReplaceTransformer.java     Jul 31, 2011
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

import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * @author aphillips
 * @since Jul 31, 2011
 *
 */
public class RegexReplaceTransformer extends TextEntryAsStringTransformer {
    protected static final String PATTERN_TO_FIND_PROPERTY = "pattern";
    protected static final String REPLACEMENT_STRING_PROPERTY = "replacement";
    
    protected final @Nonnull Pattern patternToFind;
    protected final @Nonnull String replacement;
    
    protected RegexReplaceTransformer(Map<String, String> config) {
        super(config);
        patternToFind = Pattern.compile(config.get(PATTERN_TO_FIND_PROPERTY));
        replacement = config.get(REPLACEMENT_STRING_PROPERTY);
    }
    
    @Override
    protected void validate(Map<String, String> config) {
        super.validate(config);
        checkConfigProperty(config, PATTERN_TO_FIND_PROPERTY); 
        checkConfigProperty(config, REPLACEMENT_STRING_PROPERTY);
    }
    
    /* (non-Javadoc)
     * @see com.xebialabs.deployit.cli.ext.mustachify.transform.TextEntryAsStringTransformer#transform(java.lang.String)
     */
    @Override
    protected String transform(String entryContents) {
        return patternToFind.matcher(entryContents).replaceAll(replacement);
    }
    
    public static class RegexReplaceTransformerFactory implements TransformerFactory {
        public static final String TRANSFORMER_TYPE = "regex-replace";

        @Override
        public String getTransformerType() {
            return TRANSFORMER_TYPE;
        }

        public DarEntryTransformer from(Map<String, String> config) {
            return new RegexReplaceTransformer(config);
        }
    }

}
