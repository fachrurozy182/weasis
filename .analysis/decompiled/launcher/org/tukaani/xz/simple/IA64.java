/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.simple;

import org.tukaani.xz.simple.SimpleFilter;

public final class IA64
implements SimpleFilter {
    private static final int[] BRANCH_TABLE = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 4, 6, 6, 0, 0, 7, 7, 4, 4, 0, 0, 4, 4, 0, 0};
    private final boolean isEncoder;
    private int pos;

    public IA64(boolean isEncoder, int startPos) {
        this.isEncoder = isEncoder;
        this.pos = startPos;
    }

    @Override
    public int code(byte[] buf, int off, int len) {
        int i;
        int end = off + len - 16;
        for (i = off; i <= end; i += 16) {
            int instrTemplate = buf[i] & 0x1F;
            int mask = BRANCH_TABLE[instrTemplate];
            int slot = 0;
            int bitPos = 5;
            while (slot < 3) {
                if ((mask >>> slot & 1) != 0) {
                    int bytePos = bitPos >>> 3;
                    int bitRes = bitPos & 7;
                    long instr = 0L;
                    for (int j = 0; j < 6; ++j) {
                        instr |= ((long)buf[i + bytePos + j] & 0xFFL) << 8 * j;
                    }
                    long instrNorm = instr >>> bitRes;
                    if ((instrNorm >>> 37 & 0xFL) == 5L && (instrNorm >>> 9 & 7L) == 0L) {
                        int src = (int)(instrNorm >>> 13 & 0xFFFFFL);
                        src |= ((int)(instrNorm >>> 36) & 1) << 20;
                        int dest = this.isEncoder ? src + (this.pos + i - off) : (src <<= 4) - (this.pos + i - off);
                        instrNorm &= 0xFFFFFFEE00001FFFL;
                        instrNorm |= ((long)(dest >>>= 4) & 0xFFFFFL) << 13;
                        instr &= (1L << bitRes) - 1L;
                        instr |= (instrNorm |= ((long)dest & 0x100000L) << 16) << bitRes;
                        for (int j = 0; j < 6; ++j) {
                            buf[i + bytePos + j] = (byte)(instr >>> 8 * j);
                        }
                    }
                }
                ++slot;
                bitPos += 41;
            }
        }
        this.pos += (i -= off);
        return i;
    }
}

