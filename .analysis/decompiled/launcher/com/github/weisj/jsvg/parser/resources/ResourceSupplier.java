/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser.resources;

import com.github.weisj.jsvg.renderer.PlatformSupport;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface ResourceSupplier<T> {
    @NotNull
    public @NotNull Optional<@NotNull T> get(@NotNull PlatformSupport var1);
}

