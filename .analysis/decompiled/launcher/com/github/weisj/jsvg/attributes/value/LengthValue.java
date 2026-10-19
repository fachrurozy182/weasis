/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes.value;

import com.github.weisj.jsvg.animation.value.AnimatedLength;
import com.github.weisj.jsvg.renderer.MeasureContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LengthValue {
    @Nullable
    public static LengthValue derive(@Nullable LengthValue current, @Nullable LengthValue other) {
        if (other == null) {
            return current;
        }
        if (current == null) {
            return other;
        }
        if (other instanceof AnimatedLength) {
            return ((AnimatedLength)other).derive(current);
        }
        return other;
    }

    public boolean isConstantlyZero();

    public boolean isConstantlyNonNegative();

    public float resolve(@NotNull MeasureContext var1);
}

