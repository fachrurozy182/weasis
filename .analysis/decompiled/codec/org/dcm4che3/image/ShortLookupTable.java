/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.image;

import org.dcm4che3.image.LookupTable;
import org.dcm4che3.image.StoredValue;

public class ShortLookupTable
extends LookupTable {
    private final short[] lut;

    ShortLookupTable(StoredValue inBits, int outBits, int offset, short[] lut) {
        super(inBits, outBits, offset);
        this.lut = lut;
    }

    ShortLookupTable(StoredValue inBits, int outBits, int minOut, int maxOut, int offset, int size, boolean flip) {
        this(inBits, outBits, offset, new short[minOut == maxOut ? 1 : size]);
        if (this.lut.length == 1) {
            this.lut[0] = (short)minOut;
        } else {
            int outRange = maxOut - minOut;
            int maxIndex = size - 1;
            int midIndex = maxIndex / 2;
            for (int i = 0; i < size; ++i) {
                this.lut[flip ? maxIndex - i : i] = (short)((i * outRange + midIndex) / maxIndex + minOut);
            }
        }
    }

    @Override
    public int length() {
        return this.lut.length;
    }

    @Override
    public void lookup(byte[] src, int srcPos, byte[] dest, int destPos, int length) {
        int i = srcPos;
        int endPos = srcPos + length;
        int j = destPos;
        while (i < endPos) {
            dest[j++] = (byte)this.lut[this.index(src[i++] & 0xFF)];
        }
    }

    private int index(int pixel) {
        int index = this.inBits.valueOf(pixel) - this.offset;
        return Math.min(Math.max(0, index), this.lut.length - 1);
    }

    @Override
    public void lookup(short[] src, int srcPos, byte[] dest, int destPos, int length) {
        int i = srcPos;
        int endPos = srcPos + length;
        int j = destPos;
        while (i < endPos) {
            dest[j++] = (byte)this.lut[this.index(src[i++] & 0xFFFF)];
        }
    }

    @Override
    public void lookup(byte[] src, int srcPos, short[] dest, int destPos, int length) {
        int i = srcPos;
        int endPos = srcPos + length;
        int j = destPos;
        while (i < endPos) {
            dest[j++] = this.lut[this.index(src[i++] & 0xFF)];
        }
    }

    @Override
    public void lookup(short[] src, int srcPos, short[] dest, int destPos, int length) {
        int i = srcPos;
        int endPos = srcPos + length;
        int j = destPos;
        while (i < endPos) {
            dest[j++] = this.lut[this.index(src[i++] & 0xFFFF)];
        }
    }

    @Override
    public LookupTable adjustOutBits(int outBits) {
        int diff = outBits - this.outBits;
        if (diff != 0) {
            short[] lut = this.lut;
            if (diff < 0) {
                diff = -diff;
                for (int i = 0; i < lut.length; ++i) {
                    lut[i] = (short)((lut[i] & 0xFFFF) >> diff);
                }
            } else {
                int i = 0;
                while (i < lut.length) {
                    int n = i++;
                    lut[n] = (short)(lut[n] << diff);
                }
            }
            this.outBits = outBits;
        }
        return this;
    }

    @Override
    public void inverse() {
        short[] lut = this.lut;
        int maxOut = (1 << this.outBits) - 1;
        for (int i = 0; i < lut.length; ++i) {
            lut[i] = (short)(maxOut - lut[i]);
        }
    }

    @Override
    public LookupTable combine(LookupTable other) {
        short[] lut = this.lut;
        other.lookup(lut, 0, lut, 0, lut.length);
        this.outBits = other.outBits;
        return this;
    }
}

