/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.animation.AnimationPeriod;
import com.github.weisj.jsvg.attributes.Coordinate;
import com.github.weisj.jsvg.attributes.Overflow;
import com.github.weisj.jsvg.attributes.value.LengthValue;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.Unit;
import com.github.weisj.jsvg.nodes.Anchor;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Image;
import com.github.weisj.jsvg.nodes.Marker;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.Pattern;
import com.github.weisj.jsvg.nodes.Style;
import com.github.weisj.jsvg.nodes.View;
import com.github.weisj.jsvg.nodes.container.CommonInnerViewContainer;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.nodes.text.Text;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.renderer.MeasureContext;
import com.github.weisj.jsvg.renderer.animation.AnimationState;
import com.github.weisj.jsvg.view.FloatSize;
import com.github.weisj.jsvg.view.ViewBox;
import java.awt.Point;
import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ElementCategories(value={Category.Container, Category.Structural})
@PermittedContent(categories={Category.Animation, Category.Descriptive, Category.Shape, Category.Structural, Category.Gradient}, anyOf={Anchor.class, ClipPath.class, Filter.class, Image.class, Mask.class, Marker.class, Pattern.class, Style.class, Text.class, View.class})
public final class SVG
extends CommonInnerViewContainer {
    public static final String TAG = "svg";
    @NotNull
    private static final Coordinate<LengthValue> TOP_LEVEL_TRANSFORM_ORIGIN = new Coordinate<Length>(Unit.PERCENTAGE_WIDTH.valueOf(50.0f), Unit.PERCENTAGE_WIDTH.valueOf(50.0f));
    private static final float FALLBACK_WIDTH = 300.0f;
    private static final float FALLBACK_HEIGHT = 150.0f;
    private boolean isTopLevel;
    private AnimationPeriod animationPeriod;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    public boolean isTopLevel() {
        return this.isTopLevel;
    }

    @NotNull
    public AnimationPeriod animationPeriod() {
        return this.animationPeriod;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        this.isTopLevel = attributeNode.element().parent() == null;
        super.build(attributeNode);
        this.animationPeriod = attributeNode.document().animationPeriod();
    }

    @Override
    @NotNull
    protected Point2D outerLocation(@NotNull MeasureContext context) {
        if (this.isTopLevel) {
            return new Point(0, 0);
        }
        return super.outerLocation(context);
    }

    @Override
    @NotNull
    public Coordinate<LengthValue> transformOrigin() {
        if (this.isTopLevel) {
            return TOP_LEVEL_TRANSFORM_ORIGIN;
        }
        return super.transformOrigin();
    }

    @Override
    @NotNull
    protected Overflow defaultOverflow() {
        return Overflow.Hidden;
    }

    @NotNull
    public FloatSize sizeForTopLevel(@Nullable ViewBox outerViewBox, float em, float ex) {
        FloatSize size = outerViewBox != null ? outerViewBox.size() : new FloatSize(100.0f, 100.0f);
        MeasureContext topLevelContext = MeasureContext.createInitial(size, em, ex, AnimationState.NO_ANIMATION);
        return new FloatSize(this.width.orElseIfUnspecified(this.viewBox != null ? this.viewBox.width : 300.0f).resolve(topLevelContext), this.height.orElseIfUnspecified(this.viewBox != null ? this.viewBox.height : 150.0f).resolve(topLevelContext));
    }
}

