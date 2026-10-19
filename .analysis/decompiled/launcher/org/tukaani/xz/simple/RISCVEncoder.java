/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.simple;

import org.tukaani.xz.common.ByteArrayView;
import org.tukaani.xz.simple.SimpleFilter;

public final class RISCVEncoder
implements SimpleFilter {
    private int pos;

    public RISCVEncoder(int startPos) {
        this.pos = startPos;
    }

    @Override
    public int code(byte[] buf, int off, int len) {
        int i;
        int end = off + len - 8;
        for (i = off; i <= end; i += 2) {
            int inst = buf[i] & 0xFF;
            if (inst == 239) {
                int b1 = buf[i + 1] & 0xFF;
                if ((b1 & 0xD) != 0) continue;
                int b2 = buf[i + 2] & 0xFF;
                int b3 = buf[i + 3] & 0xFF;
                int pc = this.pos + i - off;
                int addr = (b1 & 0xF0) << 8 | (b2 & 0xF) << 16 | (b2 & 0x10) << 7 | (b2 & 0xE0) >>> 4 | (b3 & 0x7F) << 4 | (b3 & 0x80) << 13;
                buf[i + 1] = (byte)(b1 & 0xF | (addr += pc) >>> 13 & 0xF0);
                buf[i + 2] = (byte)(addr >>> 9);
                buf[i + 3] = (byte)(addr >>> 1);
                i += 2;
                continue;
            }
            if ((inst & 0x7F) != 23) continue;
            inst |= (buf[i + 1] & 0xFF) << 8;
            inst |= (buf[i + 2] & 0xFF) << 16;
            if (((inst |= (buf[i + 3] & 0xFF) << 24) & 0xE80) != 0) {
                int inst2 = ByteArrayView.getIntLE(buf, i + 4);
                if (((inst << 8 ^ inst2) & 0xF8003) != 3) {
                    i += 4;
                    continue;
                }
                int addr = (inst & 0xFFFFF000) + (inst2 >> 20);
                inst = 0x117 | inst2 << 12;
                ByteArrayView.setIntLE(buf, i, inst);
                ByteArrayView.setIntBE(buf, i + 4, addr += this.pos + i - off);
            } else {
                int fakeRs1 = inst >>> 27;
                if ((inst - 12544 & 0x3F80) >= (fakeRs1 & 0x1D)) {
                    i += 2;
                    continue;
                }
                int fakeAddr = ByteArrayView.getIntLE(buf, i + 4);
                int fakeInst2 = inst >>> 12 | fakeAddr << 20;
                inst = 0x17 | fakeRs1 << 7 | fakeAddr & 0xFFFFF000;
                ByteArrayView.setIntLE(buf, i, inst);
                ByteArrayView.setIntLE(buf, i + 4, fakeInst2);
            }
            i += 6;
        }
        this.pos += (i -= off);
        return i;
    }
}

