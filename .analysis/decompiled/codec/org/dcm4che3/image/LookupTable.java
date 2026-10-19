/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.image;

import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import org.dcm4che3.image.StoredValue;

public abstract class LookupTable {
    protected StoredValue inBits;
    protected int outBits;
    protected int offset;

    public LookupTable(StoredValue inBits, int outBits, int offset) {
        this.inBits = inBits;
        this.outBits = outBits;
        this.offset = offset;
    }

    public abstract int length();

    public void lookup(Raster srcRaster, Raster destRaster) {
        ComponentSampleModel sm = (ComponentSampleModel)srcRaster.getSampleModel();
        ComponentSampleModel destsm = (ComponentSampleModel)destRaster.getSampleModel();
        DataBuffer src = srcRaster.getDataBuffer();
        DataBuffer dest = destRaster.getDataBuffer();
        switch (src.getDataType()) {
            case 0: {
                switch (dest.getDataType()) {
                    case 0: {
                        this.lookup(sm, ((DataBufferByte)src).getData(), destsm, ((DataBufferByte)dest).getData());
                        return;
                    }
                    case 1: {
                        this.lookup(sm, ((DataBufferByte)src).getData(), destsm, ((DataBufferUShort)dest).getData());
                        return;
                    }
                }
                break;
            }
            case 1: {
                switch (dest.getDataType()) {
                    case 0: {
                        this.lookup(sm, ((DataBufferUShort)src).getData(), destsm, ((DataBufferByte)dest).getData());
                        return;
                    }
                    case 1: {
                        this.lookup(sm, ((DataBufferUShort)src).getData(), destsm, ((DataBufferUShort)dest).getData());
                        return;
                    }
                }
                break;
            }
            case 2: {
                switch (dest.getDataType()) {
                    case 0: {
                        this.lookup(sm, ((DataBufferShort)src).getData(), destsm, ((DataBufferByte)dest).getData());
                        return;
                    }
                    case 1: {
                        this.lookup(sm, ((DataBufferShort)src).getData(), destsm, ((DataBufferUShort)dest).getData());
                        return;
                    }
                }
            }
        }
        throw new UnsupportedOperationException("Lookup " + src.getClass() + " -> " + dest.getClass() + " not supported");
    }

    private void lookup(ComponentSampleModel sm, byte[] src, ComponentSampleModel destsm, byte[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; ++y) {
            this.lookup(src, y * stride, dest, y * destStride, w);
        }
    }

    private void lookup(ComponentSampleModel sm, short[] src, ComponentSampleModel destsm, byte[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; ++y) {
            this.lookup(src, y * stride, dest, y * destStride, w);
        }
    }

    private void lookup(ComponentSampleModel sm, byte[] src, ComponentSampleModel destsm, short[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; ++y) {
            this.lookup(src, y * stride, dest, y * destStride, w);
        }
    }

    private void lookup(ComponentSampleModel sm, short[] src, ComponentSampleModel destsm, short[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; ++y) {
            this.lookup(src, y * stride, dest, y * destStride, w);
        }
    }

    public abstract void lookup(byte[] var1, int var2, byte[] var3, int var4, int var5);

    public abstract void lookup(short[] var1, int var2, byte[] var3, int var4, int var5);

    public abstract void lookup(byte[] var1, int var2, short[] var3, int var4, int var5);

    public abstract void lookup(short[] var1, int var2, short[] var3, int var4, int var5);

    public abstract LookupTable adjustOutBits(int var1);

    public abstract void inverse();

    public abstract LookupTable combine(LookupTable var1);
}

