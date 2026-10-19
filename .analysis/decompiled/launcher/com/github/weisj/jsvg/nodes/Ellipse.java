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
import com.github.weisj.jsvg.geometry.SVGEllipse;
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
public final class Ellipse
extends ShapeNode {
    public static final String TAG = "ellipse";

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    @NotNull
    protected SVGShape buildShape(@NotNull AttributeNode node) {
        AttributeUtil.AxisPair radius = AttributeUtil.parseAxisPair(node, "rx", "ry", Length.ZERO, Inherited.NO, v -> {
            if (!v.isConstantlyNonNegative()) {
                return null;
            }
            return v;
        });
        LengthValue rx = radius.xAxis();
        LengthValue ry = radius.yAxis();
        return new SVGEllipse(node.getLength("cx", PercentageDimension.WIDTH, Length.ZERO, Inherited.NO, Animatable.YES), node.getLength("cy", PercentageDimension.HEIGHT, Length.ZERO, Inherited.NO, Animatable.YES), rx, ry);
    }
}

