/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lz;

import org.tukaani.xz.common.ByteArrayView;
import org.tukaani.xz.lz.MatchLengthFinder;

final class UnalignedLongLEMatchLengthFinder
implements MatchLengthFinder {
    UnalignedLongLEMatchLengthFinder() {
    }

    @Override
    public int getExtraSize() {
        return 7;
    }

    @Override
    public int getLen(byte[] buf, int off, int delta, int len, int lenLimit) {
        while (len < lenLimit) {
            long x = ByteArrayView.getLongLE(buf, off + len) - ByteArrayView.getLongLE(buf, off + len - delta);
            if (x != 0L) {
                return Math.min(len += Long.numberOfTrailingZeros(x) >>> 3, lenLimit);
            }
            len += 8;
        }
        return lenLimit;
    }
}

