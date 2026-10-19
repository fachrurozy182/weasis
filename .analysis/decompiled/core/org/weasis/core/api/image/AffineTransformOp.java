/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.opencv.core.Size
 *  org.weasis.core.util.MathUtil
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageTransformer
 */
package org.weasis.core.api.image;

import java.awt.geom.Rectangle2D;
import java.util.List;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.weasis.core.Messages;
import org.weasis.core.api.image.AbstractOp;
import org.weasis.core.api.image.ZoomOp;
import org.weasis.core.util.MathUtil;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageTransformer;

public class AffineTransformOp
extends AbstractOp {
    public static final String OP_NAME = Messages.getString("AffineTransformOp.affine_op");
    public static final List<Double> IDENTITY_MATRIX = List.of(Double.valueOf(1.0), Double.valueOf(0.0), Double.valueOf(0.0), Double.valueOf(0.0), Double.valueOf(1.0), Double.valueOf(0.0));
    public static final String P_AFFINE_MATRIX = "affine.matrix";
    public static final String P_INTERPOLATION = "interpolation";
    public static final String P_DST_BOUNDS = "dest.bounds";
    private static final int MATRIX_ROWS = 2;
    private static final int MATRIX_COLS = 3;

    public AffineTransformOp() {
        this.setName(OP_NAME);
    }

    public AffineTransformOp(AffineTransformOp op) {
        super(op);
    }

    @Override
    public AffineTransformOp copy() {
        return new AffineTransformOp(this);
    }

    @Override
    public void process() throws Exception {
        PlanarImage source = this.getSourceImage();
        PlanarImage result = this.applyTransformIfNeeded(source);
        this.params.put("op.output.img", result);
    }

    private PlanarImage applyTransformIfNeeded(PlanarImage source) {
        Rectangle2D bounds;
        List<Double> matrix = this.getAffineMatrix();
        if (this.isTransformNotRequired(source, matrix, bounds = this.getDestinationBounds())) {
            return source;
        }
        return this.applyAffineTransform(source, matrix, bounds);
    }

    private boolean isTransformNotRequired(PlanarImage source, List<Double> matrix, Rectangle2D bounds) {
        if (bounds == null || matrix == null) {
            return true;
        }
        boolean isIdentity = IDENTITY_MATRIX.equals(matrix);
        boolean hasSameDimensions = MathUtil.isEqual((double)source.width(), (double)bounds.getWidth()) && MathUtil.isEqual((double)source.height(), (double)bounds.getHeight());
        return isIdentity && hasSameDimensions;
    }

    private PlanarImage applyAffineTransform(PlanarImage source, List<Double> matrix, Rectangle2D bounds) {
        if (bounds.getWidth() <= 0.0 || bounds.getHeight() <= 0.0) {
            return null;
        }
        Mat transformMatrix = this.createTransformMatrix(matrix);
        Integer interpolation = this.getInterpolationValue();
        Size outputSize = new Size(bounds.getWidth(), bounds.getHeight());
        return ImageTransformer.warpAffine((Mat)source.toMat(), (Mat)transformMatrix, (Size)outputSize, (Integer)interpolation);
    }

    private Mat createTransformMatrix(List<Double> matrix) {
        Mat mat = new Mat(2, 3, CvType.CV_64FC1);
        double[] matrixArray = matrix.stream().mapToDouble(Double::doubleValue).toArray();
        mat.put(0, 0, matrixArray);
        return mat;
    }

    private List<Double> getAffineMatrix() {
        return (List)this.params.get(P_AFFINE_MATRIX);
    }

    private Rectangle2D getDestinationBounds() {
        return (Rectangle2D)this.params.get(P_DST_BOUNDS);
    }

    private Integer getInterpolationValue() {
        ZoomOp.Interpolation interpolation = (ZoomOp.Interpolation)((Object)this.params.get(P_INTERPOLATION));
        return interpolation != null ? Integer.valueOf(interpolation.getOpencvValue()) : null;
    }
}

