/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.animation.time;

import com.github.weisj.jsvg.animation.time.Duration;
import com.github.weisj.jsvg.attributes.SuffixUnit;
import org.jetbrains.annotations.NotNull;

public enum TimeUnit implements SuffixUnit<TimeUnit, Duration>
{
    Hour(3600000L, "h"),
    Minute(60000L, "min"),
    Second(1000L, "s"),
    Millisecond(1L, "ms"),
    Raw(1000L, "");

    private static final TimeUnit[] units;
    @NotNull
    private final String suffix;
    private final long milliseconds;

    @NotNull
    public @NotNull TimeUnit @NotNull [] units() {
        return units;
    }

    private TimeUnit(long milliseconds, String suffix) {
        this.suffix = suffix;
        this.milliseconds = milliseconds;
    }

    @Override
    @NotNull
    public Duration valueOf(float value) {
        if (value == 0.0f) {
            return new Duration(0L);
        }
        return new Duration((long)((float)this.milliseconds * value));
    }

    @Override
    @NotNull
    public String suffix() {
        return this.suffix;
    }

    static {
        units = TimeUnit.values();
    }
}

