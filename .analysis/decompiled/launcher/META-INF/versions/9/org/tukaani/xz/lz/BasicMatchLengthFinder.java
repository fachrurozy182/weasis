/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lz;

import java.util.Arrays;
import org.tukaani.xz.lz.MatchLengthFinder;

final class BasicMatchLengthFinder
implements MatchLengthFinder {
    BasicMatchLengthFinder() {
    }

    @Override
    public int getExtraSize() {
        return 0;
    }

    @Override
    public int getLen(byte[] buf, int off, int delta, int len, int lenLimit) {
        int start = off + len;
        int end = off + lenLimit;
        int mismatch = Arrays.mismatch(buf, start, end, buf, start - delta, end - delta);
        return mismatch < 0 ? lenLimit : len + mismatch;
    }
}

