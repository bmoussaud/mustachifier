/*
 * @(#)DarTextEntryTransformer.java     Jul 31, 2011
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.SortedMap;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author aphillips
 * @since Jul 31, 2011
 *
 */
public abstract class DarTextEntryTransformer extends DarEntryTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DarTextEntryTransformer.class);

    private static final String ENCODING_PROPERTY = "encoding";
    private static final SortedMap<String, Charset> AVAILABLE_CHARSETS = Charset.availableCharsets();

    protected final @Nonnull Charset encoding;
    
    protected DarTextEntryTransformer(@Nonnull Map<String, String> config) {
        super(config);
        encoding = AVAILABLE_CHARSETS.get(config.get(ENCODING_PROPERTY));
    }
    
    @Override
    protected void validate(Map<String, String> config) {
        super.validate(config);
        checkConfigProperty(config, ENCODING_PROPERTY);
        checkArgument(AVAILABLE_CHARSETS.containsKey(config.get(ENCODING_PROPERTY)), 
                "charset '%s' is not available on this system", config.get(ENCODING_PROPERTY));
    }
    
    @Override
    public void transform(@Nonnull InputStream entryBytes, @Nonnull OutputStream newEntryBytes) 
            throws IOException {
        Writer newEntryContents = new BufferedWriter(new OutputStreamWriter(newEntryBytes, encoding));
        transform(new BufferedReader(new InputStreamReader(entryBytes, encoding)), 
                  newEntryContents);
        // OutputStreamWriter buffers and needs to be flushed to ensure writes to disk
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Flushing transformed contents to output stream '{}'", newEntryContents);
        }
        newEntryContents.flush();
    }

	@Override
	public String toString() {
		return "DarTextEntryTransformer{" +
				"encoding=" + encoding +
				"} " + super.toString();
	}

	protected abstract void transform(@Nonnull Reader entryContents,
            @Nonnull Writer newEntryContents) throws IOException;
}
