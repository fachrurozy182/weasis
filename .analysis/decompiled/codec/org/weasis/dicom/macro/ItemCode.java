/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.macro;

import org.weasis.dicom.ref.CodingScheme;

public interface ItemCode {
    public String getCodeValue();

    public String getCodeMeaning();

    public CodingScheme getCodingScheme();
}

