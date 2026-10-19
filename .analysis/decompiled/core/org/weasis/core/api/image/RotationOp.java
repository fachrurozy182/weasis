/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.runtime.SwitchBootstraps
 *  org.opencv.core.Mat
 *  org.opencv.core.Point
 *  org.opencv.core.Rect
 *  org.opencv.core.RotatedRect
 *  org.opencv.core.Size
 *  org.opencv.geometry.Geometry
 *  org.opencv.imgproc.Imgproc
 *  org.weasis.core.util.MathUtil
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageTransformer
 */
package org.weasis.core.api.image;

import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.geometry.Geometry;
import org.opencv.imgproc.Imgproc;
import org.weasis.core.Messages;
import org.weasis.core.api.image.AbstractOp;
import org.weasis.core.util.MathUtil;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageTransformer;

public final class RotationOp
extends AbstractOp {
    public static final String OP_NAME = Messages.getString("RotationOperation.title");
    public static final String P_ROTATE = "rotate";
    private static final int FULL_CIRCLE_DEGREES = 360;
    private static final int ROTATION_90 = 90;
    private static final int ROTATION_180 = 180;
    private static final int ROTATION_270 = 270;

    public RotationOp() {
        this.setName(OP_NAME);
    }

    public RotationOp(RotationOp op) {
        super(op);
    }

    @Override
    public RotationOp copy() {
        return new RotationOp(this);
    }

    @Override
    public void process() throws Exception {
        PlanarImage source = this.getSourceImage();
        int angle = this.getNormalizedRotationAngle();
        PlanarImage result = angle == 0 ? source : this.applyRotation(source, angle);
        this.params.put("op.output.img", result);
    }

    private int getNormalizedRotationAngle() {
        Object v = this.params.get(P_ROTATE);
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Integer.class}, v, (int)n)) {
            case 0 -> {
                Integer angle = (Integer)v;
                yield angle % 360;
            }
            default -> 0;
        };
    }

    private PlanarImage applyRotation(PlanarImage source, int angle) {
        return switch (angle) {
            case 90 -> ImageTransformer.getRotatedImage((Mat)source.toMat(), (int)0);
            case 180 -> ImageTransformer.getRotatedImage((Mat)source.toMat(), (int)1);
            case 270 -> ImageTransformer.getRotatedImage((Mat)source.toMat(), (int)2);
            default -> RotationOp.rotateArbitraryAngle(source.toMat(), angle, (double)source.width() / 2.0, (double)source.height() / 2.0);
        };
    }

    public static ImageCV rotateArbitraryAngle(Mat source, double angle, double centerX, double centerY) {
        if (MathUtil.isEqualToZero((double)angle)) {
            return ImageCV.fromMat((Mat)source);
        }
        Mat srcImg = Objects.requireNonNull(source, "Source image cannot be null");
        Point center = new Point(centerX, centerY);
        Mat rotationMatrix = Geometry.getRotationMatrix2D((Point)center, (double)(-angle), (double)1.0);
        Rect bbox = new RotatedRect(center, srcImg.size(), -angle).boundingRect();
        double[] matrix = RotationOp.transformMatrixWithOffset(rotationMatrix, bbox, center);
        rotationMatrix.put(0, 0, matrix);
        ImageCV dstImg = new ImageCV();
        Imgproc.warpAffine((Mat)srcImg, (Mat)dstImg, (Mat)rotationMatrix, (Size)bbox.size());
        return dstImg;
    }

    public static double[] transformMatrixWithOffset(Mat rot, Rect bbox, Point ptCenter) {
        double[] m = new double[rot.cols() * rot.rows()];
        rot.get(0, 0, m);
        m[2] = m[2] + ((double)bbox.width / 2.0 - ptCenter.x);
        int n = rot.cols() + 2;
        m[n] = m[n] + ((double)bbox.height / 2.0 - ptCenter.y);
        return m;
    }
}

