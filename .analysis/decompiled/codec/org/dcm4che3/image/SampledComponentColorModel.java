/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.image;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import org.dcm4che3.image.ColorSubsampling;
import org.dcm4che3.image.SampledComponentSampleModel;

public class SampledComponentColorModel
extends ColorModel {
    private static final int[] BITS = new int[]{8, 8, 8};
    private final ColorSubsampling subsampling;

    public SampledComponentColorModel(ColorSpace cspace, ColorSubsampling subsampling) {
        super(24, BITS, cspace, false, false, 1, 0);
        this.subsampling = subsampling;
    }

    @Override
    public boolean isCompatibleRaster(Raster raster) {
        return this.isCompatibleSampleModel(raster.getSampleModel());
    }

    @Override
    public boolean isCompatibleSampleModel(SampleModel sm) {
        return sm instanceof SampledComponentSampleModel;
    }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        return new SampledComponentSampleModel(w, h, this.subsampling);
    }

    @Override
    public int getAlpha(int pixel) {
        return 255;
    }

    @Override
    public int getBlue(int pixel) {
        return pixel & 0xFF;
    }

    @Override
    public int getGreen(int pixel) {
        return pixel & 0xFF00;
    }

    @Override
    public int getRed(int pixel) {
        return pixel & 0xFF0000;
    }

    @Override
    public int getAlpha(Object inData) {
        return 255;
    }

    @Override
    public int getBlue(Object inData) {
        return this.getRGB(inData) & 0xFF;
    }

    @Override
    public int getGreen(Object inData) {
        return this.getRGB(inData) >> 8 & 0xFF;
    }

    @Override
    public int getRed(Object inData) {
        return this.getRGB(inData) >> 16;
    }

    @Override
    public int getRGB(Object inData) {
        byte[] ba = (byte[])inData;
        ColorSpace cs = this.getColorSpace();
        float[] fba = new float[]{(float)(ba[0] & 0xFF) / 255.0f, (float)(ba[1] & 0xFF) / 255.0f, (float)(ba[2] & 0xFF) / 255.0f};
        float[] rgb = cs.toRGB(fba);
        int ret = (int)(rgb[0] * 255.0f) << 16 | (int)(rgb[1] * 255.0f) << 8 | (int)(rgb[2] * 255.0f);
        return ret;
    }
}

