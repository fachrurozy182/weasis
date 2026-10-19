/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry;

import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;

public interface SVGShape {
    default public boolean canBeFilled() {
        return true;
    }

    @NotNull
    default public Shape shape(@NotNull RenderContext context) {
        return this.shape(context, true);
    }

    @NotNull
    public Shape shape(@NotNull RenderContext var1, boolean var2);

    @NotNull
    public Rectangle2D bounds(@NotNull RenderContext var1, boolean var2);

    public double pathLength(@NotNull RenderContext var1);

    default public boolean usesOptimizedBoundsCalculation() {
        return true;
    }

    public boolean isClosed(@NotNull RenderContext var1);
}

