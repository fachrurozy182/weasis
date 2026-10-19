/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.prototype;

import com.github.weisj.jsvg.attributes.VectorEffect;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public interface HasVectorEffects {
    @NotNull
    public Set<VectorEffect> vectorEffects();
}

