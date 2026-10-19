/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.MustBeInvokedByOverriders
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.container;

import com.github.weisj.jsvg.attributes.value.PercentageDimension;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.container.BaseInnerViewContainer;
import com.github.weisj.jsvg.nodes.prototype.ShapedContainer;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.renderer.MeasureContext;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.view.FloatSize;
import java.awt.geom.Point2D;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CommonInnerViewContainer
extends BaseInnerViewContainer
implements ShapedContainer<SVGNode> {
    protected Length x;
    protected Length y;
    protected Length width;
    protected Length height;

    @Override
    @NotNull
    protected Point2D outerLocation(@NotNull MeasureContext context) {
        return new Point2D.Float(this.x.resolve(context), this.y.resolve(context));
    }

    @Override
    @Nullable
    protected Point2D anchorLocation(@NotNull MeasureContext context) {
        return null;
    }

    @Override
    @NotNull
    public FloatSize size(@NotNull RenderContext context) {
        MeasureContext measure = context.measureContext();
        return new FloatSize(this.width.orElseIfUnspecified(measure.viewWidth()).resolve(measure), this.height.orElseIfUnspecified(measure.viewHeight()).resolve(measure));
    }

    @Override
    public boolean isVisible(@NotNull RenderContext context) {
        return !this.width.isZero() && !this.height.isZero() && super.isVisible(context);
    }

    @Override
    @MustBeInvokedByOverriders
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.x = attributeNode.getLength("x", PercentageDimension.WIDTH, 0.0f);
        this.y = attributeNode.getLength("y", PercentageDimension.HEIGHT, 0.0f);
        this.width = attributeNode.getLength("width", PercentageDimension.WIDTH, Length.UNSPECIFIED);
        this.height = attributeNode.getLength("height", PercentageDimension.HEIGHT, Length.UNSPECIFIED);
    }
}

