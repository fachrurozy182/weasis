/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.common;

public class Util {
    public static final int STREAM_HEADER_SIZE = 12;
    public static final long BACKWARD_SIZE_MAX = 0x400000000L;
    public static final int BLOCK_HEADER_SIZE_MAX = 1024;
    public static final long VLI_MAX = Long.MAX_VALUE;
    public static final int VLI_SIZE_MAX = 9;

    public static int getVLISize(long num) {
        int size = 0;
        do {
            ++size;
        } while ((num >>= 7) != 0L);
        return size;
    }
}

