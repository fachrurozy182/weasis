/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Mat
 *  org.weasis.core.util.MathUtil
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageTransformer
 */
package org.weasis.core.api.image;

import java.awt.Dimension;
import org.opencv.core.Mat;
import org.weasis.core.Messages;
import org.weasis.core.api.image.AbstractOp;
import org.weasis.core.util.MathUtil;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageTransformer;

public class ZoomOp
extends AbstractOp {
    public static final String OP_NAME = Messages.getString("ZoomOperation.title");
    public static final String P_RATIO_X = "ratio.x";
    public static final String P_RATIO_Y = "ratio.y";
    public static final String P_INTERPOLATION = "interpolation";
    private static final double DEFAULT_ZOOM_FACTOR = 1.0;
    private static final double DOWNSCALE_THRESHOLD = 0.1;

    public ZoomOp() {
        this.setName(OP_NAME);
    }

    public ZoomOp(ZoomOp op) {
        super(op);
    }

    @Override
    public ZoomOp copy() {
        return new ZoomOp(this);
    }

    @Override
    public void process() throws Exception {
        PlanarImage source = this.getSourceImage();
        Double zoomX = (Double)this.params.get(P_RATIO_X);
        Double zoomY = (Double)this.params.get(P_RATIO_Y);
        PlanarImage result = zoomX == null || zoomY == null || !this.requiresScaling(zoomX, zoomY) ? source : this.scaleImage(source, zoomX, zoomY);
        this.params.put("op.output.img", result);
    }

    private boolean requiresScaling(double zoomX, double zoomY) {
        return MathUtil.isDifferent((double)zoomX, (double)1.0) || MathUtil.isDifferent((double)zoomY, (double)1.0);
    }

    private PlanarImage scaleImage(PlanarImage source, double zoomX, double zoomY) throws Exception {
        int newWidth = (int)(Math.abs(zoomX) * (double)source.width());
        int newHeight = (int)(Math.abs(zoomY) * (double)source.height());
        Integer interpolation = this.selectInterpolation(zoomX, zoomY);
        return ImageTransformer.scale((Mat)source.toMat(), (Dimension)new Dimension(newWidth, newHeight), (Integer)interpolation);
    }

    private Integer selectInterpolation(double zoomX, double zoomY) {
        if (Math.abs(zoomX) < 0.1 || Math.abs(zoomY) < 0.1) {
            return 3;
        }
        Interpolation interpolation = (Interpolation)((Object)this.params.get(P_INTERPOLATION));
        return interpolation != null ? Integer.valueOf(interpolation.getOpencvValue()) : null;
    }

    public static enum Interpolation {
        NEAREST_NEIGHBOUR(Messages.getString("ZoomOperation.nearest"), 0),
        BILINEAR(Messages.getString("ZoomOperation.bilinear"), 1),
        BICUBIC(Messages.getString("ZoomOperation.bicubic"), 2),
        LANCZOS(Messages.getString("ZoomOperation.lanczos"), 4);

        private final String title;
        private final int opencvValue;

        private Interpolation(String title, int opencvValue) {
            this.title = title;
            this.opencvValue = opencvValue;
        }

        public int getOpencvValue() {
            return this.opencvValue;
        }

        public String getTitle() {
            return this.title;
        }

        public String toString() {
            return this.title;
        }

        public static Interpolation fromPosition(int position) {
            Interpolation[] values = Interpolation.values();
            return position >= 0 && position < values.length ? values[position] : BILINEAR;
        }
    }
}

