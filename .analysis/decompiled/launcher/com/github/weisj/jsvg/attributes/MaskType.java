/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes;

import com.github.weisj.jsvg.util.BlittableImage;
import com.github.weisj.jsvg.util.ImageUtil;
import com.github.weisj.jsvg.util.supplier.ImmutableSupplier;
import org.jetbrains.annotations.NotNull;

public enum MaskType {
    Luminance(() -> ImageUtil::createLuminosityBuffer),
    Alpha(() -> ImageUtil::createCompatibleTransparentImage);

    @NotNull
    private final @NotNull ImmutableSupplier< @NotNull BlittableImage.BufferSurfaceSupplier> bufferSurfaceSupplier;

    private MaskType(ImmutableSupplier<BlittableImage.BufferSurfaceSupplier> bufferSurfaceSupplier) {
        this.bufferSurfaceSupplier = bufferSurfaceSupplier;
    }

    @NotNull
    public BlittableImage.BufferSurfaceSupplier bufferSurface() {
        return this.bufferSurfaceSupplier.get();
    }
}

