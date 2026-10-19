/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.prototype;

import com.github.weisj.jsvg.nodes.filter.Filter;
import org.jetbrains.annotations.Nullable;

public interface HasFilter {
    @Nullable
    public Filter filter();
}

