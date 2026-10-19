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
    private static final long[][] TABLE = new long[4][256];
    private long crc = -1L;

    public CRC64() {
        this.size = 8;
        this.name = "CRC64";
    }

    @Override
    public void update(byte[] buf, int off, int len) {
        int i;
        int end = off + len;
        int end4 = end - 3;
        for (i = off; i < end4; i += 4) {
            int tmp = (int)this.crc;
            this.crc = TABLE[3][tmp & 0xFF ^ buf[i] & 0xFF] ^ TABLE[2][tmp >>> 8 & 0xFF ^ buf[i + 1] & 0xFF] ^ this.crc >>> 32 ^ TABLE[1][tmp >>> 16 & 0xFF ^ buf[i + 2] & 0xFF] ^ TABLE[0][tmp >>> 24 & 0xFF ^ buf[i + 3] & 0xFF];
        }
        while (i < end) {
            this.crc = TABLE[0][buf[i++] & 0xFF ^ (int)this.crc & 0xFF] ^ this.crc >>> 8;
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
        for (int s = 0; s < 4; ++s) {
            for (int b = 0; b < 256; ++b) {
                long r = s == 0 ? (long)b : TABLE[s - 1][b];
                for (int i = 0; i < 8; ++i) {
                    if ((r & 1L) == 1L) {
                        r = r >>> 1 ^ 0xC96C5795D7870F42L;
                        continue;
                    }
                    r >>>= 1;
                }
                CRC64.TABLE[s][b] = r;
            }
        }
    }
}

