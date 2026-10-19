/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.text;

import com.github.weisj.jsvg.attributes.font.SVGFont;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.nodes.prototype.Renderable;
import com.github.weisj.jsvg.nodes.text.CursorContext;
import com.github.weisj.jsvg.nodes.text.GlyphAdvancement;
import com.github.weisj.jsvg.nodes.text.GlyphCursor;
import com.github.weisj.jsvg.nodes.text.GlyphRenderer;
import com.github.weisj.jsvg.nodes.text.MutableGlyphRun;
import com.github.weisj.jsvg.nodes.text.NullTextOutput;
import com.github.weisj.jsvg.nodes.text.StringTextSegment;
import com.github.weisj.jsvg.nodes.text.TextContainer;
import com.github.weisj.jsvg.nodes.text.TextLayoutGroup;
import com.github.weisj.jsvg.nodes.text.TextMetrics;
import com.github.weisj.jsvg.nodes.text.TextSegment;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.impl.NodeRenderer;
import com.github.weisj.jsvg.renderer.impl.context.RenderContextAccessor;
import com.github.weisj.jsvg.renderer.output.Output;
import com.github.weisj.jsvg.renderer.output.TextOutput;
import java.util.List;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;

class LayoutGroupSegment<E, T extends TextContainer<E>>
implements TextSegment.RenderableSegment {
    @NotNull
    private final T parent;
    @NotNull
    private final TextLayoutGroup group;

    LayoutGroupSegment(@NotNull T parent, @NotNull TextLayoutGroup group) {
        this.parent = parent;
        this.group = group;
    }

    @Override
    public boolean isSegmentVisible(@NotNull RenderContext currentContext) {
        return ((TextContainer)this.parent).isVisible(currentContext);
    }

    @Override
    public void prepareSegmentForRendering(@NotNull GlyphCursor cursor, @NotNull RenderContext context, @NotNull TextOutput textOutput) {
        SVGFont font = RenderContextAccessor.instance().font(context);
        GlyphCursor localCursor = ((CursorContext)this.parent).createLocalCursor(false, cursor);
        localCursor.setAdvancement(this.localGlyphAdvancement(context, cursor));
        LayoutGroupSegment.forEachSegment(this.group.segments(), context, (seg, ctx) -> GlyphRenderer.prepareGlyphRun(seg, localCursor, font, ctx, textOutput), (seg, ctx) -> seg.prepareSegmentForRendering(localCursor, (RenderContext)ctx, textOutput));
        ((CursorContext)this.parent).cleanUpLocalCursor(cursor, localCursor);
    }

    @NotNull
    private GlyphAdvancement localGlyphAdvancement(@NotNull RenderContext context, @NotNull GlyphCursor cursor) {
        Length length = this.group.fixedLength();
        if (length != null) {
            return new GlyphAdvancement(this.computeTextMetrics(context, TextSegment.RenderableSegment.UseTextLengthForCalculation.NO), length.resolve(context.measureContext()), ((TextContainer)this.parent).lengthAdjust);
        }
        return cursor.advancement();
    }

    @Override
    public void renderSegmentWithoutLayout(@NotNull GlyphCursor cursor, @NotNull RenderContext context, @NotNull Output output) {
        LayoutGroupSegment.forEachSegment(this.group.segments(), context, (seg, ctx) -> {
            if (this.parent instanceof Renderable && !((Renderable)this.parent).isVisible((RenderContext)ctx)) {
                return;
            }
            GlyphRenderer.renderGlyphRun(output, RenderContextAccessor.instance().paintOrder(context), ((TextContainer)this.parent).vectorEffects(), seg);
        }, (seg, ctx) -> seg.renderSegmentWithoutLayout(cursor, (RenderContext)ctx, output));
    }

    @Override
    @NotNull
    public TextMetrics computeTextMetrics(@NotNull RenderContext context, @NotNull TextSegment.RenderableSegment.UseTextLengthForCalculation flag) {
        return TextMetrics.computeTextMetrics(this.group, context, flag);
    }

    @Override
    public void appendTextShape(@NotNull GlyphCursor cursor, @NotNull MutableGlyphRun glyphRun, @NotNull RenderContext context) {
        SVGFont font = RenderContextAccessor.instance().font(context);
        GlyphCursor localCursor = ((CursorContext)this.parent).createLocalCursor(false, cursor);
        localCursor.setAdvancement(this.localGlyphAdvancement(context, cursor));
        LayoutGroupSegment.forEachSegment(this.group.segments(), context, (seg, ctx) -> glyphRun.append(GlyphRenderer.layoutGlyphRun(seg, localCursor, font, ctx, NullTextOutput.INSTANCE)), (seg, ctx) -> seg.appendTextShape(localCursor, glyphRun, (RenderContext)ctx));
        ((CursorContext)this.parent).cleanUpLocalCursor(cursor, localCursor);
    }

    static void forEachSegment(@NotNull @NotNull List<? extends @NotNull TextSegment> segments, @NotNull RenderContext context, @NotNull BiConsumer<StringTextSegment, RenderContext> onStringTextSegment, @NotNull BiConsumer<TextSegment.RenderableSegment, RenderContext> onRenderableSegment) {
        for (TextSegment textSegment : segments) {
            RenderContext currentContext = context;
            if (textSegment instanceof TextContainer) {
                currentContext = NodeRenderer.setupRenderContext(textSegment, context);
            }
            if (textSegment instanceof StringTextSegment) {
                onStringTextSegment.accept((StringTextSegment)textSegment, currentContext);
                continue;
            }
            if (textSegment instanceof TextSegment.RenderableSegment) {
                onRenderableSegment.accept((TextSegment.RenderableSegment)textSegment, currentContext);
                continue;
            }
            throw new IllegalStateException("Unexpected segment " + textSegment);
        }
    }
}

