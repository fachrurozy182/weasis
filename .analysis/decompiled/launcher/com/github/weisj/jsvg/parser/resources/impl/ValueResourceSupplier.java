/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser.resources.impl;

import com.github.weisj.jsvg.parser.resources.ResourceSupplier;
import com.github.weisj.jsvg.renderer.PlatformSupport;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public final class ValueResourceSupplier<T>
implements ResourceSupplier<T> {
    private final T value;

    public ValueResourceSupplier(T value) {
        this.value = value;
    }

    @Override
    @NotNull
    public @NotNull Optional<@NotNull T> get(@NotNull PlatformSupport platformSupport) {
        return Optional.of(this.value);
    }

    public T get() {
        return this.value;
    }
}

