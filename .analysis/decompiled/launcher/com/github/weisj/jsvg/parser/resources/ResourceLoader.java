/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser.resources;

import com.github.weisj.jsvg.parser.DomDocument;
import com.github.weisj.jsvg.parser.resources.RenderableResource;
import com.github.weisj.jsvg.parser.resources.ResourceSupplier;
import java.io.IOException;
import java.net.URI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ResourceLoader {
    @Nullable
    public ResourceSupplier<RenderableResource> loadImage(@NotNull DomDocument var1, @NotNull URI var2) throws IOException;
}

