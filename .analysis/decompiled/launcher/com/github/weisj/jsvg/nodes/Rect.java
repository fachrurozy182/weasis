/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.attributes.Animatable;
import com.github.weisj.jsvg.attributes.Inherited;
import com.github.weisj.jsvg.attributes.value.LengthValue;
import com.github.weisj.jsvg.attributes.value.PercentageDimension;
import com.github.weisj.jsvg.geometry.SVGRectangle;
import com.github.weisj.jsvg.geometry.SVGRoundRectangle;
import com.github.weisj.jsvg.geometry.SVGShape;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.nodes.ShapeNode;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.util.AttributeUtil;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={Category.BasicShape, Category.Graphic, Category.Shape})
@PermittedContent(categories={Category.Animation, Category.Descriptive})
public final class Rect
extends ShapeNode {
    public static final String TAG = "rect";

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    @NotNull
    protected SVGShape buildShape(@NotNull AttributeNode node) {
        boolean isNonRounded;
        LengthValue x = node.getLength("x", PercentageDimension.WIDTH, Length.ZERO, Inherited.NO, Animatable.YES);
        LengthValue y = node.getLength("y", PercentageDimension.HEIGHT, Length.ZERO, Inherited.NO, Animatable.YES);
        LengthValue width = node.getLength("width", PercentageDimension.WIDTH, Length.ZERO, Inherited.NO, Animatable.YES);
        LengthValue height = node.getLength("height", PercentageDimension.HEIGHT, Length.ZERO, Inherited.NO, Animatable.YES);
        AttributeUtil.AxisPair radius = AttributeUtil.parseAxisPair(node, "rx", "ry", Length.ZERO, Inherited.NO, v -> {
            if (!v.isConstantlyNonNegative()) {
                return null;
            }
            return v;
        });
        LengthValue rx = radius.xAxis();
        LengthValue ry = radius.yAxis();
        boolean bl = isNonRounded = rx.isConstantlyZero() && ry.isConstantlyZero();
        if (isNonRounded) {
            return new SVGRectangle(x, y, width, height);
        }
        return new SVGRoundRectangle(x, y, width, height, rx, ry);
    }
}

