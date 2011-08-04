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

import java.io.File;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.fs.FsSyncException;

/**
 * @author aphillips
 * @since 27 Jul 2011
 *
 */
public class DarWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DarWriter.class);
    
    public static void flush(@Nonnull File dar) throws FsSyncException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Flushing changes to DAR '{}'", dar);
        }
        TFile.umount(new TFile(dar));
    }
}
