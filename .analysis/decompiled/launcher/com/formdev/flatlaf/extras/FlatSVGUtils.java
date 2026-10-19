/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.extras;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.MultiResolutionImageSupport;
import com.formdev.flatlaf.util.SystemInfo;
import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.view.FloatSize;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FlatSVGUtils {
    public static List<Image> createWindowIconImages(String svgName) {
        return FlatSVGUtils.createWindowIconImages(FlatSVGUtils.getResource(svgName));
    }

    public static List<Image> createWindowIconImages(URL svgUrl) {
        SVGDocument document = FlatSVGIcon.loadSVG(svgUrl);
        if (SystemInfo.isWindows && MultiResolutionImageSupport.isAvailable()) {
            return Collections.singletonList(MultiResolutionImageSupport.create(0, new Dimension[]{new Dimension(16, 16), new Dimension(20, 20), new Dimension(24, 24), new Dimension(28, 28), new Dimension(32, 32), new Dimension(48, 48), new Dimension(64, 64)}, dim -> FlatSVGUtils.svg2image(document, dim.width, dim.height)));
        }
        return Arrays.asList(FlatSVGUtils.svg2image(document, 16, 16), FlatSVGUtils.svg2image(document, 20, 20), FlatSVGUtils.svg2image(document, 24, 24), FlatSVGUtils.svg2image(document, 28, 28), FlatSVGUtils.svg2image(document, 32, 32), FlatSVGUtils.svg2image(document, 48, 48), FlatSVGUtils.svg2image(document, 64, 64));
    }

    public static BufferedImage svg2image(String svgName, int width, int height) {
        return FlatSVGUtils.svg2image(FlatSVGUtils.getResource(svgName), width, height);
    }

    public static BufferedImage svg2image(URL svgUrl, int width, int height) {
        return FlatSVGUtils.svg2image(FlatSVGIcon.loadSVG(svgUrl), width, height);
    }

    public static BufferedImage svg2image(String svgName, float scaleFactor) {
        return FlatSVGUtils.svg2image(FlatSVGUtils.getResource(svgName), scaleFactor);
    }

    public static BufferedImage svg2image(URL svgUrl, float scaleFactor) {
        SVGDocument document = FlatSVGIcon.loadSVG(svgUrl);
        FloatSize size = document.size();
        int width = (int)(((Dimension2D)size).getWidth() * (double)scaleFactor);
        int height = (int)(((Dimension2D)size).getHeight() * (double)scaleFactor);
        return FlatSVGUtils.svg2image(document, width, height);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static BufferedImage svg2image(SVGDocument document, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, 2);
        Graphics2D g = image.createGraphics();
        try {
            FlatSVGIcon.setRenderingHints(g);
            FloatSize size = document.size();
            double sx = (double)width / ((Dimension2D)size).getWidth();
            double sy = (double)height / ((Dimension2D)size).getHeight();
            if (sx != 1.0 || sy != 1.0) {
                g.scale(sx, sy);
            }
            document.render(null, g);
        }
        finally {
            g.dispose();
        }
        return image;
    }

    private static URL getResource(String svgName) {
        return FlatSVGUtils.class.getResource(svgName);
    }
}

