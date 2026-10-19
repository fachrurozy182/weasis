/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.attributes.SpreadMethod;
import com.github.weisj.jsvg.attributes.UnitType;
import com.github.weisj.jsvg.attributes.value.TransformValue;
import com.github.weisj.jsvg.geometry.size.Percentage;
import com.github.weisj.jsvg.nodes.Stop;
import com.github.weisj.jsvg.nodes.container.ContainerNode;
import com.github.weisj.jsvg.paint.SVGPaint;
import com.github.weisj.jsvg.parser.PaintParser;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.renderer.MeasureContext;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.output.Output;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractGradient<Self extends AbstractGradient<Self>>
extends ContainerNode
implements SVGPaint {
    protected TransformValue gradientTransform;
    protected UnitType gradientUnits;
    protected SpreadMethod spreadMethod;
    @NotNull
    private Color[] colors;
    private Percentage[] offsets;
    private float[] tmpFractions;

    AbstractGradient() {
    }

    public final Percentage[] offsets() {
        return this.offsets;
    }

    @NotNull
    public final Color[] colors() {
        return this.colors;
    }

    @Override
    public final void build(@NotNull AttributeNode attributeNode) {
        List<Stop> stops;
        super.build(attributeNode);
        AbstractGradient<?> template = this.parseTemplate(attributeNode);
        this.gradientUnits = attributeNode.getEnum("gradientUnits", template != null ? template.gradientUnits : UnitType.ObjectBoundingBox);
        this.spreadMethod = attributeNode.getEnum("spreadMethod", template != null ? template.spreadMethod : SpreadMethod.Pad);
        this.gradientTransform = attributeNode.parseTransform("gradientTransform");
        if (this.gradientTransform == null && template != null) {
            this.gradientTransform = template.gradientTransform;
        }
        if ((stops = this.childrenOfType(Stop.class)).isEmpty() && template != null) {
            this.colors = template.colors();
            this.offsets = template.offsets();
        } else {
            this.parseStops(stops);
        }
        AbstractGradient<?> selfTemplate = this.getClass().isInstance(template) ? template : null;
        this.buildGradient(attributeNode, selfTemplate);
        this.children().clear();
    }

    private void parseStops(@NotNull List<Stop> stops) {
        stops.sort(Comparator.comparing(Stop::offset));
        ArrayList<Color> colorsList = new ArrayList<Color>();
        ArrayList<Percentage> offsetsList = new ArrayList<Percentage>();
        boolean realGradient = false;
        for (Stop stop : stops) {
            float stopOffset = Math.max(0.0f, Math.min(1.0f, stop.offset().value()));
            Color stopColor = stop.color();
            boolean isFirstStop = offsetsList.isEmpty();
            boolean effectiveStop = isFirstStop || AbstractGradient.isEffectiveStop(stopOffset, stopColor, offsetsList, colorsList);
            boolean bl = realGradient = !isFirstStop && effectiveStop;
            if (isFirstStop && stopOffset != 0.0f) {
                offsetsList.add(Percentage.ZERO);
                colorsList.add(stopColor);
            }
            if (!effectiveStop) continue;
            offsetsList.add(new Percentage(stopOffset));
            colorsList.add(stopColor);
        }
        if (!offsetsList.isEmpty() && ((Percentage)offsetsList.get(offsetsList.size() - 1)).value() != 1.0f) {
            offsetsList.add(Percentage.ONE);
            colorsList.add((Color)colorsList.get(colorsList.size() - 1));
        }
        if (!realGradient && !colorsList.isEmpty()) {
            this.colors = new Color[]{(Color)colorsList.get(0)};
            this.offsets = new Percentage[]{Percentage.ZERO};
        } else {
            this.colors = colorsList.toArray(new Color[0]);
            this.offsets = offsetsList.toArray(new Percentage[0]);
            AbstractGradient.makeStrictlyIncreasing(this.offsets);
        }
    }

    private static void makeStrictlyIncreasing(@NotNull @NotNull Percentage @NotNull [] offsets) {
        int i;
        for (i = 1; i < offsets.length; ++i) {
            if (!(offsets[i].value() <= offsets[i - 1].value())) continue;
            offsets[i] = new Percentage(Math.nextUp(offsets[i - 1].value()));
        }
        for (i = offsets.length - 1; i >= 0; --i) {
            if (!(offsets[i].value() >= 1.0f)) continue;
            offsets[i] = Percentage.ONE;
        }
        for (i = offsets.length - 2; i >= 0; --i) {
            if (!(offsets[i].value() >= offsets[i + 1].value())) continue;
            offsets[i] = new Percentage(Math.nextDown(offsets[i + 1].value()));
        }
    }

    private static boolean isEffectiveStop(float stopOffset, @NotNull Color stopColor, @NotNull @NotNull List<@NotNull Percentage> offsetsList, @NotNull @NotNull List<@NotNull Color> colorsList) {
        return stopOffset > offsetsList.get(offsetsList.size() - 1).value() || !stopColor.equals(colorsList.get(colorsList.size() - 1));
    }

    @Nullable
    private AbstractGradient<?> parseTemplate(@NotNull AttributeNode attributeNode) {
        AbstractGradient<Self> template = attributeNode.getElementByHref(AbstractGradient.class, attributeNode.getHref(), AttributeNode.ElementRelation.TEMPLATE);
        return template != this ? template : null;
    }

    protected abstract void buildGradient(@NotNull AttributeNode var1, @Nullable Self var2);

    @Override
    public void fillShape(@NotNull Output output, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
        Rectangle2D b = bounds != null ? bounds : shape.getBounds2D();
        output.setPaint(() -> this.paintForBounds(context.measureContext(), b));
        output.fillShape(shape);
    }

    @Override
    public void drawShape(@NotNull Output output, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
        Rectangle2D b = bounds != null ? bounds : shape.getBounds2D();
        output.setPaint(() -> this.paintForBounds(context.measureContext(), b));
        output.drawShape(shape);
    }

    @NotNull
    private Paint paintForBounds(@NotNull MeasureContext context, @NotNull Rectangle2D bounds) {
        Color[] gradColors = this.colors();
        if (gradColors.length == 0) {
            return PaintParser.DEFAULT_COLOR;
        }
        if (gradColors.length == 1) {
            return gradColors[0];
        }
        return this.gradientForBounds(this.gradientUnits.deriveMeasure(context), bounds, this.offsets(), gradColors);
    }

    protected float[] offsetsToFractions(Percentage[] gradOffsets) {
        if (this.tmpFractions == null || this.tmpFractions.length != gradOffsets.length) {
            this.tmpFractions = new float[gradOffsets.length];
            for (int i = 0; i < this.tmpFractions.length; ++i) {
                this.tmpFractions[i] = gradOffsets[i].value();
            }
        }
        return this.tmpFractions;
    }

    @NotNull
    protected abstract Paint gradientForBounds(@NotNull MeasureContext var1, @NotNull Rectangle2D var2, Percentage[] var3, @NotNull Color[] var4);

    @NotNull
    protected final AffineTransform computeViewTransform(@NotNull MeasureContext measure, @NotNull Rectangle2D bounds) {
        AffineTransform viewTransform = this.gradientUnits.viewTransform(bounds);
        if (this.gradientTransform != null) {
            viewTransform.concatenate(this.gradientTransform.get(measure));
        }
        return viewTransform;
    }
}

