/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.prototype;

import com.github.weisj.jsvg.nodes.prototype.Mutator;
import com.github.weisj.jsvg.renderer.impl.context.PaintContext;
import org.jetbrains.annotations.NotNull;

public interface HasPaintContext {
    @NotNull
    public Mutator<PaintContext> paintContext();
}

