/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.codec;

import java.io.File;
import java.util.List;

@FunctionalInterface
public interface FilesExtractor {
    public List<File> getExtractFiles();
}

