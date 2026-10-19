/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes.value;

import com.github.weisj.jsvg.animation.value.AnimatedTransform;
import com.github.weisj.jsvg.renderer.MeasureContext;
import java.awt.geom.AffineTransform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TransformValue {
    @Nullable
    public static TransformValue derive(@Nullable TransformValue current, @Nullable TransformValue other) {
        if (other == null) {
            return current;
        }
        if (current == null) {
            return other;
        }
        if (other instanceof AnimatedTransform) {
            return ((AnimatedTransform)other).derive(current);
        }
        return other;
    }

    @NotNull
    public AffineTransform get(@NotNull MeasureContext var1);
}

