/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.util.ResourceUtil$ResourceIconPath
 */
package org.weasis.dicom.codec;

import org.weasis.core.api.util.ResourceUtil;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSpecialElement;

public abstract class HiddenSpecialElement
extends DicomSpecialElement {
    public HiddenSpecialElement(DicomMediaIO mediaIO) {
        super(mediaIO);
    }

    public abstract ResourceUtil.ResourceIconPath getIconPath();
}

