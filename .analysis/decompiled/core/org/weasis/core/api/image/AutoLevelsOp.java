/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Core$MinMaxLocResult
 *  org.opencv.core.Mat
 *  org.weasis.core.util.LangUtil
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageAnalyzer
 *  org.weasis.opencv.op.ImageConversion
 *  org.weasis.opencv.op.ImageTransformer
 */
package org.weasis.core.api.image;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.weasis.core.Messages;
import org.weasis.core.api.image.AbstractOp;
import org.weasis.core.util.LangUtil;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageAnalyzer;
import org.weasis.opencv.op.ImageConversion;
import org.weasis.opencv.op.ImageTransformer;

public class AutoLevelsOp
extends AbstractOp {
    public static final String OP_NAME = Messages.getString("AutoLevelsOp.auto_ct");
    public static final String P_AUTO_LEVEL = "auto.level";
    private static final double MAX_BYTE_VALUE = 255.0;
    private static final double MIN_RANGE_THRESHOLD = 1.0;

    public AutoLevelsOp() {
        this.setName(OP_NAME);
    }

    public AutoLevelsOp(AutoLevelsOp op) {
        super(op);
    }

    @Override
    public AutoLevelsOp copy() {
        return new AutoLevelsOp(this);
    }

    @Override
    public void process() throws Exception {
        PlanarImage source = this.getSourceImage();
        PlanarImage result = this.isAutoLevelEnabled() ? this.applyAutoLevels(source) : source;
        this.params.put("op.output.img", result);
    }

    private boolean isAutoLevelEnabled() {
        return LangUtil.nullToFalse((Boolean)((Boolean)this.params.get(P_AUTO_LEVEL)));
    }

    private PlanarImage applyAutoLevels(PlanarImage source) {
        Core.MinMaxLocResult minMax = ImageAnalyzer.findMinMaxValues((Mat)source.toMat());
        double range = this.computeRange(minMax, source);
        double slope = 255.0 / range;
        double intercept = -slope * minMax.minVal;
        return ImageTransformer.rescaleToByte((Mat)source.toImageCV(), (double)slope, (double)intercept);
    }

    private double computeRange(Core.MinMaxLocResult minMax, PlanarImage source) {
        double range = minMax.maxVal - minMax.minVal;
        return range < 1.0 && this.isIntegerType(source) ? 1.0 : range;
    }

    private boolean isIntegerType(PlanarImage source) {
        return ImageConversion.convertToDataType((int)source.type()) == 3;
    }
}

