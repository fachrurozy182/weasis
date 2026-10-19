/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.attributes.value.PercentageDimension;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.Percentage;
import com.github.weisj.jsvg.geometry.size.Unit;
import com.github.weisj.jsvg.nodes.AbstractGradient;
import com.github.weisj.jsvg.nodes.Stop;
import com.github.weisj.jsvg.nodes.animation.Animate;
import com.github.weisj.jsvg.nodes.animation.AnimateTransform;
import com.github.weisj.jsvg.nodes.animation.Set;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.renderer.MeasureContext;
import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ElementCategories(value={Category.Gradient})
@PermittedContent(categories={Category.Descriptive}, anyOf={Stop.class, Animate.class, AnimateTransform.class, Set.class})
public final class LinearGradient
extends AbstractGradient<LinearGradient> {
    public static final String TAG = "lineargradient";
    private Length x1;
    private Length x2;
    private Length y1;
    private Length y2;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    protected void buildGradient(@NotNull AttributeNode attributeNode, @Nullable LinearGradient template) {
        this.x1 = attributeNode.getLength("x1", PercentageDimension.WIDTH, template != null ? template.x1 : Unit.PERCENTAGE_WIDTH.valueOf(0.0f));
        this.x2 = attributeNode.getLength("x2", PercentageDimension.WIDTH, template != null ? template.x2 : Unit.PERCENTAGE_WIDTH.valueOf(100.0f));
        this.y1 = attributeNode.getLength("y1", PercentageDimension.HEIGHT, template != null ? template.y1 : Unit.PERCENTAGE_HEIGHT.valueOf(0.0f));
        this.y2 = attributeNode.getLength("y2", PercentageDimension.HEIGHT, template != null ? template.y2 : Unit.PERCENTAGE_HEIGHT.valueOf(0.0f));
    }

    @Override
    @NotNull
    protected Paint gradientForBounds(@NotNull MeasureContext measure, @NotNull Rectangle2D bounds, Percentage[] gradOffsets, @NotNull Color[] gradColors) {
        Point2D.Float pt2;
        Point2D.Float pt1 = new Point2D.Float(this.x1.resolve(measure), this.y1.resolve(measure));
        if (pt1.equals(pt2 = new Point2D.Float(this.x2.resolve(measure), this.y2.resolve(measure)))) {
            return gradColors[0];
        }
        return new LinearGradientPaint(pt1, pt2, this.offsetsToFractions(gradOffsets), gradColors, this.spreadMethod.cycleMethod(), MultipleGradientPaint.ColorSpaceType.SRGB, this.computeViewTransform(measure, bounds));
    }

    @Override
    public String toString() {
        return "LinearGradient{spreadMethod=" + (Object)((Object)this.spreadMethod) + ", gradientTransform=" + this.gradientTransform + ", x1=" + this.x1 + ", x2=" + this.x2 + ", y1=" + this.y1 + ", y2=" + this.y2 + ", colors=" + Arrays.toString(this.colors()) + ", offsets=" + Arrays.toString(this.offsets()) + '}';
    }
}

