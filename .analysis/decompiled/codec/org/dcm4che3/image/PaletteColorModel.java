/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.image;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.image.LookupTableFactory;

public class PaletteColorModel
extends ColorModel {
    private static final int[] opaqueBits = new int[]{8, 8, 8};
    private final LUT lut;

    public PaletteColorModel(int bits, int dataType, ColorSpace cs, Attributes ds) {
        super(bits, opaqueBits, cs, false, false, 1, dataType);
        int[] rDesc = this.lutDescriptor(ds, 2625793);
        int[] gDesc = this.lutDescriptor(ds, 2625794);
        int[] bDesc = this.lutDescriptor(ds, 2625795);
        byte[] r = PaletteColorModel.lutData(ds, rDesc, 2626049, 0x281221);
        byte[] g = PaletteColorModel.lutData(ds, gDesc, 2626050, 0x281222);
        byte[] b = PaletteColorModel.lutData(ds, bDesc, 2626051, 2626083);
        this.lut = LUT.create(bits, r, g, b, rDesc[1], gDesc[1], bDesc[1]);
    }

    private PaletteColorModel(PaletteColorModel src, ColorSpace cs) {
        super(src.pixel_bits, opaqueBits, cs, false, false, src.getTransparency(), src.transferType);
        int[] rgb = new int[1 << src.pixel_bits];
        for (int i = 0; i < rgb.length; ++i) {
            rgb[i] = PaletteColorModel.convertTo(src.getRGB(i), src.getColorSpace(), cs);
        }
        this.lut = new LUT.Packed(src.pixel_bits, rgb);
    }

    private static int convertTo(int rgb, ColorSpace src, ColorSpace cs) {
        float[] from = new float[]{PaletteColorModel.scaleRGB(rgb >> 16), PaletteColorModel.scaleRGB(rgb >> 8), PaletteColorModel.scaleRGB(rgb)};
        float[] ciexyz = src.toCIEXYZ(from);
        float[] to = cs.fromCIEXYZ(ciexyz);
        return 0xFF000000 | PaletteColorModel.unscaleRGB(to[0]) << 16 | PaletteColorModel.unscaleRGB(to[1]) << 8 | PaletteColorModel.unscaleRGB(to[2]);
    }

    private static int unscaleRGB(float value) {
        return Math.min((int)(value * 256.0f), 255);
    }

    private static float scaleRGB(int value) {
        return (float)(value & 0xFF) / 255.0f;
    }

    public PaletteColorModel convertTo(ColorSpace cs) {
        return new PaletteColorModel(this, cs);
    }

    private int[] lutDescriptor(Attributes ds, int descTag) {
        int[] desc = ds.getInts(descTag);
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
        return desc;
    }

    private static byte[] lutData(Attributes ds, int[] desc, int dataTag, int segmTag) {
        int len = desc[0] == 0 ? 65536 : desc[0];
        int bits = desc[2];
        byte[] data = ds.getSafeBytes(dataTag);
        if (data == null) {
            int[] segm = ds.getInts(segmTag);
            if (segm == null) {
                throw new IllegalArgumentException("Missing LUT Data!");
            }
            if (bits == 8) {
                throw new IllegalArgumentException("Segmented LUT Data with LUT Descriptor: bits=8");
            }
            data = new byte[len];
            new InflateSegmentedLut(segm, 0, data, 0).inflate(-1, 0);
        } else if (bits == 16 || data.length != len) {
            int hilo;
            if (data.length != len << 1) {
                throw new IllegalArgumentException("Number of actual LUT entries: " + data.length + " mismatch specified value: " + len + " in LUT Descriptor");
            }
            int n = hilo = ds.bigEndian() ? 0 : 1;
            if (bits == 8) {
                hilo = 1 - hilo;
            }
            data = LookupTableFactory.halfLength(data, hilo);
        }
        return data;
    }

    @Override
    public boolean isCompatibleRaster(Raster raster) {
        return this.isCompatibleSampleModel(raster.getSampleModel());
    }

    @Override
    public boolean isCompatibleSampleModel(SampleModel sm) {
        return sm.getTransferType() == this.transferType && sm.getNumBands() == 1;
    }

    @Override
    public int getRed(int pixel) {
        return this.lut.getRed(pixel);
    }

    @Override
    public int getGreen(int pixel) {
        return this.lut.getGreen(pixel);
    }

    @Override
    public int getBlue(int pixel) {
        return this.lut.getBlue(pixel);
    }

    @Override
    public int getAlpha(int pixel) {
        return this.lut.getAlpha(pixel);
    }

    @Override
    public int getRGB(int pixel) {
        return this.lut.getRGB(pixel);
    }

    @Override
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        return Raster.createInterleavedRaster(this.pixel_bits <= 8 ? 0 : 1, w, h, 1, null);
    }

    public BufferedImage convertToIntDiscrete(Raster raster) {
        if (!this.isCompatibleRaster(raster)) {
            throw new IllegalArgumentException("This raster is not compatible with this PaletteColorModel.");
        }
        DirectColorModel cm = new DirectColorModel(this.getColorSpace(), 24, 0xFF0000, 65280, 255, 0, false, 3);
        int w = raster.getWidth();
        int h = raster.getHeight();
        WritableRaster discreteRaster = ((ColorModel)cm).createCompatibleWritableRaster(w, h);
        int[] discretData = ((DataBufferInt)discreteRaster.getDataBuffer()).getData();
        DataBuffer data = raster.getDataBuffer();
        if (data instanceof DataBufferByte) {
            byte[] pixels = ((DataBufferByte)data).getData();
            for (int i = 0; i < pixels.length; ++i) {
                discretData[i] = this.getRGB(pixels[i]);
            }
        } else {
            short[] pixels = ((DataBufferUShort)data).getData();
            for (int i = 0; i < pixels.length; ++i) {
                discretData[i] = this.getRGB(pixels[i]);
            }
        }
        return new BufferedImage(cm, discreteRaster, false, null);
    }

    private static abstract class LUT {
        final int mask;

        LUT(int bits) {
            this.mask = (1 << bits) - 1;
        }

        public static LUT create(int bits, byte[] r, byte[] g, byte[] b, int rOffset, int gOffset, int bOffset) {
            return r.length == g.length && g.length == b.length && rOffset == gOffset && gOffset == bOffset ? new Packed(bits, r, g, b, rOffset) : new PerColor(bits, r, g, b, rOffset, gOffset, bOffset);
        }

        int index(int pixel, int offset, int length) {
            return Math.min(Math.max(0, (pixel & this.mask) - offset), length - 1);
        }

        abstract int getRed(int var1);

        abstract int getGreen(int var1);

        abstract int getBlue(int var1);

        abstract int getAlpha(int var1);

        abstract int getRGB(int var1);

        static class Packed
        extends LUT {
            final int offset;
            final int[] rgb;

            Packed(int bits, byte[] r, byte[] g, byte[] b, int offset) {
                super(bits);
                int length = r.length;
                this.offset = offset;
                this.rgb = new int[length];
                for (int i = 0; i < r.length; ++i) {
                    this.rgb[i] = 0xFF000000 | (r[i] & 0xFF) << 16 | (g[i] & 0xFF) << 8 | b[i] & 0xFF;
                }
            }

            Packed(int bits, int[] rgb) {
                super(bits);
                this.offset = 0;
                this.rgb = rgb;
            }

            @Override
            public int getAlpha(int pixel) {
                return this.rgb[this.index(pixel, this.offset, this.rgb.length)] >> 24 & 0xFF;
            }

            @Override
            public int getRed(int pixel) {
                return this.rgb[this.index(pixel, this.offset, this.rgb.length)] >> 16 & 0xFF;
            }

            @Override
            public int getGreen(int pixel) {
                return this.rgb[this.index(pixel, this.offset, this.rgb.length)] >> 8 & 0xFF;
            }

            @Override
            public int getBlue(int pixel) {
                return this.rgb[this.index(pixel, this.offset, this.rgb.length)] & 0xFF;
            }

            @Override
            public int getRGB(int pixel) {
                return this.rgb[this.index(pixel, this.offset, this.rgb.length)];
            }
        }

        static class PerColor
        extends LUT {
            final byte[] r;
            final byte[] g;
            final byte[] b;
            final int rOffset;
            final int gOffset;
            final int bOffset;

            PerColor(int bits, byte[] r, byte[] g, byte[] b, int rOffset, int gbOffset, int bOffset) {
                super(bits);
                this.r = r;
                this.g = g;
                this.b = b;
                this.rOffset = rOffset;
                this.gOffset = gbOffset;
                this.bOffset = bOffset;
            }

            @Override
            public int getAlpha(int pixel) {
                return 255;
            }

            @Override
            public int getRed(int pixel) {
                return this.value(pixel, this.rOffset, this.r);
            }

            @Override
            public int getGreen(int pixel) {
                return this.value(pixel, this.gOffset, this.g);
            }

            @Override
            public int getBlue(int pixel) {
                return this.value(pixel, this.bOffset, this.b);
            }

            @Override
            public int getRGB(int pixel) {
                return 0xFF000000 | this.value(pixel, this.rOffset, this.r) << 16 | this.value(pixel, this.gOffset, this.g) << 8 | this.value(pixel, this.bOffset, this.b);
            }

            int value(int pixel, int offset, byte[] lut) {
                return lut[this.index(pixel, offset, lut.length)] & 0xFF;
            }
        }
    }

    private static class InflateSegmentedLut {
        final int[] segm;
        final byte[] data;
        int readPos;
        int writePos;

        private InflateSegmentedLut(int[] segm, int readPos, byte[] data, int writePos) {
            this.segm = segm;
            this.data = data;
            this.readPos = readPos;
            this.writePos = writePos;
        }

        private int inflate(int segs, int y0) {
            block5: while (segs < 0 ? this.readPos < this.segm.length : segs-- > 0) {
                int segPos = this.readPos;
                int op = this.read();
                int n = this.read();
                switch (op) {
                    case 0: {
                        y0 = this.discreteSegment(n);
                        continue block5;
                    }
                    case 1: {
                        if (this.writePos == 0) {
                            throw new IllegalArgumentException("Linear segment cannot be the first segment");
                        }
                        y0 = this.linearSegment(n, y0, this.read());
                        continue block5;
                    }
                    case 2: {
                        if (segs >= 0) {
                            throw new IllegalArgumentException("nested indirect segment at index " + segPos);
                        }
                        y0 = this.indirectSegment(n, y0);
                        continue block5;
                    }
                }
                throw new IllegalArgumentException("illegal op code " + op + " at index" + segPos);
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
            while (n-- > 0) {
                this.write(this.read());
            }
            return this.segm[this.readPos - 1] & 0xFFFF;
        }

        private int linearSegment(int n, int y0, int y1) {
            int dy = y1 - y0;
            for (int j = 1; j <= n; ++j) {
                this.write(y0 + dy * j / n);
            }
            return y1;
        }

        private int indirectSegment(int n, int y0) {
            int readPos = this.read() | this.read() << 16;
            return new InflateSegmentedLut(this.segm, readPos, this.data, this.writePos).inflate(n, y0);
        }
    }
}

