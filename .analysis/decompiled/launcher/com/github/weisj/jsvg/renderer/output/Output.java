/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.renderer.output;

import com.github.weisj.jsvg.nodes.text.NullTextOutput;
import com.github.weisj.jsvg.renderer.SVGRenderingHints;
import com.github.weisj.jsvg.renderer.output.TextOutput;
import com.github.weisj.jsvg.renderer.output.impl.Graphics2DOutput;
import com.github.weisj.jsvg.renderer.output.impl.RenderingHintsUtil;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Output {
    @NotNull
    public static Output createForGraphics(@NotNull Graphics2D g) {
        Graphics2D g2 = (Graphics2D)g.create();
        RenderingHintsUtil.setupSVGRenderingHints(g2);
        return new Graphics2DOutput(g2);
    }

    public void fillShape(@NotNull Shape var1);

    public void drawShape(@NotNull Shape var1);

    public void drawImage(@NotNull BufferedImage var1);

    public void drawImage(@NotNull Image var1, @Nullable ImageObserver var2);

    public void drawImage(@NotNull Image var1, @NotNull AffineTransform var2, @Nullable ImageObserver var3);

    public void setPaint(@NotNull Paint var1);

    public void setPaint(@NotNull Supplier<Paint> var1);

    public void setStroke(@NotNull Stroke var1);

    @NotNull
    public Stroke stroke();

    public void applyClip(@NotNull Shape var1);

    @Deprecated
    default public void setClip(@Nullable Shape shape) {
    }

    public Optional<Float> contextFontSize();

    @NotNull
    public Output createChild();

    public void dispose();

    public void debugPaint(@NotNull Consumer<Graphics2D> var1);

    @NotNull
    public Rectangle2D clipBounds();

    @Nullable
    public RenderingHints renderingHints();

    @Nullable
    public Object renderingHint(@NotNull RenderingHints.Key var1);

    public void setRenderingHint(@NotNull RenderingHints.Key var1, @Nullable Object var2);

    @NotNull
    public AffineTransform transform();

    public void setTransform(@NotNull AffineTransform var1);

    public void applyTransform(@NotNull AffineTransform var1);

    public void rotate(double var1);

    public void scale(double var1, double var3);

    public void translate(double var1, double var3);

    public float currentOpacity();

    public void applyOpacity(float var1);

    @NotNull
    public SafeState safeState();

    @NotNull
    default public TextOutput textOutput() {
        return NullTextOutput.INSTANCE;
    }

    public boolean supportsFilters();

    public boolean supportsColors();

    default public boolean isSoftClippingEnabled() {
        return this.renderingHint(SVGRenderingHints.KEY_SOFT_CLIPPING) == SVGRenderingHints.VALUE_SOFT_CLIPPING_ON;
    }

    default public boolean hasMaskedPaint() {
        return false;
    }

    public static interface SafeState {
        public void restore();
    }
}

