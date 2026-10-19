/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.filter;

import com.github.weisj.jsvg.attributes.filter.LayoutBounds;
import com.github.weisj.jsvg.geometry.noise.PerlinTurbulence;
import com.github.weisj.jsvg.geometry.size.FloatInsets;
import com.github.weisj.jsvg.nodes.animation.Animate;
import com.github.weisj.jsvg.nodes.animation.Set;
import com.github.weisj.jsvg.nodes.filter.AbstractFilterPrimitive;
import com.github.weisj.jsvg.nodes.filter.Channel;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.filter.FilterContext;
import com.github.weisj.jsvg.nodes.filter.FilterLayoutContext;
import com.github.weisj.jsvg.nodes.filter.ImageProducerChannel;
import com.github.weisj.jsvg.nodes.filter.PixelProvider;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.parser.impl.AttributeNode;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.util.ImageUtil;
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.WritableRaster;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={Category.FilterPrimitive})
@PermittedContent(anyOf={Animate.class, Set.class})
public final class FeTurbulence
extends AbstractFilterPrimitive {
    public static final String TAG = "feturbulence";
    private float seed;
    private float[] baseFrequency;
    private int numOctaves;
    private Type type;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.seed = attributeNode.getFloat("seed", 0.0f);
        this.baseFrequency = attributeNode.getFloatList("baseFrequency");
        if (this.baseFrequency.length == 0) {
            this.baseFrequency = new float[]{0.0f};
        }
        this.numOctaves = attributeNode.getInt("numOctaves", 1);
        this.numOctaves = Math.min(this.numOctaves, 8);
        this.type = attributeNode.getEnum("type", Type.fractalNoise);
    }

    @Override
    public void layoutFilter(@NotNull RenderContext context, @NotNull FilterLayoutContext filterLayoutContext) {
        this.impl().saveLayoutResult(new LayoutBounds(filterLayoutContext.filterPrimitiveRegion(context.measureContext(), this), new FloatInsets()), filterLayoutContext);
    }

    @Override
    public void applyFilter(@NotNull RenderContext context, @NotNull FilterContext filterContext) {
        Filter.FilterInfo info = filterContext.info();
        TurbulenceChannel turbulenceChannel = new TurbulenceChannel(info.imageBounds(), info.imageWidth, info.imageHeight, this.seed, this.numOctaves, this.baseFrequency[0], this.baseFrequency.length > 1 ? (double)this.baseFrequency[1] : (double)this.baseFrequency[0], this.type);
        this.impl().saveResult(turbulenceChannel, filterContext);
    }

    public static enum Type {
        fractalNoise,
        Turbulence;

    }

    public static final class TurbulenceChannel
    implements Channel,
    PixelProvider {
        private final PerlinTurbulence perlinTurbulence;
        private final double[] channels = new double[4];
        private final int imageWidth;
        private final int imageHeight;
        private final Type type;
        private final Rectangle2D tileBounds;
        private BufferedImage bufferedImage;

        public TurbulenceChannel(@NotNull Rectangle2D tileBounds, int imageWidth, int imageHeight, float seed, int octaves, double xFrequency, double yFrequency, Type type) {
            this.tileBounds = tileBounds;
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            this.type = type;
            this.perlinTurbulence = new PerlinTurbulence((int)seed, octaves, xFrequency, yFrequency);
        }

        @NotNull
        private BufferedImage ensureImageBackingStore() {
            if (this.bufferedImage == null) {
                ColorSpace cs = ColorSpace.getInstance(1004);
                DirectColorModel cm = new DirectColorModel(cs, 32, 0xFF0000, 65280, 255, -16777216, false, 3);
                WritableRaster dest = ((ColorModel)cm).createCompatibleWritableRaster(this.imageWidth, this.imageHeight);
                this.bufferedImage = new BufferedImage(cm, dest, false, null);
                int w = dest.getWidth();
                int h = dest.getHeight();
                double scaleX = this.tileBounds.getWidth() / (double)w;
                double scaleY = this.tileBounds.getHeight() / (double)h;
                double startX = this.tileBounds.getX();
                double startY = this.tileBounds.getY();
                boolean fractalNoise = this.type == Type.fractalNoise;
                int[] destPixels = ImageUtil.getINT_RGBA_DataBank(dest);
                int dstAdjust = ImageUtil.getINT_RGBA_DataAdjust(dest);
                int dp = ImageUtil.getINT_RGBA_DataOffset(dest);
                double point1 = startY;
                for (int i = 0; i < h; ++i) {
                    double point0 = startX;
                    int end = dp + w;
                    while (dp < end) {
                        this.perlinTurbulence.turbulence(this.channels, point0, point1, fractalNoise, null, null);
                        destPixels[dp] = ((ColorModel)cm).getRGB(TurbulenceChannel.channelsToRGB(this.channels));
                        point0 += scaleX;
                        ++dp;
                    }
                    point1 += scaleY;
                    dp += dstAdjust;
                }
            }
            return this.bufferedImage;
        }

        @Override
        @NotNull
        public ImageProducer producer() {
            return this.ensureImageBackingStore().getSource();
        }

        @Override
        @NotNull
        public BufferedImage toBufferedImageNonAliased(@NotNull RenderContext context) {
            BufferedImage img = this.ensureImageBackingStore();
            ColorModel cm = img.getColorModel();
            WritableRaster raster = img.copyData(null);
            return new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
        }

        @Override
        @NotNull
        public Channel applyFilter(@NotNull ImageFilter filter) {
            return new ImageProducerChannel(new FilteredImageSource(this.producer(), filter));
        }

        @Override
        @NotNull
        public PixelProvider pixels(@NotNull RenderContext context) {
            return this;
        }

        @Override
        public int pixelAt(double x, double y) {
            this.perlinTurbulence.turbulence(this.channels, x, y, this.type == Type.fractalNoise, null, null);
            return TurbulenceChannel.channelsToRGB(this.channels);
        }

        private static int channelsToRGB(double[] channels) {
            int i = (int)channels[0];
            int j = (i & 0xFFFFFF00) == 0 ? i << 16 : ((i & Integer.MIN_VALUE) != 0 ? 0 : 0xFF0000);
            i = (int)channels[1];
            j = (i & 0xFFFFFF00) == 0 ? (j |= i << 8) : (j |= (i & Integer.MIN_VALUE) != 0 ? 0 : 65280);
            i = (int)channels[2];
            j = (i & 0xFFFFFF00) == 0 ? (j |= i) : (j |= (i & Integer.MIN_VALUE) != 0 ? 0 : 255);
            i = (int)channels[3];
            j = (i & 0xFFFFFF00) == 0 ? (j |= i << 24) : (j |= (i & Integer.MIN_VALUE) != 0 ? 0 : -16777216);
            return j;
        }
    }
}

