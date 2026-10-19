/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.simple;

import org.tukaani.xz.common.ByteArrayView;
import org.tukaani.xz.simple.SimpleFilter;

public final class RISCVDecoder
implements SimpleFilter {
    private int pos;

    public RISCVDecoder(int startPos) {
        this.pos = startPos;
    }

    @Override
    public int code(byte[] buf, int off, int len) {
        int i;
        int end = off + len - 8;
        for (i = off; i <= end; i += 2) {
            int inst2;
            int inst = buf[i] & 0xFF;
            if (inst == 239) {
                int b1 = buf[i + 1] & 0xFF;
                if ((b1 & 0xD) != 0) continue;
                int b2 = buf[i + 2] & 0xFF;
                int b3 = buf[i + 3] & 0xFF;
                int pc = this.pos + i - off;
                int addr = (b1 & 0xF0) << 13 | b2 << 9 | b3 << 1;
                buf[i + 1] = (byte)(b1 & 0xF | (addr -= pc) >>> 8 & 0xF0);
                buf[i + 2] = (byte)(addr >>> 16 & 0xF | addr >>> 7 & 0x10 | addr << 4 & 0xE0);
                buf[i + 3] = (byte)(addr >>> 4 & 0x7F | addr >>> 13 & 0x80);
                i += 2;
                continue;
            }
            if ((inst & 0x7F) != 23) continue;
            inst |= (buf[i + 1] & 0xFF) << 8;
            inst |= (buf[i + 2] & 0xFF) << 16;
            if (((inst |= (buf[i + 3] & 0xFF) << 24) & 0xE80) != 0) {
                inst2 = ByteArrayView.getIntLE(buf, i + 4);
                if (((inst << 8 ^ inst2) & 0xF8003) != 3) {
                    i += 4;
                    continue;
                }
                int addr = (inst & 0xFFFFF000) + (inst2 >>> 20);
                inst = 0x117 | inst2 << 12;
                inst2 = addr;
            } else {
                int inst2Rs1 = inst >>> 27;
                if ((inst - 12544 & 0x3F80) >= (inst2Rs1 & 0x1D)) {
                    i += 2;
                    continue;
                }
                int addr = ByteArrayView.getIntBE(buf, i + 4);
                inst2 = inst >>> 12 | (addr -= this.pos + i - off) << 20;
                inst = 0x17 | inst2Rs1 << 7 | addr + 2048 & 0xFFFFF000;
            }
            ByteArrayView.setIntLE(buf, i, inst);
            ByteArrayView.setIntLE(buf, i + 4, inst2);
            i += 6;
        }
        this.pos += (i -= off);
        return i;
    }
}

