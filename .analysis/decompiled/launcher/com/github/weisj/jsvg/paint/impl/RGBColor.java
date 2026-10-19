/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.paint.impl;

import com.github.weisj.jsvg.attributes.value.ColorValue;
import com.github.weisj.jsvg.renderer.MeasureContext;
import com.github.weisj.jsvg.util.ColorUtil;
import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class RGBColor
implements Paint,
ColorValue {
    public static final RGBColor INHERITED = new RGBColor(0, 0, 0, 0);
    public static final RGBColor DEFAULT = new RGBColor(0, 0, 0, 255);
    private final int r;
    private final int g;
    private final int b;
    private final int a;
    private Color color;

    public RGBColor(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public RGBColor(@NotNull Color color) {
        this(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        this.color = color;
    }

    @NotNull
    public static RGBColor interpolate(float t, @NotNull RGBColor a, @NotNull RGBColor b) {
        return new RGBColor(Math.round((float)a.r + (float)(b.r - a.r) * t), Math.round((float)a.g + (float)(b.g - a.g) * t), Math.round((float)a.b + (float)(b.b - a.b) * t), Math.round((float)a.a + (float)(b.a - a.a) * t));
    }

    @NotNull
    public static RGBColor saxpy(float t, @NotNull RGBColor a, @NotNull RGBColor b) {
        return new RGBColor(Math.round((float)a.r + t * (float)b.r), Math.round((float)a.g + t * (float)b.g), Math.round((float)a.b + t * (float)b.b), Math.round((float)a.a + t * (float)b.a));
    }

    @NotNull
    public static RGBColor add(@NotNull RGBColor a, @NotNull RGBColor b) {
        return new RGBColor(a.r + b.r, a.g + b.g, a.b + b.b, a.a + b.a);
    }

    @NotNull
    public Color toColor() {
        if (this.color == null) {
            this.color = new Color(ColorUtil.clampColor(this.r), ColorUtil.clampColor(this.g), ColorUtil.clampColor(this.b), ColorUtil.clampColor(this.a));
        }
        return this.color;
    }

    public boolean isVisible() {
        return this.a > 0;
    }

    public static boolean isVisible(@NotNull Color c) {
        return c.getAlpha() > 0;
    }

    @Contract(pure=true)
    @NotNull
    public String toString() {
        return "ColorValue{r=" + this.r + ", g=" + this.g + ", b=" + this.b + ", a=" + this.a + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        RGBColor that = (RGBColor)o;
        return this.r == that.r && this.g == that.g && this.b == that.b && this.a == that.a;
    }

    public int hashCode() {
        return Objects.hash(this.r, this.g, this.b, this.a);
    }

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
        return this.toColor().createContext(cm, deviceBounds, userBounds, xform, hints);
    }

    @Override
    public int getTransparency() {
        return this.toColor().getTransparency();
    }

    @Override
    @NotNull
    public Color get(@NotNull MeasureContext context) {
        return this.toColor();
    }
}

