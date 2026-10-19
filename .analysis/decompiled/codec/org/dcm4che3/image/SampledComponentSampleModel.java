/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.image;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.SampleModel;
import org.dcm4che3.image.ColorSubsampling;

public class SampledComponentSampleModel
extends SampleModel {
    private final ColorSubsampling subsampling;

    public SampledComponentSampleModel(int w, int h, ColorSubsampling subsampling) {
        super(0, w, h, 3);
        this.subsampling = subsampling;
    }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        return new SampledComponentSampleModel(w, h, this.subsampling);
    }

    @Override
    public DataBuffer createDataBuffer() {
        return new DataBufferByte(this.subsampling.frameLength(this.width, this.height));
    }

    @Override
    public SampleModel createSubsetSampleModel(int[] bands) {
        if (bands.length != 3 || bands[0] != 0 || bands[1] != 1 || bands[2] != 2) {
            throw new UnsupportedOperationException();
        }
        return this;
    }

    @Override
    public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
        byte[] ret = obj instanceof byte[] && ((byte[])obj).length == 3 ? (byte[])obj : new byte[3];
        DataBufferByte dbb = (DataBufferByte)data;
        byte[] ba = dbb.getData();
        int iy = this.subsampling.indexOfY(x, y, this.width);
        int ibr = this.subsampling.indexOfBR(x, y, this.width);
        ret[0] = ba[iy];
        ret[1] = ba[ibr];
        ret[2] = ba[ibr + 1];
        return ret;
    }

    @Override
    public int getNumDataElements() {
        return 3;
    }

    @Override
    public int getSample(int x, int y, int b, DataBuffer data) {
        return ((byte[])this.getDataElements(x, y, null, data))[b];
    }

    @Override
    public int[] getSampleSize() {
        return new int[]{8, 8, 8};
    }

    @Override
    public int getSampleSize(int band) {
        return 8;
    }

    @Override
    public void setDataElements(int x, int y, Object obj, DataBuffer data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSample(int x, int y, int b, int s, DataBuffer data) {
        throw new UnsupportedOperationException();
    }
}

