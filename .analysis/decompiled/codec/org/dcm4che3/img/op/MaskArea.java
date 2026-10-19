/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Mat
 *  org.opencv.core.Rect
 *  org.opencv.core.Scalar
 *  org.opencv.core.Size
 *  org.opencv.imgproc.Imgproc
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.op.ImageAnalyzer
 */
package org.dcm4che3.img.op;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.List;
import java.util.Objects;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.op.ImageAnalyzer;

public class MaskArea {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaskArea.class);
    private static final Size DEFAULT_BLUR_SIZE = new Size(7.0, 7.0);
    private static final int MIN_BLUR_DIMENSION = 3;
    private static final Scalar DEFAULT_MASK_COLOR = new Scalar(0.0, 0.0, 0.0);
    private final Color color;
    private final List<Shape> shapeList;

    public MaskArea(List<Shape> shapeList) {
        this(shapeList, null);
    }

    public MaskArea(List<Shape> shapeList, Color color) {
        this.shapeList = Objects.requireNonNull(shapeList, "Shape list cannot be null");
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }

    public List<Shape> getShapeList() {
        return this.shapeList;
    }

    public static ImageCV drawShape(Mat srcImg, MaskArea maskArea) {
        if (maskArea == null || maskArea.shapeList.isEmpty()) {
            return ImageCV.fromMat((Mat)srcImg);
        }
        ImageCV dstImg = new ImageCV();
        srcImg.copyTo((Mat)dstImg);
        Scalar scalarColor = MaskArea.convertToScalar(maskArea.color);
        for (Shape shape : maskArea.shapeList) {
            if (maskArea.color == null && shape instanceof Rectangle) {
                Rectangle rect = (Rectangle)shape;
                MaskArea.applyBlurToRectangle(srcImg, dstImg, rect);
                continue;
            }
            List contours = ImageAnalyzer.transformShapeToContour((Shape)shape, (boolean)true);
            Imgproc.fillPoly((Mat)dstImg, (List)contours, (Scalar)scalarColor);
        }
        return dstImg;
    }

    private static Scalar convertToScalar(Color color) {
        return color == null ? DEFAULT_MASK_COLOR : new Scalar((double)color.getBlue(), (double)color.getGreen(), (double)color.getRed());
    }

    private static void applyBlurToRectangle(Mat srcImg, ImageCV dstImg, Rectangle rect) {
        Rectangle imageBounds = new Rectangle(0, 0, srcImg.width(), srcImg.height());
        Rectangle clippedRect = rect.intersection(imageBounds);
        if (clippedRect.width < 3 || clippedRect.height < 3) {
            LOGGER.warn("The masking shape is not applicable: {}", (Object)clippedRect);
            return;
        }
        Rect cvRect = new Rect(clippedRect.x, clippedRect.y, clippedRect.width, clippedRect.height);
        Imgproc.blur((Mat)srcImg.submat(cvRect), (Mat)dstImg.submat(cvRect), (Size)DEFAULT_BLUR_SIZE);
    }
}

