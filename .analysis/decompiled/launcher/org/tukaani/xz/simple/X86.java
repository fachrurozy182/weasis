/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.simple;

import org.tukaani.xz.common.ByteArrayView;
import org.tukaani.xz.simple.SimpleFilter;

public final class X86
implements SimpleFilter {
    private static final boolean[] MASK_TO_ALLOWED_STATUS = new boolean[]{true, true, true, false, true, false, false, false};
    private static final int[] MASK_TO_BIT_NUMBER = new int[]{0, 1, 2, 2, 3, 3, 3, 3};
    private final boolean isEncoder;
    private int pos;
    private int prevMask = 0;

    private static boolean test86MSByte(byte b) {
        int i = b & 0xFF;
        return i == 0 || i == 255;
    }

    public X86(boolean isEncoder, int startPos) {
        this.isEncoder = isEncoder;
        this.pos = startPos + 5;
    }

    @Override
    public int code(byte[] buf, int off, int len) {
        int i;
        int prevPos = off - 1;
        int end = off + len - 5;
        for (i = off; i <= end; ++i) {
            if ((buf[i] & 0xFE) != 232) continue;
            if (((prevPos = i - prevPos) & 0xFFFFFFFC) != 0) {
                this.prevMask = 0;
            } else {
                this.prevMask = this.prevMask << prevPos - 1 & 7;
                if (this.prevMask != 0 && (!MASK_TO_ALLOWED_STATUS[this.prevMask] || X86.test86MSByte(buf[i + 4 - MASK_TO_BIT_NUMBER[this.prevMask]]))) {
                    prevPos = i;
                    this.prevMask = this.prevMask << 1 | 1;
                    continue;
                }
            }
            prevPos = i;
            if (X86.test86MSByte(buf[i + 4])) {
                int src = ByteArrayView.getIntLE(buf, i + 1);
                while (true) {
                    int index;
                    int dest = this.isEncoder ? src + (this.pos + i - off) : src - (this.pos + i - off);
                    if (this.prevMask == 0 || !X86.test86MSByte((byte)(dest >>> 24 - (index = MASK_TO_BIT_NUMBER[this.prevMask] * 8)))) break;
                    src = dest ^ (1 << 32 - index) - 1;
                }
                dest <<= 7;
                ByteArrayView.setIntLE(buf, i + 1, dest >>= 7);
                i += 4;
                continue;
            }
            this.prevMask = this.prevMask << 1 | 1;
        }
        this.prevMask = ((prevPos = i - prevPos) & 0xFFFFFFFC) != 0 ? 0 : this.prevMask << prevPos - 1;
        this.pos += (i -= off);
        return i;
    }
}

