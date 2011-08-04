/*
 * @(#)PlainArchiveConverterTest.java     23 Jul 2011
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
package com.xebialabs.deployit.cli.ext.mustachify;

import static com.google.common.base.Charsets.ISO_8859_1;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.ByteStreams.toByteArray;
import static com.xebialabs.deployit.cli.ext.mustachify.dar.DarReader.getEntry;
import static com.xebialabs.deployit.cli.ext.mustachify.io.Files2.deleteOnExit;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Test;

import com.xebialabs.deployit.cli.api.Proxies;

import de.schlichtherle.truezip.file.TFileInputStream;

/**
 * Unit tests for the {@link Mustachifier}
 */
public class MustachifierTest {
    public static final String TEST_ARCHIVE_PATH = "src/test/resources/sample-dar.zip";
    
    private Mustachifier converter = new Mustachifier((Proxies) null);
    private File result;
    
    @Test(expected = IllegalArgumentException.class)
    public void requiresSourceArchiveToExist() throws IOException {
        converter.convert("non-existent/path");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void requiresTargetToNotExist() throws IOException {
        converter.convert(TEST_ARCHIVE_PATH, "src/test/resources/mustachifier.properties");
    }
    
    @Test
    public void transformsMatchedFileEntries() throws IOException {
        result = converter.convert(TEST_ARCHIVE_PATH);
        // source was "The ${placeholder} should become {{placeholder}}."
        assertEquals("The {{placeholder}} should become {{placeholder}}.", 
                getContents(result, "script.sql", UTF_8));
    }
    
    private static String getContents(File dar, String entryPath, Charset charset) throws IOException {
        TFileInputStream entryBytes = new TFileInputStream(getEntry(dar, entryPath));
        try {
            return new String(toByteArray(entryBytes), charset);
        } finally {
            entryBytes.close();
        }
    }
    
    @Test
    public void transformsFilesInMatchedFolderEntries() throws IOException {
        result = converter.convert(TEST_ARCHIVE_PATH);
        // source was "The 'foo' here sh\u014Duld be replaced." (note the UTF-8 char)
        assertEquals("The 'bar' here sh\u014Duld be replaced.", 
                getContents(result, "config-UTF8/config.txt", UTF_8));
        assertEquals("The 'bar' here sh\u014Duld be replaced.", 
                getContents(result, "config-UTF8/config2.txt", UTF_8));
        assertEquals("The 'bar' here sh\u014Duld be replaced.", 
                getContents(result, "config-UTF8/subfolder/config3.txt", UTF_8));
    }

    @Test
    public void usesSpecifiedEncoding() throws IOException {
        result = converter.convert(TEST_ARCHIVE_PATH);
        // source was "The 'foo' here should be replaced."
        assertEquals("The 'bar' here should be replaced.", 
                getContents(result, "config-ISO8859/config.txt", ISO_8859_1));
        assertEquals("The 'bar' here should be replaced.", 
                getContents(result, "config-ISO8859/config2.txt", ISO_8859_1));
        assertEquals("The 'bar' here should be replaced.", 
                getContents(result, "config-ISO8859/subfolder/config3.txt", ISO_8859_1));
    }

    @Test
    public void ignoresUnmatchedEntries() throws IOException {
        result = converter.convert(TEST_ARCHIVE_PATH);
        // source was "Neither the 'foo' nor the ${PLACEHOLDER} here should be replaced."
        assertEquals("Neither the 'foo' nor the ${PLACEHOLDER} here should be replaced.", 
                getContents(result, "sample.war", UTF_8));
    }
    
    @Test
    public void ignoresNonDarEntries() throws IOException {
        result = converter.convert(TEST_ARCHIVE_PATH);
        // source was "The 'foo' here should *not* be replaced."
        assertEquals("The 'foo' here should *not* be replaced.", 
                getContents(result, "other-text/config.txt", UTF_8));
    }
    
    @After
    public void removeResult() {
        deleteOnExit(result);
    }
}
