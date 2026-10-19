/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes;

import java.awt.MultipleGradientPaint;
import org.jetbrains.annotations.NotNull;

public enum SpreadMethod {
    Pad(MultipleGradientPaint.CycleMethod.NO_CYCLE),
    Reflect(MultipleGradientPaint.CycleMethod.REFLECT),
    Repeat(MultipleGradientPaint.CycleMethod.REPEAT);

    private final MultipleGradientPaint.CycleMethod cycleMethod;

    private SpreadMethod(MultipleGradientPaint.CycleMethod cycleMethod) {
        this.cycleMethod = cycleMethod;
    }

    @NotNull
    public MultipleGradientPaint.CycleMethod cycleMethod() {
        return this.cycleMethod;
    }
}

