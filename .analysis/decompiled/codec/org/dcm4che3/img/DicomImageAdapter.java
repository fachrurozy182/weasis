/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Core$MinMaxLocResult
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.MathUtil
 *  org.weasis.core.util.SoftHashMap
 *  org.weasis.opencv.data.LookupTableCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageAnalyzer
 *  org.weasis.opencv.op.lut.LutParameters
 *  org.weasis.opencv.op.lut.LutShape
 *  org.weasis.opencv.op.lut.PresentationStateLut
 *  org.weasis.opencv.op.lut.WlParams
 *  org.weasis.opencv.op.lut.WlPresentation
 */
package org.dcm4che3.img;

import java.awt.image.DataBufferUShort;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.img.data.PrDicomObject;
import org.dcm4che3.img.lut.ModalityLutModule;
import org.dcm4che3.img.lut.PresetWindowLevel;
import org.dcm4che3.img.stream.ImageDescriptor;
import org.dcm4che3.img.util.LookupTableUtils;
import org.dcm4che3.img.util.RescaleUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.MathUtil;
import org.weasis.core.util.SoftHashMap;
import org.weasis.opencv.data.LookupTableCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageAnalyzer;
import org.weasis.opencv.op.lut.LutParameters;
import org.weasis.opencv.op.lut.LutShape;
import org.weasis.opencv.op.lut.PresentationStateLut;
import org.weasis.opencv.op.lut.WlParams;
import org.weasis.opencv.op.lut.WlPresentation;

public class DicomImageAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomImageAdapter.class);
    private static final SoftHashMap<LutParameters, LookupTableCV> LUT_CACHE = new SoftHashMap();
    private final ImageDescriptor desc;
    private final Core.MinMaxLocResult minMax;
    private final int frameIndex;
    private int bitsStored;
    private List<PresetWindowLevel> windowingPresetCollection;

    public DicomImageAdapter(PlanarImage image, ImageDescriptor desc, int frameIndex) {
        int depth = CvType.depth((int)Objects.requireNonNull(image).type());
        this.desc = Objects.requireNonNull(desc);
        this.bitsStored = depth > 3 ? (int)image.elemSize1() * 8 : desc.getBitsStored();
        this.frameIndex = frameIndex;
        this.minMax = this.computeMinMaxValues(image, frameIndex);
        this.getModalityLookup(null, false);
    }

    public int getBitsStored() {
        return this.bitsStored;
    }

    public void setBitsStored(int bitsStored) {
        this.bitsStored = bitsStored;
    }

    public Core.MinMaxLocResult getMinMax() {
        return this.minMax;
    }

    public ImageDescriptor getImageDescriptor() {
        return this.desc;
    }

    public int getFrameIndex() {
        return this.frameIndex;
    }

    public int getMinAllocatedValue(WlPresentation wl) {
        boolean signed = this.isModalityLutOutSigned(wl);
        int bitsAllocated = this.desc.getBitsAllocated();
        int maxValue = signed ? (1 << bitsAllocated - 1) - 1 : (1 << bitsAllocated) - 1;
        return signed ? -(maxValue + 1) : 0;
    }

    public int getMaxAllocatedValue(WlPresentation wl) {
        boolean signed = this.isModalityLutOutSigned(wl);
        int bitsAllocated = this.desc.getBitsAllocated();
        return signed ? (1 << bitsAllocated - 1) - 1 : (1 << bitsAllocated) - 1;
    }

    public boolean isModalityLutOutSigned(WlPresentation wl) {
        return this.getMinValue(wl) < 0.0 || this.desc.isSigned();
    }

    public double getMinValue(WlPresentation wl) {
        return this.computeTransformedValue(true, wl);
    }

    public double getMaxValue(WlPresentation wl) {
        return this.computeTransformedValue(false, wl);
    }

    public double getRescaleIntercept(PrDicomObject dcm) {
        return this.getRescaleValue(dcm, ModalityLutModule::getRescaleIntercept, this.desc.getModalityLutForFrame(this.frameIndex).getRescaleIntercept(), 0.0);
    }

    public double getRescaleSlope(PrDicomObject dcm) {
        return this.getRescaleValue(dcm, ModalityLutModule::getRescaleSlope, this.desc.getModalityLutForFrame(this.frameIndex).getRescaleSlope(), 1.0);
    }

    private double getRescaleValue(PrDicomObject dcm, Function<ModalityLutModule, OptionalDouble> extractor, OptionalDouble descriptorValue, double defaultValue) {
        OptionalDouble prValue;
        if (dcm != null && (prValue = extractor.apply(dcm.getModalityLutModule())).isPresent()) {
            return prValue.getAsDouble();
        }
        return descriptorValue.orElse(defaultValue);
    }

    public double getFullDynamicWidth(WlPresentation wl) {
        return this.getMaxValue(wl) - this.getMinValue(wl);
    }

    public double getFullDynamicCenter(WlPresentation wl) {
        double minValue = this.getMinValue(wl);
        double maxValue = this.getMaxValue(wl);
        return minValue + (maxValue - minValue) / 2.0;
    }

    public PresetWindowLevel getDefaultPreset(WlPresentation wlp) {
        List<PresetWindowLevel> presetList = this.getPresetList(wlp);
        return presetList != null && !presetList.isEmpty() ? presetList.get(0) : null;
    }

    public synchronized List<PresetWindowLevel> getPresetList(WlPresentation wl) {
        return this.getPresetList(wl, false);
    }

    public synchronized List<PresetWindowLevel> getPresetList(WlPresentation wl, boolean reload) {
        if (this.minMax != null && (this.windowingPresetCollection == null || reload)) {
            this.windowingPresetCollection = PresetWindowLevel.getPresetCollection(this, "[DICOM]", wl);
        }
        return this.windowingPresetCollection;
    }

    public int getPresetCollectionSize() {
        return this.windowingPresetCollection != null ? this.windowingPresetCollection.size() : 0;
    }

    public LutShape getDefaultShape(WlPresentation wlp) {
        PresetWindowLevel defaultPreset = this.getDefaultPreset(wlp);
        return defaultPreset != null ? defaultPreset.getLutShape() : LutShape.LINEAR;
    }

    public double getDefaultWindow(WlPresentation wlp) {
        PresetWindowLevel defaultPreset = this.getDefaultPreset(wlp);
        if (defaultPreset != null) {
            return defaultPreset.getWindow();
        }
        return this.minMax != null ? this.minMax.maxVal - this.minMax.minVal : 0.0;
    }

    public double getDefaultLevel(WlPresentation wlp) {
        PresetWindowLevel defaultPreset = this.getDefaultPreset(wlp);
        if (defaultPreset != null) {
            return defaultPreset.getLevel();
        }
        if (this.minMax != null) {
            return this.minMax.minVal + (this.minMax.maxVal - this.minMax.minVal) / 2.0;
        }
        return 0.0;
    }

    public Number pixelToRealValue(Number pixelValue, WlPresentation wlp) {
        int val;
        if (pixelValue == null) {
            return null;
        }
        LookupTableCV lookup = this.getModalityLookup(wlp, false);
        if (lookup != null && this.isValueInLookupRange(val = pixelValue.intValue(), lookup)) {
            return lookup.lookup(0, val);
        }
        return pixelValue;
    }

    public LookupTableCV getModalityLookup(WlPresentation wlp, boolean inverseLUTAction) {
        if (this.desc.getModalityLutForFrame(this.frameIndex).isReset()) {
            return null;
        }
        Optional<Integer> paddingValue = this.desc.getPixelPaddingValue();
        boolean pixelPadding = wlp == null || wlp.isPixelPadding();
        PrDicomObject pr = this.extractPresentationState(wlp);
        LookupTableCV modalityLut = this.getModalityLutFromPresentationState(pr);
        if (modalityLut != null && this.canApplyModalityLut(modalityLut, pixelPadding, paddingValue)) {
            return modalityLut;
        }
        return this.createModalityLookup(pixelPadding, modalityLut, inverseLUTAction, pr);
    }

    public boolean isPhotometricInterpretationInverse(PresentationStateLut pr) {
        Optional prLUTShape = pr != null ? pr.getPrLutShapeMode() : Optional.empty();
        PhotometricInterpretation p = this.desc.getPhotometricInterpretation();
        return prLUTShape.map("INVERSE"::equals).orElseGet(() -> p == PhotometricInterpretation.MONOCHROME1);
    }

    public LutParameters getLutParameters(boolean pixelPadding, LookupTableCV mLUTSeq, boolean inversePaddingMLUT, PrDicomObject pr) {
        Optional<Integer> paddingValue = this.desc.getPixelPaddingValue();
        boolean isSigned = this.desc.isSigned();
        double intercept = this.getRescaleIntercept(pr);
        double slope = this.getRescaleSlope(pr);
        if (this.bitsStored > 16 || this.isIdentityTransformation(slope, intercept, paddingValue)) {
            return null;
        }
        Optional<Integer> paddingLimit = this.desc.getPixelPaddingRangeLimit();
        int bitsOutputLut = this.calculateOutputBits(mLUTSeq, slope, intercept);
        boolean outputSigned = this.calculateOutputSigned(mLUTSeq, slope, intercept, isSigned);
        if (bitsOutputLut <= 0 || bitsOutputLut > 32) {
            return null;
        }
        return new LutParameters(intercept, slope, pixelPadding, (Integer)paddingValue.orElse(null), (Integer)paddingLimit.orElse(null), this.bitsStored, isSigned, outputSigned, bitsOutputLut, inversePaddingMLUT);
    }

    public LookupTableCV getVOILookup(WlParams wl) {
        if (wl == null || wl.getLutShape() == null) {
            return null;
        }
        ValueRange valueRange = this.calculateVoiValueRange(wl);
        return LookupTableUtils.createVoiLut(wl.getLutShape(), wl.getWindow(), wl.getLevel(), valueRange.minValue(), valueRange.maxValue(), 8, false, this.isPhotometricInterpretationInverse(wl.getPresentationState()));
    }

    public static Core.MinMaxLocResult getMinMaxValues(PlanarImage image, ImageDescriptor desc, int frameIndex) {
        Core.MinMaxLocResult val = desc.getMinMaxPixelValue(frameIndex);
        if (val != null) {
            return val;
        }
        if (desc.getPhotometricInterpretation().isMonochrome()) {
            val = DicomImageAdapter.computeMinMaxWithPadding(image, desc);
        }
        return val != null ? val : ImageAnalyzer.findRawMinMaxValues((PlanarImage)image, (!desc.getPhotometricInterpretation().isMonochrome() ? 1 : 0) != 0);
    }

    private Core.MinMaxLocResult computeMinMaxValues(PlanarImage image, int frameIndex) {
        Core.MinMaxLocResult result = this.desc.getMinMaxPixelValue(frameIndex);
        if (result == null) {
            result = this.findMinMaxValues(image, frameIndex);
            this.desc.setMinMaxPixelValue(frameIndex, result);
        }
        return result;
    }

    private double computeTransformedValue(boolean isMin, WlPresentation wl) {
        Number min = this.pixelToRealValue(this.minMax.minVal, wl);
        Number max = this.pixelToRealValue(this.minMax.maxVal, wl);
        if (min == null || max == null) {
            return 0.0;
        }
        double minValue = min.doubleValue();
        double maxValue = max.doubleValue();
        return isMin ? Math.min(minValue, maxValue) : Math.max(minValue, maxValue);
    }

    private Core.MinMaxLocResult findMinMaxValues(PlanarImage image, int frameIndex) {
        Core.MinMaxLocResult val = DicomImageAdapter.getMinMaxValues(image, this.desc, frameIndex);
        this.validateAndAdjustBitsStored(val);
        return val;
    }

    private void validateAndAdjustBitsStored(Core.MinMaxLocResult val) {
        int bitsAllocated = this.desc.getBitsAllocated();
        if (this.bitsStored < bitsAllocated) {
            int maxInValue;
            boolean isSigned = this.desc.isSigned();
            int minInValue = isSigned ? -(1 << this.bitsStored - 1) : 0;
            int n = maxInValue = isSigned ? (1 << this.bitsStored - 1) - 1 : (1 << this.bitsStored) - 1;
            if (val.minVal < (double)minInValue || val.maxVal > (double)maxInValue) {
                this.setBitsStored(bitsAllocated);
            }
        }
    }

    private static Core.MinMaxLocResult computeMinMaxWithPadding(PlanarImage image, ImageDescriptor desc) {
        return desc.getPixelPaddingValue().map(paddingValue -> {
            Integer paddingLimit = desc.getPixelPaddingRangeLimit().orElse(null);
            int paddingMin = paddingLimit == null ? paddingValue : Math.min(paddingValue, paddingLimit);
            int paddingMax = paddingLimit == null ? paddingValue : Math.max(paddingValue, paddingLimit);
            return DicomImageAdapter.findMinMaxValues(image, paddingMin, paddingMax);
        }).orElse(null);
    }

    private static Core.MinMaxLocResult findMinMaxValues(PlanarImage image, Integer paddingValueMin, Integer paddingValueMax) {
        if (CvType.depth((int)image.type()) <= 1) {
            Core.MinMaxLocResult val = new Core.MinMaxLocResult();
            val.minVal = 0.0;
            val.maxVal = 255.0;
            return val;
        }
        Core.MinMaxLocResult val = ImageAnalyzer.findMinMaxValues((Mat)image.toMat(), (Integer)paddingValueMin, (Integer)paddingValueMax);
        if (val != null && val.minVal == val.maxVal) {
            val.maxVal += 1.0;
        }
        return val;
    }

    private boolean isValueInLookupRange(int val, LookupTableCV lookup) {
        return val >= lookup.getOffset() && val < lookup.getOffset() + lookup.getNumEntries();
    }

    private PrDicomObject extractPresentationState(WlPresentation wlp) {
        PrDicomObject pr;
        PresentationStateLut presentationStateLut;
        return wlp != null && (presentationStateLut = wlp.getPresentationState()) instanceof PrDicomObject ? (pr = (PrDicomObject)presentationStateLut) : null;
    }

    private LookupTableCV getModalityLutFromPresentationState(PrDicomObject pr) {
        LookupTableCV prModLut = pr != null ? (LookupTableCV)pr.getModalityLutModule().getLut().orElse(null) : null;
        return prModLut != null ? prModLut : (LookupTableCV)this.desc.getModalityLutForFrame(this.frameIndex).getLut().orElse(null);
    }

    private boolean canApplyModalityLut(LookupTableCV mLUTSeq, boolean pixelPadding, Optional<Integer> paddingValue) {
        if (!pixelPadding || paddingValue.isEmpty()) {
            return this.isMinMaxInLutRange(mLUTSeq);
        }
        LOGGER.warn("Cannot apply Modality LUT sequence and Pixel Padding");
        return false;
    }

    private boolean isMinMaxInLutRange(LookupTableCV mLUTSeq) {
        if (this.minMax.minVal >= (double)mLUTSeq.getOffset() && this.minMax.maxVal < (double)(mLUTSeq.getOffset() + mLUTSeq.getNumEntries())) {
            return true;
        }
        LOGGER.warn("Pixel values don't match Modality LUT sequence table. Modality LUT not applied.");
        return false;
    }

    private LookupTableCV createModalityLookup(boolean pixelPadding, LookupTableCV mLUTSeq, boolean inverseLUTAction, PrDicomObject pr) {
        LutParameters lutParams;
        boolean inverseLut = this.isPhotometricInterpretationInverse(pr);
        if (pixelPadding) {
            inverseLut ^= inverseLUTAction;
        }
        if ((lutParams = this.getLutParameters(pixelPadding, mLUTSeq, inverseLut, pr)) == null) {
            return null;
        }
        return this.getOrCreateCachedLookup(lutParams, mLUTSeq);
    }

    private LookupTableCV getOrCreateCachedLookup(LutParameters lutParams, LookupTableCV mLUTSeq) {
        LookupTableCV modalityLookup = (LookupTableCV)LUT_CACHE.get((Object)lutParams);
        if (modalityLookup != null) {
            return modalityLookup;
        }
        modalityLookup = this.createLookupTable(mLUTSeq, lutParams);
        if (this.desc.getPhotometricInterpretation().isMonochrome()) {
            RescaleUtils.applyPixelPaddingToModalityLUT(modalityLookup, lutParams);
        }
        LUT_CACHE.put((Object)lutParams, (Object)modalityLookup);
        return modalityLookup;
    }

    private LookupTableCV createLookupTable(LookupTableCV mLUTSeq, LutParameters lutParams) {
        return mLUTSeq != null ? this.createLookupFromSequence(mLUTSeq) : RescaleUtils.createRescaleRampLut(lutParams);
    }

    private LookupTableCV createLookupFromSequence(LookupTableCV mLUTSeq) {
        if (mLUTSeq.getNumBands() != 1) {
            return mLUTSeq;
        }
        if (mLUTSeq.getDataType() == 0) {
            byte[] data = mLUTSeq.getByteData(0);
            return data != null ? new LookupTableCV(data, mLUTSeq.getOffset(0)) : mLUTSeq;
        }
        short[] data = mLUTSeq.getShortData(0);
        return data != null ? new LookupTableCV(data, mLUTSeq.getOffset(0), mLUTSeq.getData() instanceof DataBufferUShort) : mLUTSeq;
    }

    private boolean isIdentityTransformation(double slope, double intercept, Optional<Integer> paddingValue) {
        return MathUtil.isEqual((double)slope, (double)1.0) && MathUtil.isEqualToZero((double)intercept) && paddingValue.isEmpty();
    }

    private int calculateOutputBits(LookupTableCV mLUTSeq, double slope, double intercept) {
        if (mLUTSeq != null) {
            return mLUTSeq.getDataType() == 0 ? 8 : 16;
        }
        double minValue = this.minMax.minVal * slope + intercept;
        double maxValue = this.minMax.maxVal * slope + intercept;
        int bitsOutputLut = 32 - Integer.numberOfLeadingZeros((int)Math.round(maxValue - minValue));
        if (minValue < 0.0 && bitsOutputLut <= 8) {
            bitsOutputLut = 9;
        }
        return bitsOutputLut;
    }

    private boolean calculateOutputSigned(LookupTableCV mLUTSeq, double slope, double intercept, boolean isSigned) {
        if (mLUTSeq != null) {
            return false;
        }
        double minValue = this.minMax.minVal * slope + intercept;
        return minValue < 0.0 || isSigned;
    }

    private ValueRange calculateVoiValueRange(WlParams wl) {
        if (this.shouldExtendVoiRange(wl)) {
            return new ValueRange(this.getMinAllocatedValue((WlPresentation)wl), this.getMaxAllocatedValue((WlPresentation)wl));
        }
        return new ValueRange((int)wl.getLevelMin(), (int)wl.getLevelMax());
    }

    private boolean shouldExtendVoiRange(WlParams wl) {
        return wl.isFillOutsideLutRange() || this.desc.getPixelPaddingValue().isPresent() && this.desc.getPhotometricInterpretation().isMonochrome();
    }

    private record ValueRange(int minValue, int maxValue) {
    }
}

