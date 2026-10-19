/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lz;

import java.io.DataInputStream;
import java.io.IOException;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.CorruptedInputException;

public final class LZDecoder {
    private final byte[] buf;
    private final int bufSize;
    private int start = 0;
    private int pos = 0;
    private int full = 0;
    private int limit = 0;
    private int pendingLen = 0;
    private int pendingDist = 0;

    public LZDecoder(int dictSize, byte[] presetDict, ArrayCache arrayCache) {
        this.bufSize = dictSize;
        this.buf = arrayCache.getByteArray(this.bufSize, false);
        this.buf[this.bufSize - 1] = 0;
        if (presetDict != null) {
            this.full = this.pos = Math.min(presetDict.length, dictSize);
            this.start = this.pos;
            System.arraycopy(presetDict, presetDict.length - this.pos, this.buf, 0, this.pos);
        }
    }

    public void putArraysToCache(ArrayCache arrayCache) {
        arrayCache.putArray(this.buf);
    }

    public void reset() {
        this.start = 0;
        this.pos = 0;
        this.full = 0;
        this.limit = 0;
        this.buf[this.bufSize - 1] = 0;
    }

    public void setLimit(int outMax) {
        this.limit = this.bufSize - this.pos <= outMax ? this.bufSize : this.pos + outMax;
    }

    public boolean hasSpace() {
        return this.pos < this.limit;
    }

    public boolean hasPending() {
        return this.pendingLen > 0;
    }

    public int getPos() {
        return this.pos;
    }

    public int getByte(int dist) {
        int offset = this.pos - dist - 1;
        if (dist >= this.pos) {
            offset += this.bufSize;
        }
        return this.buf[offset] & 0xFF;
    }

    public void putByte(byte b) {
        this.buf[this.pos++] = b;
        if (this.full < this.pos) {
            this.full = this.pos;
        }
    }

    public void repeat(int dist, int len) throws IOException {
        int copySize;
        if (dist < 0 || dist >= this.full) {
            throw new CorruptedInputException();
        }
        int left = Math.min(this.limit - this.pos, len);
        this.pendingLen = len - left;
        this.pendingDist = dist;
        int back = this.pos - dist - 1;
        if (back < 0) {
            assert (this.full == this.bufSize);
            copySize = Math.min(this.bufSize - (back += this.bufSize), left);
            assert (copySize <= dist + 1);
            System.arraycopy(this.buf, back, this.buf, this.pos, copySize);
            this.pos += copySize;
            back = 0;
            if ((left -= copySize) == 0) {
                return;
            }
        }
        assert (back < this.pos);
        assert (left > 0);
        do {
            copySize = Math.min(left, this.pos - back);
            System.arraycopy(this.buf, back, this.buf, this.pos, copySize);
            this.pos += copySize;
        } while ((left -= copySize) > 0);
        if (this.full < this.pos) {
            this.full = this.pos;
        }
    }

    public void repeatPending() throws IOException {
        if (this.pendingLen > 0) {
            this.repeat(this.pendingDist, this.pendingLen);
        }
    }

    public void copyUncompressed(DataInputStream inData, int len) throws IOException {
        int copySize = Math.min(this.bufSize - this.pos, len);
        inData.readFully(this.buf, this.pos, copySize);
        this.pos += copySize;
        if (this.full < this.pos) {
            this.full = this.pos;
        }
    }

    public int flush(byte[] out, int outOff) {
        int copySize = this.pos - this.start;
        if (this.pos == this.bufSize) {
            this.pos = 0;
        }
        System.arraycopy(this.buf, this.start, out, outOff, copySize);
        this.start = this.pos;
        return copySize;
    }
}

