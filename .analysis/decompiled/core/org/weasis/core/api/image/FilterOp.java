/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.api.image;

import org.weasis.core.Messages;
import org.weasis.core.api.image.AbstractOp;
import org.weasis.core.api.image.cv.CvUtil;
import org.weasis.core.api.image.util.KernelData;
import org.weasis.opencv.data.PlanarImage;

public final class FilterOp
extends AbstractOp {
    public static final String OP_NAME = Messages.getString("FilterOperation.title");
    public static final String P_KERNEL_DATA = "kernel";

    public FilterOp() {
        this.setName(OP_NAME);
    }

    public FilterOp(FilterOp op) {
        super(op);
    }

    @Override
    public FilterOp copy() {
        return new FilterOp(this);
    }

    @Override
    public void process() throws Exception {
        PlanarImage source = this.getSourceImage();
        KernelData kernel = (KernelData)this.params.get(P_KERNEL_DATA);
        PlanarImage result = kernel != null && !kernel.equals(KernelData.NONE) ? CvUtil.filter(source.toMat(), kernel) : source;
        this.params.put("op.output.img", result);
    }
}

