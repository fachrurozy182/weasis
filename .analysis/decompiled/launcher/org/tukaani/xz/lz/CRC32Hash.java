/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lz;

class CRC32Hash {
    private static final int CRC32_POLY = -306674912;
    static final int[] crcTable = new int[256];

    CRC32Hash() {
    }

    static {
        for (int i = 0; i < 256; ++i) {
            int r = i;
            for (int j = 0; j < 8; ++j) {
                if ((r & 1) != 0) {
                    r = r >>> 1 ^ 0xEDB88320;
                    continue;
                }
                r >>>= 1;
            }
            CRC32Hash.crcTable[i] = r;
        }
    }
}

