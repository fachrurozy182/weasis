/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.util.ResourceUtil$ResourcePath
 */
package org.weasis.dicom.codec.utils;

import org.weasis.core.api.util.ResourceUtil;

public enum DicomResource implements ResourceUtil.ResourcePath
{
    ATTRIBUTES_VIEW("attributes-view.xml"),
    CALLING_NODES("dicomCallingNodes.xml"),
    LUTS("luts"),
    PRESETS("presets.xml"),
    SERIES_SPITTING_RULES("series-splitting-rules.xml"),
    CGET_SOP_UID("store-tcs.properties");

    private final String path;

    private DicomResource(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }
}

