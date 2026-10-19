/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.size;

import com.github.weisj.jsvg.geometry.size.AngleUnit;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class Angle {
    public static final float UNSPECIFIED_RAW = Float.NaN;
    @NotNull
    public static final Angle UNSPECIFIED = new Angle(AngleUnit.Raw, Float.NaN);
    @NotNull
    public static final Angle ZERO = new Angle(AngleUnit.Raw, 0.0f);
    private final float radian;

    public Angle(AngleUnit unit, float value) {
        this.radian = unit.toRadians(value);
    }

    public static boolean isUnspecified(float value) {
        return Float.isNaN(value);
    }

    public static boolean isSpecified(float value) {
        return !Angle.isUnspecified(value);
    }

    public float radians() {
        return this.radian;
    }

    public boolean isUnspecified() {
        return Angle.isUnspecified(this.radian);
    }

    public boolean isSpecified() {
        return !this.isUnspecified();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Angle angle = (Angle)o;
        return Float.compare(this.radian, angle.radian) == 0;
    }

    public int hashCode() {
        return Objects.hash(Float.valueOf(this.radian));
    }

    public String toString() {
        return "Angle{radian=" + this.radian + '}';
    }
}

