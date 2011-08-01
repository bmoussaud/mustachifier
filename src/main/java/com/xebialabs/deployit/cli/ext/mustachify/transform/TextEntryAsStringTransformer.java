/*
 * @(#)TextEntryAsStringTransformer.java     Jul 31, 2011
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

import javax.annotation.Nonnull;

import com.google.common.io.CharStreams;

/**
 * <strong>Use with caution!</strong> Reads the entire entry into memory, so not
 * suitable for large files.
 */
public abstract class TextEntryAsStringTransformer extends DarTextEntryTransformer {

    protected TextEntryAsStringTransformer(Map<String, String> config) {
        super(config);
    }

    @Override
    protected void transform(@Nonnull Reader entryContentStream, 
            @Nonnull Writer newEntryContentStream) throws IOException {
        newEntryContentStream.write(transform(CharStreams.toString(entryContentStream)));
    }
    
    protected abstract @Nonnull String transform(@Nonnull String entryContents);
}
