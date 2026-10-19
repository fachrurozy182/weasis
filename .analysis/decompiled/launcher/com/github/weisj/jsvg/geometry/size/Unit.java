/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.size;

import com.github.weisj.jsvg.attributes.SuffixUnit;
import com.github.weisj.jsvg.geometry.size.Length;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;

public enum Unit implements SuffixUnit<Unit, Length>
{
    PX,
    CM,
    Q,
    MM,
    IN,
    EM,
    REM,
    EX,
    CH,
    PT,
    PC,
    VW,
    VH,
    VI,
    VB,
    V_MIN("vmin"),
    V_MAX("vmax"),
    PERCENTAGE("%"),
    PERCENTAGE_LENGTH("%"),
    PERCENTAGE_WIDTH("%"),
    PERCENTAGE_HEIGHT("%"),
    RAW("");

    private static final Unit[] units;
    @NotNull
    private final String suffix;

    @NotNull
    public @NotNull Unit @NotNull [] units() {
        return units;
    }

    private Unit(String suffix) {
        this.suffix = suffix;
    }

    private Unit() {
        this.suffix = this.name().toLowerCase(Locale.ENGLISH);
    }

    @Override
    @NotNull
    public Length valueOf(float value) {
        if (value == 0.0f) {
            return Length.ZERO;
        }
        return new Length(this, value);
    }

    @Override
    @NotNull
    public String suffix() {
        return this.suffix;
    }

    public boolean isPercentage() {
        switch (this.ordinal()) {
            case 17: 
            case 18: 
            case 19: 
            case 20: {
                return true;
            }
        }
        return false;
    }

    static {
        units = Unit.values();
    }
}

