/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.filter;

import com.github.weisj.jsvg.attributes.UnitType;
import com.github.weisj.jsvg.attributes.filter.ColorChannel;
import com.github.weisj.jsvg.attributes.filter.DefaultFilterChannel;
import com.github.weisj.jsvg.attributes.filter.FilterChannelKey;
import com.github.weisj.jsvg.attributes.filter.LayoutBounds;
import com.github.weisj.jsvg.geometry.size.FloatInsets;
import com.github.weisj.jsvg.nodes.animation.Animate;
import com.github.weisj.jsvg.nodes.animation.Set;
import com.github.weisj.jsvg.nodes.filter.AbstractFilterPrimitive;
import com.github.weisj.jsvg.nodes.filter.Channel;
import com.github.weisj.jsvg.nodes.filter.FilterContext;
import com.github.weisj.jsvg.nodes.filter.FilterLayoutContext;
import com.github.weisj.jsvg.nodes.filter.PixelProvider;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.util.ImageUtil;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageFilter;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={Category.FilterPrimitive})
@PermittedContent(anyOf={Animate.class, Set.class})
public final class FeDisplacementMap
extends AbstractFilterPrimitive {
    public static final String TAG = "fedisplacementmap";
    private ColorChannel xChannelSelector;
    private ColorChannel yChannelSelector;
    private float scale;
    private FilterChannelKey inputChannel2;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.xChannelSelector = attributeNode.getEnum("xChannelSelector", ColorChannel.A);
        this.yChannelSelector = attributeNode.getEnum("yChannelSelector", ColorChannel.A);
        this.scale = attributeNode.getFloat("scale", 0.0f);
        this.inputChannel2 = attributeNode.getFilterChannelKey("in2", DefaultFilterChannel.LastResult);
    }

    @Override
    public void layoutFilter(@NotNull RenderContext context, @NotNull FilterLayoutContext filterLayoutContext) {
        LayoutBounds layoutBounds = new LayoutBounds(filterLayoutContext.filterPrimitiveRegion(context.measureContext(), this), new FloatInsets());
        this.impl().saveLayoutResult(layoutBounds, filterLayoutContext);
    }

    @Override
    public void applyFilter(@NotNull RenderContext context, @NotNull FilterContext filterContext) {
        if (this.scale == 0.0f) {
            this.impl().noop(filterContext);
            return;
        }
        Channel input = this.impl().inputChannel(filterContext);
        Channel displacementInput = filterContext.getChannel(this.inputChannel2);
        double displacementScaleX = this.scale;
        double displacementScaleY = this.scale;
        if (filterContext.primitiveUnits() == UnitType.ObjectBoundingBox) {
            Rectangle2D elementBounds = filterContext.info().elementBounds();
            displacementScaleX *= elementBounds.getWidth();
            displacementScaleY *= elementBounds.getHeight();
        }
        BufferedImageFilter displacementFilter = new BufferedImageFilter(new DisplacementOp(displacementInput.pixels(context), filterContext.info().tile(), displacementScaleX, displacementScaleY));
        this.impl().saveResult(input.applyFilter(displacementFilter), filterContext);
    }

    private final class DisplacementOp
    implements BufferedImageOp {
        @NotNull
        private final PixelProvider displacementChannel;
        @NotNull
        private final Rectangle2D sourceBounds;
        private final double displacementScaleX;
        private final double displacementScaleY;

        public DisplacementOp(@NotNull PixelProvider displacementChannel, Rectangle2D sourceBounds, double displacementScaleX, double displacementScaleY) {
            this.displacementChannel = displacementChannel;
            this.sourceBounds = sourceBounds;
            this.displacementScaleX = displacementScaleX;
            this.displacementScaleY = displacementScaleY;
        }

        @Override
        public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
            if (dstCM == null) {
                dstCM = src.getColorModel();
            }
            return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
        }

        @Override
        public Rectangle2D getBounds2D(@NotNull BufferedImage src) {
            return new Rectangle(0, 0, src.getWidth(), src.getHeight());
        }

        @Override
        public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
            return (Point2D)srcPt.clone();
        }

        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dest) {
            if (src == null) {
                throw new NullPointerException("src image is null");
            }
            if (src == dest) {
                throw new IllegalArgumentException("src image cannot be the same as the dst image");
            }
            BufferedImage result = dest;
            if (result == null) {
                ColorSpace cs = ColorSpace.getInstance(1000);
                DirectColorModel cm = new DirectColorModel(cs, 32, 0xFF0000, 65280, 255, -16777216, false, 3);
                result = this.createCompatibleDestImage(src, cm);
            }
            WritableRaster raster = result.getRaster();
            int w = raster.getWidth();
            int h = raster.getHeight();
            double scaleX = this.sourceBounds.getWidth() / (double)w;
            double scaleY = this.sourceBounds.getHeight() / (double)h;
            double startX = this.sourceBounds.getX();
            double startY = this.sourceBounds.getY();
            WritableRaster sourceRaster = src.getRaster();
            Rectangle sourceRasterBounds = sourceRaster.getBounds();
            int[] destPixels = ImageUtil.getINT_RGBA_DataBank(raster);
            int dstAdjust = ImageUtil.getINT_RGBA_DataAdjust(raster);
            int dp = ImageUtil.getINT_RGBA_DataOffset(raster);
            double point1 = startY;
            int y = 0;
            for (int i = 0; i < h; ++i) {
                int x = 0;
                double point0 = startX;
                int end = dp + w;
                while (dp < end) {
                    double yDisplacement;
                    int yDest;
                    int displacementRGB = this.displacementChannel.pixelAt(point0, point1);
                    double xDisplacement = (double)FeDisplacementMap.this.xChannelSelector.value(displacementRGB) / 255.0 - 0.5;
                    int xDest = (int)((double)x + this.displacementScaleX * xDisplacement / scaleX);
                    destPixels[dp] = sourceRasterBounds.contains(xDest, yDest = (int)((double)y + this.displacementScaleY * (yDisplacement = (double)FeDisplacementMap.this.yChannelSelector.value(displacementRGB) / 255.0 - 0.5) / scaleY)) ? src.getRGB(xDest, yDest) : 0;
                    point0 += scaleX;
                    ++x;
                    ++dp;
                }
                point1 += scaleY;
                dp += dstAdjust;
                ++y;
            }
            return result;
        }

        @Override
        public RenderingHints getRenderingHints() {
            return null;
        }
    }
}

