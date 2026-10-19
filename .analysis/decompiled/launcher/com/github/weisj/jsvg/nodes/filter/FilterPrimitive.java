/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.filter;

import com.github.weisj.jsvg.attributes.ColorInterpolation;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.nodes.filter.FilterContext;
import com.github.weisj.jsvg.nodes.filter.FilterLayoutContext;
import com.github.weisj.jsvg.renderer.RenderContext;
import org.jetbrains.annotations.NotNull;

public interface FilterPrimitive {
    @NotNull
    public Length y();

    @NotNull
    public Length x();

    @NotNull
    public Length width();

    @NotNull
    public Length height();

    default public boolean isValid() {
        return true;
    }

    public void layoutFilter(@NotNull RenderContext var1, @NotNull FilterLayoutContext var2);

    public void applyFilter(@NotNull RenderContext var1, @NotNull FilterContext var2);

    public ColorInterpolation colorInterpolation(@NotNull FilterContext var1);
}

