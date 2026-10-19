/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Internal
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.prototype;

import com.github.weisj.jsvg.nodes.SVGNode;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Container<E> {
    @ApiStatus.Internal
    public void addChild(@Nullable String var1, @NotNull SVGNode var2);

    public List<? extends @NotNull E> children();

    default public <T extends E> List<@NotNull T> childrenOfType(Class<T> type) {
        return this.children().stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
    }
}

