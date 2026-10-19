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
import org.opencv.core.Mat;
import org.weasis.core.Messages;
import org.weasis.core.api.image.AbstractOp;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageTransformer;

public final class CropOp
extends AbstractOp {
    public static final String OP_NAME = Messages.getString("CropOperation.name");
    public static final String P_AREA = "area";

    public CropOp() {
        this.setName(OP_NAME);
    }

    public CropOp(CropOp op) {
        super(op);
    }

    @Override
    public CropOp copy() {
        return new CropOp(this);
    }

    @Override
    public void process() throws Exception {
        PlanarImage source = this.getSourceImage();
        Rectangle cropArea = (Rectangle)this.params.get(P_AREA);
        PlanarImage result = this.needsCrop(cropArea, source) ? ImageTransformer.crop((Mat)source.toMat(), (Rectangle)cropArea) : source;
        this.params.put("op.output.img", result);
    }

    private boolean needsCrop(Rectangle cropArea, PlanarImage source) {
        if (cropArea == null || cropArea.isEmpty()) {
            return false;
        }
        Rectangle imageArea = new Rectangle(0, 0, source.width(), source.height());
        return !cropArea.equals(imageArea);
    }
}

