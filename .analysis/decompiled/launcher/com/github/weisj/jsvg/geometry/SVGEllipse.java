/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry;

import com.github.weisj.jsvg.attributes.value.LengthValue;
import com.github.weisj.jsvg.geometry.SVGCircle;
import com.github.weisj.jsvg.geometry.SVGShape;
import com.github.weisj.jsvg.renderer.MeasureContext;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;

public final class SVGEllipse
implements SVGShape {
    @NotNull
    private final Ellipse2D.Float ellipse = new Ellipse2D.Float();
    @NotNull
    private final LengthValue cx;
    @NotNull
    private final LengthValue cy;
    @NotNull
    private final LengthValue rx;
    @NotNull
    private final LengthValue ry;

    public SVGEllipse(@NotNull LengthValue cx, @NotNull LengthValue cy, @NotNull LengthValue rx, @NotNull LengthValue ry) {
        this.cx = cx;
        this.cy = cy;
        this.rx = rx;
        this.ry = ry;
    }

    private void validateShape(@NotNull MeasureContext measureContext) {
        float x = this.cx.resolve(measureContext);
        float y = this.cy.resolve(measureContext);
        float rrx = this.rx.resolve(measureContext);
        float rry = this.ry.resolve(measureContext);
        this.ellipse.setFrame(x - rrx, y - rry, 2.0f * rrx, 2.0f * rry);
    }

    @Override
    @NotNull
    public Shape shape(@NotNull RenderContext context, boolean validate) {
        if (validate) {
            this.validateShape(context.measureContext());
        }
        return this.ellipse;
    }

    @Override
    @NotNull
    public Rectangle2D bounds(@NotNull RenderContext context, boolean validate) {
        if (validate) {
            this.validateShape(context.measureContext());
        }
        return this.ellipse.getBounds2D();
    }

    @Override
    public double pathLength(@NotNull RenderContext context) {
        float b;
        MeasureContext measureContext = context.measureContext();
        float a = this.rx.resolve(measureContext);
        if (a == (b = this.ry.resolve(measureContext))) {
            return SVGCircle.circumference(a);
        }
        return SVGEllipse.ellipseCircumference(a, b);
    }

    @Override
    public boolean isClosed(@NotNull RenderContext context) {
        return true;
    }

    static double ellipseCircumference(double a, double b) {
        double h = (a - b) * (a - b) / ((a + b) * (a + b));
        double h4 = h / 4.0;
        return 0.7853981633974483 * (a + b) * (3.0 * (1.0 + h4) + 1.0 / (1.0 - h4));
    }
}

