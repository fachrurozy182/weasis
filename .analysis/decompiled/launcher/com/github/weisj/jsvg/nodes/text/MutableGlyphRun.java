/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.text;

import com.github.weisj.jsvg.nodes.text.AbstractGlyphRun;
import com.github.weisj.jsvg.nodes.text.GlyphRun;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

class MutableGlyphRun
extends AbstractGlyphRun<Path2D> {
    public MutableGlyphRun() {
        super(new Path2D.Float(), AbstractGlyphRun.Metrics.createDefault(), new ArrayList<AbstractGlyphRun.PaintableEmoji>());
    }

    public void append(@NotNull GlyphRun glyphRun) {
        ((Path2D)this.shape()).append((Shape)glyphRun.shape(), false);
        this.metrics().union(glyphRun.metrics());
        this.emojis().addAll(glyphRun.emojis());
    }
}

