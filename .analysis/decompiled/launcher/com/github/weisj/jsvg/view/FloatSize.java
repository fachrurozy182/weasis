/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.view;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class FloatSize
extends Dimension2D {
    public float width;
    public float height;

    public FloatSize(@NotNull Rectangle2D r) {
        this((float)r.getWidth(), (float)r.getHeight());
    }

    public FloatSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double getWidth() {
        return this.width;
    }

    @Override
    public double getHeight() {
        return this.height;
    }

    @Override
    public void setSize(double width, double height) {
        this.width = (float)width;
        this.height = (float)height;
    }

    public String toString() {
        return "FloatSize{width=" + this.width + ", height=" + this.height + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FloatSize)) {
            return false;
        }
        FloatSize floatSize = (FloatSize)o;
        return Float.compare(floatSize.width, this.width) == 0 && Float.compare(floatSize.height, this.height) == 0;
    }

    public int hashCode() {
        return Objects.hash(Float.valueOf(this.width), Float.valueOf(this.height));
    }
}

