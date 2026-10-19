/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.filter;

import com.github.weisj.jsvg.attributes.filter.FilterChannelKey;
import com.github.weisj.jsvg.nodes.filter.IllegalFilterStateException;
import com.github.weisj.jsvg.util.supplier.ConstantSupplier;
import com.github.weisj.jsvg.util.supplier.LazySupplier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public final class ChannelStorage<T> {
    @NotNull
    private final @NotNull Map<@NotNull Object, @NotNull Supplier<T>> storage = new HashMap<Object, Supplier<T>>();

    public void addResult(@NotNull FilterChannelKey key, @NotNull T value) {
        this.storage.put(key.key(), new ConstantSupplier<T>(value));
    }

    public void addResult(@NotNull FilterChannelKey key, @NotNull Supplier<T> value) {
        this.storage.put(key.key(), new LazySupplier<T>(value));
    }

    @NotNull
    public T get(@NotNull FilterChannelKey key) {
        Supplier<T> provider = this.storage.get(key.key());
        if (provider == null) {
            throw new IllegalFilterStateException("Channel " + key + " not found.");
        }
        return provider.get();
    }
}

