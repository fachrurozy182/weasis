/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.prototype;

import com.github.weisj.jsvg.geometry.util.GeometryUtil;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.prototype.Transformable;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.impl.ElementBounds;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;

public interface HasShape
extends SVGNode {
    @NotNull
    default public Shape elementShape(@NotNull RenderContext context, Box box) {
        Shape shape = this.untransformedElementShape(context, box);
        if (this instanceof Transformable) {
            return ((Transformable)((Object)this)).transformShape(shape, context, ElementBounds.fromUntransformedBounds(this, context, shape.getBounds2D(), box));
        }
        return shape;
    }

    @NotNull
    public Shape untransformedElementShape(@NotNull RenderContext var1, Box var2);

    @NotNull
    default public Rectangle2D elementBounds(@NotNull RenderContext context, Box box) {
        Rectangle2D shape = this.untransformedElementBounds(context, box);
        if (!GeometryUtil.isValidRect(shape)) {
            return shape;
        }
        if (this instanceof Transformable) {
            return ((Transformable)((Object)this)).transformShape(shape, context, ElementBounds.fromUntransformedBounds(this, context, shape, box)).getBounds2D();
        }
        return shape;
    }

    @NotNull
    public Rectangle2D untransformedElementBounds(@NotNull RenderContext var1, Box var2);

    public static enum Box {
        BoundingBox,
        StrokeBox;

    }
}

