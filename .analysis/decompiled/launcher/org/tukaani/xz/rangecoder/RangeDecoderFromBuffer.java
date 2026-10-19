/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.rangecoder;

import java.io.DataInputStream;
import java.io.IOException;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.CorruptedInputException;
import org.tukaani.xz.rangecoder.RangeDecoder;

public final class RangeDecoderFromBuffer
extends RangeDecoder {
    private static final int INIT_SIZE = 5;
    private final byte[] buf;
    private int pos;

    public RangeDecoderFromBuffer(int inputSizeMax, ArrayCache arrayCache) {
        this.buf = arrayCache.getByteArray(inputSizeMax - 5, false);
        this.pos = this.buf.length;
    }

    public void putArraysToCache(ArrayCache arrayCache) {
        arrayCache.putArray(this.buf);
    }

    public void prepareInputBuffer(DataInputStream in, int len) throws IOException {
        if (len < 5) {
            throw new CorruptedInputException();
        }
        if (in.readUnsignedByte() != 0) {
            throw new CorruptedInputException();
        }
        this.code = in.readInt();
        this.range = -1;
        this.pos = this.buf.length - (len -= 5);
        in.readFully(this.buf, this.pos, len);
    }

    public boolean isFinished() {
        return this.pos == this.buf.length && this.code == 0;
    }

    @Override
    public void normalize() throws IOException {
        if ((this.range & 0xFF000000) == 0) {
            try {
                this.code = this.code << 8 | this.buf[this.pos++] & 0xFF;
                this.range <<= 8;
            }
            catch (ArrayIndexOutOfBoundsException e) {
                throw new CorruptedInputException();
            }
        }
    }
}

