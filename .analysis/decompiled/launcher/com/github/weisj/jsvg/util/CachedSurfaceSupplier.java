/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.util;

import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.SVGRenderingHints;
import com.github.weisj.jsvg.renderer.output.Output;
import com.github.weisj.jsvg.util.BlittableImage;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CachedSurfaceSupplier {
    @NotNull
    private final BlittableImage.BufferSurfaceSupplier surfaceSupplier;
    @NotNull
    private final ThreadLocal<Cache> cache = ThreadLocal.withInitial(() -> new Cache());

    public CachedSurfaceSupplier(@NotNull BlittableImage.BufferSurfaceSupplier surfaceSupplier) {
        this.surfaceSupplier = surfaceSupplier;
    }

    public boolean useCache(@NotNull Output output, @NotNull RenderContext renderContext) {
        return renderContext.platformSupport().isLongLived() && output.renderingHint(SVGRenderingHints.KEY_CACHE_OFFSCREEN_IMAGE) != SVGRenderingHints.VALUE_NO_CACHE;
    }

    @NotNull
    public BlittableImage.BufferSurfaceSupplier surfaceSupplier(boolean useCache) {
        if (!useCache) {
            return this.surfaceSupplier;
        }
        return this::createBufferSurface;
    }

    @NotNull
    public BufferedImage createBufferSurface(@Nullable AffineTransform at, double width, double height) {
        if (at != null) {
            throw new UnsupportedOperationException("CachedSurfaceSupplier does not support transformations");
        }
        Cache c = this.cache.get();
        Iterator it = c.images.iterator();
        while (it.hasNext()) {
            CachedImage cachedImage = (CachedImage)it.next();
            BufferedImage img = (BufferedImage)cachedImage.image.get();
            if (img == null) {
                it.remove();
                continue;
            }
            if (cachedImage.inUse || !((double)img.getWidth() >= width) || !((double)img.getHeight() >= height)) continue;
            cachedImage.inUse = true;
            c.lastIssuedCleaner = new ResourceCleaner(null, () -> cachedImage.free());
            return img.getSubimage(0, 0, (int)width, (int)height);
        }
        BufferedImage image = this.surfaceSupplier.createBufferSurface(null, width, height);
        CachedImage cachedImage = new CachedImage(image);
        c.images.add(cachedImage);
        c.lastIssuedCleaner = new ResourceCleaner(null, () -> cachedImage.free());
        return image;
    }

    @Nullable
    public ResourceCleaner resourceCleaner(Object owner, boolean useCache) {
        if (!useCache) {
            return null;
        }
        ResourceCleaner cleaner = this.cache.get().lastIssuedCleaner;
        if (cleaner != null) {
            return cleaner.withOwner(owner);
        }
        return null;
    }

    private static class Cache {
        private final List<CachedImage> images = new ArrayList<CachedImage>();
        @Nullable
        private ResourceCleaner lastIssuedCleaner;

        private Cache() {
        }
    }

    private static class CachedImage {
        @NotNull
        private final SoftReference<BufferedImage> image;
        private boolean inUse = true;

        private CachedImage(@NotNull BufferedImage image) {
            this.image = new SoftReference<BufferedImage>(image);
        }

        private void free() {
            this.inUse = false;
        }
    }

    public static class ResourceCleaner {
        @Nullable
        private final Object owner;
        @Nullable
        private Runnable cleaner;

        public ResourceCleaner(@Nullable Object owner, @Nullable Runnable cleaner) {
            this.owner = owner;
            this.cleaner = cleaner;
        }

        public void clean(Object owner) {
            if (this.owner == owner) {
                if (this.cleaner == null) {
                    throw new IllegalStateException("Resource already cleaned");
                }
                this.cleaner.run();
                this.cleaner = null;
            }
        }

        @NotNull
        private ResourceCleaner withOwner(Object owner) {
            return new ResourceCleaner(owner, this.cleaner);
        }
    }
}

