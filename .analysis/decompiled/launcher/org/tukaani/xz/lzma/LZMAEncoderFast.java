/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lzma;

import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.lz.LZEncoder;
import org.tukaani.xz.lz.Matches;
import org.tukaani.xz.lzma.LZMAEncoder;
import org.tukaani.xz.rangecoder.RangeEncoder;

final class LZMAEncoderFast
extends LZMAEncoder {
    private static final int EXTRA_SIZE_BEFORE = 1;
    private static final int EXTRA_SIZE_AFTER = 272;
    private Matches matches = null;

    static int getMemoryUsage(int dictSize, int extraSizeBefore, int mf) {
        return LZEncoder.getMemoryUsage(dictSize, Math.max(extraSizeBefore, 1), 272, 273, mf);
    }

    LZMAEncoderFast(RangeEncoder rc, int lc, int lp, int pb, int dictSize, int extraSizeBefore, int niceLen, int mf, int depthLimit, ArrayCache arrayCache) {
        super(rc, LZEncoder.getInstance(dictSize, Math.max(extraSizeBefore, 1), 272, niceLen, 273, mf, depthLimit, arrayCache), lc, lp, pb, dictSize, niceLen);
    }

    private boolean changePair(int smallDist, int bigDist) {
        return smallDist < bigDist >>> 7;
    }

    @Override
    int getNextSymbol() {
        if (this.readAhead == -1) {
            this.matches = this.getMatches();
        }
        this.back = -1;
        int avail = Math.min(this.lz.getAvail(), 273);
        if (avail < 2) {
            return 1;
        }
        int bestRepLen = 0;
        int bestRepIndex = 0;
        for (int rep = 0; rep < 4; ++rep) {
            int len = this.lz.getMatchLen(this.reps[rep], avail);
            if (len < 2) continue;
            if (len >= this.niceLen) {
                this.back = rep;
                this.skip(len - 1);
                return len;
            }
            if (len <= bestRepLen) continue;
            bestRepIndex = rep;
            bestRepLen = len;
        }
        int mainLen = 0;
        int mainDist = 0;
        if (this.matches.count > 0) {
            mainLen = this.matches.len[this.matches.count - 1];
            mainDist = this.matches.dist[this.matches.count - 1];
            if (mainLen >= this.niceLen) {
                this.back = mainDist + 4;
                this.skip(mainLen - 1);
                return mainLen;
            }
            while (this.matches.count > 1 && mainLen == this.matches.len[this.matches.count - 2] + 1 && this.changePair(this.matches.dist[this.matches.count - 2], mainDist)) {
                --this.matches.count;
                mainLen = this.matches.len[this.matches.count - 1];
                mainDist = this.matches.dist[this.matches.count - 1];
            }
            if (mainLen == 2 && mainDist >= 128) {
                mainLen = 1;
            }
        }
        if (bestRepLen >= 2 && (bestRepLen + 1 >= mainLen || bestRepLen + 2 >= mainLen && mainDist >= 512 || bestRepLen + 3 >= mainLen && mainDist >= 32768)) {
            this.back = bestRepIndex;
            this.skip(bestRepLen - 1);
            return bestRepLen;
        }
        if (mainLen < 2 || avail <= 2) {
            return 1;
        }
        this.matches = this.getMatches();
        if (this.matches.count > 0) {
            int newLen = this.matches.len[this.matches.count - 1];
            int newDist = this.matches.dist[this.matches.count - 1];
            if (newLen >= mainLen && newDist < mainDist || newLen == mainLen + 1 && !this.changePair(mainDist, newDist) || newLen > mainLen + 1 || newLen + 1 >= mainLen && mainLen >= 3 && this.changePair(newDist, mainDist)) {
                return 1;
            }
        }
        int limit = Math.max(mainLen - 1, 2);
        for (int rep = 0; rep < 4; ++rep) {
            if (this.lz.getMatchLen(this.reps[rep], limit) != limit) continue;
            return 1;
        }
        this.back = mainDist + 4;
        this.skip(mainLen - 2);
        return mainLen;
    }
}

