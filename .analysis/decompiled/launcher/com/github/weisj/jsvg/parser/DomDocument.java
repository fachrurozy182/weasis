/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.parser.LoaderContext;
import java.net.URI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DomDocument {
    @NotNull
    public LoaderContext loaderContext();

    public void registerNamedElement(@NotNull String var1, @Nullable Object var2);

    @Nullable
    public <T> T getElementById(@NotNull Class<T> var1, @Nullable String var2);

    @Nullable
    public URI rootURI();
}

