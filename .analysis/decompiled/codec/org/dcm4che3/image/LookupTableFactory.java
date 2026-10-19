/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.image;

import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.image.ByteLookupTable;
import org.dcm4che3.image.LookupTable;
import org.dcm4che3.image.ShortLookupTable;
import org.dcm4che3.image.StoredValue;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StringUtils;

public class LookupTableFactory {
    private static final String[] XA_XRF_CUIDS = new String[]{"1.2.840.10008.5.1.4.1.1.12.1", "1.2.840.10008.5.1.4.1.1.12.2", "1.2.840.10008.5.1.4.1.1.12.3"};
    private static final String[] LOG_DISP = new String[]{"LOG", "DISP"};
    private final StoredValue storedValue;
    private float rescaleSlope = 1.0f;
    private float rescaleIntercept = 0.0f;
    private LookupTable modalityLUT;
    private float windowCenter;
    private float windowWidth;
    private String voiLUTFunction;
    private LookupTable voiLUT;
    private LookupTable presentationLUT;
    private boolean inverse;

    public LookupTableFactory(StoredValue storedValue) {
        this.storedValue = storedValue;
    }

    public static boolean applyModalityLUT(Attributes attrs) {
        return !StringUtils.contains(XA_XRF_CUIDS, attrs.getString(524310)) || !StringUtils.contains(LOG_DISP, attrs.getString(2625600));
    }

    public void setModalityLUT(Attributes attrs) {
        this.rescaleIntercept = attrs.getFloat(2625618, 0.0f);
        this.rescaleSlope = attrs.getFloat(2625619, 1.0f);
        boolean unsigned = attrs.getInt(2621699, -1) == 0;
        this.modalityLUT = this.createLUT(this.storedValue, attrs.getNestedDataset(2633728), unsigned);
    }

    public void setPresentationLUT(Attributes attrs) {
        this.setPresentationLUT(attrs, false);
    }

    public void setPresentationLUT(Attributes attrs, boolean ignorePresentationLUTShape) {
        Attributes pLUT = attrs.getNestedDataset(542113808);
        if (pLUT != null) {
            int[] desc = pLUT.getInts(2633730);
            if (desc != null && desc.length == 3) {
                int len = desc[0] == 0 ? 65536 : desc[0];
                boolean unsigned = attrs.getInt(2621699, -1) == 0;
                this.presentationLUT = this.createLUT(new StoredValue.Unsigned(LookupTableFactory.log2(len)), this.resetOffset(desc), pLUT.getSafeBytes(2633734), pLUT.bigEndian(), unsigned);
            }
        } else {
            String pShape;
            this.inverse = ignorePresentationLUTShape || (pShape = attrs.getString(0x20500020)) == null ? "MONOCHROME1".equals(attrs.getString(2621444)) : "INVERSE".equals(pShape);
        }
    }

    private int[] resetOffset(int[] desc) {
        if (desc[1] == 0) {
            return desc;
        }
        int[] copy = (int[])desc.clone();
        copy[1] = 0;
        return copy;
    }

    public void setWindowCenter(float windowCenter) {
        this.windowCenter = windowCenter;
    }

    public void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
    }

    public void setVOI(Attributes img, int windowIndex, int voiLUTIndex, boolean preferWindow) {
        if (img == null) {
            return;
        }
        Attributes vLUT = img.getNestedDataset(2633744, voiLUTIndex);
        if (preferWindow || vLUT == null) {
            float[] wcs = img.getFloats(2625616);
            float[] wws = img.getFloats(2625617);
            if (wcs != null && wcs.length != 0 && wws != null && wws.length != 0) {
                int index = windowIndex < Math.min(wcs.length, wws.length) ? windowIndex : 0;
                this.windowCenter = wcs[index];
                this.windowWidth = wws[index];
                return;
            }
        }
        if (vLUT != null) {
            this.adjustVOILUTDescriptor(vLUT);
            boolean unsigned = img.getInt(2621699, -1) == 0;
            this.voiLUT = this.createLUT(this.modalityLUT != null ? new StoredValue.Unsigned(this.modalityLUT.outBits) : this.storedValue, vLUT, unsigned);
        }
    }

    private void adjustVOILUTDescriptor(Attributes vLUT) {
        byte[] data;
        int[] desc = vLUT.getInts(2633730);
        if (desc != null && desc.length == 3 && desc[2] == 16 && (data = vLUT.getSafeBytes(2633734)) != null) {
            int i;
            int hiByte = 0;
            int n = i = vLUT.bigEndian() ? 0 : 1;
            while (i < data.length) {
                hiByte |= data[i];
                ++i;
                ++i;
            }
            if ((hiByte & 0x80) == 0) {
                desc[2] = 40 - Integer.numberOfLeadingZeros(hiByte & 0xFF);
                vLUT.setInt(2633730, VR.SS, desc);
            }
        }
    }

    private LookupTable createLUT(StoredValue inBits, Attributes attrs, boolean unsigned) {
        if (attrs == null) {
            return null;
        }
        return this.createLUT(inBits, attrs.getInts(2633730), attrs.getSafeBytes(2633734), attrs.bigEndian(), unsigned);
    }

    private LookupTable createLUT(StoredValue inBits, int[] desc, byte[] data, boolean bigEndian, boolean unsigned) {
        if (desc == null) {
            return null;
        }
        if (desc.length != 3) {
            return null;
        }
        int len = desc[0] == 0 ? 65536 : desc[0];
        short offset = unsigned ? (short)(desc[1] & 0xFFFF) : (short)desc[1];
        int outBits = desc[2];
        if (data == null) {
            return null;
        }
        if (data.length == len << 1) {
            if (outBits > 8) {
                if (outBits > 16) {
                    return null;
                }
                short[] ss = new short[len];
                if (bigEndian) {
                    for (int i = 0; i < ss.length; ++i) {
                        ss[i] = (short)ByteUtils.bytesToShortBE(data, i << 1);
                    }
                } else {
                    for (int i = 0; i < ss.length; ++i) {
                        ss[i] = (short)ByteUtils.bytesToShortLE(data, i << 1);
                    }
                }
                return new ShortLookupTable(inBits, outBits, offset, ss);
            }
            data = LookupTableFactory.halfLength(data, bigEndian ? 1 : 0);
        }
        if (data.length != len) {
            return null;
        }
        if (outBits > 8) {
            return null;
        }
        return new ByteLookupTable(inBits, outBits, offset, data);
    }

    static byte[] halfLength(byte[] data, int hilo) {
        byte[] bs = new byte[data.length >> 1];
        for (int i = 0; i < bs.length; ++i) {
            bs[i] = data[i << 1 | hilo];
        }
        return bs;
    }

    public LookupTable createLUT(int outBits) {
        LookupTable lut = this.combineModalityVOILUT(this.presentationLUT != null ? LookupTableFactory.log2(this.presentationLUT.length()) : outBits);
        if (this.presentationLUT != null) {
            lut = lut.combine(this.presentationLUT.adjustOutBits(outBits));
        } else if (this.inverse) {
            lut.inverse();
        }
        return lut;
    }

    private static int log2(int value) {
        int i = 0;
        while (value >>> i != 0) {
            ++i;
        }
        return i - 1;
    }

    private LookupTable combineModalityVOILUT(int outBits) {
        float m = this.rescaleSlope;
        float b = this.rescaleIntercept;
        LookupTable modalityLUT = this.modalityLUT;
        LookupTable lut = this.voiLUT;
        if (lut == null) {
            int offset;
            int size;
            float c = this.windowCenter;
            float w = this.windowWidth;
            if (w == 0.0f && modalityLUT != null) {
                return modalityLUT.adjustOutBits(outBits);
            }
            StoredValue inBits = modalityLUT != null ? new StoredValue.Unsigned(modalityLUT.outBits) : this.storedValue;
            int minOut = 0;
            int maxOut = (1 << outBits) - 1;
            if (w != 0.0f) {
                float M = Math.abs(m);
                size = Math.max(2, Math.round(w / M));
                offset = Math.round((c - w / 2.0f - b) / M);
                int minIndex = inBits.minValue() - offset;
                int maxIndex = inBits.maxValue() - offset;
                int size_1 = size - 1;
                int midIndex = size_1 / 2;
                if (minIndex > 0) {
                    offset += minIndex;
                    size -= minIndex;
                    minOut = (minIndex * maxOut + midIndex) / size_1;
                }
                if (maxIndex < size_1) {
                    size -= size_1 - maxIndex;
                    maxOut = (maxIndex * maxOut + midIndex) / size_1;
                }
            } else {
                offset = inBits.minValue();
                size = inBits.maxValue() - inBits.minValue() + 1;
            }
            lut = outBits > 8 ? new ShortLookupTable(inBits, outBits, minOut, maxOut, offset, size, m < 0.0f) : new ByteLookupTable(inBits, outBits, minOut, maxOut, offset, size, m < 0.0f);
        } else {
            lut = lut.adjustOutBits(outBits);
        }
        return modalityLUT != null ? modalityLUT.combine(lut) : lut;
    }

    public boolean autoWindowing(Attributes img, Raster raster) {
        return this.autoWindowing(img, raster, false);
    }

    public boolean autoWindowing(Attributes img, Raster raster, boolean addAutoWindow) {
        if (this.modalityLUT != null || this.voiLUT != null || this.windowWidth != 0.0f) {
            return false;
        }
        int[] min_max = this.calcMinMax(raster);
        if (min_max[0] == min_max[1]) {
            return false;
        }
        this.windowCenter = (float)((min_max[0] + min_max[1] + 1) / 2) * this.rescaleSlope + this.rescaleIntercept;
        this.windowWidth = Math.abs((float)(min_max[1] + 1 - min_max[0]) * this.rescaleSlope);
        if (addAutoWindow) {
            img.setFloat(2625616, VR.DS, this.windowCenter);
            img.setFloat(2625617, VR.DS, this.windowWidth);
        }
        return true;
    }

    private int[] calcMinMax(Raster raster) {
        ComponentSampleModel sm = (ComponentSampleModel)raster.getSampleModel();
        DataBuffer dataBuffer = raster.getDataBuffer();
        switch (dataBuffer.getDataType()) {
            case 0: {
                return this.calcMinMax(this.storedValue, sm, ((DataBufferByte)dataBuffer).getData());
            }
            case 1: {
                return this.calcMinMax(this.storedValue, sm, ((DataBufferUShort)dataBuffer).getData());
            }
            case 2: {
                return this.calcMinMax(this.storedValue, sm, ((DataBufferShort)dataBuffer).getData());
            }
        }
        throw new UnsupportedOperationException("DataBuffer: " + dataBuffer.getClass() + " not supported");
    }

    private int[] calcMinMax(StoredValue storedValue, ComponentSampleModel sm, byte[] data) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        for (int y = 0; y < h; ++y) {
            int i = y * stride;
            int end = i + w;
            while (i < end) {
                int val;
                if ((val = storedValue.valueOf(data[i++])) < min) {
                    min = val;
                }
                if (val <= max) continue;
                max = val;
            }
        }
        return new int[]{min, max};
    }

    private int[] calcMinMax(StoredValue storedValue, ComponentSampleModel sm, short[] data) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        for (int y = 0; y < h; ++y) {
            int i = y * stride;
            int end = i + w;
            while (i < end) {
                int val;
                if ((val = storedValue.valueOf(data[i++])) < min) {
                    min = val;
                }
                if (val <= max) continue;
                max = val;
            }
        }
        return new int[]{min, max};
    }
}

