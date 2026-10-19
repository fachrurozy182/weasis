/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.param;

import org.weasis.dicom.param.DicomProgress;

@FunctionalInterface
public interface ProgressListener {
    public void handleProgression(DicomProgress var1);
}

