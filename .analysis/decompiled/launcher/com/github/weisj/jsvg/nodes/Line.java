/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.attributes.value.PercentageDimension;
import com.github.weisj.jsvg.geometry.SVGLine;
import com.github.weisj.jsvg.geometry.SVGShape;
import com.github.weisj.jsvg.nodes.ShapeNode;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={Category.BasicShape, Category.Graphic, Category.Shape})
@PermittedContent(categories={Category.Animation, Category.Descriptive})
public final class Line
extends ShapeNode {
    public static final String TAG = "line";

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    @NotNull
    protected SVGShape buildShape(@NotNull AttributeNode attributeNode) {
        return new SVGLine(attributeNode.getLength("x1", PercentageDimension.WIDTH, 0.0f), attributeNode.getLength("y1", PercentageDimension.HEIGHT, 0.0f), attributeNode.getLength("x2", PercentageDimension.WIDTH, 0.0f), attributeNode.getLength("y2", PercentageDimension.HEIGHT, 0.0f));
    }
}

