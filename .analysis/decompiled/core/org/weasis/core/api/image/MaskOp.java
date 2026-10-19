/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Mat
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageTransformer
 */
package org.weasis.core.api.image;

import java.awt.Rectangle;
import java.util.Optional;
import org.opencv.core.Mat;
import org.weasis.core.api.image.AbstractOp;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageTransformer;

public class MaskOp
extends AbstractOp {
    public static final String OP_NAME = "Mask";
    public static final String P_SHOW = "show";
    public static final String P_SHAPE = "shape";
    public static final String P_ALPHA = "img.alpha";
    private static final double DEFAULT_ALPHA = 0.7;
    private static final double MIN_ALPHA = 0.0;
    private static final double MAX_ALPHA = 1.0;

    public MaskOp() {
        this.setName(OP_NAME);
    }

    public MaskOp(MaskOp op) {
        super(op);
    }

    @Override
    public MaskOp copy() {
        return new MaskOp(this);
    }

    @Override
    public void process() throws Exception {
        PlanarImage source = this.getSourceImage();
        PlanarImage result = this.shouldApplyMask() ? this.processWithMask(source) : source;
        this.params.put("op.output.img", result);
    }

    private PlanarImage processWithMask(PlanarImage source) throws Exception {
        return this.getMaskArea(source).map(area -> this.applyMask(source, (Rectangle)area, this.getAlphaValue())).orElse(source);
    }

    private boolean shouldApplyMask() {
        Boolean show;
        Object v = this.params.get(P_SHOW);
        return v instanceof Boolean && (show = (Boolean)v) != false;
    }

    private Optional<Rectangle> getMaskArea(PlanarImage source) {
        Object v = this.params.get(P_SHAPE);
        if (!(v instanceof Rectangle)) {
            return Optional.empty();
        }
        Rectangle area = (Rectangle)v;
        Rectangle imageBounds = new Rectangle(0, 0, source.width(), source.height());
        if (area.equals(imageBounds)) {
            return Optional.empty();
        }
        this.validateMaskArea(area, imageBounds);
        return Optional.of(area);
    }

    private void validateMaskArea(Rectangle maskArea, Rectangle imageBounds) {
        if (maskArea.x < 0 || maskArea.y < 0 || maskArea.x + maskArea.width > imageBounds.width || maskArea.y + maskArea.height > imageBounds.height) {
            throw new IllegalArgumentException("Mask area %s is outside image bounds %s".formatted(new Object[]{maskArea, imageBounds}));
        }
    }

    private double getAlphaValue() {
        double d;
        Object v = this.params.get(P_ALPHA);
        if (v instanceof Number) {
            Number num = (Number)v;
            d = Math.clamp((double)num.doubleValue(), (double)0.0, (double)1.0);
        } else {
            d = 0.7;
        }
        return d;
    }

    private PlanarImage applyMask(PlanarImage source, Rectangle area, double alpha) {
        try {
            return ImageTransformer.applyCropMask((Mat)source.toMat(), (Rectangle)area, (double)alpha);
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to apply mask: " + e.getMessage(), e);
        }
    }
}

