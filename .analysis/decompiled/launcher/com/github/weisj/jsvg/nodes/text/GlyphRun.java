/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.text;

import com.github.weisj.jsvg.nodes.text.AbstractGlyphRun;
import java.awt.Shape;
import java.util.List;
import org.jetbrains.annotations.NotNull;

class GlyphRun
extends AbstractGlyphRun<Shape> {
    GlyphRun(@NotNull Shape shape, @NotNull AbstractGlyphRun.Metrics metrics, @NotNull @NotNull List<@NotNull AbstractGlyphRun.PaintableEmoji> emojis) {
        super(shape, metrics, emojis);
    }
}

