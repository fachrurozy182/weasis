/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes.transform;

import com.github.weisj.jsvg.attributes.HasMatchName;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.util.GeometryUtil;
import com.github.weisj.jsvg.renderer.MeasureContext;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class TransformPart {
    private static final TransformPart IDENTITY_MATRIX = new TransformPart(TransformType.MATRIX, new Length[]{Length.ONE, Length.ZERO, Length.ZERO, Length.ONE, Length.ZERO, Length.ZERO});
    private static final TransformPart IDENTITY_TRANSLATE = new TransformPart(TransformType.TRANSLATE, new Length[]{Length.ZERO, Length.ZERO});
    private static final TransformPart IDENTITY_SCALE = new TransformPart(TransformType.SCALE, new Length[]{Length.ONE, Length.ONE});
    private static final TransformPart IDENTITY_ROTATE = new TransformPart(TransformType.ROTATE, new Length[]{Length.ZERO, Length.ZERO, Length.ZERO});
    private static final TransformPart IDENTITY_SKEW = new TransformPart(TransformType.ROTATE, new Length[]{Length.ZERO, Length.ZERO});
    private final TransformType type;
    private final Length[] values;

    public TransformPart(TransformType type, @NotNull @NotNull Length @NotNull [] values) {
        this.type = type;
        this.values = values;
    }

    @NotNull
    public static TransformPart identityOfType(@NotNull TransformType type) {
        switch (type.ordinal()) {
            case 0: {
                return IDENTITY_MATRIX;
            }
            case 1: 
            case 2: 
            case 3: {
                return IDENTITY_TRANSLATE;
            }
            case 4: 
            case 5: 
            case 6: {
                return IDENTITY_SCALE;
            }
            case 7: {
                return IDENTITY_ROTATE;
            }
            case 8: 
            case 9: 
            case 10: {
                return IDENTITY_SKEW;
            }
        }
        throw new IllegalArgumentException("Unknown transform type: " + type);
    }

    private static float getEntry(@NotNull TransformPart part, int index, float fallback, @NotNull MeasureContext context) {
        if (part.values.length > index) {
            return part.values[index].resolve(context);
        }
        return fallback;
    }

    @NotNull
    public static AffineTransform interpolate(@NotNull TransformPart a, @NotNull TransformPart b, @NotNull MeasureContext measureContext, float t) {
        TransformType bType;
        TransformType aType = a.type.interpolationType();
        if (aType != (bType = b.type.interpolationType())) {
            return GeometryUtil.interpolate(a.toTransform(measureContext), b.toTransform(measureContext), t);
        }
        switch (aType.ordinal()) {
            case 0: {
                return GeometryUtil.interpolate(a.toTransform(measureContext), b.toTransform(measureContext), t);
            }
            case 1: 
            case 2: 
            case 3: {
                return AffineTransform.getTranslateInstance(GeometryUtil.lerp(t, a.values[0].resolve(measureContext), b.values[0].resolve(measureContext)), GeometryUtil.lerp(t, TransformPart.getEntry(a, 1, 0.0f, measureContext), TransformPart.getEntry(b, 1, 0.0f, measureContext)));
            }
            case 4: 
            case 5: 
            case 6: {
                float aScaleX = a.values[0].resolve(measureContext);
                float aScaleY = TransformPart.getEntry(a, 1, aScaleX, measureContext);
                float bScaleX = b.values[0].resolve(measureContext);
                float bScaleY = TransformPart.getEntry(a, 1, bScaleX, measureContext);
                return AffineTransform.getScaleInstance(GeometryUtil.lerp(t, aScaleX, bScaleX), GeometryUtil.lerp(t, aScaleY, bScaleY));
            }
            case 7: {
                return AffineTransform.getRotateInstance(Math.toRadians(GeometryUtil.lerp(t, a.values[0].resolve(measureContext), b.values[0].resolve(measureContext))), GeometryUtil.lerp(t, TransformPart.getEntry(a, 1, 0.0f, measureContext), TransformPart.getEntry(b, 1, 0.0f, measureContext)), GeometryUtil.lerp(t, TransformPart.getEntry(a, 2, 0.0f, measureContext), TransformPart.getEntry(b, 2, 0.0f, measureContext)));
            }
            case 8: 
            case 9: 
            case 10: {
                return AffineTransform.getShearInstance(Math.tan(Math.toRadians(GeometryUtil.lerp(t, a.values[0].resolve(measureContext), b.values[0].resolve(measureContext)))), Math.tan(Math.toRadians(GeometryUtil.lerp(t, TransformPart.getEntry(a, 1, 0.0f, measureContext), TransformPart.getEntry(b, 1, 0.0f, measureContext)))));
            }
        }
        throw new IllegalStateException();
    }

    public boolean canBeFlattened() {
        for (Length value : this.values) {
            if (value.isAbsolute()) continue;
            return false;
        }
        return true;
    }

    @Contract(value="_ -> new", pure=true)
    @NotNull
    public AffineTransform toTransform(@NotNull MeasureContext measureContext) {
        return this.applyToTransform(new AffineTransform(), measureContext);
    }

    @Contract(value="_,_ -> param1", pure=true)
    @NotNull
    public AffineTransform applyToTransform(@NotNull AffineTransform transform, @NotNull MeasureContext measureContext) {
        return this.applyToTransform(transform, measureContext, 1.0f);
    }

    @Contract(value="_,_,_ -> param1", pure=true)
    @NotNull
    public AffineTransform applyToTransform(@NotNull AffineTransform transform, @NotNull MeasureContext measureContext, float progress) {
        switch (this.type.ordinal()) {
            case 0: {
                transform.concatenate(new AffineTransform(this.values[0].resolve(measureContext), this.values[1].resolve(measureContext), this.values[2].resolve(measureContext), this.values[3].resolve(measureContext), this.values[4].resolve(measureContext), this.values[5].resolve(measureContext)));
                break;
            }
            case 1: {
                transform.translate(progress * this.values[0].resolve(measureContext), progress * this.values[1].resolve(measureContext));
                break;
            }
            case 2: {
                transform.translate(this.values[0].resolve(measureContext), 0.0);
                break;
            }
            case 3: {
                transform.translate(0.0, this.values[0].resolve(measureContext));
                break;
            }
            case 4: {
                if (this.values.length == 1) {
                    float scale = this.values[0].resolve(measureContext);
                    transform.scale(scale, scale);
                    break;
                }
                transform.scale(this.values[0].resolve(measureContext), this.values[1].resolve(measureContext));
                break;
            }
            case 5: {
                transform.scale(this.values[0].resolve(measureContext), 1.0);
                break;
            }
            case 6: {
                transform.scale(1.0, this.values[0].resolve(measureContext));
                break;
            }
            case 7: {
                if (this.values.length == 1) {
                    transform.rotate(Math.toRadians(this.values[0].resolve(measureContext)));
                    break;
                }
                transform.rotate(Math.toRadians(this.values[0].resolve(measureContext)), this.values[1].resolve(measureContext), this.values[2].resolve(measureContext));
                break;
            }
            case 8: {
                if (this.values.length == 1) {
                    transform.shear(Math.tan(Math.toRadians(this.values[0].resolve(measureContext))), 0.0);
                    break;
                }
                transform.shear(Math.tan(Math.toRadians(this.values[0].resolve(measureContext))), Math.tan(Math.toRadians(this.values[1].resolve(measureContext))));
                break;
            }
            case 9: {
                transform.shear(Math.tan(Math.toRadians(this.values[0].resolve(measureContext))), 0.0);
                break;
            }
            case 10: {
                transform.shear(0.0, Math.tan(Math.toRadians(this.values[0].resolve(measureContext))));
            }
        }
        return transform;
    }

    public String toString() {
        return "TransformPart{type=" + this.type + ", values=" + Arrays.toString(this.values) + '}';
    }

    public static enum TransformType implements HasMatchName
    {
        MATRIX,
        TRANSLATE,
        TRANSLATE_X("translateX"),
        TRANSLATE_Y("translateY"),
        SCALE,
        SCALE_X("scaleX"),
        SCALE_Y("scaleY"),
        ROTATE,
        SKEW,
        SKEW_X("skewX"),
        SKEW_Y("skewY");

        @NotNull
        private final String matchName;

        private TransformType(String matchName) {
            this.matchName = matchName;
        }

        private TransformType() {
            this.matchName = this.name();
        }

        @Override
        @NotNull
        public String matchName() {
            return this.matchName;
        }

        TransformType interpolationType() {
            switch (this.ordinal()) {
                case 2: 
                case 3: {
                    return TRANSLATE;
                }
                case 5: 
                case 6: {
                    return SCALE;
                }
                case 9: 
                case 10: {
                    return SKEW;
                }
            }
            return this;
        }
    }
}

