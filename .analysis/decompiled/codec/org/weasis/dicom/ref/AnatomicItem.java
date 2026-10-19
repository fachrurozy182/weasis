/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.ref;

import org.weasis.dicom.macro.ItemCode;

public interface AnatomicItem
extends ItemCode {
    public String getLegacyCode();

    public boolean isPaired();

    default public boolean isContextGroupExtension() {
        return false;
    }
}

