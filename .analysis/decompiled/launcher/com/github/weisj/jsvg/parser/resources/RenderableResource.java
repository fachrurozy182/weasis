/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser.resources;

import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.output.Output;
import com.github.weisj.jsvg.view.FloatSize;
import java.awt.geom.AffineTransform;
import org.jetbrains.annotations.NotNull;

public interface RenderableResource {
    @NotNull
    public FloatSize intrinsicSize(@NotNull RenderContext var1);

    public void render(@NotNull Output var1, @NotNull RenderContext var2, @NotNull AffineTransform var3);
}

