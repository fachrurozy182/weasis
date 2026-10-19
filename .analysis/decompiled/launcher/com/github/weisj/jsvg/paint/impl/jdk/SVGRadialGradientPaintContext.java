/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.paint.impl.jdk;

import com.github.weisj.jsvg.paint.impl.jdk.SVGMultipleGradientPaintContext;
import com.github.weisj.jsvg.paint.impl.jdk.SVGRadialGradientPaint;
import java.awt.Color;
import java.awt.MultipleGradientPaint;
import java.awt.geom.AffineTransform;
import org.jetbrains.annotations.NotNull;

final class SVGRadialGradientPaintContext
extends SVGMultipleGradientPaintContext {
    private final boolean isSimpleFocus;
    private final boolean isNonCyclic;
    private final float centerX;
    private final float centerY;
    private float focusX;
    private float focusY;
    private final float radiusSq;
    private final float focusRadius;
    private final float focusRadiusSq;
    private final float constA;
    private final float constB;
    private final float gDeltaDelta;
    private final float trivial;
    private static final float FOCUS_CLAMP_DOWNSCALE = 0.99f;
    private static final int SQRT_LUT_SIZE = 2048;
    private static final float[] sqrtLookup = new float[2049];

    SVGRadialGradientPaintContext(@NotNull SVGRadialGradientPaint paint, @NotNull AffineTransform t, float cx, float cy, float r, float fx, float fy, float fr, float @NotNull [] fractions, @NotNull @NotNull Color @NotNull [] colors, MultipleGradientPaint.CycleMethod cycleMethod, MultipleGradientPaint.ColorSpaceType colorSpace) {
        super(paint, t, fractions, colors, cycleMethod, colorSpace);
        this.centerX = cx;
        this.centerY = cy;
        this.focusX = fx;
        this.focusY = fy;
        this.isSimpleFocus = this.focusX == this.centerX && this.focusY == this.centerY && fr == 0.0f;
        this.isNonCyclic = cycleMethod == MultipleGradientPaint.CycleMethod.NO_CYCLE;
        this.radiusSq = r * r;
        this.focusRadius = fr;
        this.focusRadiusSq = fr * fr;
        float dX = this.focusX - this.centerX;
        float dY = this.focusY - this.centerY;
        double distSq = dX * dX + dY * dY;
        if (distSq > (double)(this.radiusSq * 0.99f)) {
            float scale = (float)Math.sqrt((double)(this.radiusSq * 0.99f) / distSq);
            this.focusX = this.centerX + (dX *= scale);
            this.focusY = this.centerY + (dY *= scale);
        }
        this.trivial = (float)Math.sqrt(this.radiusSq - dX * dX);
        this.constA = this.a02 - this.centerX;
        this.constB = this.a12 - this.centerY;
        this.gDeltaDelta = 2.0f * (this.a00 * this.a00 + this.a10 * this.a10) / this.radiusSq;
    }

    @Override
    protected void fillRaster(int[] pixels, int off, int adjust, int x, int y, int w, int h) {
        if (this.isSimpleFocus && this.isNonCyclic && this.isSimpleLookup) {
            this.simpleNonCyclicFillRaster(pixels, off, adjust, x, y, w, h);
        } else {
            this.cyclicCircularGradientFillRaster(pixels, off, adjust, x, y, w, h);
        }
    }

    private void simpleNonCyclicFillRaster(int[] pixels, int off, int adjust, int x, int y, int w, int h) {
        float rowX = this.a00 * (float)x + this.a01 * (float)y + this.constA;
        float rowY = this.a10 * (float)x + this.a11 * (float)y + this.constB;
        float deltaDelta = this.gDeltaDelta;
        adjust += w;
        int rgbclip = this.gradient[this.fastGradientArraySize];
        for (int j = 0; j < h; ++j) {
            int i;
            float gRel = (rowX * rowX + rowY * rowY) / this.radiusSq;
            float gDelta = 2.0f * (this.a00 * rowX + this.a10 * rowY) / this.radiusSq + deltaDelta / 2.0f;
            for (i = 0; i < w && gRel >= 1.0f; ++i) {
                pixels[off + i] = rgbclip;
                gRel += gDelta;
                gDelta += deltaDelta;
            }
            while (i < w && gRel < 1.0f) {
                int gIndex;
                if (gRel <= 0.0f) {
                    gIndex = 0;
                } else {
                    float fIndex = gRel * 2048.0f;
                    int iIndex = (int)fIndex;
                    float s0 = sqrtLookup[iIndex];
                    float s1 = sqrtLookup[iIndex + 1] - s0;
                    fIndex = s0 + (fIndex - (float)iIndex) * s1;
                    gIndex = (int)(fIndex * (float)this.fastGradientArraySize);
                }
                pixels[off + i] = this.gradient[gIndex];
                gRel += gDelta;
                gDelta += deltaDelta;
                ++i;
            }
            while (i < w) {
                pixels[off + i] = rgbclip;
                ++i;
            }
            off += adjust;
            rowX += this.a01;
            rowY += this.a11;
        }
    }

    private void cyclicCircularGradientFillRaster(int[] pixels, int off, int adjust, int x, int y, int w, int h) {
        double constC = -this.radiusSq + this.centerX * this.centerX + this.centerY * this.centerY;
        float constX = this.a00 * (float)x + this.a01 * (float)y + this.a02;
        float constY = this.a10 * (float)x + this.a11 * (float)y + this.a12;
        float precalc2 = 2.0f * this.centerY;
        float precalc3 = -2.0f * this.centerX;
        int indexer = off;
        int pixInc = w + adjust;
        for (int j = 0; j < h; ++j) {
            float userX = this.a01 * (float)j + constX;
            float userY = this.a11 * (float)j + constY;
            for (int i = 0; i < w; ++i) {
                int colorAtPoint;
                double solutionY;
                double solutionX;
                if (userX == this.focusX) {
                    solutionX = this.focusX;
                    solutionY = this.centerY;
                    solutionY += userY > this.focusY ? (double)this.trivial : (double)(-this.trivial);
                } else {
                    float slope = (userY - this.focusY) / (userX - this.focusX);
                    float yIntercept = userY - slope * userX;
                    double a = slope * slope + 1.0f;
                    double b = precalc3 + -2.0f * slope * (this.centerY - yIntercept);
                    double c = constC + (double)(yIntercept * (yIntercept - precalc2));
                    float det = (float)Math.sqrt(b * b - 4.0 * a * c);
                    solutionX = -b;
                    solutionX += userX < this.focusX ? (double)(-det) : (double)det;
                    solutionY = (double)slope * (solutionX /= 2.0 * a) + (double)yIntercept;
                }
                pixels[indexer + i] = colorAtPoint = this.getColorAtPoint(userX, userY, (float)solutionX, (float)solutionY);
                userX += this.a00;
                userY += this.a10;
            }
            indexer += pixInc;
        }
    }

    private int getColorAtPoint(float userX, float userY, float solutionX, float solutionY) {
        float currentToFocusSq = this.getCurrentToFocusSq(userX, userY);
        if (currentToFocusSq <= this.focusRadiusSq) {
            return this.indexIntoGradientsArrays(0.0f);
        }
        return this.getColorAtPointOutsideFocusCircle(solutionX, solutionY, currentToFocusSq);
    }

    private float getCurrentToFocusSq(float x, float y) {
        float deltaXSq = x - this.focusX;
        deltaXSq *= deltaXSq;
        float deltaYSq = y - this.focusY;
        deltaYSq *= deltaYSq;
        return deltaXSq + deltaYSq;
    }

    private int getColorAtPointOutsideFocusCircle(float solutionX, float solutionY, float currentToFocusSq) {
        float intersectToFocusSq = this.getCurrentToFocusSq(solutionX, solutionY);
        float gradientPosition = this.computeGradientPosition(currentToFocusSq, intersectToFocusSq);
        return this.indexIntoGradientsArrays(gradientPosition);
    }

    private float computeGradientPosition(float currentToFocusSq, float intersectToFocusSq) {
        float gradientPosition;
        if (this.focusRadius > 0.0f) {
            float currentToFocus = (float)Math.sqrt(currentToFocusSq);
            float intersectToFocus = (float)Math.sqrt(intersectToFocusSq);
            gradientPosition = (currentToFocus - this.focusRadius) / (intersectToFocus - this.focusRadius);
        } else {
            gradientPosition = (float)Math.sqrt(currentToFocusSq / intersectToFocusSq);
        }
        return gradientPosition;
    }

    static {
        for (int i = 0; i < sqrtLookup.length; ++i) {
            SVGRadialGradientPaintContext.sqrtLookup[i] = (float)Math.sqrt((float)i / 2048.0f);
        }
    }
}

