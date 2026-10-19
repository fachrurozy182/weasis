/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.mf;

public enum QueryMode {
    DB,
    DICOM,
    DICOM_WEB;

    public static final QueryMode DEFAULT;

    public static QueryMode fromValue(String value) {
        if (value != null) {
            for (QueryMode mode : QueryMode.values()) {
                if (!mode.name().equalsIgnoreCase(value.trim())) continue;
                return mode;
            }
        }
        return DEFAULT;
    }

    static {
        DEFAULT = DICOM;
    }
}

