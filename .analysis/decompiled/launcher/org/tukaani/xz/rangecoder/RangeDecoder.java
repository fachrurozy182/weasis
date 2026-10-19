/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.rangecoder;

import java.io.IOException;
import org.tukaani.xz.rangecoder.RangeCoder;

public abstract class RangeDecoder
extends RangeCoder {
    int range = 0;
    int code = 0;

    public abstract void normalize() throws IOException;

    public int decodeBit(short[] probs, int index) throws IOException {
        int bit;
        this.normalize();
        short prob = probs[index];
        int bound = (this.range >>> 11) * prob;
        if (Integer.compareUnsigned(this.code, bound) < 0) {
            this.range = bound;
            probs[index] = (short)(prob + (2048 - prob >>> 5));
            bit = 0;
        } else {
            this.range -= bound;
            this.code -= bound;
            probs[index] = (short)(prob - (prob >>> 5));
            bit = 1;
        }
        return bit;
    }

    public int decodeBitTree(short[] probs) throws IOException {
        int symbol = 1;
        while ((symbol = symbol << 1 | this.decodeBit(probs, symbol)) < probs.length) {
        }
        return symbol - probs.length;
    }

    public int decodeReverseBitTree(short[] probs) throws IOException {
        int symbol = 1;
        int i = 0;
        int result = 0;
        do {
            int bit = this.decodeBit(probs, symbol);
            symbol = symbol << 1 | bit;
            result |= bit << i++;
        } while (symbol < probs.length);
        return result;
    }

    public int decodeDirectBits(int count) throws IOException {
        int result = 0;
        do {
            this.normalize();
            this.range >>>= 1;
            int t = this.code - this.range >>> 31;
            this.code -= this.range & t - 1;
            result = result << 1 | 1 - t;
        } while (--count != 0);
        return result;
    }
}

