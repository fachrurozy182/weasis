/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Mat
 *  org.weasis.opencv.data.LookupTableCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageTransformer
 */
package org.dcm4che3.img.util;

import java.util.Arrays;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.util.LookupTableUtils;
import org.opencv.core.Mat;
import org.weasis.opencv.data.LookupTableCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageTransformer;

public final class PaletteColorUtils {
    private PaletteColorUtils() {
    }

    public static LookupTableCV getPaletteColorLookupTable(Attributes ds) {
        if (!PaletteColorUtils.hasPaletteColorDescriptors(ds)) {
            return null;
        }
        ColorComponents colorComponents = PaletteColorUtils.extractColorComponents(ds);
        return colorComponents.isValid() ? PaletteColorUtils.createPaletteLookupTable(colorComponents) : null;
    }

    public static PlanarImage getRGBImageFromPaletteColorModel(PlanarImage source, LookupTableCV lookup) {
        if (lookup == null) {
            return source;
        }
        return PaletteColorUtils.hasZeroOffsets(source, lookup) ? PaletteColorUtils.performOptimizedTransform(source, lookup) : PaletteColorUtils.performGeneralLookup(source, lookup);
    }

    private static boolean hasPaletteColorDescriptors(Attributes ds) {
        return ds != null && ds.containsValue(2625793) && ds.containsValue(2625794) && ds.containsValue(2625795);
    }

    private static ColorComponents extractColorComponents(Attributes ds) {
        int[][] descriptors = new int[][]{LookupTableUtils.lutDescriptor(ds, 2625793), LookupTableUtils.lutDescriptor(ds, 2625794), LookupTableUtils.lutDescriptor(ds, 2625795)};
        byte[][] data = new byte[][]{LookupTableUtils.lutData(ds, descriptors[0], 2626049, 0x281221), LookupTableUtils.lutData(ds, descriptors[1], 2626050, 0x281222), LookupTableUtils.lutData(ds, descriptors[2], 2626051, 2626083)};
        return new ColorComponents(descriptors[0], descriptors[1], descriptors[2], data[0], data[1], data[2]);
    }

    private static LookupTableCV createPaletteLookupTable(ColorComponents components) {
        return new LookupTableCV((byte[][])new byte[][]{components.blueData(), components.greenData(), components.redData()}, new int[]{components.blueDesc()[1], components.greenDesc()[1], components.redDesc()[1]}, true);
    }

    private static boolean hasZeroOffsets(PlanarImage source, LookupTableCV lookup) {
        return source.depth() <= 1 && lookup.getOffset(0) == 0 && lookup.getOffset(1) == 0 && lookup.getOffset(2) == 0;
    }

    private static PlanarImage performOptimizedTransform(PlanarImage source, LookupTableCV lookup) {
        return ImageTransformer.applyLUT((Mat)source.toMat(), (byte[][])lookup.getByteData());
    }

    private static PlanarImage performGeneralLookup(PlanarImage source, LookupTableCV lookup) {
        return lookup.lookup(source.toMat());
    }

    private record ColorComponents(int[] redDesc, int[] greenDesc, int[] blueDesc, byte[] redData, byte[] greenData, byte[] blueData) {
        boolean isValid() {
            return Arrays.stream(new byte[][]{this.redData, this.greenData, this.blueData}).allMatch(data -> data != null && ((byte[])data).length > 0);
        }
    }
}

