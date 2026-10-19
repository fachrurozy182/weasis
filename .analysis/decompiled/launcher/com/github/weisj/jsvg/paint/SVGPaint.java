/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.paint;

import com.github.weisj.jsvg.animation.value.AnimatedColor;
import com.github.weisj.jsvg.animation.value.AnimatedPaint;
import com.github.weisj.jsvg.paint.impl.PredefinedPaints;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.output.Output;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SVGPaint {
    @Nullable
    public static SVGPaint derive(@Nullable SVGPaint current, @Nullable SVGPaint other) {
        if (other == null) {
            return current;
        }
        if (current == null) {
            return other;
        }
        if (other instanceof AnimatedPaint) {
            return ((AnimatedPaint)other).derive(current);
        }
        if (other instanceof AnimatedColor) {
            return ((AnimatedColor)other).derive(current);
        }
        return other;
    }

    public void fillShape(@NotNull Output var1, @NotNull RenderContext var2, @NotNull Shape var3, @Nullable Rectangle2D var4);

    public void drawShape(@NotNull Output var1, @NotNull RenderContext var2, @NotNull Shape var3, @Nullable Rectangle2D var4);

    default public boolean isVisible(@NotNull RenderContext context) {
        return this != PredefinedPaints.NONE;
    }
}

