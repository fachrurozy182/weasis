/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Core
 *  org.opencv.core.Mat
 *  org.opencv.imgproc.Imgproc
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.seg.RegionAttributes
 */
package org.weasis.core.ui.model.graphic.imp.seg;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.weasis.core.ui.model.graphic.imp.seg.ByteLutAlpha;
import org.weasis.core.ui.model.graphic.imp.seg.SegContour;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.seg.RegionAttributes;

public final class FractionalOverlay {
    private static final Map<LutKey, ByteLutAlpha> LUT_CACHE = new ConcurrentHashMap<LutKey, ByteLutAlpha>();

    private FractionalOverlay() {
    }

    public static ByteLutAlpha getOrBuildLut(Color color, float opacity) {
        int rgb = color.getRGB() & 0xFFFFFF;
        int op = Math.clamp((long)Math.round(opacity * 255.0f), (int)0, (int)255);
        return LUT_CACHE.computeIfAbsent(new LutKey(rgb, op), lutKey -> ByteLutAlpha.forSegment("seg-" + Integer.toHexString(rgb), color, (float)op / 255.0f));
    }

    public static void clearLutCache() {
        LUT_CACHE.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static BufferedImage applyLut(PlanarImage mask, ByteLutAlpha lut) {
        int w = mask.width();
        int h = mask.height();
        Mat src = mask.toMat();
        Mat src4 = new Mat();
        Mat dst = new Mat();
        Mat lutMat = lut.toLutMat();
        try {
            Imgproc.cvtColor((Mat)src, (Mat)src4, (int)9);
            Core.LUT((Mat)src4, (Mat)lutMat, (Mat)dst);
            BufferedImage overlay = new BufferedImage(w, h, 6);
            byte[] raster = ((DataBufferByte)overlay.getRaster().getDataBuffer()).getData();
            dst.get(0, 0, raster);
            BufferedImage bufferedImage = overlay;
            return bufferedImage;
        }
        finally {
            lutMat.release();
            src4.release();
            dst.release();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static BufferedImage compositeOverlays(Iterable<SegContour> contours, int width, int height) {
        BufferedImage composite = null;
        Graphics g2d = null;
        try {
            for (SegContour contour : contours) {
                PlanarImage mask = contour.getFractionalMask();
                RegionAttributes attrs = contour.getAttributes();
                if (mask == null || attrs == null || !attrs.isVisible()) continue;
                if (composite == null) {
                    composite = new BufferedImage(width, height, 6);
                    g2d = composite.createGraphics();
                    ((Graphics2D)g2d).setComposite(AlphaComposite.SrcOver);
                }
                FractionalOverlay.drawContourInto((Graphics2D)g2d, mask, attrs.getColor());
            }
        }
        finally {
            if (g2d != null) {
                g2d.dispose();
            }
        }
        return composite;
    }

    private static void drawContourInto(Graphics2D g2d, PlanarImage mask, Color color) {
        ByteLutAlpha lut = FractionalOverlay.getOrBuildLut(color, (float)color.getAlpha() / 255.0f);
        g2d.drawImage((Image)FractionalOverlay.applyLut(mask, lut), 0, 0, null);
    }

    private record LutKey(int rgb, int opacity) {
    }
}

