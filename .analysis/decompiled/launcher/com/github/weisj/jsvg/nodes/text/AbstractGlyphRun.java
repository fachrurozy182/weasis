/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.text;

import com.github.weisj.jsvg.attributes.font.SVGFont;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.nodes.text.EmojiGlyph;
import com.github.weisj.jsvg.renderer.output.Output;
import com.github.weisj.jsvg.renderer.output.impl.Graphics2DOutput;
import com.github.weisj.jsvg.util.ImageUtil;
import com.github.weisj.jsvg.util.SystemUtil;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.jetbrains.annotations.NotNull;

class AbstractGlyphRun<T extends Shape> {
    @NotNull
    private final T shape;
    @NotNull
    private final Metrics metrics;
    @NotNull
    private final @NotNull List<@NotNull PaintableEmoji> emojis;

    public AbstractGlyphRun(@NotNull T shape, @NotNull Metrics metrics, @NotNull @NotNull List<@NotNull PaintableEmoji> emojis) {
        this.shape = shape;
        this.metrics = metrics;
        this.emojis = emojis;
    }

    @NotNull
    public T shape() {
        return this.shape;
    }

    @NotNull
    public Metrics metrics() {
        return this.metrics;
    }

    @NotNull
    public @NotNull List<@NotNull PaintableEmoji> emojis() {
        return this.emojis;
    }

    static class Metrics {
        @NotNull
        final Rectangle2D paintBounds;
        @NotNull
        final Rectangle2D layoutBounds;

        Metrics(@NotNull Rectangle2D paintBounds, @NotNull Rectangle2D layoutBounds) {
            this.paintBounds = paintBounds;
            this.layoutBounds = layoutBounds;
        }

        @NotNull
        static Metrics createDefault() {
            return new Metrics(new Rectangle2D.Float(Float.NaN, Float.NaN, 0.0f, 0.0f), new Rectangle2D.Float(Float.NaN, Float.NaN, 0.0f, 0.0f));
        }

        void union(@NotNull Metrics metrics) {
            if (Length.isUnspecified((float)this.paintBounds.getX())) {
                this.paintBounds.setRect(metrics.paintBounds);
                this.layoutBounds.setRect(metrics.layoutBounds);
                return;
            }
            Rectangle2D.union(this.paintBounds, metrics.paintBounds, this.paintBounds);
            Rectangle2D.union(this.layoutBounds, metrics.layoutBounds, this.layoutBounds);
        }

        @NotNull
        Metrics copy() {
            return new Metrics(this.paintBounds, this.layoutBounds);
        }
    }

    public static class PaintableEmoji {
        @NotNull
        private final EmojiGlyph glyph;
        @NotNull
        private final AffineTransform transform;

        PaintableEmoji(@NotNull EmojiGlyph glyph, @NotNull AffineTransform transform) {
            this.glyph = glyph;
            this.transform = transform;
        }

        public void render(@NotNull Output output, @NotNull SVGFont font) {
            int maxFontSize;
            output.applyTransform(this.transform);
            int fontSize = font.size();
            int n = maxFontSize = SystemUtil.isMacOS ? 100 : Integer.MAX_VALUE;
            if (output.transform().getScaleY() * (double)fontSize > (double)maxFontSize) {
                float baselinePosition = 0.9f;
                if (this.glyph.largeBitmap == null) {
                    BufferedImage bitmap = ImageUtil.createCompatibleTransparentImage(maxFontSize, maxFontSize);
                    Graphics g = bitmap.getGraphics();
                    g.setFont(g.getFont().deriveFont((float)maxFontSize));
                    g.drawString(this.glyph.codepoint(), 0, (int)(baselinePosition * (float)maxFontSize));
                    g.dispose();
                    this.glyph.largeBitmap = bitmap;
                }
                output.scale((double)fontSize / (double)maxFontSize, (double)fontSize / (double)maxFontSize);
                output.translate(0.0, -((int)(baselinePosition * (float)maxFontSize)));
                output.drawImage(this.glyph.largeBitmap);
            } else if (output instanceof Graphics2DOutput) {
                Graphics2D g = ((Graphics2DOutput)output).graphics();
                g.setFont(g.getFont().deriveFont((float)fontSize));
                g.drawString(this.glyph.codepoint(), 0, 0);
            }
        }
    }
}

