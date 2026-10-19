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
import com.github.weisj.jsvg.nodes.prototype.HasGeometryContext;
import com.github.weisj.jsvg.nodes.prototype.Renderable;
import com.github.weisj.jsvg.nodes.prototype.impl.HasGeometryContextImpl;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.nodes.text.GlyphCursor;
import com.github.weisj.jsvg.nodes.text.LinearTextContainer;
import com.github.weisj.jsvg.nodes.text.LinearTextLayoutGroup;
import com.github.weisj.jsvg.nodes.text.StringTextSegment;
import com.github.weisj.jsvg.nodes.text.TextContainer;
import com.github.weisj.jsvg.nodes.text.TextLayoutGroup;
import com.github.weisj.jsvg.nodes.text.TextSegment;
import com.github.weisj.jsvg.parser.TextContent;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.impl.NodeRenderer;
import com.github.weisj.jsvg.renderer.output.Output;
import com.github.weisj.jsvg.util.AttributeUtil;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ElementCategories(value={Category.Graphic, Category.TextContent})
@PermittedContent(categories={Category.Animation, Category.Descriptive, Category.TextContentChild}, anyOf={Anchor.class}, charData=true)
public final class Text
extends LinearTextContainer<TextLayoutGroup>
implements HasGeometryContext.ByDelegate,
Renderable {
    public static final String TAG = "text";
    private HasGeometryContext geometryContext;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.geometryContext = HasGeometryContextImpl.parse(attributeNode);
    }

    @Override
    @NotNull
    public HasGeometryContext geometryContextDelegate() {
        return this.geometryContext;
    }

    @Override
    public boolean isVisible(@NotNull RenderContext context) {
        return this.isVisible;
    }

    @Override
    protected boolean acceptChild(@Nullable String id, @NotNull SVGNode node) {
        return node instanceof TextSegment || node instanceof TextLayoutGroup;
    }

    @Override
    protected void doAdd(@NotNull SVGNode node) {
        if (node instanceof TextSegment) {
            this.lastLinearLayoutGroup().segments().add((TextSegment)((Object)node));
        }
        if (node instanceof TextLayoutGroup) {
            this.children.add((TextLayoutGroup)((Object)node));
        }
    }

    @Override
    public void addContent(@NotNull TextContent.Segment content) {
        if (content.isConstant() && content.text().isEmpty()) {
            return;
        }
        if (this.children.isEmpty() && content.isConstant() && AttributeUtil.isBlank(content.text())) {
            return;
        }
        LinearTextLayoutGroup linearTextLayoutGroup = this.lastLinearLayoutGroup();
        linearTextLayoutGroup.segments().add(new StringTextSegment(this, linearTextLayoutGroup, linearTextLayoutGroup.segments().size(), content));
    }

    @NotNull
    private LinearTextLayoutGroup lastLinearLayoutGroup() {
        TextLayoutGroup lastGroup;
        TextLayoutGroup textLayoutGroup = lastGroup = this.children.isEmpty() ? null : (TextLayoutGroup)this.children.get(this.children.size() - 1);
        if (!(lastGroup instanceof LinearTextLayoutGroup)) {
            lastGroup = new LinearTextLayoutGroup(this);
            this.children.add(lastGroup);
        }
        return (LinearTextLayoutGroup)lastGroup;
    }

    @Override
    public void render(@NotNull RenderContext context, @NotNull Output output) {
        Point2D start = null;
        for (TextLayoutGroup layoutGroup : this.children()) {
            RenderContext currentContext = context;
            if (layoutGroup instanceof TextContainer) {
                currentContext = NodeRenderer.setupRenderContext(layoutGroup, context);
            }
            start = layoutGroup.renderText(start, currentContext, output);
        }
    }

    @Override
    @NotNull
    protected Shape glyphShape(@NotNull RenderContext context) {
        Path2D.Float shape = new Path2D.Float();
        Point2D start = null;
        for (TextLayoutGroup layoutGroup : this.children()) {
            start = layoutGroup.appendGlyphShape(start, context, shape);
        }
        return shape;
    }

    @Override
    @NotNull
    public GlyphCursor createLocalCursor(boolean isInitial, @NotNull GlyphCursor current) {
        if (!isInitial) {
            return current;
        }
        return super.createLocalCursor(isInitial, current);
    }
}

