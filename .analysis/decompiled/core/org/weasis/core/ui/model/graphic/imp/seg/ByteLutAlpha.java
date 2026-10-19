/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 */
package org.weasis.core.ui.model.graphic.imp.seg;

import java.awt.Color;
import java.util.Arrays;
import java.util.Objects;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public record ByteLutAlpha(String name, byte[][] lutTable) {
    public static final int CHANNEL_COUNT = 4;
    public static final int CHANNEL_SIZE = 256;
    public static final int A = 0;
    public static final int B = 1;
    public static final int G = 2;
    public static final int R = 3;

    public ByteLutAlpha {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(lutTable, "LUT table cannot be null");
        if (lutTable.length != 4) {
            throw new IllegalArgumentException("LUT must have exactly %d channels (ABGR)".formatted(new Object[]{4}));
        }
        for (byte[] channel : lutTable) {
            if (channel != null && channel.length == 256) continue;
            throw new IllegalArgumentException("Each LUT channel must have exactly %d values".formatted(new Object[]{256}));
        }
    }

    public static ByteLutAlpha forSegment(String name, Color color, float opacity) {
        Objects.requireNonNull(color, "Color cannot be null");
        byte[] alpha = ByteLutAlpha.buildAlphaRamp(Math.clamp((float)opacity, (float)0.0f, (float)1.0f));
        byte[] blue = ByteLutAlpha.filled((byte)color.getBlue());
        byte[] green = ByteLutAlpha.filled((byte)color.getGreen());
        byte[] red = ByteLutAlpha.filled((byte)color.getRed());
        return new ByteLutAlpha(name, new byte[][]{alpha, blue, green, red});
    }

    public static ByteLutAlpha fromColorLut(String name, byte[][] bgrLut, float opacity) {
        return ByteLutAlpha.fromColorLut(name, bgrLut, opacity, 0, 255);
    }

    public static ByteLutAlpha fromColorLut(String name, byte[][] bgrLut, float opacity, int transparentBelow, int opaqueFrom) {
        Objects.requireNonNull(bgrLut, "Color LUT cannot be null");
        if (bgrLut.length < 3) {
            throw new IllegalArgumentException("Color LUT must have at least 3 channels (BGR)");
        }
        for (int c = 0; c < 3; ++c) {
            if (bgrLut[c] != null && bgrLut[c].length == 256) continue;
            throw new IllegalArgumentException("Each color LUT channel must have exactly %d values".formatted(new Object[]{256}));
        }
        byte[] alpha = ByteLutAlpha.buildAlphaRamp(Math.clamp((float)opacity, (float)0.0f, (float)1.0f), transparentBelow, opaqueFrom);
        return new ByteLutAlpha(name, new byte[][]{alpha, (byte[])bgrLut[0].clone(), (byte[])bgrLut[1].clone(), (byte[])bgrLut[2].clone()});
    }

    private static byte[] filled(byte value) {
        byte[] out = new byte[256];
        Arrays.fill(out, value);
        return out;
    }

    private static byte[] buildAlphaRamp(float clampedOpacity) {
        return ByteLutAlpha.buildAlphaRamp(clampedOpacity, 0, 255);
    }

    private static byte[] buildAlphaRamp(float clampedOpacity, int transparentBelow, int opaqueFrom) {
        byte[] alpha = new byte[256];
        int start = Math.clamp((long)transparentBelow, (int)0, (int)255);
        int end = Math.clamp((long)opaqueFrom, (int)(start + 1), (int)255);
        int full = Math.round(clampedOpacity * 255.0f);
        for (int i = start + 1; i < 256; ++i) {
            int a = i >= end ? full : Math.round((float)(i - start) / (float)(end - start) * (float)full);
            alpha[i] = (byte)Math.clamp((long)a, (int)0, (int)255);
        }
        return alpha;
    }

    public Mat toLutMat() {
        Mat lutMat = new Mat(1, 256, CvType.CV_8UC4);
        byte[] interleaved = new byte[1024];
        byte[] aCh = this.lutTable[0];
        byte[] bCh = this.lutTable[1];
        byte[] gCh = this.lutTable[2];
        byte[] rCh = this.lutTable[3];
        for (int i = 0; i < 256; ++i) {
            int o = i << 2;
            interleaved[o] = aCh[i];
            interleaved[o + 1] = bCh[i];
            interleaved[o + 2] = gCh[i];
            interleaved[o + 3] = rCh[i];
        }
        lutMat.put(0, 0, interleaved);
        return lutMat;
    }

    public String toString() {
        return this.name;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        String otherName;
        Object object;
        if (this == o) return true;
        if (!(o instanceof ByteLutAlpha)) return false;
        ByteLutAlpha byteLutAlpha = (ByteLutAlpha)((Object)o);
        try {
            object = byteLutAlpha.name();
            otherName = object;
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        Object otherLut = object = (Object)byteLutAlpha.lutTable();
        if (!this.name.equals(otherName)) return false;
        if (!Arrays.deepEquals((Object[])this.lutTable, (Object[])otherLut)) return false;
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.name, Arrays.deepHashCode((Object[])this.lutTable));
    }
}

