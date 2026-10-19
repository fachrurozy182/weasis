/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.errorprone.annotations.Immutable
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.size;

import com.github.weisj.jsvg.attributes.value.PercentageValue;
import com.github.weisj.jsvg.renderer.MeasureContext;
import com.google.errorprone.annotations.Immutable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@Immutable
public final class Percentage
implements Comparable<Percentage>,
PercentageValue {
    public static final float UNSPECIFIED_RAW = Float.NaN;
    @NotNull
    public static final Percentage UNSPECIFIED = new Percentage(Float.NaN);
    @NotNull
    public static final Percentage ZERO = new Percentage(0.0f);
    @NotNull
    public static final Percentage ONE = new Percentage(1.0f);
    @NotNull
    public static final Percentage INHERITED = new Percentage(1.0f);
    private final float value;

    public Percentage(float value) {
        this.value = value;
    }

    public static boolean isUnspecified(float value) {
        return Float.isNaN(value);
    }

    public static boolean isSpecified(float value) {
        return !Percentage.isUnspecified(value);
    }

    public float value() {
        return this.value;
    }

    @Override
    public float get(@NotNull MeasureContext context) {
        return this.value;
    }

    @Override
    @NotNull
    public PercentageValue multiply(@NotNull PercentageValue other) {
        if (this.value == 1.0f) {
            return other;
        }
        if (other instanceof Percentage) {
            float otherValue = ((Percentage)other).value;
            if (otherValue == 0.0f || this.value == 0.0f) {
                return ZERO;
            }
            if (otherValue == 1.0f) {
                return this;
            }
            return new Percentage(this.value * otherValue);
        }
        return other.multiply(this);
    }

    public boolean isUnspecified() {
        return Percentage.isUnspecified(this.value);
    }

    public boolean isSpecified() {
        return !this.isUnspecified();
    }

    @NotNull
    public Percentage orElseIfUnspecified(float value) {
        if (this.isUnspecified()) {
            return new Percentage(value);
        }
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Percentage percentage = (Percentage)o;
        return Float.compare(this.value, percentage.value) == 0;
    }

    public int hashCode() {
        return Objects.hash(Float.valueOf(this.value));
    }

    public String toString() {
        return this.value * 100.0f + "%";
    }

    @Override
    public int compareTo(@NotNull Percentage o) {
        return Float.compare(this.value, o.value);
    }
}

