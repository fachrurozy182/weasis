/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.image.measure;

import java.util.Objects;

public record MeasurementsAdapter(double calibrationRatio, int offsetX, int offsetY, boolean upYAxis, int imageHeight, String unit) {
    private static final double DEFAULT_CALIBRATION_RATIO = 1.0;
    private static final String DEFAULT_UNIT = "px";

    public MeasurementsAdapter {
        if (Double.isNaN(calibrationRatio) || Double.isInfinite(calibrationRatio) || calibrationRatio <= 0.0) {
            throw new IllegalArgumentException("Calibration ratio must be a positive finite number");
        }
        imageHeight = Math.max(0, imageHeight - 1);
        unit = Objects.requireNonNullElse(unit, DEFAULT_UNIT);
    }

    public double getXUncalibratedValue(double xVal) {
        return xVal + (double)this.offsetX;
    }

    public double getYUncalibratedValue(double yVal) {
        return this.transformY(yVal) + (double)this.offsetY;
    }

    public double getXCalibratedValue(double xVal) {
        return this.calibrationRatio * this.getXUncalibratedValue(xVal);
    }

    public double getYCalibratedValue(double yVal) {
        return this.calibrationRatio * this.getYUncalibratedValue(yVal);
    }

    public boolean isCalibrated() {
        return Double.compare(this.calibrationRatio, 1.0) != 0;
    }

    private double transformY(double yVal) {
        return this.upYAxis ? (double)this.imageHeight - yVal : yVal;
    }

    public String toString() {
        return "MeasurementsAdapter{ratio=%.3f, unit='%s', offsetX=%d, offsetY=%d, upYAxis=%s, imageHeight=%d}".formatted(new Object[]{this.calibrationRatio, this.unit, this.offsetX, this.offsetY, this.upYAxis, this.imageHeight});
    }
}

