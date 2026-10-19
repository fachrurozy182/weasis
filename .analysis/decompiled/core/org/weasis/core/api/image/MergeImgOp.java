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

public class MergeImgOp
extends AbstractOp {
    public static final String OP_NAME = "merge.img";
    public static final String INPUT_IMG2 = "op.input.img.2";
    public static final String P_OPACITY_1 = "opacity1";
    public static final String P_OPACITY_2 = "opacity2";
    private static final double DEFAULT_OPACITY = 1.0;

    public MergeImgOp() {
        this.setName(OP_NAME);
    }

    public MergeImgOp(MergeImgOp op) {
        super(op);
    }

    @Override
    public MergeImgOp copy() {
        return new MergeImgOp(this);
    }

    @Override
    public void process() throws Exception {
        PlanarImage source = this.getSourceImage();
        PlanarImage result = this.mergeImages(source);
        this.params.put("op.output.img", result);
    }

    private PlanarImage mergeImages(PlanarImage source) {
        PlanarImage secondImage = this.getParam(INPUT_IMG2, PlanarImage.class);
        if (secondImage == null) {
            return source;
        }
        double opacity1 = this.getOpacity(P_OPACITY_1);
        double opacity2 = this.getOpacity(P_OPACITY_2);
        if (opacity1 == 1.0 && opacity2 == 1.0) {
            return source;
        }
        return ImageTransformer.mergeImages((Mat)source.toMat(), (Mat)secondImage.toMat(), (double)opacity1, (double)opacity2);
    }

    private double getOpacity(String paramKey) {
        return this.getParam(paramKey, Double.class, 1.0);
    }
}

