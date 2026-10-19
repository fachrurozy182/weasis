/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.prototype;

import com.github.weisj.jsvg.geometry.util.GeometryUtil;
import com.github.weisj.jsvg.nodes.prototype.Container;
import com.github.weisj.jsvg.nodes.prototype.HasShape;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.impl.NodeRenderer;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;

public interface ShapedContainer<E>
extends Container<E>,
HasShape {
    @Override
    @NotNull
    default public Shape untransformedElementShape(@NotNull RenderContext context, HasShape.Box box) {
        Path2D.Float shape = new Path2D.Float();
        for (Object child : this.children()) {
            if (!(child instanceof HasShape)) continue;
            RenderContext childContext = NodeRenderer.setupRenderContext(child, context);
            Shape childShape = ((HasShape)child).elementShape(childContext, box);
            shape.append(childShape, false);
        }
        return shape;
    }

    @Override
    @NotNull
    default public Rectangle2D untransformedElementBounds(@NotNull RenderContext context, HasShape.Box box) {
        Rectangle2D bounds = null;
        for (Object child : this.children()) {
            RenderContext childContext;
            Rectangle2D childBounds;
            if (!(child instanceof HasShape) || !GeometryUtil.isValidRect(childBounds = ((HasShape)child).elementBounds(childContext = NodeRenderer.setupRenderContext(child, context), box)) || childBounds.isEmpty()) continue;
            if (bounds == null) {
                bounds = childBounds;
                continue;
            }
            Rectangle2D.union(bounds, childBounds, bounds);
        }
        if (bounds == null) {
            return new Rectangle2D.Float(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, 0.0f, 0.0f);
        }
        return bounds;
    }
}

