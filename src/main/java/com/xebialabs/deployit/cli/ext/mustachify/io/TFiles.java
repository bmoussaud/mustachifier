/*
 * @(#)TFiles.java     27 Jul 2011
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
package com.xebialabs.deployit.cli.ext.mustachify.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.transform;

import java.io.File;

import javax.annotation.Nonnull;

import com.google.common.base.Function;

import de.schlichtherle.truezip.file.TFile;

/**
 * @author aphillips
 * @since 27 Jul 2011
 *
 */
public class TFiles {
    private static final Function<File, TFile> TO_TFILE = new Function<File, TFile>() {
        @Override
        public TFile apply(File input) {
            checkArgument(input instanceof TFile, "'%s' is not a TFile", input);
            return (TFile) input;
        }
    };
    
    public static @Nonnull Iterable<TFile> listTEntries(@Nonnull TFile folder) {
        return transform(Files2.listEntriesRecursively(folder), TO_TFILE);
    }

    public static @Nonnull Iterable<TFile> listTFiles(@Nonnull TFile folder) {
        return transform(Files2.listFilesRecursively(folder), TO_TFILE);
    }
}
