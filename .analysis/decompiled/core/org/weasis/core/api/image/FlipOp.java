/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Mat
 *  org.weasis.core.util.LangUtil
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageTransformer
 */
package org.weasis.core.api.image;

import org.opencv.core.Mat;
import org.weasis.core.Messages;
import org.weasis.core.api.image.AbstractOp;
import org.weasis.core.util.LangUtil;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageTransformer;

public final class FlipOp
extends AbstractOp {
    public static final String OP_NAME = Messages.getString("FlipOperation.title");
    public static final String P_FLIP = "flip";
    private static final int HORIZONTAL_FLIP_CODE = 1;

    public FlipOp() {
        this.setName(OP_NAME);
    }

    public FlipOp(FlipOp op) {
        super(op);
    }

    @Override
    public FlipOp copy() {
        return new FlipOp(this);
    }

    @Override
    public void process() throws Exception {
        PlanarImage source = this.getSourceImage();
        PlanarImage result = LangUtil.nullToFalse((Boolean)((Boolean)this.params.get(P_FLIP))) ? ImageTransformer.flip((Mat)source.toMat(), (int)1) : source;
        this.params.put("op.output.img", result);
    }
}

