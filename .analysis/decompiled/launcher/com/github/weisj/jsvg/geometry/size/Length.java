/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.size;

import com.github.weisj.jsvg.attributes.UnitType;
import com.github.weisj.jsvg.attributes.value.LengthValue;
import com.github.weisj.jsvg.attributes.value.PercentageDimension;
import com.github.weisj.jsvg.geometry.size.Unit;
import com.github.weisj.jsvg.renderer.MeasureContext;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class Length
implements LengthValue {
    public static final float UNSPECIFIED_RAW = Float.NaN;
    @NotNull
    public static final Length UNSPECIFIED = new Length(Unit.RAW, Float.NaN);
    @NotNull
    public static final Length ZERO = new Length(Unit.RAW, 0.0f);
    @NotNull
    public static final Length ONE = new Length(Unit.RAW, 1.0f);
    @NotNull
    public static final Length INHERITED = new Length(Unit.RAW, 0.0f);
    @NotNull
    private final Unit unit;
    private final float value;
    private static final float PIXELS_PER_INCH = 96.0f;
    private static final float INCHES_PER_CM = 0.3936f;

    public Length(@NotNull Unit unit, float value) {
        this.unit = unit;
        this.value = value;
    }

    public Length(@NotNull Length ry) {
        this.unit = ry.unit;
        this.value = ry.value;
    }

    public static boolean isUnspecified(float value) {
        return Float.isNaN(value);
    }

    public static boolean isSpecified(float value) {
        return !Length.isUnspecified(value);
    }

    private static float resolveNonPercentage(@NotNull MeasureContext context, Unit unit, float value) {
        if (unit == Unit.RAW) {
            return value;
        }
        assert (!unit.isPercentage());
        switch (unit) {
            case PX: {
                return value;
            }
            case IN: {
                return 96.0f * value;
            }
            case CM: {
                return 37.7856f * value;
            }
            case MM: {
                return 3.7785597f * value;
            }
            case Q: {
                return 0.9446399f * value;
            }
            case PT: {
                return 1.3333334f * value;
            }
            case PC: {
                return 16.0f * value;
            }
            case EM: {
                return context.em() * value;
            }
            case REM: {
                return context.rem() * value;
            }
            case CH: 
            case EX: {
                return context.ex() * value;
            }
        }
        throw new UnsupportedOperationException("Not implemented: Can't convert " + unit + " to pixel");
    }

    @Override
    public float resolve(@NotNull MeasureContext context) {
        float raw = this.raw();
        switch (this.unit) {
            case PERCENTAGE_LENGTH: {
                return raw * context.normedDiagonalLength() / 100.0f;
            }
            case VW: 
            case VI: 
            case PERCENTAGE_WIDTH: {
                return raw * context.viewWidth() / 100.0f;
            }
            case VH: 
            case VB: 
            case PERCENTAGE_HEIGHT: {
                return raw * context.viewHeight() / 100.0f;
            }
            case PERCENTAGE: {
                return raw / 100.0f;
            }
            case V_MIN: {
                return raw * Math.min(context.viewWidth(), context.viewHeight()) / 100.0f;
            }
            case V_MAX: {
                return raw * Math.max(context.viewWidth(), context.viewHeight()) / 100.0f;
            }
        }
        return Length.resolveNonPercentage(context, this.unit, raw);
    }

    public float resolveFontSize(@NotNull MeasureContext context) {
        float raw = this.raw();
        switch (this.unit) {
            case PERCENTAGE_LENGTH: 
            case PERCENTAGE_WIDTH: 
            case PERCENTAGE_HEIGHT: {
                throw new IllegalStateException("Can't resolve font size with geometric percentage unit");
            }
            case PERCENTAGE: {
                return raw / 100.0f * context.em();
            }
        }
        return Length.resolveNonPercentage(context, this.unit, raw);
    }

    @NotNull
    public String toString() {
        if (this.isUnspecified()) {
            return "<unspecified>";
        }
        return this.value + this.unit.suffix();
    }

    public boolean isZero() {
        return this.value == 0.0f;
    }

    @Override
    public boolean isConstantlyZero() {
        return this.isZero();
    }

    @Override
    public boolean isConstantlyNonNegative() {
        return this.raw() >= 0.0f;
    }

    public boolean isAbsolute() {
        switch (this.unit) {
            case PX: 
            case IN: 
            case CM: 
            case MM: 
            case Q: 
            case PT: 
            case PC: 
            case RAW: {
                return true;
            }
        }
        return false;
    }

    public float raw() {
        return this.value;
    }

    @NotNull
    public Unit unit() {
        return this.unit;
    }

    public boolean isUnspecified() {
        return Length.isUnspecified(this.value);
    }

    public boolean isSpecified() {
        return Length.isSpecified(this.value);
    }

    @NotNull
    public Length coerceNonNegative() {
        if (this.isSpecified() && this.value <= 0.0f) {
            return ZERO;
        }
        return this;
    }

    @NotNull
    public Length coercePercentageToCorrectUnit(@NotNull UnitType unitType, @NotNull PercentageDimension dimension) {
        Unit u;
        if (unitType == UnitType.UserSpaceOnUse) {
            return this;
        }
        if (this.unit.isPercentage()) {
            return this;
        }
        switch (dimension) {
            case WIDTH: {
                u = Unit.PERCENTAGE_WIDTH;
                break;
            }
            case HEIGHT: {
                u = Unit.PERCENTAGE_HEIGHT;
                break;
            }
            case LENGTH: {
                u = Unit.PERCENTAGE_LENGTH;
                break;
            }
            case CUSTOM: {
                u = Unit.PERCENTAGE;
                break;
            }
            default: {
                throw new IllegalStateException("Can't coerce to percentage with no dimension");
            }
        }
        return new Length(u, this.raw() * 100.0f);
    }

    @NotNull
    public Length orElseIfUnspecified(float value) {
        if (this.isUnspecified()) {
            return Unit.RAW.valueOf(value);
        }
        return this;
    }

    @NotNull
    public Length multiply(float scalingFactor) {
        if (scalingFactor == 0.0f) {
            return ZERO;
        }
        if (scalingFactor == 1.0f) {
            return this;
        }
        return new Length(this.unit, scalingFactor * this.value);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Length)) {
            return false;
        }
        Length length = (Length)o;
        return this.unit == length.unit && Float.compare(length.value, this.value) == 0;
    }

    public int hashCode() {
        return Objects.hash(this.unit, Float.valueOf(this.value));
    }
}

