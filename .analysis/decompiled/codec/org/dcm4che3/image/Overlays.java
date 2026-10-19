/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.image;

import java.awt.color.ColorSpace;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.function.Function;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.image.CIELabColorSpace;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Overlays {
    private static final Logger LOG = LoggerFactory.getLogger(Overlays.class);

    public static int[] getActiveOverlayGroupOffsets(Attributes psattrs) {
        return Overlays.getOverlayGroupOffsets(psattrs, 0x60001001, -1);
    }

    public static int[] getActiveOverlayGroupOffsets(Attributes attrs, int activationMask) {
        return Overlays.getOverlayGroupOffsets(attrs, 0x60000010, activationMask);
    }

    public static int[] getOverlayGroupOffsets(Attributes attrs, int tag, int activationMask) {
        int len = 0;
        int[] result = new int[16];
        for (int i = 0; i < result.length; ++i) {
            int gg0000 = i << 17;
            if ((activationMask & 1 << i) == 0 || !attrs.containsValue(tag | gg0000)) continue;
            result[len++] = gg0000;
        }
        return Arrays.copyOf(result, len);
    }

    public static int[] getEmbeddedOverlayGroupOffsets(Attributes attrs) {
        int len = 0;
        int[] result = new int[16];
        int bitsAllocated = attrs.getInt(2621696, 8);
        int bitsStored = attrs.getInt(2621697, bitsAllocated);
        for (int i = 0; i < result.length; ++i) {
            int gg0000 = i << 17;
            if (attrs.getInt(0x60000100 | gg0000, 1) == 1) continue;
            int ovlyBitPosition = attrs.getInt(0x60000102 | gg0000, 0);
            if (ovlyBitPosition < bitsStored) {
                LOG.info("Ignore embedded overlay #{} from bit #{} < bits stored: {}", new Object[]{(gg0000 >>> 17) + 1, ovlyBitPosition, bitsStored});
                continue;
            }
            result[len++] = gg0000;
        }
        return Arrays.copyOf(result, len);
    }

    public static void extractFromPixeldata(Raster raster, int mask, byte[] ovlyData, int off, int length) {
        ComponentSampleModel sm = (ComponentSampleModel)raster.getSampleModel();
        int rows = raster.getHeight();
        int columns = raster.getWidth();
        int stride = sm.getScanlineStride();
        DataBuffer db = raster.getDataBuffer();
        switch (db.getDataType()) {
            case 0: {
                Overlays.extractFromPixeldata(((DataBufferByte)db).getData(), rows, columns, stride, mask, ovlyData, off, length);
                break;
            }
            case 1: {
                Overlays.extractFromPixeldata(((DataBufferUShort)db).getData(), rows, columns, stride, mask, ovlyData, off, length);
                break;
            }
            case 2: {
                Overlays.extractFromPixeldata(((DataBufferShort)db).getData(), rows, columns, stride, mask, ovlyData, off, length);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unsupported DataBuffer type: " + db.getDataType());
            }
        }
    }

    private static void extractFromPixeldata(byte[] pixeldata, int rows, int columns, int stride, int mask, byte[] ovlyData, int off, int length) {
        int i = off;
        int imax = off + length;
        for (int y = 0; y < columns && i < imax; ++y) {
            int j;
            int jmax = j + rows;
            for (j = y * stride; j < jmax && i < imax; ++j, ++i) {
                if ((pixeldata[j] & mask) == 0) continue;
                int n = i >>> 3;
                ovlyData[n] = (byte)(ovlyData[n] | 1 << (i & 7));
            }
        }
    }

    private static void extractFromPixeldata(short[] pixeldata, int rows, int columns, int stride, int mask, byte[] ovlyData, int off, int length) {
        int i = off;
        int imax = off + length;
        for (int y = 0; y < rows && i < imax; ++y) {
            int j;
            int jmax = j + columns;
            for (j = y * stride; j < jmax && i < imax; ++j, ++i) {
                if ((pixeldata[j] & mask) == 0) continue;
                int n = i >>> 3;
                ovlyData[n] = (byte)(ovlyData[n] | 1 << (i & 7));
            }
        }
    }

    public static int[] getRecommendedGrayscalePixelValue(Attributes psAttrs, int gg0000, int bits) {
        int[] nArray;
        int[] grayscaleValue = Overlays.getRecommendedPixelValue(6422540, psAttrs, gg0000);
        if (grayscaleValue != null && grayscaleValue.length > 0) {
            int[] nArray2 = new int[1];
            nArray = nArray2;
            nArray2[0] = grayscaleValue[0] >> 16 - bits;
        } else {
            nArray = null;
        }
        return nArray;
    }

    public static int[] getRecommendedRGBPixelValue(Attributes psAttrs, int gg0000) {
        return Overlays.getRecommendedRGBPixelValue(psAttrs, gg0000, Function.identity());
    }

    public static int[] getRecommendedRGBPixelValue(Attributes psAttrs, int gg0000, ColorSpace cspace) {
        return Overlays.getRecommendedRGBPixelValue(psAttrs, gg0000, cspace::fromRGB);
    }

    private static int[] getRecommendedRGBPixelValue(Attributes psAttrs, int gg0000, Function<float[], float[]> fromRGB) {
        int[] cieLabValue = Overlays.getRecommendedPixelValue(6422541, psAttrs, gg0000);
        return cieLabValue != null && cieLabValue.length == 3 ? Overlays.cieLab2RGB(cieLabValue, fromRGB) : null;
    }

    private static int[] cieLab2RGB(int[] cieLabValue, Function<float[], float[]> adjustColorSpace) {
        float[] colorvalue = new float[]{(float)(cieLabValue[0] & 0xFFFF) / 655.35f, (float)((cieLabValue[1] & 0xFFFF) - 32896) / 257.0f, (float)((cieLabValue[2] & 0xFFFF) - 32896) / 257.0f};
        float[] rgb = CIELabColorSpace.getInstance().toRGB(colorvalue);
        rgb = adjustColorSpace.apply(rgb);
        int[] pixel = new int[3];
        for (int i = 0; i < 3; ++i) {
            pixel[i] = (int)(rgb[i] * 255.0f + 0.5f);
        }
        return pixel;
    }

    private static int[] getRecommendedPixelValue(int tag, Attributes psAttrs, int gg0000) {
        int tagOverlayActivationLayer = 0x60001001 | gg0000;
        String layerName = psAttrs.getString(tagOverlayActivationLayer);
        if (layerName == null) {
            throw new IllegalArgumentException("Missing " + TagUtils.toString(tagOverlayActivationLayer) + " Overlay Activation Layer");
        }
        Sequence layers = psAttrs.getSequence(0x700060);
        if (layers == null) {
            throw new IllegalArgumentException("Missing " + TagUtils.toString(0x700060) + " Graphic Layer Sequence");
        }
        for (Attributes layer : layers) {
            if (!layerName.equals(layer.getString(0x700002))) continue;
            return layer.getInts(tag);
        }
        throw new IllegalArgumentException("No Graphic Layer: " + layerName);
    }

    public static void applyOverlay(int frameIndex, WritableRaster raster, Attributes attrs, int gg0000, int pixelValue, byte[] ovlyData) {
        Overlays.applyOverlay(frameIndex, raster, attrs, gg0000, new int[]{pixelValue}, ovlyData);
    }

    public static void applyOverlay(int frameIndex, WritableRaster raster, Attributes attrs, int gg0000, int[] pixelValue, byte[] ovlyData) {
        int imageFrameOrigin = attrs.getInt(0x60000051 | gg0000, 1);
        int framesInOverlay = attrs.getInt(0x60000015 | gg0000, 1);
        int ovlyFrameIndex = frameIndex - imageFrameOrigin + 1;
        if (ovlyFrameIndex < 0 || ovlyFrameIndex >= framesInOverlay) {
            return;
        }
        int tagOverlayRows = 0x60000010 | gg0000;
        int tagOverlayColumns = 0x60000011 | gg0000;
        int tagOverlayData = 0x60003000 | gg0000;
        int tagOverlayOrigin = 0x60000050 | gg0000;
        int ovlyRows = attrs.getInt(tagOverlayRows, -1);
        int ovlyColumns = attrs.getInt(tagOverlayColumns, -1);
        int[] ovlyOrigin = attrs.getInts(tagOverlayOrigin);
        if (ovlyData == null) {
            ovlyData = attrs.getSafeBytes(tagOverlayData);
        }
        if (ovlyData == null) {
            throw new IllegalArgumentException("Missing " + TagUtils.toString(tagOverlayData) + " Overlay Data");
        }
        if (ovlyRows <= 0) {
            throw new IllegalArgumentException(TagUtils.toString(tagOverlayRows) + " Overlay Rows [" + ovlyRows + "]");
        }
        if (ovlyColumns <= 0) {
            throw new IllegalArgumentException(TagUtils.toString(tagOverlayColumns) + " Overlay Columns [" + ovlyColumns + "]");
        }
        if (ovlyOrigin == null) {
            throw new IllegalArgumentException("Missing " + TagUtils.toString(tagOverlayOrigin) + " Overlay Origin");
        }
        if (ovlyOrigin.length != 2) {
            throw new IllegalArgumentException(TagUtils.toString(tagOverlayOrigin) + " Overlay Origin " + Arrays.toString(ovlyOrigin));
        }
        int x0 = ovlyOrigin[1] - 1;
        int y0 = ovlyOrigin[0] - 1;
        int ovlyLen = ovlyRows * ovlyColumns;
        int ovlyOff = ovlyLen * ovlyFrameIndex;
        int end = ovlyOff + ovlyLen + 7 >>> 3;
        if (end > ovlyData.length) {
            LOG.warn("OverlayData to small ({} vs. {})! Skip this overlay:{}", new Object[]{ovlyData.length, end, TagUtils.toString(tagOverlayData)});
            return;
        }
        for (int i = ovlyOff >>> 3; i < end; ++i) {
            int ovlyBits = ovlyData[i] & 0xFF;
            int j = 0;
            while (ovlyBits >>> j != 0) {
                int ovlyIndex;
                if ((ovlyBits & 1 << j) != 0 && (ovlyIndex = (i << 3) + j - ovlyOff) < ovlyLen) {
                    int y = y0 + ovlyIndex / ovlyColumns;
                    int x = x0 + ovlyIndex % ovlyColumns;
                    try {
                        raster.setPixel(x, y, pixelValue);
                    }
                    catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                        // empty catch block
                    }
                }
                ++j;
            }
        }
    }
}

