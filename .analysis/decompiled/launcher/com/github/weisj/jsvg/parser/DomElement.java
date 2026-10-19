/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.parser.DomDocument;
import com.github.weisj.jsvg.parser.TextContent;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DomElement {
    @Nullable
    public String id();

    @NotNull
    public String tagName();

    @NotNull
    public @NotNull List<@NotNull String> classNames();

    @NotNull
    public DomDocument document();

    @NotNull
    public @NotNull List<? extends @NotNull DomElement> children();

    @Nullable
    public String attribute(@NotNull String var1);

    @NotNull
    default public String attribute(@NotNull String name, @NotNull String fallback) {
        String value = this.attribute(name);
        return value != null ? value : fallback;
    }

    public void setAttribute(@NotNull String var1, @Nullable String var2);

    @ApiStatus.Experimental
    @NotNull
    public TextContent textContent();

    @Nullable
    public DomElement parent();
}

