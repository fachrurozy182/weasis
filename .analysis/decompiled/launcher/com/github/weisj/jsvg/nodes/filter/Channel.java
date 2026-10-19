/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.filter;

import com.github.weisj.jsvg.nodes.filter.AlphaImageFilter;
import com.github.weisj.jsvg.nodes.filter.PixelProvider;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.util.ImageUtil;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import org.jetbrains.annotations.NotNull;

public interface Channel {
    @NotNull
    public ImageProducer producer();

    @NotNull
    default public Image toImage(@NotNull RenderContext context) {
        return context.platformSupport().createImage(this.producer());
    }

    @NotNull
    default public BufferedImage toBufferedImageNonAliased(@NotNull RenderContext context) {
        return Channel.makeNonAliased(this.toImage(context));
    }

    @NotNull
    public static BufferedImage makeNonAliased(@NotNull Image img) {
        BufferedImage bufferedImage = ImageUtil.createCompatibleTransparentImage(img.getWidth(null), img.getHeight(null));
        Graphics imageGraphics = bufferedImage.getGraphics();
        imageGraphics.drawImage(img, 0, 0, null);
        imageGraphics.dispose();
        return bufferedImage;
    }

    @NotNull
    public Channel applyFilter(@NotNull ImageFilter var1);

    @NotNull
    public PixelProvider pixels(@NotNull RenderContext var1);

    @NotNull
    default public Channel alphaChannel() {
        return this.applyFilter(new AlphaImageFilter());
    }
}

