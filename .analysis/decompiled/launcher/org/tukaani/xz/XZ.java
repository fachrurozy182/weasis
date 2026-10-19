/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

public class XZ {
    public static final byte[] HEADER_MAGIC = new byte[]{-3, 55, 122, 88, 90, 0};
    public static final byte[] FOOTER_MAGIC = new byte[]{89, 90};
    public static final int CHECK_NONE = 0;
    public static final int CHECK_CRC32 = 1;
    public static final int CHECK_CRC64 = 4;
    public static final int CHECK_SHA256 = 10;

    private XZ() {
    }
}

