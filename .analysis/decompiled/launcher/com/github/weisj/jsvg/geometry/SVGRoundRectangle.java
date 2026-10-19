/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry;

import com.github.weisj.jsvg.attributes.value.LengthValue;
import com.github.weisj.jsvg.geometry.SVGCircle;
import com.github.weisj.jsvg.geometry.SVGEllipse;
import com.github.weisj.jsvg.geometry.SVGShape;
import com.github.weisj.jsvg.renderer.MeasureContext;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.jetbrains.annotations.NotNull;

public final class SVGRoundRectangle
implements SVGShape {
    @NotNull
    private final RoundRectangle2D.Float rect = new RoundRectangle2D.Float();
    @NotNull
    private final LengthValue x;
    @NotNull
    private final LengthValue y;
    @NotNull
    private final LengthValue w;
    @NotNull
    private final LengthValue h;
    @NotNull
    private final LengthValue rx;
    @NotNull
    private final LengthValue ry;

    public SVGRoundRectangle(@NotNull LengthValue x, @NotNull LengthValue y, @NotNull LengthValue w, @NotNull LengthValue h, @NotNull LengthValue rx, @NotNull LengthValue ry) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.rx = rx;
        this.ry = ry;
    }

    private void validateShape(@NotNull MeasureContext measureContext) {
        this.rect.setRoundRect(this.x.resolve(measureContext), this.y.resolve(measureContext), this.w.resolve(measureContext), this.h.resolve(measureContext), Math.max(0.0f, this.rx.resolve(measureContext) * 2.0f), Math.max(0.0f, this.ry.resolve(measureContext) * 2.0f));
    }

    @Override
    @NotNull
    public Shape shape(@NotNull RenderContext context, boolean validate) {
        if (validate) {
            this.validateShape(context.measureContext());
        }
        return this.rect;
    }

    @Override
    @NotNull
    public Rectangle2D bounds(@NotNull RenderContext context, boolean validate) {
        if (validate) {
            this.validateShape(context.measureContext());
        }
        return this.rect.getBounds2D();
    }

    @Override
    public double pathLength(@NotNull RenderContext context) {
        MeasureContext measureContext = context.measureContext();
        float a = this.rx.resolve(measureContext);
        float b = this.ry.resolve(measureContext);
        double l = 2.0f * (this.w.resolve(measureContext) - 2.0f * a + (this.h.resolve(measureContext) - 2.0f * b));
        if (a == b) {
            return l + SVGCircle.circumference(a);
        }
        return l + SVGEllipse.ellipseCircumference(a, b);
    }

    @Override
    public boolean isClosed(@NotNull RenderContext context) {
        return true;
    }
}

