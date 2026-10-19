/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.MathUtil
 *  org.weasis.opencv.data.LookupTableCV
 *  org.weasis.opencv.op.lut.LutParameters
 */
package org.dcm4che3.img.util;

import java.util.Arrays;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.util.DicomUtils;
import org.weasis.core.util.MathUtil;
import org.weasis.opencv.data.LookupTableCV;
import org.weasis.opencv.op.lut.LutParameters;

public final class RescaleUtils {
    private static final int MIN_BITS = 1;
    private static final int MAX_BITS = 16;
    private static final int BYTE_THRESHOLD = 8;
    private static final int BYTE_BITS = 8;
    private static final int SHORT_BITS = 16;
    private static final double DEFAULT_SLOPE = 1.0;
    private static final double DEFAULT_INTERCEPT = 0.0;

    private RescaleUtils() {
    }

    public static LookupTableCV createRescaleRampLut(LutParameters params) {
        return RescaleUtils.createRescaleRampLut(params.intercept(), params.slope(), params.bitsStored(), params.signed(), params.outputSigned(), params.bitsOutput());
    }

    public static LookupTableCV createRescaleRampLut(double intercept, double slope, int bitsStored, boolean isSigned, boolean outputSigned, int bitsOutput) {
        return RescaleUtils.createRescaleRampLut(intercept, slope, Integer.MIN_VALUE, Integer.MAX_VALUE, bitsStored, isSigned, false, outputSigned, bitsOutput);
    }

    public static LookupTableCV createRescaleRampLut(double intercept, double slope, int minValue, int maxValue, int bitsStored, boolean isSigned, boolean inverse, boolean outputSigned, int bitsOutput) {
        BitConfiguration bitConfig = BitConfiguration.create(bitsStored, bitsOutput, isSigned, outputSigned);
        InputRange inputRange = InputRange.create(minValue, maxValue, bitConfig);
        Object lutData = RescaleUtils.createLutArray(inputRange, bitConfig, intercept, slope, inverse);
        return RescaleUtils.createLookupTable(lutData, inputRange.min(), outputSigned);
    }

    public static void applyPixelPaddingToModalityLUT(LookupTableCV modalityLookup, LutParameters params) {
        if (!RescaleUtils.isPaddingApplicable(modalityLookup, params)) {
            return;
        }
        PaddingRange paddingRange = PaddingRange.from(params);
        PaddingInfo paddingInfo = PaddingInfo.calculate(paddingRange, modalityLookup);
        if (paddingInfo.isValid()) {
            RescaleUtils.applyPaddingToLookupData(modalityLookup, paddingInfo, params.inversePaddingMLUT());
        }
    }

    public static double pixel2rescale(LookupTableCV lookup, double pixelValue) {
        return lookup != null && RescaleUtils.isValueInRange(lookup, pixelValue) ? (double)lookup.lookup(0, (int)pixelValue) : pixelValue;
    }

    public static double pixel2rescale(Attributes dcm, double pixelValue) {
        if (dcm == null) {
            return pixelValue;
        }
        RescaleParameters rescaleParams = RescaleParameters.from(dcm);
        return rescaleParams.hasRescaleParameters() ? pixelValue * rescaleParams.slope() + rescaleParams.intercept() : pixelValue;
    }

    private static Object createLutArray(InputRange inputRange, BitConfiguration bitConfig, double intercept, double slope, boolean inverse) {
        int numEntries = inputRange.size();
        return bitConfig.isByteOutput() ? RescaleUtils.createByteLut(numEntries, inputRange.min(), intercept, slope, bitConfig, inverse) : (byte[])RescaleUtils.createShortLut(numEntries, inputRange.min(), intercept, slope, bitConfig, inverse);
    }

    private static byte[] createByteLut(int numEntries, int minInValue, double intercept, double slope, BitConfiguration config, boolean inverse) {
        byte[] lut = new byte[numEntries];
        for (int i = 0; i < numEntries; ++i) {
            int value = RescaleUtils.calculateRescaledValue(i + minInValue, intercept, slope, config, inverse);
            lut[i] = (byte)value;
        }
        return lut;
    }

    private static short[] createShortLut(int numEntries, int minInValue, double intercept, double slope, BitConfiguration config, boolean inverse) {
        short[] lut = new short[numEntries];
        for (int i = 0; i < numEntries; ++i) {
            int value = RescaleUtils.calculateRescaledValue(i + minInValue, intercept, slope, config, inverse);
            lut[i] = (short)value;
        }
        return lut;
    }

    private static int calculateRescaledValue(int inputValue, double intercept, double slope, BitConfiguration config, boolean inverse) {
        int value = (int)Math.round((double)inputValue * slope + intercept);
        value = MathUtil.clamp((int)value, (int)config.minOutValue(), (int)config.maxOutValue());
        return inverse ? config.maxOutValue() + config.minOutValue() - value : value;
    }

    private static LookupTableCV createLookupTable(Object lutData, int minInValue, boolean outputSigned) {
        LookupTableCV lookupTableCV;
        if (lutData instanceof byte[]) {
            byte[] bytes = (byte[])lutData;
            lookupTableCV = new LookupTableCV(bytes, minInValue);
        } else {
            lookupTableCV = new LookupTableCV((short[])lutData, minInValue, !outputSigned);
        }
        return lookupTableCV;
    }

    private static boolean isPaddingApplicable(LookupTableCV modalityLookup, LutParameters params) {
        return modalityLookup != null && params != null && params.applyPadding() && params.paddingMinValue() != null && modalityLookup.getDataType() <= 2;
    }

    private static void applyPaddingToLookupData(LookupTableCV lookup, PaddingInfo info, boolean inverse) {
        switch (lookup.getDataType()) {
            case 0: {
                byte[] data = lookup.getByteData(0);
                byte fillVal = inverse ? (byte)-1 : 0;
                Arrays.fill(data, info.startIndex(), info.endIndex(), fillVal);
                break;
            }
            case 1: 
            case 2: {
                short[] data = lookup.getShortData(0);
                short fillVal = inverse ? data[data.length - 1] : data[0];
                Arrays.fill(data, info.startIndex(), info.endIndex(), fillVal);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported data type for padding: " + lookup.getDataType());
            }
        }
    }

    private static boolean isValueInRange(LookupTableCV lookup, double pixelValue) {
        return pixelValue >= (double)lookup.getOffset() && pixelValue <= (double)(lookup.getOffset() + lookup.getNumEntries() - 1);
    }

    private record BitConfiguration(int stored, int bitsOutLut, int minOutValue, int maxOutValue, boolean isSigned) {
        static BitConfiguration create(int bitsStored, int bitsOutput, boolean isSigned, boolean outputSigned) {
            int stored = MathUtil.clamp((int)bitsStored, (int)1, (int)16);
            int bitsOutLut = bitsOutput <= 8 ? 8 : 16;
            int outRangeSize = (1 << bitsOutLut) - 1;
            int maxOutValue = outputSigned ? (1 << bitsOutLut - 1) - 1 : outRangeSize;
            int minOutValue = outputSigned ? -(maxOutValue + 1) : 0;
            return new BitConfiguration(stored, bitsOutLut, minOutValue, maxOutValue, isSigned);
        }

        boolean isByteOutput() {
            return this.bitsOutLut == 8;
        }
    }

    private record InputRange(int min, int max) {
        static InputRange create(int minValue, int maxValue, BitConfiguration config) {
            int minInValue = config.isSigned() ? -(1 << config.stored() - 1) : 0;
            int maxInValue = config.isSigned() ? (1 << config.stored() - 1) - 1 : (1 << config.stored()) - 1;
            minInValue = Math.max(minInValue, Math.min(maxValue, minValue));
            maxInValue = Math.min(maxInValue, Math.max(maxValue, minValue));
            return new InputRange(minInValue, maxInValue);
        }

        int size() {
            return this.max - this.min + 1;
        }
    }

    private record PaddingRange(int min, int max) {
        static PaddingRange from(LutParameters params) {
            int paddingValue = params.paddingMinValue();
            Integer paddingLimit = params.paddingMaxValue();
            int paddingValueMin = paddingLimit == null ? paddingValue : Math.min(paddingValue, paddingLimit);
            int paddingValueMax = paddingLimit == null ? paddingValue : Math.max(paddingValue, paddingLimit);
            return new PaddingRange(paddingValueMin, paddingValueMax);
        }

        int size() {
            return this.max - this.min + 1;
        }
    }

    private record PaddingInfo(int startIndex, int numValues, boolean valid) {
        static PaddingInfo invalid() {
            return new PaddingInfo(0, 0, false);
        }

        static PaddingInfo calculate(PaddingRange range, LookupTableCV lookup) {
            int numPaddingValues = range.size();
            int startIndex = range.min() - lookup.getOffset();
            if (startIndex >= lookup.getNumEntries()) {
                return PaddingInfo.invalid();
            }
            if (startIndex < 0) {
                if ((numPaddingValues += startIndex) < 1) {
                    return PaddingInfo.invalid();
                }
                startIndex = 0;
            }
            return new PaddingInfo(startIndex, numPaddingValues, true);
        }

        boolean isValid() {
            return this.valid;
        }

        int endIndex() {
            return this.startIndex + this.numValues;
        }
    }

    private record RescaleParameters(double slope, double intercept, boolean hasRescaleParameters) {
        static RescaleParameters from(Attributes dcm) {
            Double slope = DicomUtils.getDoubleFromDicomElement(dcm, 2625619, null);
            Double intercept = DicomUtils.getDoubleFromDicomElement(dcm, 2625618, null);
            return new RescaleParameters(slope != null ? slope : 1.0, intercept != null ? intercept : 0.0, slope != null || intercept != null);
        }
    }
}

