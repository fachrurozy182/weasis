/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Mat
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageTransformer
 */
package org.weasis.core.api.image;

import org.opencv.core.Mat;
import org.weasis.core.api.image.AbstractOp;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageTransformer;

public class BrightnessOp
extends AbstractOp {
    public static final String OP_NAME = "rescale";
    public static final String P_BRIGHTNESS_VALUE = "rescale.brightness";
    public static final String P_CONTRAST_VALUE = "rescale.contrast";
    private static final double CONTRAST_SCALE_FACTOR = 100.0;
    private static final double DEFAULT_CONTRAST = 1.0;
    private static final double DEFAULT_BRIGHTNESS = 0.0;

    public BrightnessOp() {
        this.setName(OP_NAME);
    }

    public BrightnessOp(BrightnessOp op) {
        super(op);
    }

    @Override
    public BrightnessOp copy() {
        return new BrightnessOp(this);
    }

    @Override
    public void process() throws Exception {
        PlanarImage source = this.getSourceImage();
        double contrast = this.getParam(P_CONTRAST_VALUE, Double.class, 1.0);
        double brightness = this.getParam(P_BRIGHTNESS_VALUE, Double.class, 0.0);
        PlanarImage result = contrast == 1.0 && brightness == 0.0 ? source : ImageTransformer.rescaleToByte((Mat)source.toImageCV(), (double)(contrast / 100.0), (double)brightness);
        this.params.put("op.output.img", result);
    }
}

