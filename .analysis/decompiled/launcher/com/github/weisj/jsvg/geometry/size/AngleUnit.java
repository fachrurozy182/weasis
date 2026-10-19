/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.size;

import java.util.Locale;
import org.jetbrains.annotations.NotNull;

public enum AngleUnit {
    Deg,
    Grad,
    Rad,
    Turn,
    Raw("");

    private static final AngleUnit[] units;
    private static final double GRADIANS_TO_RADIANS = 0.015707962916848627;
    @NotNull
    private final String suffix;

    public static AngleUnit[] units() {
        return units;
    }

    private AngleUnit(String suffix) {
        this.suffix = suffix;
    }

    private AngleUnit() {
        this.suffix = this.name().toLowerCase(Locale.ENGLISH);
    }

    @NotNull
    public String suffix() {
        return this.suffix;
    }

    public float toRadians(float value) {
        switch (this.ordinal()) {
            case 0: 
            case 4: {
                return (float)Math.toRadians(value);
            }
            case 1: {
                return (float)((double)value * 0.015707962916848627);
            }
            case 2: {
                return value;
            }
            case 3: {
                return (float)((double)value * Math.PI * 2.0);
            }
        }
        throw new IllegalArgumentException("Unknown angle unit " + (Object)((Object)this));
    }

    static {
        units = AngleUnit.values();
    }
}

