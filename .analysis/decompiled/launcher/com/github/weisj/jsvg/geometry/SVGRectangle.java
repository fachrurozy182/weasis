/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry;

import com.github.weisj.jsvg.attributes.value.LengthValue;
import com.github.weisj.jsvg.geometry.SVGShape;
import com.github.weisj.jsvg.renderer.MeasureContext;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;

public final class SVGRectangle
implements SVGShape {
    @NotNull
    private final Rectangle2D.Float rect = new Rectangle2D.Float();
    @NotNull
    private final LengthValue x;
    @NotNull
    private final LengthValue y;
    @NotNull
    private final LengthValue w;
    @NotNull
    private final LengthValue h;

    public SVGRectangle(@NotNull LengthValue x, @NotNull LengthValue y, @NotNull LengthValue w, @NotNull LengthValue h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    private void validateShape(@NotNull MeasureContext measureContext) {
        this.rect.setRect(this.x.resolve(measureContext), this.y.resolve(measureContext), this.w.resolve(measureContext), this.h.resolve(measureContext));
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
        return this.rect;
    }

    @Override
    public double pathLength(@NotNull RenderContext context) {
        MeasureContext measureContext = context.measureContext();
        return 2.0f * (this.w.resolve(measureContext) + this.h.resolve(measureContext));
    }

    @Override
    public boolean isClosed(@NotNull RenderContext context) {
        return true;
    }
}

