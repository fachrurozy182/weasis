/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.op.lut.DefaultWlPresentation
 *  org.weasis.opencv.op.lut.LutShape
 *  org.weasis.opencv.op.lut.PresentationStateLut
 *  org.weasis.opencv.op.lut.WlParams
 *  org.weasis.opencv.op.lut.WlPresentation
 */
package org.dcm4che3.img.lut;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import org.dcm4che3.img.DicomImageAdapter;
import org.dcm4che3.img.DicomImageReadParam;
import org.dcm4che3.img.data.PrDicomObject;
import org.weasis.opencv.op.lut.DefaultWlPresentation;
import org.weasis.opencv.op.lut.LutShape;
import org.weasis.opencv.op.lut.PresentationStateLut;
import org.weasis.opencv.op.lut.WlParams;
import org.weasis.opencv.op.lut.WlPresentation;

public final class WindLevelParameters
implements WlParams {
    private final double window;
    private final double level;
    private final double levelMin;
    private final double levelMax;
    private final boolean pixelPadding;
    private final boolean inverseLut;
    private final boolean fillOutsideLutRange;
    private final boolean allowWinLevelOnColorImage;
    private final LutShape lutShape;
    private final PrDicomObject dcmPR;

    public WindLevelParameters(DicomImageAdapter adapter) {
        this(adapter, null);
    }

    public WindLevelParameters(DicomImageAdapter adapter, DicomImageReadParam params) {
        Objects.requireNonNull(adapter, "DicomImageAdapter cannot be null");
        this.dcmPR = params != null ? (PrDicomObject)params.getPresentationState().orElse(null) : null;
        this.pixelPadding = this.extractBooleanParam(params, DicomImageReadParam::getApplyPixelPadding, true);
        DefaultWlPresentation def = new DefaultWlPresentation((PresentationStateLut)this.dcmPR, this.pixelPadding);
        this.fillOutsideLutRange = this.extractBooleanParam(params, DicomImageReadParam::getFillOutsideLutRange, false);
        this.allowWinLevelOnColorImage = this.extractBooleanParam(params, DicomImageReadParam::getApplyWindowLevelToColorImage, false);
        this.inverseLut = this.extractBooleanParam(params, DicomImageReadParam::getInverseLut, false);
        this.window = this.validateAndExtractDoubleParam(params, DicomImageReadParam::getWindowWidth, () -> adapter.getDefaultWindow((WlPresentation)def), 1.0, false);
        this.level = this.validateAndExtractDoubleParam(params, DicomImageReadParam::getWindowCenter, () -> adapter.getDefaultLevel((WlPresentation)def), 0.0, true);
        this.lutShape = this.extractParam(params, DicomImageReadParam::getVoiLutShape, () -> adapter.getDefaultShape((WlPresentation)def));
        double halfWindow = this.window / 2.0;
        double calculatedMin = this.level - halfWindow;
        double calculatedMax = this.level + halfWindow;
        double paramMin = params != null ? params.getLevelMin().orElse(calculatedMin) : calculatedMin;
        double paramMax = params != null ? params.getLevelMax().orElse(calculatedMax) : calculatedMax;
        this.levelMin = this.validateDouble(Math.min(paramMin, adapter.getMinValue((WlPresentation)def)), 0.0, true);
        this.levelMax = this.validateDouble(Math.max(paramMax, adapter.getMaxValue((WlPresentation)def)), 255.0, true);
    }

    private double validateAndExtractDoubleParam(DicomImageReadParam params, Function<DicomImageReadParam, OptionalDouble> extractor, DoubleSupplier defaultSupplier, double defaultValue, boolean allowNegative) {
        OptionalDouble optional;
        double value = params != null ? ((optional = extractor.apply(params)).isPresent() ? optional.getAsDouble() : defaultSupplier.getAsDouble()) : defaultSupplier.getAsDouble();
        return this.validateDouble(value, defaultValue, allowNegative);
    }

    private double validateDouble(double value, double fallback, boolean allowNegative) {
        if (!Double.isFinite(value)) {
            return fallback;
        }
        if (!allowNegative && value <= 1.0E-9) {
            return fallback;
        }
        return value;
    }

    private boolean extractBooleanParam(DicomImageReadParam params, Function<DicomImageReadParam, Optional<Boolean>> extractor, boolean defaultValue) {
        return params != null ? extractor.apply(params).orElse(defaultValue) : defaultValue;
    }

    private <T> T extractParam(DicomImageReadParam params, Function<DicomImageReadParam, Optional<T>> extractor, Supplier<T> defaultSupplier) {
        return params != null ? extractor.apply(params).orElseGet(defaultSupplier) : defaultSupplier.get();
    }

    public double getWindow() {
        return this.window;
    }

    public double getLevel() {
        return this.level;
    }

    public double getLevelMin() {
        return this.levelMin;
    }

    public double getLevelMax() {
        return this.levelMax;
    }

    public boolean isPixelPadding() {
        return this.pixelPadding;
    }

    public boolean isInverseLut() {
        return this.inverseLut;
    }

    public boolean isFillOutsideLutRange() {
        return this.fillOutsideLutRange;
    }

    public boolean isAllowWinLevelOnColorImage() {
        return this.allowWinLevelOnColorImage;
    }

    public LutShape getLutShape() {
        return this.lutShape;
    }

    public PrDicomObject getPresentationState() {
        return this.dcmPR;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof WindLevelParameters)) return false;
        WindLevelParameters other = (WindLevelParameters)obj;
        if (Double.compare(this.window, other.window) != 0) return false;
        if (Double.compare(this.level, other.level) != 0) return false;
        if (Double.compare(this.levelMin, other.levelMin) != 0) return false;
        if (Double.compare(this.levelMax, other.levelMax) != 0) return false;
        if (this.pixelPadding != other.pixelPadding) return false;
        if (this.inverseLut != other.inverseLut) return false;
        if (this.fillOutsideLutRange != other.fillOutsideLutRange) return false;
        if (this.allowWinLevelOnColorImage != other.allowWinLevelOnColorImage) return false;
        if (!Objects.equals(this.lutShape, other.lutShape)) return false;
        if (!Objects.equals(this.dcmPR, other.dcmPR)) return false;
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.window, this.level, this.levelMin, this.levelMax, this.pixelPadding, this.inverseLut, this.fillOutsideLutRange, this.allowWinLevelOnColorImage, this.lutShape, this.dcmPR);
    }

    public String toString() {
        return "WindLevelParameters{window=%.2f, level=%.2f, levelMin=%.2f, levelMax=%.2f, pixelPadding=%s, inverseLut=%s, fillOutsideLutRange=%s, allowWinLevelOnColorImage=%s, lutShape=%s, dcmPR=%s}".formatted(new Object[]{this.window, this.level, this.levelMin, this.levelMax, this.pixelPadding, this.inverseLut, this.fillOutsideLutRange, this.allowWinLevelOnColorImage, this.lutShape, this.dcmPR});
    }
}

