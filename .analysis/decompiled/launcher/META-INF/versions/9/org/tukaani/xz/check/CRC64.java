/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.check;

import org.tukaani.xz.check.Check;
import org.tukaani.xz.common.ByteArrayView;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
public class CRC64
extends Check {
    private static final long[] TABLE = new long[1024];
    private long crc = -1L;

    public CRC64() {
        this.size = 8;
        this.name = "CRC64";
    }

    @Override
    public void update(byte[] buf, int off, int len) {
        int end = off + len;
        int i = off;
        while ((i & 3) != ByteArrayView.ALIGN_INT && i < end) {
            this.crc = TABLE[buf[i++] & 0xFF ^ (int)this.crc & 0xFF] ^ this.crc >>> 8;
        }
        int end4 = end - ByteArrayView.ALIGN_INT & 0xFFFFFFFC;
        while (i < end4) {
            int tmp = ByteArrayView.getIntLE(buf, i) ^ (int)this.crc;
            this.crc = TABLE[768 + (tmp & 0xFF)] ^ TABLE[512 + (tmp >>> 8 & 0xFF)] ^ this.crc >>> 32 ^ TABLE[256 + (tmp >>> 16 & 0xFF)] ^ TABLE[tmp >>> 24 & 0xFF];
            i += 4;
        }
        while (i < end) {
            this.crc = TABLE[buf[i++] & 0xFF ^ (int)this.crc & 0xFF] ^ this.crc >>> 8;
        }
    }

    @Override
    public byte[] finish() {
        byte[] buf = new byte[8];
        ByteArrayView.setLongLE(buf, 0, this.crc ^ 0xFFFFFFFFFFFFFFFFL);
        this.crc = -1L;
        return buf;
    }

    static {
        long poly64 = -3932672073523589310L;
        for (int s = 0; s < 1024; s += 256) {
            for (int b = 0; b < 256; ++b) {
                long r = s == 0 ? (long)b : TABLE[s - 256 + b];
                for (int i = 0; i < 8; ++i) {
                    if ((r & 1L) == 1L) {
                        r = r >>> 1 ^ 0xC96C5795D7870F42L;
                        continue;
                    }
                    r >>>= 1;
                }
                CRC64.TABLE[s + b] = r;
            }
        }
    }
}

