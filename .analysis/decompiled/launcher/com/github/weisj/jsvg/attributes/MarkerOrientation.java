/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes;

import com.github.weisj.jsvg.geometry.size.Angle;
import com.github.weisj.jsvg.parser.impl.AttributeParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MarkerOrientation {
    private MarkerOrientation() {
    }

    @NotNull
    public static MarkerOrientation parse(@Nullable String value, @NotNull AttributeParser parser) {
        if (value == null) {
            return AngleOrientation.DEFAULT;
        }
        if ("auto".equals(value)) {
            return AutoOrientation.INSTANCE;
        }
        if ("auto-start-reverse".equals(value)) {
            return AutoStartReverseOrientation.INSTANCE;
        }
        Angle angle = parser.parseAngle(value, Angle.UNSPECIFIED);
        if (angle.isSpecified()) {
            return new AngleOrientation(angle);
        }
        return AngleOrientation.DEFAULT;
    }

    public abstract float orientationFor(@NotNull MarkerType var1, float var2, float var3, float var4, float var5);

    private static final class AngleOrientation
    extends MarkerOrientation {
        @NotNull
        private static final AngleOrientation DEFAULT = new AngleOrientation(Angle.ZERO);
        @NotNull
        private final Angle angle;

        private AngleOrientation(@NotNull Angle angle) {
            this.angle = angle;
        }

        @Override
        public float orientationFor(@NotNull MarkerType type, float dxIn, float dyIn, float dxOut, float dyOut) {
            return this.angle.radians();
        }
    }

    private static final class AutoOrientation
    extends MarkerOrientation {
        @NotNull
        private static final AutoOrientation INSTANCE = new AutoOrientation();

        private AutoOrientation() {
        }

        @Override
        public float orientationFor(@NotNull MarkerType type, float dxIn, float dyIn, float dxOut, float dyOut) {
            switch (type.ordinal()) {
                case 0: {
                    return (float)Math.atan2(dyOut, dxOut);
                }
                case 2: {
                    return (float)Math.atan2(dyIn, dxIn);
                }
                case 1: {
                    return (float)Math.atan2((dyIn + dyOut) / 2.0f, (dxIn + dxOut) / 2.0f);
                }
            }
            throw new IllegalStateException();
        }
    }

    private static final class AutoStartReverseOrientation
    extends MarkerOrientation {
        @NotNull
        private static final AutoStartReverseOrientation INSTANCE = new AutoStartReverseOrientation();

        private AutoStartReverseOrientation() {
        }

        @Override
        public float orientationFor(@NotNull MarkerType type, float dxIn, float dyIn, float dxOut, float dyOut) {
            switch (type.ordinal()) {
                case 0: {
                    return (float)Math.atan2(-dyOut, -dxOut);
                }
                case 2: {
                    return (float)Math.atan2(dyIn, dxIn);
                }
                case 1: {
                    return (float)Math.atan2((dyIn + dyOut) / 2.0f, (dxIn + dxOut) / 2.0f);
                }
            }
            throw new IllegalStateException();
        }
    }

    public static enum MarkerType {
        START,
        MID,
        END;

    }
}

