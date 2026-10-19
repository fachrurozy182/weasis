/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.renderer;

import com.github.weisj.jsvg.attributes.font.SVGFont;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PlatformSupport {
    @Nullable
    public ImageObserver imageObserver();

    @Nullable
    public TargetSurface targetSurface();

    default public float fontSize() {
        return SVGFont.defaultFontSize();
    }

    @NotNull
    default public String fontFamily() {
        return SVGFont.defaultFontFamily();
    }

    @NotNull
    default public Image createImage(@NotNull ImageProducer imageProducer) {
        return Toolkit.getDefaultToolkit().createImage(imageProducer);
    }

    default public boolean isLongLived() {
        return false;
    }

    public static interface TargetSurface {
        public void repaint();
    }
}

