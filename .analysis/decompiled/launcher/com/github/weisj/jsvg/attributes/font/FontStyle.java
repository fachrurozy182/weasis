/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes.font;

import com.github.weisj.jsvg.geometry.size.Angle;
import com.github.weisj.jsvg.geometry.size.AngleUnit;
import java.awt.geom.AffineTransform;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class FontStyle {
    private FontStyle() {
    }

    @Nullable
    public AffineTransform transform() {
        return null;
    }

    @NotNull
    public static FontStyle normal() {
        return Normal.INSTANCE;
    }

    @NotNull
    public static FontStyle italic() {
        return Italic.INSTANCE;
    }

    @NotNull
    public static FontStyle oblique() {
        return Oblique.DEFAULT;
    }

    @NotNull
    public static FontStyle oblique(@NotNull Angle angle) {
        return new Oblique(angle);
    }

    static final class Normal
    extends FontStyle {
        @NotNull
        private static final Normal INSTANCE = new Normal();

        Normal() {
        }

        public String toString() {
            return "Normal";
        }

        public boolean equals(Object obj) {
            return obj instanceof Normal;
        }

        public int hashCode() {
            return Normal.class.hashCode();
        }
    }

    static final class Italic
    extends FontStyle {
        @NotNull
        private static final Italic INSTANCE = new Italic();

        Italic() {
        }

        public String toString() {
            return "Italic";
        }

        public boolean equals(Object obj) {
            return obj instanceof Italic;
        }

        public int hashCode() {
            return Italic.class.hashCode();
        }
    }

    static final class Oblique
    extends FontStyle {
        @NotNull
        public static final Angle DEFAULT_ANGLE = new Angle(AngleUnit.Deg, 14.0f);
        @NotNull
        public static final Oblique DEFAULT = new Oblique(DEFAULT_ANGLE);
        @NotNull
        private final Angle angle;

        public Oblique(@NotNull Angle angle) {
            this.angle = angle;
        }

        @Override
        @NotNull
        public AffineTransform transform() {
            return AffineTransform.getShearInstance(-this.angle.radians(), 0.0);
        }

        public String toString() {
            return "Oblique{" + this.angle + '}';
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Oblique)) {
                return false;
            }
            Oblique that = (Oblique)o;
            return this.angle.equals(that.angle);
        }

        public int hashCode() {
            return Objects.hashCode(this.angle);
        }
    }
}

