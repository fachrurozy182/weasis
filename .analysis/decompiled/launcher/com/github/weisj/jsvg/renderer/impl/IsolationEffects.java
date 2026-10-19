/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.renderer.impl;

import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.filter.Filter;
import org.jetbrains.annotations.Nullable;

class IsolationEffects {
    @Nullable
    public final Filter filter;
    @Nullable
    public final Mask mask;
    @Nullable
    public final ClipPath clipPath;

    public IsolationEffects(@Nullable Filter filter, @Nullable Mask mask, @Nullable ClipPath clipPath) {
        this.filter = filter;
        this.mask = mask;
        this.clipPath = clipPath;
    }
}

