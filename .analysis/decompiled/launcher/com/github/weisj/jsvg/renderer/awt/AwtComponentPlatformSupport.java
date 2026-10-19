/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.renderer.awt;

import com.github.weisj.jsvg.renderer.PlatformSupport;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import org.jetbrains.annotations.NotNull;

public final class AwtComponentPlatformSupport
implements PlatformSupport {
    @NotNull
    private final Component component;

    public AwtComponentPlatformSupport(@NotNull Component component) {
        this.component = component;
    }

    @Override
    public float fontSize() {
        Font font = this.component.getFont();
        if (font != null) {
            return font.getSize2D();
        }
        return PlatformSupport.super.fontSize();
    }

    @Override
    @NotNull
    public String fontFamily() {
        Font font = this.component.getFont();
        if (font != null) {
            return font.getFamily();
        }
        return PlatformSupport.super.fontFamily();
    }

    @Override
    @NotNull
    public PlatformSupport.TargetSurface targetSurface() {
        return this.component::repaint;
    }

    @Override
    public boolean isLongLived() {
        return true;
    }

    @Override
    @NotNull
    public ImageObserver imageObserver() {
        return this.component;
    }

    @Override
    @NotNull
    public Image createImage(@NotNull ImageProducer imageProducer) {
        return this.component.createImage(imageProducer);
    }

    public String toString() {
        return "AwtComponentSupport{component=" + this.component + '}';
    }
}

