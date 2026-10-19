/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lz;

import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.lz.Hash234;
import org.tukaani.xz.lz.LZEncoder;
import org.tukaani.xz.lz.MatchLength;
import org.tukaani.xz.lz.Matches;

final class HC4
extends LZEncoder {
    private final Hash234 hash;
    private final int[] chain;
    private final Matches matches;
    private final int depthLimit;
    private final int cyclicSize;
    private int cyclicPos = -1;
    private int lzPos;

    static int getMemoryUsage(int dictSize) {
        return Hash234.getMemoryUsage(dictSize) + dictSize / 256 + 10;
    }

    HC4(int dictSize, int beforeSizeMin, int readAheadMax, int niceLen, int matchLenMax, int depthLimit, ArrayCache arrayCache) {
        super(dictSize, beforeSizeMin, readAheadMax, niceLen, matchLenMax, arrayCache);
        this.hash = new Hash234(dictSize, arrayCache);
        this.cyclicSize = dictSize + 1;
        this.chain = arrayCache.getIntArray(this.cyclicSize, false);
        this.lzPos = this.cyclicSize;
        this.matches = new Matches(niceLen - 1);
        this.depthLimit = depthLimit > 0 ? depthLimit : 4 + niceLen / 4;
    }

    @Override
    public void putArraysToCache(ArrayCache arrayCache) {
        arrayCache.putArray(this.chain);
        this.hash.putArraysToCache(arrayCache);
        super.putArraysToCache(arrayCache);
    }

    private int movePos() {
        int avail = this.movePos(4, 4);
        if (avail != 0) {
            if (++this.lzPos == Integer.MAX_VALUE) {
                int normalizationOffset = Integer.MAX_VALUE - this.cyclicSize;
                this.hash.normalize(normalizationOffset);
                HC4.normalize(this.chain, this.cyclicSize, normalizationOffset);
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
        this.chain[this.cyclicPos] = currentMatch;
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
                return this.matches;
            }
        }
        if (lenBest < 3) {
            lenBest = 3;
        }
        int depth = this.depthLimit;
        while (true) {
            int len;
            int delta = this.lzPos - currentMatch;
            if (depth-- == 0 || delta >= this.cyclicSize) {
                return this.matches;
            }
            currentMatch = this.chain[this.cyclicPos - delta + (delta > this.cyclicPos ? this.cyclicSize : 0)];
            if (this.buf[this.readPos + lenBest - delta] != this.buf[this.readPos + lenBest] || this.buf[this.readPos - delta] != this.buf[this.readPos] || (len = MatchLength.getLen(this.buf, this.readPos, delta, 1, matchLenLimit)) <= lenBest) continue;
            lenBest = len;
            this.matches.len[this.matches.count] = len;
            this.matches.dist[this.matches.count] = delta - 1;
            ++this.matches.count;
            if (len >= niceLenLimit) break;
        }
        return this.matches;
    }

    @Override
    public void skip(int len) {
        assert (len >= 0);
        while (len-- > 0) {
            if (this.movePos() == 0) continue;
            this.hash.calcHashes(this.buf, this.readPos);
            this.chain[this.cyclicPos] = this.hash.getHash4Pos();
            this.hash.updateTables(this.lzPos);
        }
    }
}

