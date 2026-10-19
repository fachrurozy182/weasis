/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.animation.interpolation;

import com.github.weisj.jsvg.animation.Additive;
import com.github.weisj.jsvg.animation.AnimationValuesType;
import com.github.weisj.jsvg.animation.interpolation.FloatInterpolator;
import com.github.weisj.jsvg.animation.interpolation.FloatListInterpolator;
import com.github.weisj.jsvg.animation.interpolation.PaintInterpolator;
import com.github.weisj.jsvg.animation.interpolation.TransformInterpolator;
import com.github.weisj.jsvg.attributes.transform.TransformPart;
import com.github.weisj.jsvg.attributes.value.TransformValue;
import com.github.weisj.jsvg.geometry.util.GeometryUtil;
import com.github.weisj.jsvg.paint.SVGPaint;
import com.github.weisj.jsvg.paint.SimplePaintSVGPaint;
import com.github.weisj.jsvg.paint.impl.AwtSVGPaint;
import com.github.weisj.jsvg.paint.impl.RGBColor;
import com.github.weisj.jsvg.renderer.MeasureContext;
import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DefaultInterpolator
implements FloatInterpolator,
FloatListInterpolator,
PaintInterpolator,
TransformInterpolator {
    private final Additive additive;
    private final AnimationValuesType valuesType;

    public DefaultInterpolator(AnimationValuesType valuesType, Additive additive) {
        this.valuesType = valuesType;
        this.additive = additive;
    }

    @Override
    public float interpolate(float initial, float a, float b, float progress) {
        switch (this.valuesType) {
            case FROM_BY: {
                return a + b * progress;
            }
            case BY: {
                return initial + b * progress;
            }
            case TO: {
                return initial + (b - initial) * progress;
            }
        }
        float result = a + (b - a) * progress;
        if (this.additive == Additive.SUM) {
            result += initial;
        }
        return result;
    }

    private static float @NotNull [] ensureCacheCapacity(float @Nullable [] cache, int length) {
        if (cache == null || cache.length != length) {
            return new float[length];
        }
        return cache;
    }

    private static float @NotNull [] arrayLerp(float @Nullable [] cache, float @NotNull [] from, float @Nullable [] to, float progress) {
        float t = progress;
        boolean isEndTime = GeometryUtil.approximatelyEqual(t, 1.0);
        if (to == null || from.length != to.length && !isEndTime) {
            t = 0.0f;
        }
        float[] result = cache;
        if (to == null || GeometryUtil.approximatelyEqual(t, 0.0)) {
            result = DefaultInterpolator.ensureCacheCapacity(result, from.length);
            System.arraycopy(from, 0, result, 0, from.length);
        } else if (isEndTime) {
            result = DefaultInterpolator.ensureCacheCapacity(result, to.length);
            System.arraycopy(to, 0, result, 0, to.length);
        } else {
            result = DefaultInterpolator.ensureCacheCapacity(result, from.length);
            for (int i = 0; i < from.length; ++i) {
                result[i] = GeometryUtil.lerp(t, from[i], to[i]);
            }
        }
        return result;
    }

    private static float @NotNull [] saxpy(float @Nullable [] cache, float @NotNull [] b, float @Nullable [] x, float a) {
        int i;
        float[] result = DefaultInterpolator.ensureCacheCapacity(cache, b.length);
        System.arraycopy(b, 0, result, 0, b.length);
        if (x == null) {
            return b;
        }
        int n = Math.min(result.length, x.length);
        for (i = 0; i < n; ++i) {
            int n2 = i;
            result[n2] = result[n2] + a * x[i];
        }
        for (i = n; i < result.length; ++i) {
            int n3 = i;
            result[n3] = result[n3] + a * x[i % n];
        }
        return result;
    }

    @Override
    public float @NotNull [] interpolate(float @NotNull [] initial, float @NotNull [] a, float @Nullable [] b, float progress, float @Nullable [] cache) {
        switch (this.valuesType) {
            case FROM_BY: {
                return DefaultInterpolator.saxpy(cache, a, b, progress);
            }
            case BY: {
                return DefaultInterpolator.saxpy(cache, initial, b, progress);
            }
            case TO: {
                return DefaultInterpolator.arrayLerp(cache, initial, b, progress);
            }
        }
        float[] result = DefaultInterpolator.arrayLerp(cache, a, b, progress);
        if (this.additive == Additive.SUM) {
            result = DefaultInterpolator.saxpy(result, result, initial, 1.0f);
        }
        return result;
    }

    @Nullable
    private static RGBColor extractColor(@NotNull SVGPaint p) {
        if (!(p instanceof SimplePaintSVGPaint)) {
            return null;
        }
        Paint paint = ((SimplePaintSVGPaint)p).paint();
        if (paint instanceof Color) {
            return new RGBColor((Color)paint);
        }
        if (paint instanceof RGBColor) {
            return (RGBColor)paint;
        }
        return null;
    }

    @Override
    @NotNull
    public SVGPaint interpolate(@NotNull SVGPaint initial, @NotNull SVGPaint a, @NotNull SVGPaint b, float progress) {
        RGBColor colorA = DefaultInterpolator.extractColor(a);
        RGBColor colorB = DefaultInterpolator.extractColor(b);
        if (colorA == null || colorB == null) {
            return this.discreteAnimation(initial, a, b, progress);
        }
        switch (this.valuesType) {
            case FROM_BY: {
                return new AwtSVGPaint(RGBColor.saxpy(progress, colorA, colorB));
            }
            case BY: {
                RGBColor initialColor = DefaultInterpolator.extractColor(initial);
                if (initialColor == null) {
                    return initial;
                }
                return new AwtSVGPaint(RGBColor.saxpy(progress, initialColor, colorB));
            }
            case TO: {
                RGBColor initialColor = DefaultInterpolator.extractColor(initial);
                if (initialColor == null) {
                    return initial;
                }
                return new AwtSVGPaint(RGBColor.interpolate(progress, initialColor, colorB));
            }
        }
        RGBColor result = RGBColor.interpolate(progress, colorA, colorB);
        if (this.additive == Additive.SUM) {
            RGBColor initialColor = DefaultInterpolator.extractColor(initial);
            if (initialColor == null) {
                return initial;
            }
            result = RGBColor.add(initialColor, result);
        }
        return new AwtSVGPaint(result);
    }

    private SVGPaint discreteAnimation(@NotNull SVGPaint initial, @NotNull SVGPaint a, @NotNull SVGPaint b, float progress) {
        if (this.additive != Additive.REPLACE) {
            return initial;
        }
        return GeometryUtil.approximatelyEqual(progress, 1.0) ? b : a;
    }

    @Override
    @NotNull
    public AffineTransform interpolate(@NotNull MeasureContext context, @NotNull TransformValue initial, @NotNull TransformPart a, @NotNull TransformPart b, float progress) {
        switch (this.valuesType) {
            case FROM_BY: {
                return b.applyToTransform(a.applyToTransform(new AffineTransform(), context), context, progress);
            }
            case BY: {
                return b.applyToTransform(initial.get(context), context, progress);
            }
            case TO: {
                return GeometryUtil.interpolate(initial.get(context), b.toTransform(context), progress);
            }
        }
        AffineTransform result = TransformPart.interpolate(a, b, context, progress);
        if (this.additive == Additive.SUM) {
            result.preConcatenate(initial.get(context));
        }
        return result;
    }
}

