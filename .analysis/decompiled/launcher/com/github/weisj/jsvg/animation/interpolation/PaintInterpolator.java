/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.animation.interpolation;

import com.github.weisj.jsvg.paint.SVGPaint;
import org.jetbrains.annotations.NotNull;

public interface PaintInterpolator {
    @NotNull
    public SVGPaint interpolate(@NotNull SVGPaint var1, @NotNull SVGPaint var2, @NotNull SVGPaint var3, float var4);
}

