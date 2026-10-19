/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.renderer.impl;

import com.github.weisj.jsvg.attributes.font.MeasurableFontSpec;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.SVG;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.container.BaseInnerViewContainer;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.prototype.HasClip;
import com.github.weisj.jsvg.nodes.prototype.HasFilter;
import com.github.weisj.jsvg.nodes.prototype.HasFontContext;
import com.github.weisj.jsvg.nodes.prototype.HasFontRenderContext;
import com.github.weisj.jsvg.nodes.prototype.HasPaintContext;
import com.github.weisj.jsvg.nodes.prototype.Instantiator;
import com.github.weisj.jsvg.nodes.prototype.Mutator;
import com.github.weisj.jsvg.nodes.prototype.Renderable;
import com.github.weisj.jsvg.nodes.prototype.Transformable;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.SVGRenderingHints;
import com.github.weisj.jsvg.renderer.impl.ElementBounds;
import com.github.weisj.jsvg.renderer.impl.Info;
import com.github.weisj.jsvg.renderer.impl.IsolationEffects;
import com.github.weisj.jsvg.renderer.impl.context.ContextElementAttributes;
import com.github.weisj.jsvg.renderer.impl.context.FontRenderContext;
import com.github.weisj.jsvg.renderer.impl.context.PaintContext;
import com.github.weisj.jsvg.renderer.impl.context.RenderContextAccessor;
import com.github.weisj.jsvg.renderer.output.Output;
import com.github.weisj.jsvg.view.FloatSize;
import com.github.weisj.jsvg.view.ViewBox;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NodeRenderer {
    private NodeRenderer() {
    }

    public static void renderRootSVG(@NotNull SVG svgRoot, @NotNull RenderContext context, @NotNull Output output) {
        RenderContext viewContext = svgRoot.createInnerContextForViewBox(svgRoot.size(context), svgRoot.viewBox(context), context, output);
        try (Info info = NodeRenderer.createRenderInfo(svgRoot, viewContext, output, null);){
            if (info != null) {
                ((SVG)info.renderable()).renderWithEstablishedViewBox(info.context(), info.output());
            }
        }
    }

    public static void renderNode(@NotNull SVGNode node, @NotNull RenderContext context, @NotNull Output output) {
        NodeRenderer.renderNode(node, context, output, null);
    }

    public static void renderNode(@NotNull SVGNode node, @NotNull RenderContext context, @NotNull Output output, @Nullable Instantiator instantiator) {
        try (Info info = NodeRenderer.createRenderInfo(node, context, output, instantiator);){
            if (info != null) {
                info.renderable().render(info.context(), info.output());
            }
        }
    }

    public static void renderWithSize(@NotNull BaseInnerViewContainer node, @NotNull FloatSize size, @NotNull RenderContext context, @NotNull Output output, @Nullable Instantiator instantiator) {
        try (Info info = NodeRenderer.createRenderInfo(node, context, output, instantiator);){
            if (info != null) {
                node.renderWithSize(size, node.viewBox(info.context()), info.context(), info.output());
            }
        }
    }

    @NotNull
    public static RenderContext createChildContext(@NotNull Renderable node, @NotNull RenderContext context, @Nullable Instantiator instantiator) {
        return NodeRenderer.setupRenderContext(instantiator, node, context);
    }

    @Nullable
    private static Info createRenderInfo(@NotNull SVGNode node, @NotNull RenderContext context, @NotNull Output output, @Nullable Instantiator instantiator) {
        Info.InfoWithIsolation info;
        if (!(node instanceof Renderable)) {
            return null;
        }
        Renderable renderable = (Renderable)((Object)node);
        if (!NodeRenderer.checkInstantiation(node, instantiator, renderable)) {
            return null;
        }
        if (!renderable.isVisible(context)) {
            return null;
        }
        RenderContext childContext = NodeRenderer.createChildContext(renderable, context, instantiator);
        Output childOutput = output.createChild();
        ElementBounds elementBounds = new ElementBounds(node, childContext);
        NodeRenderer.applyTransform(renderable, childOutput, childContext, elementBounds);
        Mask maskForIsolation = null;
        ClipPath clipPathForIsolation = null;
        if (renderable instanceof HasClip) {
            maskForIsolation = NodeRenderer.setupMask((HasClip)((Object)renderable), elementBounds, childOutput, childContext);
            ClipPath clipPath = NodeRenderer.setupClip((HasClip)((Object)renderable), elementBounds, childContext, childOutput);
            if (clipPath != null && !clipPath.isValid()) {
                return null;
            }
            if (NodeRenderer.useAccurateMasking(childOutput)) {
                clipPathForIsolation = clipPath;
            }
        }
        Filter filter = null;
        if (renderable instanceof HasFilter) {
            filter = NodeRenderer.setupFilter((HasFilter)((Object)renderable), childOutput);
        }
        if ((info = Info.InfoWithIsolation.create(renderable, childContext, childOutput, elementBounds, new IsolationEffects(filter, maskForIsolation, clipPathForIsolation))) != null) {
            return info;
        }
        return new Info(renderable, childContext, childOutput);
    }

    private static void applyTransform(@NotNull Renderable renderable, @NotNull Output childOutput, @NotNull RenderContext childContext, @NotNull ElementBounds elementBounds) {
        if (renderable instanceof Transformable && ((Transformable)((Object)renderable)).shouldTransform()) {
            ((Transformable)((Object)renderable)).applyTransform(childOutput, childContext, elementBounds);
        }
    }

    private static boolean checkInstantiation(@NotNull SVGNode node, @Nullable Instantiator instantiator, @NotNull Renderable renderable) {
        boolean instantiated = renderable.requiresInstantiation();
        return !instantiated || instantiator != null && instantiator.canInstantiate(node);
    }

    @Nullable
    private static ClipPath setupClip(@NotNull HasClip renderable, @NotNull ElementBounds elementBounds, @NotNull RenderContext childContext, @NotNull Output childOutput) {
        ClipPath childClip = renderable.clipPath();
        if (childClip == null) {
            return null;
        }
        if (!childClip.isValid()) {
            return childClip;
        }
        childClip.applyClip(childOutput, childContext, elementBounds);
        return childClip;
    }

    @Nullable
    private static Mask setupMask(HasClip renderable, ElementBounds elementBounds, Output childOutput, RenderContext childContext) {
        Mask mask = renderable.mask();
        if (mask == null) {
            return null;
        }
        Rectangle2D bounds = elementBounds.geometryBox();
        if (bounds.isEmpty()) {
            return null;
        }
        if (NodeRenderer.useAccurateMasking(childOutput)) {
            return mask;
        }
        childOutput.setPaint(() -> mask.createMaskPaint(childOutput, childContext, elementBounds));
        return null;
    }

    @Nullable
    private static Filter setupFilter(@NotNull HasFilter hasFilter, @NotNull Output childOutput) {
        Filter filter = hasFilter.filter();
        if (!(filter == null || filter.hasEffect() && childOutput.supportsFilters())) {
            filter = null;
        }
        return filter;
    }

    private static boolean useAccurateMasking(@NotNull Output output) {
        return output.renderingHint(SVGRenderingHints.KEY_MASK_CLIP_RENDERING) == SVGRenderingHints.VALUE_MASK_CLIP_RENDERING_ACCURACY;
    }

    @NotNull
    public static RenderContext setupRenderContext(@NotNull Object node, @NotNull RenderContext context) {
        return NodeRenderer.setupRenderContext(null, node, context);
    }

    @NotNull
    private static RenderContext setupRenderContext(@Nullable Instantiator instantiator, @NotNull Object node, @NotNull RenderContext context) {
        @Nullable Mutator<PaintContext> paintContext = null;
        Mutator<MeasurableFontSpec> fontSpec = null;
        FontRenderContext fontRenderContext = null;
        if (node instanceof HasPaintContext) {
            paintContext = ((HasPaintContext)node).paintContext();
        }
        if (node instanceof HasFontContext) {
            fontSpec = ((HasFontContext)node).fontSpec();
        }
        if (node instanceof HasFontRenderContext) {
            fontRenderContext = ((HasFontRenderContext)node).fontRenderContext();
        }
        ContextElementAttributes contextElementAttributes = null;
        if (instantiator != null) {
            contextElementAttributes = instantiator.createContextAttributes(context);
        }
        return RenderContextAccessor.instance().deriveForNode(context, paintContext, fontSpec, fontRenderContext, contextElementAttributes, node);
    }

    @NotNull
    public static RenderContext setupInnerViewRenderContext(@NotNull ViewBox viewBox, @NotNull RenderContext context, boolean inheritAttributes) {
        return RenderContextAccessor.instance().setupInnerViewRenderContext(viewBox, context, inheritAttributes);
    }
}

