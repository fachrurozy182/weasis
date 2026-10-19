/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lz;

import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.lz.CRC32Hash;
import org.tukaani.xz.lz.LZEncoder;

final class Hash234
extends CRC32Hash {
    private static final int HASH_2_SIZE = 1024;
    private static final int HASH_2_MASK = 1023;
    private static final int HASH_3_SIZE = 65536;
    private static final int HASH_3_MASK = 65535;
    private final int hash4Mask;
    private final int[] hash2Table;
    private final int[] hash3Table;
    private final int[] hash4Table;
    private final int hash4Size;
    private int hash2Value = 0;
    private int hash3Value = 0;
    private int hash4Value = 0;

    static int getHash4Size(int dictSize) {
        int h = dictSize - 1;
        h |= h >>> 1;
        h |= h >>> 2;
        h |= h >>> 4;
        h |= h >>> 8;
        h >>>= 1;
        if ((h |= 0xFFFF) > 0x1000000) {
            h >>>= 1;
        }
        return h + 1;
    }

    static int getMemoryUsage(int dictSize) {
        return (66560 + Hash234.getHash4Size(dictSize)) / 256 + 4;
    }

    Hash234(int dictSize, ArrayCache arrayCache) {
        this.hash2Table = arrayCache.getIntArray(1024, true);
        this.hash3Table = arrayCache.getIntArray(65536, true);
        this.hash4Size = Hash234.getHash4Size(dictSize);
        this.hash4Table = arrayCache.getIntArray(this.hash4Size, true);
        this.hash4Mask = this.hash4Size - 1;
    }

    void putArraysToCache(ArrayCache arrayCache) {
        arrayCache.putArray(this.hash4Table);
        arrayCache.putArray(this.hash3Table);
        arrayCache.putArray(this.hash2Table);
    }

    void calcHashes(byte[] buf, int off) {
        int temp = crcTable[buf[off] & 0xFF] ^ buf[off + 1] & 0xFF;
        this.hash2Value = temp & 0x3FF;
        this.hash3Value = (temp ^= (buf[off + 2] & 0xFF) << 8) & 0xFFFF;
        this.hash4Value = (temp ^= crcTable[buf[off + 3] & 0xFF] << 5) & this.hash4Mask;
    }

    int getHash2Pos() {
        return this.hash2Table[this.hash2Value];
    }

    int getHash3Pos() {
        return this.hash3Table[this.hash3Value];
    }

    int getHash4Pos() {
        return this.hash4Table[this.hash4Value];
    }

    void updateTables(int pos) {
        this.hash2Table[this.hash2Value] = pos;
        this.hash3Table[this.hash3Value] = pos;
        this.hash4Table[this.hash4Value] = pos;
    }

    void normalize(int normalizationOffset) {
        LZEncoder.normalize(this.hash2Table, 1024, normalizationOffset);
        LZEncoder.normalize(this.hash3Table, 65536, normalizationOffset);
        LZEncoder.normalize(this.hash4Table, this.hash4Size, normalizationOffset);
    }
}

