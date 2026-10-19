/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling.helper;

public enum CompressionMode {
    NONE,
    GZ,
    ZIP,
    XZ;

    public static String GZ_SUFFIX;
    public static String XZ_SUFFIX;
    public static String ZIP_SUFFIX;

    static {
        GZ_SUFFIX = ".gz";
        XZ_SUFFIX = ".xz";
        ZIP_SUFFIX = ".zip";
    }
}

