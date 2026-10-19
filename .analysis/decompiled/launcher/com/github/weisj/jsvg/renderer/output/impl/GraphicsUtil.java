/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.renderer.output.impl;

import com.github.weisj.jsvg.logging.Logger;
import com.github.weisj.jsvg.logging.impl.LogFactory;
import com.github.weisj.jsvg.renderer.output.Output;
import com.github.weisj.jsvg.util.ImageUtil;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GraphicsUtil {
    private static final Logger LOGGER = LogFactory.createLogger(GraphicsUtil.class);

    private GraphicsUtil() {
    }

    public static void safelySetPaint(@NotNull Output output, @NotNull Graphics2D g, @NotNull Paint paint) {
        g.setPaint(GraphicsUtil.exchangePaint(output, g.getPaint(), paint, true));
    }

    public static void cleanupPaint(@NotNull Output output, @NotNull Paint paint) {
        if (paint instanceof WrappingPaint) {
            GraphicsUtil.cleanupPaint(output, ((WrappingPaint)((Object)paint)).paint());
        }
        if (paint instanceof DisposablePaint) {
            ((DisposablePaint)((Object)paint)).cleanupIfNeeded(output);
        }
    }

    public static void preparePaint(@NotNull Paint paint) {
        if (paint instanceof WrappingPaint) {
            GraphicsUtil.preparePaint(((WrappingPaint)((Object)paint)).paint());
        }
    }

    @NotNull
    public static Paint exchangePaint(@NotNull Output output, @NotNull Paint current, @NotNull Paint paint, boolean doCleanUp) {
        if (paint instanceof WrappingPaint) {
            WrappingPaint wrappingPaint = (WrappingPaint)((Object)paint);
            wrappingPaint.safelySetPaint(output, current, doCleanUp);
            return paint;
        }
        if (current instanceof WrappingPaint) {
            WrappingPaint wrappingPaint = (WrappingPaint)((Object)current);
            wrappingPaint.safelySetPaint(output, paint, doCleanUp);
            return current;
        }
        if (doCleanUp) {
            GraphicsUtil.preparePaint(paint);
            GraphicsUtil.cleanupPaint(output, current);
        }
        return paint;
    }

    @NotNull
    public static Graphics2D createGraphics(@NotNull BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.clipRect(0, 0, image.getWidth(), image.getHeight());
        return g;
    }

    @NotNull
    public static Composite deriveComposite(@NotNull Graphics2D g, float opacity) {
        Composite composite = g.getComposite();
        if (composite instanceof AlphaComposite) {
            AlphaComposite ac = (AlphaComposite)composite;
            return AlphaComposite.getInstance(ac.getRule(), ac.getAlpha() * opacity);
        }
        if (composite != null) {
            LOGGER.log(Logger.Level.WARNING, () -> String.format("Composite %s will be overridden by opacity %s", composite, Float.valueOf(opacity)));
        }
        return AlphaComposite.getInstance(3, opacity);
    }

    public static void safelyDrawImage(@NotNull Output output, @NotNull Graphics2D g, @NotNull Image image, @Nullable ImageObserver observer) {
        Paint p = g.getPaint();
        if (p instanceof WrappingPaint) {
            WrappingPaint wrappingPaint = (WrappingPaint)((Object)p);
            Paint inner = wrappingPaint.innerPaint();
            Rectangle r = new Rectangle(0, 0, image.getWidth(observer), image.getHeight(observer));
            BufferedImage img = image instanceof BufferedImage ? (BufferedImage)image : ImageUtil.toBufferedImage(image);
            TexturePaint texturePaint = new TexturePaint(img, r);
            wrappingPaint.setPaint(GraphicsUtil.exchangePaint(output, wrappingPaint.paint(), texturePaint, false));
            g.fill(r);
            wrappingPaint.setPaint(GraphicsUtil.exchangePaint(output, texturePaint, inner, false));
        } else {
            g.drawImage(image, 0, 0, observer);
        }
    }

    public static interface WrappingPaint {
        public void setPaint(@NotNull Paint var1);

        @NotNull
        public Paint paint();

        @NotNull
        default public Paint innerPaint() {
            Paint p = this.paint();
            if (p instanceof WrappingPaint) {
                return ((WrappingPaint)((Object)p)).innerPaint();
            }
            return p;
        }

        default public void safelySetPaint(@NotNull Output output, @NotNull Paint paint, boolean updateRefCounts) {
            this.setPaint(GraphicsUtil.exchangePaint(output, this.paint(), paint, updateRefCounts));
        }
    }

    public static interface DisposablePaint {
        public void cleanupIfNeeded(@NotNull Output var1);
    }
}

