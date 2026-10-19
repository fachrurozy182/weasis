/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.attributes.Overflow;
import com.github.weisj.jsvg.geometry.size.Length;
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
import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={Category.Container, Category.Structural})
@PermittedContent(categories={Category.Animation, Category.Descriptive, Category.Shape, Category.Structural, Category.Gradient}, anyOf={Anchor.class, ClipPath.class, Filter.class, Image.class, Mask.class, Marker.class, Pattern.class, Style.class, Text.class, View.class})
public final class Symbol
extends CommonInnerViewContainer {
    public static final String TAG = "symbol";
    private Length refX;
    private Length refY;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    @NotNull
    protected Point2D anchorLocation(@NotNull MeasureContext context) {
        return new Point2D.Float(-this.refX.resolve(context), -this.refY.resolve(context));
    }

    @Override
    @NotNull
    protected Overflow defaultOverflow() {
        return Overflow.Hidden;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.refX = attributeNode.getHorizontalReferenceLengthFromKey("refX");
        this.refY = attributeNode.getVerticalReferenceLengthFromKey("refY");
    }

    @Override
    public boolean requiresInstantiation() {
        return true;
    }
}

