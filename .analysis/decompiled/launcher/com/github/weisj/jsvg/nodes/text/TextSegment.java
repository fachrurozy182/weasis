/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.text;

import com.github.weisj.jsvg.nodes.text.GlyphCursor;
import com.github.weisj.jsvg.nodes.text.MutableGlyphRun;
import com.github.weisj.jsvg.nodes.text.TextMetrics;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.output.Output;
import com.github.weisj.jsvg.renderer.output.TextOutput;
import org.jetbrains.annotations.NotNull;

interface TextSegment {
    public boolean isSegmentVisible(@NotNull RenderContext var1);

    public static interface RenderableSegment
    extends TextSegment {
        public void prepareSegmentForRendering(@NotNull GlyphCursor var1, @NotNull RenderContext var2, @NotNull TextOutput var3);

        public void renderSegmentWithoutLayout(@NotNull GlyphCursor var1, @NotNull RenderContext var2, @NotNull Output var3);

        @NotNull
        public TextMetrics computeTextMetrics(@NotNull RenderContext var1, @NotNull UseTextLengthForCalculation var2);

        public void appendTextShape(@NotNull GlyphCursor var1, @NotNull MutableGlyphRun var2, @NotNull RenderContext var3);

        public static enum UseTextLengthForCalculation {
            YES,
            NO;

        }
    }
}

