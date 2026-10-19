/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.param;

import java.util.List;
import org.weasis.dicom.param.DicomNode;

public interface DicomNodeSource {
    public String getGroupName();

    public List<DicomNode> getNodes();
}

