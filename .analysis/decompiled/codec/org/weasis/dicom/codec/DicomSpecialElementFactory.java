/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.codec;

import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSpecialElement;

public interface DicomSpecialElementFactory {
    public String getSeriesMimeType();

    public String[] getModalities();

    default public boolean isHidden() {
        return false;
    }

    public DicomSpecialElement buildDicomSpecialElement(DicomMediaIO var1);
}

