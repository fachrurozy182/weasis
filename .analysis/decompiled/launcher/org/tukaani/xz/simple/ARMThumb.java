/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.simple;

import org.tukaani.xz.simple.SimpleFilter;

public final class ARMThumb
implements SimpleFilter {
    private final boolean isEncoder;
    private int pos;

    public ARMThumb(boolean isEncoder, int startPos) {
        this.isEncoder = isEncoder;
        this.pos = startPos + 4;
    }

    @Override
    public int code(byte[] buf, int off, int len) {
        int i;
        int end = off + len - 4;
        for (i = off; i <= end; i += 2) {
            if ((buf[i + 1] & 0xF8) != 240 || (buf[i + 3] & 0xF8) != 248) continue;
            int src = (buf[i + 1] & 7) << 19 | (buf[i] & 0xFF) << 11 | (buf[i + 3] & 7) << 8 | buf[i + 2] & 0xFF;
            int dest = this.isEncoder ? src + (this.pos + i - off) : (src <<= 1) - (this.pos + i - off);
            buf[i + 1] = (byte)(0xF0 | (dest >>>= 1) >>> 19 & 7);
            buf[i] = (byte)(dest >>> 11);
            buf[i + 3] = (byte)(0xF8 | dest >>> 8 & 7);
            buf[i + 2] = (byte)dest;
            i += 2;
        }
        this.pos += (i -= off);
        return i;
    }
}

