/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.image;

import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.image.ColorModelFactory;
import org.dcm4che3.image.ColorSubsampling;
import org.dcm4che3.image.SampledComponentSampleModel;
import org.dcm4che3.image.YBR;
import org.dcm4che3.image.YBRColorSpace;

public enum PhotometricInterpretation {
    MONOCHROME1(true, true, false, false){

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createMonochromeColorModel(bits, dataType);
        }
    }
    ,
    MONOCHROME2(true, false, false, false){

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createMonochromeColorModel(bits, dataType);
        }
    }
    ,
    PALETTE_COLOR(false, false, false, false){

        public String toString() {
            return "PALETTE COLOR";
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createPaletteColorModel(bits, dataType, cspace, ds);
        }
    }
    ,
    RGB(false, false, false, false){

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createRGBColorModel(bits, dataType, cspace);
        }

        @Override
        public PhotometricInterpretation compress(String tsuid) {
            switch (tsuid) {
                case "1.2.840.10008.1.2.4.50": 
                case "1.2.840.10008.1.2.4.51": {
                    return YBR_FULL_422;
                }
                case "1.2.840.10008.1.2.4.53": 
                case "1.2.840.10008.1.2.4.55": {
                    return YBR_FULL;
                }
                case "1.2.840.10008.1.2.4.90": 
                case "1.2.840.10008.1.2.4.92": 
                case "1.2.840.10008.1.2.4.201": 
                case "1.2.840.10008.1.2.4.202": {
                    return YBR_RCT;
                }
                case "1.2.840.10008.1.2.4.91": 
                case "1.2.840.10008.1.2.4.93": 
                case "1.2.840.10008.1.2.4.203": {
                    return YBR_ICT;
                }
                case "1.2.840.10008.1.2.4.112": 
                case "1.2.840.10008.1.2.4.110": 
                case "1.2.840.10008.1.2.4.111": {
                    return XYB;
                }
            }
            return this;
        }
    }
    ,
    YBR_FULL(false, false, true, false){

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createYBRFullColorModel(bits, dataType, new YBRColorSpace(cspace, YBR.FULL));
        }
    }
    ,
    YBR_FULL_422(false, false, true, true){

        @Override
        public int frameLength(int w, int h, int samples, int bitsAllocated) {
            return ColorSubsampling.YBR_XXX_422.frameLength(w, h);
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createYBRColorModel(bits, dataType, new YBRColorSpace(cspace, YBR.PARTIAL), ColorSubsampling.YBR_XXX_422);
        }

        @Override
        public SampleModel createSampleModel(int dataType, int w, int h, int samples, boolean banded) {
            return new SampledComponentSampleModel(w, h, ColorSubsampling.YBR_XXX_422);
        }
    }
    ,
    YBR_ICT(false, false, true, false){

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            throw new UnsupportedOperationException();
        }
    }
    ,
    YBR_PARTIAL_420(false, false, true, true){

        @Override
        public int frameLength(int w, int h, int samples, int bitsAllocated) {
            return ColorSubsampling.YBR_XXX_420.frameLength(w, h);
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createYBRColorModel(bits, dataType, new YBRColorSpace(cspace, YBR.PARTIAL), ColorSubsampling.YBR_XXX_420);
        }

        @Override
        public SampleModel createSampleModel(int dataType, int w, int h, int samples, boolean banded) {
            return new SampledComponentSampleModel(w, h, ColorSubsampling.YBR_XXX_420);
        }
    }
    ,
    YBR_PARTIAL_422(false, false, true, true){

        @Override
        public int frameLength(int w, int h, int samples, int bitsAllocated) {
            return ColorSubsampling.YBR_XXX_422.frameLength(w, h);
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            return ColorModelFactory.createYBRColorModel(bits, dataType, new YBRColorSpace(cspace, YBR.PARTIAL), ColorSubsampling.YBR_XXX_422);
        }

        @Override
        public SampleModel createSampleModel(int dataType, int w, int h, int samples, boolean banded) {
            return new SampledComponentSampleModel(w, h, ColorSubsampling.YBR_XXX_422);
        }
    }
    ,
    YBR_RCT(false, false, true, false){

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            throw new UnsupportedOperationException();
        }
    }
    ,
    XYB(false, false, true, false){

        @Override
        public ColorModel createColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
            throw new UnsupportedOperationException();
        }
    };

    private final boolean monochrome;
    private final boolean inverse;
    private final boolean ybr;
    private final boolean subSampled;

    private PhotometricInterpretation(boolean monochrome, boolean inverse, boolean ybr, boolean subSampled) {
        this.monochrome = monochrome;
        this.inverse = inverse;
        this.ybr = ybr;
        this.subSampled = subSampled;
    }

    public static PhotometricInterpretation fromString(String s) {
        return s.equals("PALETTE COLOR") ? PALETTE_COLOR : PhotometricInterpretation.valueOf(s);
    }

    public int frameLength(int w, int h, int samples, int bitsAllocated) {
        return w * h * samples * bitsAllocated / 8;
    }

    public boolean isMonochrome() {
        return this.monochrome;
    }

    public boolean isYBR() {
        return this.ybr;
    }

    public PhotometricInterpretation compress(String tsuid) {
        return this;
    }

    public boolean isInverse() {
        return this.inverse;
    }

    public boolean isSubSampled() {
        return this.subSampled;
    }

    public abstract ColorModel createColorModel(int var1, int var2, ColorSpace var3, Attributes var4);

    public SampleModel createSampleModel(int dataType, int w, int h, int samples, boolean banded) {
        int[] indicies = new int[samples];
        for (int i = 1; i < samples; ++i) {
            indicies[i] = i;
        }
        return banded && samples > 1 ? new BandedSampleModel(dataType, w, h, w, indicies, new int[samples]) : new PixelInterleavedSampleModel(dataType, w, h, samples, w * samples, indicies);
    }
}

