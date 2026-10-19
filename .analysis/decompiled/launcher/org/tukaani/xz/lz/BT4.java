/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lz;

import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.lz.Hash234;
import org.tukaani.xz.lz.LZEncoder;
import org.tukaani.xz.lz.MatchLength;
import org.tukaani.xz.lz.Matches;

final class BT4
extends LZEncoder {
    private final Hash234 hash;
    private final int[] tree;
    private final Matches matches;
    private final int depthLimit;
    private final int cyclicSize;
    private int cyclicPos = -1;
    private int lzPos;

    static int getMemoryUsage(int dictSize) {
        return Hash234.getMemoryUsage(dictSize) + dictSize / 128 + 10;
    }

    BT4(int dictSize, int beforeSizeMin, int readAheadMax, int niceLen, int matchLenMax, int depthLimit, ArrayCache arrayCache) {
        super(dictSize, beforeSizeMin, readAheadMax, niceLen, matchLenMax, arrayCache);
        this.lzPos = this.cyclicSize = dictSize + 1;
        this.hash = new Hash234(dictSize, arrayCache);
        this.tree = arrayCache.getIntArray(this.cyclicSize * 2, false);
        this.matches = new Matches(niceLen - 1);
        this.depthLimit = depthLimit > 0 ? depthLimit : 16 + niceLen / 2;
    }

    @Override
    public void putArraysToCache(ArrayCache arrayCache) {
        arrayCache.putArray(this.tree);
        this.hash.putArraysToCache(arrayCache);
        super.putArraysToCache(arrayCache);
    }

    private int movePos() {
        int avail = this.movePos(this.niceLen, 4);
        if (avail != 0) {
            if (++this.lzPos == Integer.MAX_VALUE) {
                int normalizationOffset = Integer.MAX_VALUE - this.cyclicSize;
                this.hash.normalize(normalizationOffset);
                BT4.normalize(this.tree, this.cyclicSize * 2, normalizationOffset);
                this.lzPos -= normalizationOffset;
            }
            if (++this.cyclicPos == this.cyclicSize) {
                this.cyclicPos = 0;
            }
        }
        return avail;
    }

    @Override
    public Matches getMatches() {
        this.matches.count = 0;
        int matchLenLimit = this.matchLenMax;
        int niceLenLimit = this.niceLen;
        int avail = this.movePos();
        if (avail < matchLenLimit) {
            if (avail == 0) {
                return this.matches;
            }
            matchLenLimit = avail;
            if (niceLenLimit > avail) {
                niceLenLimit = avail;
            }
        }
        this.hash.calcHashes(this.buf, this.readPos);
        int delta2 = this.lzPos - this.hash.getHash2Pos();
        int delta3 = this.lzPos - this.hash.getHash3Pos();
        int currentMatch = this.hash.getHash4Pos();
        this.hash.updateTables(this.lzPos);
        int lenBest = 0;
        if (delta2 < this.cyclicSize && this.buf[this.readPos - delta2] == this.buf[this.readPos]) {
            lenBest = 2;
            this.matches.len[0] = 2;
            this.matches.dist[0] = delta2 - 1;
            this.matches.count = 1;
        }
        if (delta2 != delta3 && delta3 < this.cyclicSize && this.buf[this.readPos - delta3] == this.buf[this.readPos]) {
            lenBest = 3;
            this.matches.dist[this.matches.count++] = delta3 - 1;
            delta2 = delta3;
        }
        if (this.matches.count > 0) {
            this.matches.len[this.matches.count - 1] = lenBest = MatchLength.getLen(this.buf, this.readPos, delta2, lenBest, matchLenLimit);
            if (lenBest >= niceLenLimit) {
                this.skip(niceLenLimit, currentMatch);
                return this.matches;
            }
        }
        if (lenBest < 3) {
            lenBest = 3;
        }
        int depth = this.depthLimit;
        int ptr0 = (this.cyclicPos << 1) + 1;
        int ptr1 = this.cyclicPos << 1;
        int len0 = 0;
        int len1 = 0;
        while (true) {
            int delta = this.lzPos - currentMatch;
            if (depth-- == 0 || delta >= this.cyclicSize) {
                this.tree[ptr0] = 0;
                this.tree[ptr1] = 0;
                return this.matches;
            }
            int pair = this.cyclicPos - delta + (delta > this.cyclicPos ? this.cyclicSize : 0) << 1;
            int len = Math.min(len0, len1);
            if (this.buf[this.readPos + len - delta] == this.buf[this.readPos + len] && (len = MatchLength.getLen(this.buf, this.readPos, delta, len + 1, matchLenLimit)) > lenBest) {
                lenBest = len;
                this.matches.len[this.matches.count] = len;
                this.matches.dist[this.matches.count] = delta - 1;
                ++this.matches.count;
                if (len >= niceLenLimit) {
                    this.tree[ptr1] = this.tree[pair];
                    this.tree[ptr0] = this.tree[pair + 1];
                    return this.matches;
                }
            }
            if ((this.buf[this.readPos + len - delta] & 0xFF) < (this.buf[this.readPos + len] & 0xFF)) {
                this.tree[ptr1] = currentMatch;
                ptr1 = pair + 1;
                currentMatch = this.tree[ptr1];
                len1 = len;
                continue;
            }
            this.tree[ptr0] = currentMatch;
            ptr0 = pair;
            currentMatch = this.tree[ptr0];
            len0 = len;
        }
    }

    private void skip(int niceLenLimit, int currentMatch) {
        int depth = this.depthLimit;
        int ptr0 = (this.cyclicPos << 1) + 1;
        int ptr1 = this.cyclicPos << 1;
        int len0 = 0;
        int len1 = 0;
        while (true) {
            int delta = this.lzPos - currentMatch;
            if (depth-- == 0 || delta >= this.cyclicSize) {
                this.tree[ptr0] = 0;
                this.tree[ptr1] = 0;
                return;
            }
            int pair = this.cyclicPos - delta + (delta > this.cyclicPos ? this.cyclicSize : 0) << 1;
            int len = Math.min(len0, len1);
            if (this.buf[this.readPos + len - delta] == this.buf[this.readPos + len] && (len = MatchLength.getLen(this.buf, this.readPos, delta, len + 1, niceLenLimit)) == niceLenLimit) {
                this.tree[ptr1] = this.tree[pair];
                this.tree[ptr0] = this.tree[pair + 1];
                return;
            }
            if ((this.buf[this.readPos + len - delta] & 0xFF) < (this.buf[this.readPos + len] & 0xFF)) {
                this.tree[ptr1] = currentMatch;
                ptr1 = pair + 1;
                currentMatch = this.tree[ptr1];
                len1 = len;
                continue;
            }
            this.tree[ptr0] = currentMatch;
            ptr0 = pair;
            currentMatch = this.tree[ptr0];
            len0 = len;
        }
    }

    @Override
    public void skip(int len) {
        while (len-- > 0) {
            int niceLenLimit = this.niceLen;
            int avail = this.movePos();
            if (avail < niceLenLimit) {
                if (avail == 0) continue;
                niceLenLimit = avail;
            }
            this.hash.calcHashes(this.buf, this.readPos);
            int currentMatch = this.hash.getHash4Pos();
            this.hash.updateTables(this.lzPos);
            this.skip(niceLenLimit, currentMatch);
        }
    }
}

