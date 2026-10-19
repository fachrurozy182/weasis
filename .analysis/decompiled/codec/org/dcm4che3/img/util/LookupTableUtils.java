/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.MathUtil
 *  org.weasis.opencv.data.LookupTableCV
 *  org.weasis.opencv.op.lut.LutShape
 */
package org.dcm4che3.img.util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Optional;
import java.util.stream.IntStream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.util.DicomAttributeUtils;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.MathUtil;
import org.weasis.opencv.data.LookupTableCV;
import org.weasis.opencv.op.lut.LutShape;

public final class LookupTableUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(LookupTableUtils.class);
    private static final int MAX_BITS_STORED = 16;
    private static final double MIN_WINDOW_WIDTH = 1.0;
    private static final int SIGMOID_FACTOR = -20;
    private static final int LOG_EXP_FACTOR = 20;
    private static final double FACTOR_DIVISOR = 10.0;
    private static final int MAX_8_BIT_ENTRIES = 256;
    private static final int BYTE_MASK = 255;
    private static final int SHORT_MASK = 65535;

    private LookupTableUtils() {
    }

    public static LookupTableCV createVoiLut(LutShape lutShape, double window, double level, int minValue, int maxValue, int bitsStored, boolean isSigned, boolean inverse) {
        if (lutShape == null) {
            return null;
        }
        LutConfiguration lutConfig = new LutConfiguration(window, level, minValue, maxValue, bitsStored, isSigned);
        Object outLut = LookupTableUtils.createOutputArray(lutConfig);
        LookupTableUtils.populateLut(lutShape, lutConfig, outLut, inverse);
        return LookupTableUtils.createLookupTable(outLut, lutConfig);
    }

    public static Optional<LookupTableCV> createLut(Attributes dicomLutObject) {
        if (dicomLutObject == null || dicomLutObject.isEmpty()) {
            return Optional.empty();
        }
        int[] descriptor = DicomUtils.getIntArrayFromDicomElement(dicomLutObject, 2633730, null);
        if (!LookupTableUtils.isValidDescriptor(descriptor)) {
            return Optional.empty();
        }
        DicomLutInfo lutInfo = new DicomLutInfo(descriptor);
        byte[] lutData = LookupTableUtils.extractLutData(dicomLutObject, lutInfo);
        return lutData == null ? Optional.empty() : LookupTableUtils.createDicomLookupTable(lutData, lutInfo);
    }

    public static int[] lutDescriptor(Attributes ds, int descTag) {
        int[] desc = ds.getInts(descTag);
        LookupTableUtils.validateDescriptor(desc);
        return desc;
    }

    public static byte[] lutData(Attributes ds, int[] desc, int dataTag, int segmTag) {
        int len = desc[0] <= 0 ? desc[0] + 65536 : desc[0];
        int bits = desc[2];
        Optional<byte[]> odata = DicomAttributeUtils.getByteData(ds, dataTag);
        if (odata.isEmpty()) {
            return LookupTableUtils.handleSegmentedLutData(ds, segmTag, len, bits);
        }
        return LookupTableUtils.processLutData(odata.get(), len, bits, ds.bigEndian());
    }

    private static Object createOutputArray(LutConfiguration config) {
        return config.bitsStored() <= 8 ? new byte[config.numEntries()] : (byte[])new short[config.numEntries()];
    }

    private static void populateLut(LutShape lutShape, LutConfiguration config, Object outLut, boolean inverse) {
        if (lutShape.getFunctionType() != null) {
            switch (lutShape.getFunctionType()) {
                case LINEAR: {
                    LookupTableUtils.setLinearLut(config, outLut, inverse);
                    break;
                }
                case SIGMOID: {
                    LookupTableUtils.setSigmoidLut(config, outLut, inverse, false);
                    break;
                }
                case SIGMOID_NORM: {
                    LookupTableUtils.setSigmoidLut(config, outLut, inverse, true);
                    break;
                }
                case LOG: {
                    LookupTableUtils.setLogarithmicLut(config, outLut, inverse);
                    break;
                }
                case LOG_INV: {
                    LookupTableUtils.setExponentialLut(config, outLut, inverse);
                }
            }
        } else {
            LookupTableUtils.setSequenceLut(config, lutShape.getLookup(), outLut, inverse);
        }
    }

    private static LookupTableCV createLookupTable(Object outLut, LutConfiguration config) {
        LookupTableCV lookupTableCV;
        if (outLut instanceof byte[]) {
            byte[] bytes = (byte[])outLut;
            lookupTableCV = new LookupTableCV(bytes, config.minInValue());
        } else {
            lookupTableCV = new LookupTableCV((short[])outLut, config.minInValue(), !config.isSigned());
        }
        return lookupTableCV;
    }

    private static void setLinearLut(LutConfiguration config, Object outLut, boolean inverse) {
        double slope = (double)(config.maxOutValue() - config.minOutValue()) / config.window();
        double intercept = (double)config.maxOutValue() - slope * (config.level() + config.window() / 2.0);
        IntStream.range(0, Array.getLength(outLut)).forEach(i -> {
            int value = (int)((double)(i + config.minInValue()) * slope + intercept);
            LookupTableUtils.setLutValue(outLut, config.minOutValue(), config.maxOutValue(), inverse, i, value);
        });
    }

    private static void setSigmoidLut(LutConfiguration config, Object outLut, boolean inverse, boolean normalize) {
        double outRange = (double)config.maxOutValue() - (double)config.minOutValue();
        NormalizationParams normParams = LookupTableUtils.calculateNormalization(config, normalize, -20.0, outRange, LutType.SIGMOID);
        IntStream.range(0, Array.getLength(outLut)).forEach(i -> {
            double value = outRange / (1.0 + Math.exp(-4.0 * ((double)(i + config.minInValue()) - config.level()) / config.window()));
            LookupTableUtils.setNormalizedLutValue(outLut, config, inverse, normalize, normParams, i, value);
        });
    }

    private static void setExponentialLut(LutConfiguration config, Object outLut, boolean inverse) {
        double outRange = (double)config.maxOutValue() - (double)config.minOutValue();
        NormalizationParams normParams = LookupTableUtils.calculateNormalization(config, true, 20.0, outRange, LutType.EXPONENTIAL);
        IntStream.range(0, Array.getLength(outLut)).forEach(i -> {
            double value = outRange * Math.exp(2.0 * ((double)(i + config.minInValue()) - config.level()) / config.window());
            LookupTableUtils.setNormalizedLutValue(outLut, config, inverse, true, normParams, i, value);
        });
    }

    private static void setLogarithmicLut(LutConfiguration config, Object outLut, boolean inverse) {
        double outRange = (double)config.maxOutValue() - (double)config.minOutValue();
        NormalizationParams normParams = LookupTableUtils.calculateNormalization(config, true, 20.0, outRange, LutType.LOGARITHMIC);
        IntStream.range(0, Array.getLength(outLut)).forEach(i -> {
            double value = outRange * Math.log(2.0 * (1.0 + ((double)(i + config.minInValue()) - config.level()) / config.window()));
            LookupTableUtils.setNormalizedLutValue(outLut, config, inverse, true, normParams, i, value);
        });
    }

    private static void setSequenceLut(LutConfiguration config, LookupTableCV lookupSequence, Object outLut, boolean inverse) {
        Object inLutDataArray = LookupTableUtils.getLutDataArray(lookupSequence);
        if (inLutDataArray == null) {
            return;
        }
        SequenceLutProcessor processor = new SequenceLutProcessor(config, inLutDataArray, outLut, inverse);
        processor.process();
    }

    private static NormalizationParams calculateNormalization(LutConfiguration config, boolean normalize, double nFactor, double outRange, LutType type) {
        if (!normalize) {
            return new NormalizationParams(0.0, 1.0);
        }
        double lowLevel = config.level() - config.window() / 2.0;
        double highLevel = config.level() + config.window() / 2.0;
        double minValue = LookupTableUtils.calculateLutValue(config, nFactor, outRange, type, lowLevel);
        double maxValue = LookupTableUtils.calculateLutValue(config, nFactor, outRange, type, highLevel);
        double rescaleRatio = (double)(config.maxOutValue() - config.minOutValue()) / Math.abs(maxValue - minValue);
        return new NormalizationParams(minValue, rescaleRatio);
    }

    private static double calculateLutValue(LutConfiguration config, double nFactor, double outRange, LutType type, double level) {
        return switch (type) {
            default -> throw new IncompatibleClassChangeError();
            case LutType.SIGMOID -> (double)config.minOutValue() + outRange / (1.0 + Math.exp(2.0 * nFactor / 10.0 * (level - config.level()) / config.window()));
            case LutType.EXPONENTIAL -> (double)config.minOutValue() + outRange * Math.exp(nFactor / 10.0 * (level - config.level()) / config.window());
            case LutType.LOGARITHMIC -> (double)config.minOutValue() + outRange * Math.log(nFactor / 10.0 * (1.0 + (level - config.level()) / config.window()));
        };
    }

    private static void setNormalizedLutValue(Object outLut, LutConfiguration config, boolean inverse, boolean normalize, NormalizationParams normParams, int i, double value) {
        if (normalize) {
            value = (value - normParams.minValue()) * normParams.rescaleRatio();
        }
        value = Math.round(value + (double)config.minOutValue());
        value = MathUtil.clamp((double)value, (double)config.minOutValue(), (double)config.maxOutValue());
        value = inverse ? (double)(config.maxOutValue() + config.minOutValue()) - value : value;
        LookupTableUtils.setArrayValue(outLut, i, (int)value);
    }

    private static void setLutValue(Object outLut, int minOutValue, int maxOutValue, boolean inverse, int i, int value) {
        value = MathUtil.clamp((int)value, (int)minOutValue, (int)maxOutValue);
        value = inverse ? maxOutValue + minOutValue - value : value;
        LookupTableUtils.setArrayValue(outLut, i, value);
    }

    private static void setArrayValue(Object outLut, int index, int value) {
        if (outLut instanceof byte[]) {
            Array.set(outLut, index, (byte)value);
        } else if (outLut instanceof short[]) {
            Array.set(outLut, index, (short)value);
        }
    }

    private static Object getLutDataArray(LookupTableCV lookup) {
        if (lookup == null) {
            return null;
        }
        return switch (lookup.getDataType()) {
            case 0 -> lookup.getByteData(0);
            case 1, 2 -> (Object[])lookup.getShortData(0);
            default -> null;
        };
    }

    private static boolean isValidDescriptor(int[] descriptor) {
        if (descriptor == null) {
            LOGGER.debug("Missing LUT Descriptor");
            return false;
        }
        if (descriptor.length != 3) {
            LOGGER.debug("Illegal number of LUT Descriptor values: {}", (Object)descriptor.length);
            return false;
        }
        return true;
    }

    private static void validateDescriptor(int[] desc) {
        if (desc == null) {
            throw new IllegalArgumentException("Missing LUT Descriptor!");
        }
        if (desc.length != 3) {
            throw new IllegalArgumentException("Illegal number of LUT Descriptor values: " + desc.length);
        }
        if (desc[0] < 0) {
            throw new IllegalArgumentException("Illegal LUT Descriptor: len=" + desc[0]);
        }
        int bits = desc[2];
        if (bits != 8 && bits != 16) {
            throw new IllegalArgumentException("Illegal LUT Descriptor: bits=" + bits);
        }
    }

    private static byte[] extractLutData(Attributes dicomLutObject, DicomLutInfo lutInfo) {
        try {
            byte[] bData = dicomLutObject.getBytes(2633734);
            if (bData == null || bData.length == 0) {
                return null;
            }
            return LookupTableUtils.processLutDataByBits(bData, lutInfo, dicomLutObject.bigEndian());
        }
        catch (IOException e) {
            LOGGER.error("Cannot get byte[] of {}", (Object)TagUtils.toString(2633734), (Object)e);
            return null;
        }
    }

    private static byte[] processLutDataByBits(byte[] bData, DicomLutInfo lutInfo, boolean bigEndian) {
        return switch (Integer.compare(lutInfo.numBits(), 8)) {
            case -1, 0 -> LookupTableUtils.processEightBitLutData(bData, lutInfo, bigEndian);
            case 1 -> {
                if (lutInfo.numBits() <= 16) {
                    yield LookupTableUtils.processSixteenBitLutData(bData, lutInfo, bigEndian);
                }
                yield null;
            }
            default -> {
                LOGGER.debug("Illegal number of bits for each entry in the LUT Data");
                yield null;
            }
        };
    }

    private static byte[] processEightBitLutData(byte[] bData, DicomLutInfo lutInfo, boolean bigEndian) {
        if (lutInfo.numEntries() <= 256 && bData.length == lutInfo.numEntries() << 1) {
            byte[] bDataNew = new byte[lutInfo.numEntries()];
            int byteShift = bigEndian ? 1 : 0;
            IntStream.range(0, bDataNew.length).forEach(i -> {
                bDataNew[i] = bData[i << 1 | byteShift];
            });
            return bDataNew;
        }
        return bData;
    }

    private static byte[] processSixteenBitLutData(byte[] bData, DicomLutInfo lutInfo, boolean bigEndian) {
        short[] sData = new short[lutInfo.numEntries()];
        ByteUtils.bytesToShorts(bData, sData, 0, sData.length, bigEndian);
        if (lutInfo.numEntries() <= 256) {
            return LookupTableUtils.convertSixteenBitToEightBit(sData, lutInfo);
        }
        return LookupTableUtils.convertShortArrayToByteArray(sData);
    }

    private static byte[] convertSixteenBitToEightBit(short[] sData, DicomLutInfo lutInfo) {
        int maxIn = (1 << lutInfo.numBits) - 1;
        int maxOut = lutInfo.numEntries - 1;
        byte[] bDataNew = new byte[lutInfo.numEntries];
        for (int i = 0; i < lutInfo.numEntries; ++i) {
            bDataNew[i] = (byte)((sData[i] & 0xFFFF) * maxOut / maxIn);
        }
        return bDataNew;
    }

    private static byte[] convertShortArrayToByteArray(short[] sData) {
        byte[] result = new byte[sData.length * 2];
        IntStream.range(0, sData.length).forEach(i -> {
            result[i * 2] = (byte)(sData[i] & 0xFF);
            result[i * 2 + 1] = (byte)(sData[i] >> 8 & 0xFF);
        });
        return result;
    }

    private static Optional<LookupTableCV> createDicomLookupTable(byte[] lutData, DicomLutInfo lutInfo) {
        if (lutInfo.numBits() <= 8 || lutInfo.numEntries() <= 256) {
            return Optional.of(new LookupTableCV(lutData, lutInfo.offset()));
        }
        short[] shortData = new short[lutData.length / 2];
        for (int i = 0; i < shortData.length; ++i) {
            shortData[i] = (short)(lutData[i * 2] & 0xFF | (lutData[i * 2 + 1] & 0xFF) << 8);
        }
        return Optional.of(new LookupTableCV(shortData, lutInfo.offset(), true));
    }

    private static byte[] handleSegmentedLutData(Attributes ds, int segmTag, int len, int bits) {
        int[] lut = ds.getInts(segmTag);
        if (lut == null) {
            throw new IllegalArgumentException("Missing LUT Data!");
        }
        if (bits == 8) {
            throw new IllegalArgumentException("Segmented LUT Data with LUT Descriptor: bits=8");
        }
        byte[] data = new byte[len];
        new InflateSegmentedLut(lut, 0, data, 0).inflate(-1, 0);
        return data;
    }

    private static byte[] processLutData(byte[] data, int len, int bits, boolean bigEndian) {
        if (bits == 16 || data.length != len) {
            int hilo;
            if (data.length != len << 1) {
                throw new IllegalArgumentException("Number of actual LUT entries: %d mismatch specified value: %d in LUT Descriptor".formatted(new Object[]{data.length, len}));
            }
            int n = hilo = bigEndian ? 0 : 1;
            if (bits == 8) {
                hilo = 1 - hilo;
            }
            return LookupTableUtils.halfLength(data, hilo);
        }
        return data;
    }

    private static byte[] halfLength(byte[] data, int hilo) {
        byte[] bs = new byte[data.length >> 1];
        for (int i = 0; i < bs.length; ++i) {
            bs[i] = data[i << 1 | hilo];
        }
        return bs;
    }

    private record LutConfiguration(int bitsStored, double window, double level, int minInValue, int maxInValue, int numEntries, int minOutValue, int maxOutValue, boolean isSigned) {
        LutConfiguration(double window, double level, int minValue, int maxValue, int bitsStored, boolean isSigned) {
            this(MathUtil.clamp((int)bitsStored, (int)1, (int)16), Math.max(window, 1.0), level, Math.min(maxValue, minValue), Math.max(maxValue, minValue), Math.max(maxValue, minValue) - Math.min(maxValue, minValue) + 1, LutConfiguration.calculateMinOutValue(MathUtil.clamp((int)bitsStored, (int)1, (int)16), isSigned), LutConfiguration.calculateMaxOutValue(MathUtil.clamp((int)bitsStored, (int)1, (int)16), isSigned), isSigned);
        }

        private static int calculateMinOutValue(int bitsStored, boolean isSigned) {
            int bitsAllocated = bitsStored <= 8 ? 8 : 16;
            return isSigned ? -(1 << bitsAllocated - 1) : 0;
        }

        private static int calculateMaxOutValue(int bitsStored, boolean isSigned) {
            int bitsAllocated = bitsStored <= 8 ? 8 : 16;
            return isSigned ? (1 << bitsAllocated - 1) - 1 : (1 << bitsAllocated) - 1;
        }
    }

    private record DicomLutInfo(int numEntries, int offset, int numBits) {
        DicomLutInfo(int[] descriptor) {
            this(descriptor[0] <= 0 ? descriptor[0] + 65536 : descriptor[0], (short)descriptor[1], descriptor[2]);
        }
    }

    private static enum LutType {
        SIGMOID,
        EXPONENTIAL,
        LOGARITHMIC;

    }

    private record NormalizationParams(double minValue, double rescaleRatio) {
    }

    private static final class SequenceLutProcessor {
        private final LutConfiguration config;
        private final Object inLutDataArray;
        private final Object outLut;
        private final boolean inverse;
        private final int lutDataValueMask;
        private final double lowLevel;
        private final double highLevel;
        private final int maxInLutIndex;

        SequenceLutProcessor(LutConfiguration config, Object inLutDataArray, Object outLut, boolean inverse) {
            this.config = config;
            this.inLutDataArray = inLutDataArray;
            this.outLut = outLut;
            this.inverse = inverse;
            this.lutDataValueMask = SequenceLutProcessor.calculateValueMask(inLutDataArray);
            this.lowLevel = config.level() - config.window() / 2.0;
            this.highLevel = config.level() + config.window() / 2.0;
            this.maxInLutIndex = Array.getLength(inLutDataArray) - 1;
        }

        void process() {
            LookupRange lookupRange = this.calculateLookupRange();
            double outRescaleRatio = (double)(this.config.maxOutValue - this.config.minOutValue) / (double)lookupRange.range;
            for (int i = 0; i < Array.getLength(this.outLut); ++i) {
                double inValueRescaled = this.calculateRescaledInput(i);
                int value = this.interpolateValue(inValueRescaled);
                value = (int)Math.round((double)value * outRescaleRatio);
                LookupTableUtils.setLutValue(this.outLut, this.config.minOutValue, this.config.maxOutValue, this.inverse, i, value);
            }
        }

        private static int calculateValueMask(Object array) {
            return array instanceof byte[] ? 255 : (array instanceof short[] ? 65535 : -1);
        }

        private LookupRange calculateLookupRange() {
            int minLookupValue = Integer.MAX_VALUE;
            int maxLookupValue = Integer.MIN_VALUE;
            for (int i = 0; i < Array.getLength(this.inLutDataArray); ++i) {
                int val = this.lutDataValueMask & Array.getInt(this.inLutDataArray, i);
                minLookupValue = Math.min(minLookupValue, val);
                maxLookupValue = Math.max(maxLookupValue, val);
            }
            return new LookupRange(minLookupValue, Math.abs(maxLookupValue - minLookupValue));
        }

        private double calculateRescaledInput(int i) {
            int inputValue = i + this.config.minInValue;
            if ((double)inputValue <= this.lowLevel) {
                return 0.0;
            }
            if ((double)inputValue > this.highLevel) {
                return this.maxInLutIndex;
            }
            return ((double)inputValue - this.lowLevel) * ((double)this.maxInLutIndex / this.config.window);
        }

        private int interpolateValue(double inValueRescaled) {
            int inValueRoundDown = Math.max(0, (int)Math.floor(inValueRescaled));
            int inValueRoundUp = Math.min(this.maxInLutIndex, (int)Math.ceil(inValueRescaled));
            int valueDown = this.lutDataValueMask & Array.getInt(this.inLutDataArray, inValueRoundDown);
            int valueUp = this.lutDataValueMask & Array.getInt(this.inLutDataArray, inValueRoundUp);
            return inValueRoundUp == inValueRoundDown ? valueDown : (int)Math.round((double)valueDown + (inValueRescaled - (double)inValueRoundDown) * (double)(valueUp - valueDown) / (double)(inValueRoundUp - inValueRoundDown));
        }

        private record LookupRange(int min, int range) {
        }

        private static final class LookupRangeAccumulator {
            private int min = Integer.MAX_VALUE;
            private int max = Integer.MIN_VALUE;

            private LookupRangeAccumulator() {
            }

            void accept(int value) {
                this.min = Math.min(this.min, value);
                this.max = Math.max(this.max, value);
            }

            LookupRangeAccumulator combine(LookupRangeAccumulator other) {
                this.min = Math.min(this.min, other.min);
                this.max = Math.max(this.max, other.max);
                return this;
            }

            LookupRange toLookupRange() {
                return new LookupRange(this.min, Math.abs(this.max - this.min));
            }
        }
    }

    private static final class InflateSegmentedLut {
        private final int[] segm;
        private final byte[] data;
        private int readPos;
        private int writePos;

        InflateSegmentedLut(int[] segm, int readPos, byte[] data, int writePos) {
            this.segm = segm;
            this.data = data;
            this.readPos = readPos;
            this.writePos = writePos;
        }

        int inflate(int segs, int y0) {
            while (segs < 0 ? this.readPos < this.segm.length : segs-- > 0) {
                int segPos = this.readPos;
                int op = this.read();
                int n = this.read();
                y0 = switch (op) {
                    case 0 -> this.discreteSegment(n);
                    case 1 -> {
                        if (this.writePos == 0) {
                            throw new IllegalArgumentException("Linear segment cannot be the first segment");
                        }
                        yield this.linearSegment(n, y0, this.read());
                    }
                    case 2 -> {
                        if (segs >= 0) {
                            throw new IllegalArgumentException("nested indirect segment at index " + segPos);
                        }
                        yield this.indirectSegment(n, y0);
                    }
                    default -> throw new IllegalArgumentException("illegal op code " + op + " at index " + segPos);
                };
            }
            return y0;
        }

        private int read() {
            if (this.readPos >= this.segm.length) {
                throw new IllegalArgumentException("Running out of data inflating segmented LUT");
            }
            return this.segm[this.readPos++] & 0xFFFF;
        }

        private void write(int y) {
            if (this.writePos >= this.data.length) {
                throw new IllegalArgumentException("Number of entries in inflated segmented LUT exceeds specified value: " + this.data.length + " in LUT Descriptor");
            }
            this.data[this.writePos++] = (byte)(y >> 8);
        }

        private int discreteSegment(int n) {
            IntStream.range(0, n).forEach(ignored -> this.write(this.read()));
            return this.segm[this.readPos - 1] & 0xFFFF;
        }

        private int linearSegment(int n, int y0, int y1) {
            int dy = y1 - y0;
            IntStream.rangeClosed(1, n).forEach(j -> this.write(y0 + dy * j / n));
            return y1;
        }

        private int indirectSegment(int n, int y0) {
            int segmentValue = this.read() | this.read() << 16;
            return new InflateSegmentedLut(this.segm, segmentValue, this.data, this.writePos).inflate(n, y0);
        }
    }
}

