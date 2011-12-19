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

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.COMMENTS;

/**
 * @author aphillips
 * @since Jul 31, 2011
 *
 */
public class RegexReplaceTransformer extends TextEntryAsStringTransformer {

	protected static final String defaultTextFilenameRegExp = ".+\\.(cfg | conf | config | ini | properties | props | txt | xml )";

	private static final Logger LOGGER = LoggerFactory.getLogger(RegexReplaceTransformer.class);

	protected static final String PATTERN_TO_FIND_PROPERTY = "pattern";
	protected static final String REPLACEMENT_STRING_PROPERTY = "replacement";

	protected static final String TEXTFILENAMESREGEXP_STRING_PROPERTY = "textFileNamesRegex";

    protected final @Nonnull Pattern patternToFind;
    protected final @Nonnull String replacement;
	protected final @Nonnull Pattern textFileNamesPattern;

    protected RegexReplaceTransformer(Map<String, String> config) {
        super(config);
        patternToFind = Pattern.compile(config.get(PATTERN_TO_FIND_PROPERTY));
        replacement = config.get(REPLACEMENT_STRING_PROPERTY);
	    String regExp = config.get(TEXTFILENAMESREGEXP_STRING_PROPERTY);
	    if (Strings.isNullOrEmpty(regExp)) {
		    regExp =  defaultTextFilenameRegExp;
		    LOGGER.warn("setting default text file regexp {}", defaultTextFilenameRegExp);
	    }
	    textFileNamesPattern = Pattern.compile(regExp, COMMENTS | CASE_INSENSITIVE);
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

	@Override
	public boolean canApply(File f) {
		Matcher textFileNamesMatcher = textFileNamesPattern.matcher(f.getName());
        return textFileNamesMatcher.matches();
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


	@Override
	public String toString() {
		return "RegexReplaceTransformer{" +
				"patternToFind=" + patternToFind +
				", replacement='" + replacement + '\'' +
				"} " + super.toString();
	}


}
