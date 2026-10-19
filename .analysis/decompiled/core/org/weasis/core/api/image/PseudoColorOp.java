/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Mat
 *  org.weasis.core.util.LangUtil
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageTransformer
 *  org.weasis.opencv.op.lut.ByteLut
 */
package org.weasis.core.api.image;

import org.opencv.core.Mat;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.image.AbstractOp;
import org.weasis.core.api.image.op.ByteLutCollection;
import org.weasis.core.util.LangUtil;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageTransformer;
import org.weasis.opencv.op.lut.ByteLut;

public final class PseudoColorOp
extends AbstractOp {
    public static final String OP_NAME = Messages.getString("PseudoColorOperation.title");
    public static final String P_LUT = ActionW.LUT.cmd();
    public static final String P_LUT_INVERSE = ActionW.INVERT_LUT.cmd();

    public PseudoColorOp() {
        this.setName(OP_NAME);
    }

    public PseudoColorOp(PseudoColorOp op) {
        super(op);
    }

    @Override
    public PseudoColorOp copy() {
        return new PseudoColorOp(this);
    }

    @Override
    public void process() throws Exception {
        PlanarImage source = this.getSourceImage();
        ByteLut lutTable = (ByteLut)this.params.get(P_LUT);
        boolean invertLut = LangUtil.nullToFalse((Boolean)((Boolean)this.params.get(P_LUT_INVERSE)));
        PlanarImage result = this.applyPseudoColor(source, lutTable, invertLut);
        this.params.put("op.output.img", result);
    }

    private PlanarImage applyPseudoColor(PlanarImage source, ByteLut lutTable, boolean invertLut) {
        if (lutTable == null) {
            return source;
        }
        byte[][] lut = lutTable.lutTable();
        if (lut == null) {
            return invertLut ? ImageTransformer.invertLUT((ImageCV)source.toImageCV()) : source;
        }
        byte[][] finalLut = invertLut ? ByteLutCollection.invert(lut) : lut;
        return ImageTransformer.applyLUT((Mat)source.toMat(), (byte[][])finalLut);
    }
}

