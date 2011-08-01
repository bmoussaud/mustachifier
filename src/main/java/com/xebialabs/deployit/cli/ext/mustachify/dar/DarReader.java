/*
 * @(#)DarWriter.java     27 Jul 2011
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
package com.xebialabs.deployit.cli.ext.mustachify.dar;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.util.jar.Manifest;

import javax.annotation.Nonnull;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;

/**
 * @author aphillips
 * @since 27 Jul 2011
 *
 */
public class DarReader {
    public static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
    
    public static @Nonnull Manifest getManifest(@Nonnull File dar) throws IOException {
        TFileInputStream manifestEntryStream = 
            new TFileInputStream(getEntry(dar, MANIFEST_PATH));
        try {
            return new Manifest(manifestEntryStream);
        } finally {
            manifestEntryStream.close();
        }
    }
    
    public static @Nonnull TFile getEntry(@Nonnull File dar, @Nonnull String path) {
        checkArgument(new TFile(dar, path).exists(), 
                "DAR '%s' does not contain an entry at '%s'", dar, path);
        return new TFile(dar, path);
    }
    
    public static void checkValidDar(TFile file) {
        checkArgument(file.exists(), "File '%s' does not exist or cannot be read", file);
        checkArgument(file.isArchive(), "File '%s' is not a valid ZIP archive", file);
        checkArgument(new TFile(file, MANIFEST_PATH).exists(), 
                "Archive '%s' does not contain a manifest at '%s'", file, MANIFEST_PATH);
    }
}
