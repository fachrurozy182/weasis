/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.simple;

import org.tukaani.xz.common.ByteArrayView;
import org.tukaani.xz.simple.SimpleFilter;

public final class ARM64
implements SimpleFilter {
    private final boolean isEncoder;
    private int pos;

    public ARM64(boolean isEncoder, int startPos) {
        this.isEncoder = isEncoder;
        this.pos = startPos;
    }

    @Override
    public int code(byte[] buf, int off, int len) {
        int i;
        int end = off + len - 4;
        for (i = off; i <= end; i += 4) {
            int src;
            int instr = buf[i + 3];
            if ((instr & 0xFC) == 148) {
                instr = ByteArrayView.getIntLE(buf, i);
                int pc = this.pos + i - off >>> 2;
                if (!this.isEncoder) {
                    pc = -pc;
                }
                instr = 0x94000000 | instr + pc & 0x3FFFFFF;
                ByteArrayView.setIntLE(buf, i, instr);
                continue;
            }
            if ((instr & 0x9F) != 144 || ((src = (instr = ByteArrayView.getIntLE(buf, i)) >>> 29 & 3 | instr >>> 3 & 0x1FFFFC) + 131072 & 0x1C0000) != 0) continue;
            int pc = this.pos + i - off >>> 12;
            if (!this.isEncoder) {
                pc = -pc;
            }
            int dest = src + pc;
            instr &= 0x9000001F;
            instr |= (dest & 3) << 29;
            instr |= (dest & 0x3FFFC) << 3;
            ByteArrayView.setIntLE(buf, i, instr |= -(dest & 0x20000) & 0xE00000);
        }
        this.pos += (i -= off);
        return i;
    }
}

