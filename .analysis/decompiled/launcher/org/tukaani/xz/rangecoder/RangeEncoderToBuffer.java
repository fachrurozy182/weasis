/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.rangecoder;

import java.io.IOException;
import java.io.OutputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.rangecoder.RangeEncoder;

public final class RangeEncoderToBuffer
extends RangeEncoder {
    private final byte[] buf;
    private int bufPos;

    public RangeEncoderToBuffer(int bufSize, ArrayCache arrayCache) {
        this.buf = arrayCache.getByteArray(bufSize, false);
        this.reset();
    }

    public void putArraysToCache(ArrayCache arrayCache) {
        arrayCache.putArray(this.buf);
    }

    @Override
    public void reset() {
        super.reset();
        this.bufPos = 0;
    }

    @Override
    public int getPendingSize() {
        return this.bufPos + (int)this.cacheSize + 5 - 1;
    }

    @Override
    public int finish() {
        try {
            super.finish();
        }
        catch (IOException e) {
            throw new Error();
        }
        return this.bufPos;
    }

    public void write(OutputStream out) throws IOException {
        out.write(this.buf, 0, this.bufPos);
    }

    @Override
    void writeByte(int b) {
        this.buf[this.bufPos++] = (byte)b;
    }
}

