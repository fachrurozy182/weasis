/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.prototype;

import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Mask;
import org.jetbrains.annotations.Nullable;

public interface HasClip {
    @Nullable
    public ClipPath clipPath();

    @Nullable
    public Mask mask();
}

