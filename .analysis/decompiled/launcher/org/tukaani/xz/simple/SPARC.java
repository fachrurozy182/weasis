/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.simple;

import org.tukaani.xz.common.ByteArrayView;
import org.tukaani.xz.simple.SimpleFilter;

public final class SPARC
implements SimpleFilter {
    private final boolean isEncoder;
    private int pos;

    public SPARC(boolean isEncoder, int startPos) {
        this.isEncoder = isEncoder;
        this.pos = startPos;
    }

    @Override
    public int code(byte[] buf, int off, int len) {
        int i;
        int end = off + len - 4;
        for (i = off; i <= end; i += 4) {
            if ((buf[i] != 64 || (buf[i + 1] & 0xC0) != 0) && (buf[i] != 127 || (buf[i + 1] & 0xC0) != 192)) continue;
            int src = ByteArrayView.getIntBE(buf, i);
            int pc = this.pos + i - off >>> 2;
            if (!this.isEncoder) {
                pc = -pc;
            }
            int dest = src + pc;
            dest <<= 9;
            dest >>= 9;
            dest = 0x40000000 | dest & 0x3FFFFFFF;
            ByteArrayView.setIntBE(buf, i, dest);
        }
        this.pos += (i -= off);
        return i;
    }
}

