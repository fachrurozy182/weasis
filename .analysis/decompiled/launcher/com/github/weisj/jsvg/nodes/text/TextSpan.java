/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.text;

import com.github.weisj.jsvg.nodes.Anchor;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.animation.Animate;
import com.github.weisj.jsvg.nodes.animation.AnimateTransform;
import com.github.weisj.jsvg.nodes.animation.Set;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.nodes.text.GlyphCursor;
import com.github.weisj.jsvg.nodes.text.LinearTextContainer;
import com.github.weisj.jsvg.nodes.text.LinearTextLayoutGroup;
import com.github.weisj.jsvg.nodes.text.MutableGlyphRun;
import com.github.weisj.jsvg.nodes.text.StringTextSegment;
import com.github.weisj.jsvg.nodes.text.TextMetrics;
import com.github.weisj.jsvg.nodes.text.TextSegment;
import com.github.weisj.jsvg.parser.TextContent;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.output.Output;
import com.github.weisj.jsvg.renderer.output.TextOutput;
import com.github.weisj.jsvg.util.AttributeUtil;
import java.awt.Shape;
import java.awt.geom.Path2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ElementCategories(value={Category.TextContent, Category.TextContentChild})
@PermittedContent(categories={Category.Descriptive}, anyOf={Anchor.class, TextSpan.class, Animate.class, AnimateTransform.class, Set.class}, charData=true)
public final class TextSpan
extends LinearTextContainer<TextSegment>
implements TextSegment.RenderableSegment {
    public static final String TAG = "tspan";
    @NotNull
    private final LinearTextLayoutGroup layoutGroup;

    public TextSpan() {
        this.layoutGroup = new LinearTextLayoutGroup(this, this.children);
    }

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    protected void doAdd(@NotNull SVGNode node) {
        this.children.add((TextSegment)((Object)node));
    }

    @Override
    public void addContent(@NotNull TextContent.Segment content) {
        if (content.isConstant() && content.text().isEmpty()) {
            return;
        }
        if (this.children.isEmpty() && content.isConstant() && AttributeUtil.isBlank(content.text())) {
            return;
        }
        this.children.add(new StringTextSegment(this, this.layoutGroup, this.children.size(), content));
    }

    @Override
    protected boolean acceptChild(@Nullable String id, @NotNull SVGNode node) {
        return node instanceof TextSegment;
    }

    @Override
    public void prepareSegmentForRendering(@NotNull GlyphCursor cursor, @NotNull RenderContext context, @NotNull TextOutput textOutput) {
        this.layoutGroup.asSegment().prepareSegmentForRendering(cursor, context, textOutput);
    }

    @Override
    public void renderSegmentWithoutLayout(@NotNull GlyphCursor cursor, @NotNull RenderContext context, @NotNull Output output) {
        this.layoutGroup.asSegment().renderSegmentWithoutLayout(cursor, context, output);
    }

    @Override
    @NotNull
    public TextMetrics computeTextMetrics(@NotNull RenderContext context, @NotNull TextSegment.RenderableSegment.UseTextLengthForCalculation flag) {
        return this.layoutGroup.asSegment().computeTextMetrics(context, flag);
    }

    @Override
    public void appendTextShape(@NotNull GlyphCursor cursor, @NotNull MutableGlyphRun glyphRun, @NotNull RenderContext context) {
        this.layoutGroup.asSegment().appendTextShape(cursor, glyphRun, context);
    }

    @Override
    @NotNull
    Shape glyphShape(@NotNull RenderContext context) {
        Path2D.Float shape = new Path2D.Float();
        this.layoutGroup.appendGlyphShape(null, context, shape);
        return shape;
    }

    @Override
    public boolean isSegmentVisible(@NotNull RenderContext currentContext) {
        return this.layoutGroup.asSegment().isSegmentVisible(currentContext);
    }
}

