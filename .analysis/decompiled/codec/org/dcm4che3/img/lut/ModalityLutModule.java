/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.LangUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.core.util.annotations.Generated
 *  org.weasis.opencv.data.LookupTableCV
 */
package org.dcm4che3.img.lut;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.util.DicomAttributeUtils;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.img.util.LookupTableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.LangUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.core.util.annotations.Generated;
import org.weasis.opencv.data.LookupTableCV;

public final class ModalityLutModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModalityLutModule.class);
    private static final Set<String> SPECIAL_MODALITIES = Set.of("XA", "XRF");
    private static final Set<String> RESTRICTED_PIXEL_INTENSITIES = Set.of("LOG", "DISP");
    private static final String DEFAULT_RESCALE_TYPE = "US";
    private static final double DEFAULT_RESCALE_INTERCEPT = 0.0;
    private static final double DEFAULT_RESCALE_SLOPE = 1.0;
    private final boolean overlayBitMaskApplied;
    private final Double rescaleSlope;
    private final Double rescaleIntercept;
    private final String rescaleType;
    private final String lutType;
    private final String lutExplanation;
    private final LookupTableCV lut;
    private final boolean isReset;

    public ModalityLutModule(Attributes dcm) {
        Objects.requireNonNull(dcm, "DICOM attributes cannot be null");
        InitializationData initialization = this.initializeFromDicom(dcm);
        this.isReset = false;
        this.overlayBitMaskApplied = false;
        this.rescaleSlope = initialization.rescaleSlope();
        this.rescaleIntercept = initialization.rescaleIntercept();
        this.rescaleType = initialization.rescaleType();
        this.lutType = initialization.lutData.lutType();
        this.lutExplanation = initialization.lutData.lutExplanation();
        this.lut = initialization.lutData.lut();
    }

    private ModalityLutModule(boolean isReset, boolean overlayBitMaskApplied, Double rescaleSlope, Double rescaleIntercept, String rescaleType, String lutType, String lutExplanation, LookupTableCV lut) {
        this.isReset = isReset;
        this.overlayBitMaskApplied = overlayBitMaskApplied;
        this.rescaleSlope = rescaleSlope;
        this.rescaleIntercept = rescaleIntercept;
        this.rescaleType = rescaleType;
        this.lutType = lutType;
        this.lutExplanation = lutExplanation;
        this.lut = lut;
    }

    private InitializationData initializeFromDicom(Attributes dcm) {
        String modality = DicomAttributeUtils.getModality(dcm);
        Double slope = DicomUtils.getDoubleFromDicomElement(dcm, 2625619, null);
        Double intercept = DicomUtils.getDoubleFromDicomElement(dcm, 2625618, null);
        String type = dcm.getString(2625620);
        LutData lutData = this.initializeLutIfApplicable(dcm, modality);
        this.logComplianceWarnings(intercept, lutData.lut());
        return new InitializationData(slope, intercept, type, lutData);
    }

    private LutData initializeLutIfApplicable(Attributes dcm, String modality) {
        Attributes lutAttributes = dcm.getNestedDataset(2633728);
        if (this.isValidLutAttributes(lutAttributes) && this.canApplyModalityLUT(dcm, modality)) {
            String type = lutAttributes.getString(2633732);
            String explanation = lutAttributes.getString(2633731);
            LookupTableCV lookupTable = LookupTableUtils.createLut(lutAttributes).orElse(null);
            return new LutData(type, explanation, lookupTable);
        }
        return new LutData(null, null, null);
    }

    private boolean isValidLutAttributes(Attributes lutAttributes) {
        return lutAttributes != null && lutAttributes.containsValue(2633732) && lutAttributes.containsValue(2633730) && lutAttributes.containsValue(2633734);
    }

    private boolean canApplyModalityLUT(Attributes dcm, String modality) {
        if (StringUtil.hasText((CharSequence)modality) && !SPECIAL_MODALITIES.contains(modality)) {
            return true;
        }
        String pixelIntensityRelationship = dcm.getString(2625600);
        return pixelIntensityRelationship == null || !RESTRICTED_PIXEL_INTENSITIES.contains(pixelIntensityRelationship.toUpperCase(Locale.ROOT));
    }

    private void logComplianceWarnings(Double intercept, LookupTableCV lutTable) {
        if (intercept != null && lutTable != null) {
            LOGGER.warn("Either a Modality LUT Sequence or Rescale Slope and Intercept values shall be present but not both!");
        }
        this.logTraceInfo(intercept, lutTable);
    }

    @Generated
    private void logTraceInfo(Double intercept, LookupTableCV lutTable) {
        if (LOGGER.isTraceEnabled()) {
            if (lutTable != null) {
                if (intercept != null) {
                    LOGGER.trace("Modality LUT Sequence shall NOT be present if Rescale Intercept is present");
                }
                if (this.lutType == null) {
                    LOGGER.trace("Modality Type is required if Modality LUT Sequence is present.");
                }
            } else if (intercept != null && this.rescaleSlope == null) {
                LOGGER.trace("Modality Rescale Slope is required if Rescale Intercept is present.");
            }
        }
    }

    public OptionalDouble getRescaleSlope() {
        return LangUtil.toOptional((Double)this.rescaleSlope);
    }

    public OptionalDouble getRescaleIntercept() {
        return LangUtil.toOptional((Double)this.rescaleIntercept);
    }

    public Optional<String> getRescaleType() {
        return Optional.ofNullable(this.rescaleType);
    }

    public Optional<String> getLutType() {
        return Optional.ofNullable(this.lutType);
    }

    public Optional<String> getLutExplanation() {
        return Optional.ofNullable(this.lutExplanation);
    }

    public Optional<LookupTableCV> getLut() {
        return Optional.ofNullable(this.lut);
    }

    public ModalityLutModule withOverlayBitMask(int shiftHighBit) {
        if (this.overlayBitMaskApplied) {
            return this;
        }
        double baseSlope = this.getRescaleSlope().orElse(1.0);
        double adjustedSlope = baseSlope / (double)(1 << shiftHighBit);
        DefaultValues defaults = this.calculateDefaults();
        return new ModalityLutModule(this.isReset, true, adjustedSlope, defaults.intercept(), defaults.type(), this.lutType, this.lutExplanation, this.lut);
    }

    private DefaultValues calculateDefaults() {
        Double finalIntercept = this.rescaleSlope == null && this.rescaleIntercept == null ? 0.0 : this.rescaleIntercept;
        String finalType = this.rescaleSlope == null && this.rescaleType == null ? DEFAULT_RESCALE_TYPE : this.rescaleType;
        return new DefaultValues(finalIntercept, finalType);
    }

    public boolean isOverlayBitMaskApplied() {
        return this.overlayBitMaskApplied;
    }

    public boolean isReset() {
        return this.isReset;
    }

    public static ModalityLutModule getResetInstance() {
        return ModalityLutModule.getResetInstance(null, null, null);
    }

    public static ModalityLutModule getResetInstance(Double rescaleSlope, Double rescaleIntercept, String rescaleType) {
        return new ModalityLutModule(true, false, rescaleSlope, rescaleIntercept, rescaleType, null, null, null);
    }

    private record InitializationData(Double rescaleSlope, Double rescaleIntercept, String rescaleType, LutData lutData) {
    }

    private record LutData(String lutType, String lutExplanation, LookupTableCV lut) {
    }

    private record DefaultValues(Double intercept, String type) {
    }
}

