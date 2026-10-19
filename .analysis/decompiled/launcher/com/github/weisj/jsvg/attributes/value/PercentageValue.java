/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes.value;

import com.github.weisj.jsvg.animation.value.AnimatedPercentage;
import com.github.weisj.jsvg.renderer.MeasureContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PercentageValue {
    @Nullable
    public static PercentageValue derive(@Nullable PercentageValue current, @Nullable PercentageValue other) {
        if (other == null) {
            return current;
        }
        if (current == null) {
            return other;
        }
        if (other instanceof AnimatedPercentage) {
            return ((AnimatedPercentage)other).derive(current);
        }
        return other;
    }

    public float get(@NotNull MeasureContext var1);

    @NotNull
    public PercentageValue multiply(@NotNull PercentageValue var1);
}

