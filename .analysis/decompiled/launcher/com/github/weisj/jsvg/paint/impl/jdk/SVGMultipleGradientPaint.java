/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.paint.impl.jdk;

import java.awt.Color;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.lang.ref.SoftReference;
import org.jetbrains.annotations.NotNull;

public abstract class SVGMultipleGradientPaint
implements Paint {
    final int transparency;
    final float @NotNull [] fractions;
    @NotNull
    final @NotNull Color @NotNull [] colors;
    @NotNull
    final AffineTransform gradientTransform;
    @NotNull
    final MultipleGradientPaint.CycleMethod cycleMethod;
    @NotNull
    final MultipleGradientPaint.ColorSpaceType colorSpace;
    ColorModel model;
    float[] normalizedIntervals;
    boolean isSimpleLookup;
    SoftReference<int[][]> gradients;
    SoftReference<int[]> gradient;
    int fastGradientArraySize;

    SVGMultipleGradientPaint(float @NotNull [] fractions, @NotNull @NotNull Color @NotNull [] colors, @NotNull MultipleGradientPaint.CycleMethod cycleMethod, @NotNull MultipleGradientPaint.ColorSpaceType colorSpace, @NotNull AffineTransform gradientTransform) {
        if (fractions.length != colors.length) {
            throw new IllegalArgumentException("Colors and fractions must have equal size");
        }
        if (colors.length < 2) {
            throw new IllegalArgumentException("User must specify at least 2 colors");
        }
        float previousFraction = -1.0f;
        for (float currentFraction : fractions) {
            if (currentFraction < 0.0f || currentFraction > 1.0f) {
                throw new IllegalArgumentException("Fraction values must be in the range 0 to 1: " + currentFraction);
            }
            if (currentFraction <= previousFraction) {
                throw new IllegalArgumentException("Keyframe fractions must be increasing: " + currentFraction);
            }
            previousFraction = currentFraction;
        }
        if (fractions[0] != 0.0f) {
            throw new IllegalStateException("Gradient start point must be equal to zero");
        }
        if (fractions[fractions.length - 1] != 1.0f) {
            throw new IllegalStateException("Gradient end point must be equal to one");
        }
        this.fractions = fractions;
        this.colors = colors;
        this.colorSpace = colorSpace;
        this.cycleMethod = cycleMethod;
        this.gradientTransform = gradientTransform;
        boolean opaque = true;
        for (Color color : colors) {
            opaque = opaque && color.getAlpha() == 255;
        }
        this.transparency = opaque ? 1 : 3;
    }

    @Override
    public int getTransparency() {
        return this.transparency;
    }
}

