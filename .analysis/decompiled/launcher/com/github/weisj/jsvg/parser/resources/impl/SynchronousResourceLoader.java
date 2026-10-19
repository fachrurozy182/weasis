/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser.resources.impl;

import com.github.weisj.jsvg.parser.DomDocument;
import com.github.weisj.jsvg.parser.resources.RenderableResource;
import com.github.weisj.jsvg.parser.resources.ResourceLoader;
import com.github.weisj.jsvg.parser.resources.ResourceSupplier;
import com.github.weisj.jsvg.parser.resources.impl.ValueResourceSupplier;
import com.github.weisj.jsvg.util.ResourceUtil;
import java.io.IOException;
import java.net.URI;
import org.jetbrains.annotations.NotNull;

public final class SynchronousResourceLoader
implements ResourceLoader {
    @Override
    @NotNull
    public ResourceSupplier<RenderableResource> loadImage(@NotNull DomDocument document, @NotNull URI uri) throws IOException {
        return new ValueResourceSupplier<RenderableResource>(ResourceUtil.loadImage(document, uri));
    }
}

