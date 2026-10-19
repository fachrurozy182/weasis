/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes.stroke;

import com.github.weisj.jsvg.attributes.stroke.LineCap;
import com.github.weisj.jsvg.attributes.stroke.LineJoin;
import com.github.weisj.jsvg.attributes.value.LengthValue;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.renderer.MeasureContext;
import com.github.weisj.jsvg.renderer.impl.context.StrokeContext;
import java.awt.BasicStroke;
import java.awt.Stroke;
import org.jetbrains.annotations.NotNull;

public final class StrokeResolver {
    private StrokeResolver() {
    }

    @NotNull
    public static Stroke resolve(float pathLengthFactor, @NotNull MeasureContext measureContext, @NotNull StrokeContext context) {
        LengthValue strokeWidth = context.strokeWidth;
        LineCap lineCap = context.lineCap;
        LineJoin lineJoin = context.lineJoin;
        float miterLimit = context.miterLimit;
        Length[] dashPattern = context.dashPattern;
        LengthValue dashOffset = context.dashOffset;
        assert (strokeWidth != null);
        assert (lineCap != null);
        assert (lineJoin != null);
        assert (Length.isSpecified(miterLimit));
        assert (dashOffset != null);
        assert (dashPattern != null);
        miterLimit = Math.max(1.0f, miterLimit);
        float[] dashes = new float[dashPattern.length];
        float offsetLength = 0.0f;
        for (int i = 0; i < dashes.length; ++i) {
            float dash = dashPattern[i].resolve(measureContext) * pathLengthFactor;
            offsetLength += dash;
            dashes[i] = dash;
        }
        float phase = dashOffset.resolve(measureContext) * pathLengthFactor;
        if (phase < 0.0f) {
            phase += offsetLength;
        }
        if (dashes.length == 0) {
            return new BasicStroke(strokeWidth.resolve(measureContext), lineCap.awtCode(), lineJoin.awtCode(), miterLimit);
        }
        return new BasicStroke(strokeWidth.resolve(measureContext), lineCap.awtCode(), lineJoin.awtCode(), miterLimit, dashes, phase);
    }
}

