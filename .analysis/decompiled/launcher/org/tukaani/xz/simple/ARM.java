/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.simple;

import org.tukaani.xz.simple.SimpleFilter;

public final class ARM
implements SimpleFilter {
    private final boolean isEncoder;
    private int pos;

    public ARM(boolean isEncoder, int startPos) {
        this.isEncoder = isEncoder;
        this.pos = startPos + 8;
    }

    @Override
    public int code(byte[] buf, int off, int len) {
        int i;
        int end = off + len - 4;
        for (i = off; i <= end; i += 4) {
            if ((buf[i + 3] & 0xFF) != 235) continue;
            int src = (buf[i + 2] & 0xFF) << 16 | (buf[i + 1] & 0xFF) << 8 | buf[i] & 0xFF;
            int dest = this.isEncoder ? src + (this.pos + i - off) : (src <<= 2) - (this.pos + i - off);
            buf[i + 2] = (byte)((dest >>>= 2) >>> 16);
            buf[i + 1] = (byte)(dest >>> 8);
            buf[i] = (byte)dest;
        }
        this.pos += (i -= off);
        return i;
    }
}

