/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.text;

import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.nodes.text.TextSegment;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.output.Output;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

interface TextLayoutGroup {
    @NotNull
    public List<? extends TextSegment> segments();

    @NotNull
    public Point2D renderText(@Nullable Point2D var1, @NotNull RenderContext var2, @NotNull Output var3);

    @NotNull
    public Point2D appendGlyphShape(@Nullable Point2D var1, @NotNull RenderContext var2, @NotNull Path2D var3);

    @Nullable
    public Length fixedLength();
}

