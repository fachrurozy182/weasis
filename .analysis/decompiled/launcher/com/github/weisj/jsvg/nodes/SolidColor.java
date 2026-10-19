/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.geometry.size.Percentage;
import com.github.weisj.jsvg.nodes.AbstractSVGNode;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.paint.SimplePaintSVGPaint;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.util.ColorUtil;
import java.awt.Color;
import java.awt.Paint;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={Category.Gradient})
@PermittedContent(categories={Category.Animation, Category.Descriptive})
public final class SolidColor
extends AbstractSVGNode
implements SimplePaintSVGPaint {
    public static final String TAG = "solidcolor";
    private Color color;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    @NotNull
    public Paint paint() {
        return this.color;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        Color c = attributeNode.getColor("solid-color");
        float opacity = attributeNode.getPercentage("solid-opacity", new Percentage((float)c.getAlpha() / 255.0f)).value();
        this.color = ColorUtil.withAlpha(c, opacity);
    }
}

