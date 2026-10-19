/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.simple;

import org.tukaani.xz.common.ByteArrayView;
import org.tukaani.xz.simple.SimpleFilter;

public final class PowerPC
implements SimpleFilter {
    private final boolean isEncoder;
    private int pos;

    public PowerPC(boolean isEncoder, int startPos) {
        this.isEncoder = isEncoder;
        this.pos = startPos;
    }

    @Override
    public int code(byte[] buf, int off, int len) {
        int i;
        int end = off + len - 4;
        for (i = off; i <= end; i += 4) {
            if ((buf[i] & 0xFC) != 72 || (buf[i + 3] & 3) != 1) continue;
            int instr = ByteArrayView.getIntBE(buf, i);
            int pc = this.pos + i - off;
            if (!this.isEncoder) {
                pc = -pc;
            }
            instr = 0x48000001 | instr + pc & 0x3FFFFFC;
            ByteArrayView.setIntBE(buf, i, instr);
        }
        this.pos += (i -= off);
        return i;
    }
}

