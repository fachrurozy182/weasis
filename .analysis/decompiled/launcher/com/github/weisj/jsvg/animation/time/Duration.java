/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.errorprone.annotations.Immutable
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.animation.time;

import com.google.errorprone.annotations.Immutable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@Immutable
public final class Duration
implements Comparable<Duration> {
    public static final long INDEFINITE_RAW = Long.MAX_VALUE;
    @NotNull
    public static final Duration INDEFINITE = new Duration(Long.MAX_VALUE);
    public static final Duration ZERO = new Duration(0L);
    private final long milliseconds;

    public Duration(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public long milliseconds() {
        return this.milliseconds;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Duration duration = (Duration)o;
        return this.milliseconds == duration.milliseconds;
    }

    public int hashCode() {
        return Objects.hash(this.milliseconds);
    }

    public boolean isIndefinite() {
        return this.milliseconds == Long.MAX_VALUE;
    }

    @NotNull
    public Duration plus(@NotNull Duration duration) {
        if (duration.isIndefinite() || this.isIndefinite()) {
            return INDEFINITE;
        }
        return new Duration(this.milliseconds + duration.milliseconds);
    }

    @NotNull
    public Duration minus(@NotNull Duration duration) {
        if (duration.isIndefinite() || this.isIndefinite()) {
            return INDEFINITE;
        }
        return new Duration(this.milliseconds - duration.milliseconds);
    }

    @Override
    public int compareTo(@NotNull Duration o) {
        return Long.compare(this.milliseconds, o.milliseconds);
    }

    public String toString() {
        return "Duration{milliseconds=" + this.milliseconds + '}';
    }
}

