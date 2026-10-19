/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class Coordinate<T> {
    @NotNull
    private final T x;
    @NotNull
    private final T y;

    public Coordinate(@NotNull T x, @NotNull T y) {
        this.x = x;
        this.y = y;
    }

    @NotNull
    public T x() {
        return this.x;
    }

    @NotNull
    public T y() {
        return this.y;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Coordinate that = (Coordinate)o;
        return Objects.equals(this.x, that.x) && Objects.equals(this.y, that.y);
    }

    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }

    public String toString() {
        return "Coordinate{x=" + this.x + ", y=" + this.y + '}';
    }
}

