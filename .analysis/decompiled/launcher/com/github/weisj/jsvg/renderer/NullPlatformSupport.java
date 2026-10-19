/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.renderer;

import com.github.weisj.jsvg.renderer.PlatformSupport;
import java.awt.image.ImageObserver;
import org.jetbrains.annotations.Nullable;

public final class NullPlatformSupport
implements PlatformSupport {
    public static final NullPlatformSupport INSTANCE = new NullPlatformSupport();

    private NullPlatformSupport() {
    }

    @Override
    @Nullable
    public ImageObserver imageObserver() {
        return null;
    }

    @Override
    @Nullable
    public PlatformSupport.TargetSurface targetSurface() {
        return null;
    }
}

