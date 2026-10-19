/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lz;

import java.io.IOException;
import java.io.OutputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.lz.BT4;
import org.tukaani.xz.lz.HC4;
import org.tukaani.xz.lz.MatchLength;
import org.tukaani.xz.lz.Matches;

public abstract class LZEncoder {
    public static final int MF_HC4 = 4;
    public static final int MF_BT4 = 20;
    private final int keepSizeBefore;
    private final int keepSizeAfter;
    final int matchLenMax;
    final int niceLen;
    final byte[] buf;
    final int bufSize;
    int readPos = -1;
    private int readLimit = -1;
    private boolean finishing = false;
    private int writePos = 0;
    private int pendingSize = 0;

    static void normalize(int[] positions, int positionsCount, int normalizationOffset) {
        for (int i = 0; i < positionsCount; ++i) {
            if (positions[i] <= normalizationOffset) {
                positions[i] = 0;
                continue;
            }
            int n = i;
            positions[n] = positions[n] - normalizationOffset;
        }
    }

    private static int getBufSize(int dictSize, int extraSizeBefore, int extraSizeAfter, int matchLenMax) {
        int keepSizeBefore = extraSizeBefore + dictSize;
        int keepSizeAfter = extraSizeAfter + matchLenMax;
        int reserveSize = Math.min(dictSize / 2 + 262144, 0x20000000);
        return keepSizeBefore + keepSizeAfter + reserveSize;
    }

    public static int getMemoryUsage(int dictSize, int extraSizeBefore, int extraSizeAfter, int matchLenMax, int mf) {
        int m = LZEncoder.getBufSize(dictSize, extraSizeBefore, extraSizeAfter, matchLenMax) / 1024 + 10;
        switch (mf) {
            case 4: {
                m += HC4.getMemoryUsage(dictSize);
                break;
            }
            case 20: {
                m += BT4.getMemoryUsage(dictSize);
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
        return m;
    }

    public static LZEncoder getInstance(int dictSize, int extraSizeBefore, int extraSizeAfter, int niceLen, int matchLenMax, int mf, int depthLimit, ArrayCache arrayCache) {
        switch (mf) {
            case 4: {
                return new HC4(dictSize, extraSizeBefore, extraSizeAfter, niceLen, matchLenMax, depthLimit, arrayCache);
            }
            case 20: {
                return new BT4(dictSize, extraSizeBefore, extraSizeAfter, niceLen, matchLenMax, depthLimit, arrayCache);
            }
        }
        throw new IllegalArgumentException();
    }

    LZEncoder(int dictSize, int extraSizeBefore, int extraSizeAfter, int niceLen, int matchLenMax, ArrayCache arrayCache) {
        this.bufSize = LZEncoder.getBufSize(dictSize, extraSizeBefore, extraSizeAfter, matchLenMax);
        this.buf = arrayCache.getByteArray(this.bufSize + MatchLength.getExtraSize(), false);
        this.keepSizeBefore = extraSizeBefore + dictSize;
        this.keepSizeAfter = extraSizeAfter + matchLenMax;
        this.matchLenMax = matchLenMax;
        this.niceLen = niceLen;
    }

    public void putArraysToCache(ArrayCache arrayCache) {
        arrayCache.putArray(this.buf);
    }

    public void setPresetDict(int dictSize, byte[] presetDict) {
        assert (!this.isStarted());
        assert (this.writePos == 0);
        if (presetDict != null) {
            int copySize = Math.min(presetDict.length, dictSize);
            int offset = presetDict.length - copySize;
            System.arraycopy(presetDict, offset, this.buf, 0, copySize);
            this.writePos += copySize;
            this.skip(copySize);
        }
    }

    private void moveWindow() {
        int moveOffset = this.readPos + 1 - this.keepSizeBefore & 0xFFFFFFF0;
        int moveSize = this.writePos - moveOffset;
        System.arraycopy(this.buf, moveOffset, this.buf, 0, moveSize);
        this.readPos -= moveOffset;
        this.readLimit -= moveOffset;
        this.writePos -= moveOffset;
    }

    public int fillWindow(byte[] in, int off, int len) {
        assert (!this.finishing);
        if (this.readPos >= this.bufSize - this.keepSizeAfter) {
            this.moveWindow();
        }
        if (len > this.bufSize - this.writePos) {
            len = this.bufSize - this.writePos;
        }
        System.arraycopy(in, off, this.buf, this.writePos, len);
        this.writePos += len;
        if (this.writePos >= this.keepSizeAfter) {
            this.readLimit = this.writePos - this.keepSizeAfter;
        }
        this.processPendingBytes();
        return len;
    }

    private void processPendingBytes() {
        if (this.pendingSize > 0 && this.readPos < this.readLimit) {
            this.readPos -= this.pendingSize;
            int oldPendingSize = this.pendingSize;
            this.pendingSize = 0;
            this.skip(oldPendingSize);
            assert (this.pendingSize < oldPendingSize);
        }
    }

    public boolean isStarted() {
        return this.readPos != -1;
    }

    public void setFlushing() {
        this.readLimit = this.writePos - 1;
        this.processPendingBytes();
    }

    public void setFinishing() {
        this.readLimit = this.writePos - 1;
        this.finishing = true;
        this.processPendingBytes();
    }

    public boolean hasEnoughData(int alreadyReadLen) {
        return this.readPos - alreadyReadLen < this.readLimit;
    }

    public void copyUncompressed(OutputStream out, int backward, int len) throws IOException {
        out.write(this.buf, this.readPos + 1 - backward, len);
    }

    public int getAvail() {
        assert (this.isStarted());
        return this.writePos - this.readPos;
    }

    public int getPos() {
        return this.readPos;
    }

    public int getByte(int backward) {
        return this.buf[this.readPos - backward] & 0xFF;
    }

    public int getByte(int forward, int backward) {
        return this.buf[this.readPos + forward - backward] & 0xFF;
    }

    public int getMatchLen(int dist, int lenLimit) {
        return MatchLength.getLen(this.buf, this.readPos, dist + 1, 0, lenLimit);
    }

    public int getMatchLen(int forward, int dist, int lenLimit) {
        return MatchLength.getLen(this.buf, this.readPos + forward, dist + 1, 0, lenLimit);
    }

    public boolean verifyMatches(Matches matches) {
        int lenLimit = Math.min(this.getAvail(), this.matchLenMax);
        for (int i = 0; i < matches.count; ++i) {
            if (this.getMatchLen(matches.dist[i], lenLimit) == matches.len[i]) continue;
            return false;
        }
        return true;
    }

    int movePos(int requiredForFlushing, int requiredForFinishing) {
        assert (requiredForFlushing >= requiredForFinishing);
        ++this.readPos;
        int avail = this.writePos - this.readPos;
        if (!(avail >= requiredForFlushing || avail >= requiredForFinishing && this.finishing)) {
            ++this.pendingSize;
            avail = 0;
        }
        return avail;
    }

    public abstract Matches getMatches();

    public abstract void skip(int var1);
}

