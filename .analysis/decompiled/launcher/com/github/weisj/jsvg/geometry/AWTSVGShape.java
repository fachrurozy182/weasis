/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry;

import com.github.weisj.jsvg.attributes.value.ConstantValue;
import com.github.weisj.jsvg.attributes.value.Value;
import com.github.weisj.jsvg.geometry.SVGEllipse;
import com.github.weisj.jsvg.geometry.SVGShape;
import com.github.weisj.jsvg.geometry.util.GeometryUtil;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;

public class AWTSVGShape<T extends Shape>
implements SVGShape {
    public static final Rectangle2D EMPTY_SHAPE = new Rectangle();
    @NotNull
    protected final @NotNull Value<@NotNull T> shapeValue;
    private Rectangle2D boundsCache;
    private T shapeCache;
    private double pathLength;

    public AWTSVGShape(@NotNull T shape) {
        this(new ConstantValue<T>(shape));
    }

    public AWTSVGShape(@NotNull @NotNull Value<@NotNull T> shapeValue) {
        this(shapeValue, Double.NaN);
    }

    private AWTSVGShape(@NotNull @NotNull Value<@NotNull T> shapeValue, double pathLength) {
        this.shapeValue = shapeValue;
        this.pathLength = pathLength;
    }

    @NotNull
    public T shape(@NotNull RenderContext context, boolean validate) {
        if (this.shapeCache == null || validate) {
            this.shapeCache = (Shape)this.shapeValue.get(context.measureContext());
        }
        return this.shapeCache;
    }

    @Override
    @NotNull
    public Rectangle2D bounds(@NotNull RenderContext context, boolean validate) {
        if (this.boundsCache == null || validate) {
            T shape = this.shape(context, validate);
            this.boundsCache = shape.getBounds2D();
        }
        return this.boundsCache;
    }

    @Override
    public double pathLength(@NotNull RenderContext context) {
        if (Double.isNaN(this.pathLength)) {
            this.pathLength = this.computePathLength(context);
        }
        return this.pathLength;
    }

    private double computePathLength(@NotNull RenderContext context) {
        T shape = this.shape(context, false);
        if (shape instanceof Rectangle2D) {
            Rectangle2D r = (Rectangle2D)shape;
            return 2.0 * (r.getWidth() + r.getHeight());
        }
        if (shape instanceof Ellipse2D) {
            double h;
            Ellipse2D e = (Ellipse2D)shape;
            double w = e.getWidth();
            if (w == (h = e.getHeight())) {
                return Math.PI * w;
            }
            return SVGEllipse.ellipseCircumference(w / 2.0, h / 2.0);
        }
        return GeometryUtil.pathLength(shape);
    }

    @Override
    public boolean isClosed(@NotNull RenderContext context) {
        T shape = this.shape(context, false);
        if (shape instanceof Rectangle2D) {
            return true;
        }
        if (shape instanceof Ellipse2D) {
            return true;
        }
        return GeometryUtil.isSingleClosedPath(shape);
    }
}

