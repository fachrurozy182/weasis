/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.attributes.MarkerOrientation;
import com.github.weisj.jsvg.attributes.MarkerUnitType;
import com.github.weisj.jsvg.attributes.Overflow;
import com.github.weisj.jsvg.attributes.value.LengthValue;
import com.github.weisj.jsvg.attributes.value.PercentageDimension;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.nodes.Anchor;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Image;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.Pattern;
import com.github.weisj.jsvg.nodes.Style;
import com.github.weisj.jsvg.nodes.View;
import com.github.weisj.jsvg.nodes.container.BaseInnerViewContainer;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.nodes.text.Text;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.renderer.MeasureContext;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.impl.context.RenderContextAccessor;
import com.github.weisj.jsvg.view.FloatSize;
import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={Category.Container})
@PermittedContent(categories={Category.Animation, Category.Descriptive, Category.Shape, Category.Structural, Category.Gradient}, anyOf={Anchor.class, ClipPath.class, Filter.class, Image.class, Mask.class, Marker.class, Pattern.class, Style.class, Text.class, View.class})
public final class Marker
extends BaseInnerViewContainer {
    public static final String TAG = "marker";
    private Length refX;
    private Length refY;
    private MarkerOrientation orientation;
    private MarkerUnitType markerUnits;
    private Length markerHeight;
    private Length markerWidth;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @NotNull
    public MarkerOrientation orientation() {
        return this.orientation;
    }

    @Override
    @NotNull
    protected Point2D outerLocation(@NotNull MeasureContext context) {
        return new Point2D.Float(0.0f, 0.0f);
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
    @NotNull
    public FloatSize size(@NotNull RenderContext context) {
        MeasureContext measure = context.measureContext();
        if (this.markerUnits == MarkerUnitType.StrokeWidth) {
            LengthValue strokeWidthLength = RenderContextAccessor.instance().strokeContext((RenderContext)context).strokeWidth;
            assert (strokeWidthLength != null);
            float strokeWidth = strokeWidthLength.resolve(measure);
            return new FloatSize(this.markerWidth.raw() * strokeWidth, this.markerHeight.raw() * strokeWidth);
        }
        return new FloatSize(this.markerWidth.resolve(measure), this.markerHeight.resolve(measure));
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.refX = attributeNode.getHorizontalReferenceLengthFromKey("refX");
        this.refY = attributeNode.getVerticalReferenceLengthFromKey("refY");
        this.orientation = MarkerOrientation.parse(attributeNode.getValue("orient"), attributeNode.parser());
        this.markerUnits = attributeNode.getEnum("markerUnits", MarkerUnitType.StrokeWidth);
        this.markerWidth = attributeNode.getLength("markerWidth", PercentageDimension.WIDTH, 3.0f);
        this.markerHeight = attributeNode.getLength("markerHeight", PercentageDimension.HEIGHT, 3.0f);
    }

    @Override
    public boolean requiresInstantiation() {
        return true;
    }

    @Override
    protected boolean inheritAttributes() {
        return false;
    }
}

