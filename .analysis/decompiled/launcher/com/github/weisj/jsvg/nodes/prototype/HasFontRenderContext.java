/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.prototype;

import com.github.weisj.jsvg.renderer.impl.context.FontRenderContext;
import org.jetbrains.annotations.NotNull;

public interface HasFontRenderContext {
    @NotNull
    public FontRenderContext fontRenderContext();
}

