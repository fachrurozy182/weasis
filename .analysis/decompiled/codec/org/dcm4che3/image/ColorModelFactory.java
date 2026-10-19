/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.image;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.image.ColorSubsampling;
import org.dcm4che3.image.PaletteColorModel;
import org.dcm4che3.image.SampledComponentColorModel;

public class ColorModelFactory {
    public static ColorModel createMonochromeColorModel(int bits, int dataType) {
        return new ComponentColorModel(ColorSpace.getInstance(1003), new int[]{bits}, false, false, 1, dataType);
    }

    public static ColorModel createPaletteColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
        return new PaletteColorModel(bits, dataType, cspace, ds);
    }

    public static ColorModel createRGBColorModel(int bits, int dataType, ColorSpace cspace) {
        return new ComponentColorModel(cspace, new int[]{bits, bits, bits}, false, false, 1, dataType);
    }

    public static ColorModel createYBRFullColorModel(int bits, int dataType, ColorSpace cspace) {
        return new ComponentColorModel(cspace, new int[]{bits, bits, bits}, false, false, 1, dataType);
    }

    public static ColorModel createYBRColorModel(int bits, int dataType, ColorSpace cspace, ColorSubsampling subsampling) {
        return new SampledComponentColorModel(cspace, subsampling);
    }
}

