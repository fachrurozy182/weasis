/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes.value;

import com.github.weisj.jsvg.attributes.value.TransformValue;
import com.github.weisj.jsvg.renderer.MeasureContext;
import java.awt.geom.AffineTransform;
import org.jetbrains.annotations.NotNull;

public final class ConstantTransform
implements TransformValue {
    @NotNull
    private final AffineTransform value;

    public ConstantTransform(@NotNull AffineTransform value) {
        this.value = value;
    }

    @Override
    @NotNull
    public AffineTransform get(@NotNull MeasureContext context) {
        return this.value;
    }
}

