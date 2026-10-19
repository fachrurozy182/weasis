/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.util;

import java.awt.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ColorUtil {
    private static final int[][] SRGBtoLinearRGBPre = new int[256][256];
    private static final int[] SRGBtoLinearRGB = new int[256];
    private static final int[][] LinearRGBtoSRGBPre = new int[256][256];
    private static final int[] LinearRGBtoSRGB = new int[256];

    private ColorUtil() {
    }

    public static int div255(int x) {
        return (x += 128) + (x >> 8) >> 8;
    }

    public static int computeLuminance(int r, int g, int b) {
        return ColorUtil.toRgbRange(0.2125 * (double)r + 0.7164 * (double)g + 0.0712 * (double)b);
    }

    public static int toRgbRange(double value) {
        return (int)Math.max(Math.min(Math.round(value), 255L), 0L);
    }

    public static Color withAlpha(@NotNull Color c, float alpha) {
        int a = Math.max(Math.min(255, (int)(alpha * 255.0f)), 0);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    public static String toString(@Nullable Color c) {
        if (c == null) {
            return "null";
        }
        return String.format("Color[%d,%d,%d,%d]", c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }

    public static void convertRGBPretoHSL(int r, int g, int b, int a, float @NotNull [] hsl) {
        float s;
        float h;
        float maxComponent;
        float minComponent;
        if (r < 0) {
            r = 0;
        } else if (r > 255) {
            r = 255;
        }
        if (g < 0) {
            g = 0;
        } else if (g > 255) {
            g = 255;
        }
        if (b < 0) {
            b = 0;
        } else if (b > 255) {
            b = 255;
        }
        float componentR = (float)r / (float)a;
        float componentG = (float)g / (float)a;
        float componentB = (float)b / (float)a;
        if (componentR > componentG) {
            minComponent = componentG;
            maxComponent = componentR;
        } else {
            minComponent = componentR;
            maxComponent = componentG;
        }
        if (componentB > maxComponent) {
            maxComponent = componentB;
        }
        if (componentB < minComponent) {
            minComponent = componentB;
        }
        float deltaMax = maxComponent - minComponent;
        float l = (maxComponent + minComponent) / 2.0f;
        if (deltaMax - 0.01f <= 0.0f) {
            h = 0.0f;
            s = 0.0f;
        } else {
            if (l < 0.5f) {
                assert (maxComponent + minComponent != 0.0f);
                s = deltaMax / (maxComponent + minComponent);
            } else {
                s = deltaMax / (2.0f - maxComponent - minComponent);
            }
            assert (deltaMax > 0.0f);
            float deltaR = ((maxComponent - componentR) / 6.0f + deltaMax / 2.0f) / deltaMax;
            float deltaG = ((maxComponent - componentG) / 6.0f + deltaMax / 2.0f) / deltaMax;
            float deltaB = ((maxComponent - componentB) / 6.0f + deltaMax / 2.0f) / deltaMax;
            h = componentR == maxComponent ? deltaB - deltaG : (componentG == maxComponent ? 0.33333334f + deltaR - deltaB : 0.6666667f + deltaG - deltaR);
            if (h < 0.0f) {
                h += 1.0f;
            }
            if (h > 1.0f) {
                h -= 1.0f;
            }
        }
        hsl[0] = h;
        hsl[1] = s;
        hsl[2] = l;
    }

    public static void convertHSLtoRGB(float h, float s, float l, int @NotNull [] rgb) {
        int b;
        int g;
        int r;
        if (h < 0.0f) {
            h = 0.0f;
        } else if (h > 1.0f) {
            h = 1.0f;
        }
        if (s < 0.0f) {
            s = 0.0f;
        } else if (s > 1.0f) {
            s = 1.0f;
        }
        if (l < 0.0f) {
            l = 0.0f;
        } else if (l > 1.0f) {
            l = 1.0f;
        }
        if (s - 0.01f <= 0.0f) {
            r = (int)(l * 255.0f);
            g = (int)(l * 255.0f);
            b = (int)(l * 255.0f);
        } else {
            float y = l < 0.5f ? l * (1.0f + s) : l + s - s * l;
            float x = 2.0f * l - y;
            r = (int)(255.0f * ColorUtil.hue2RGB(x, y, h + 0.33333334f));
            g = (int)(255.0f * ColorUtil.hue2RGB(x, y, h));
            b = (int)(255.0f * ColorUtil.hue2RGB(x, y, h - 0.33333334f));
        }
        rgb[0] = r;
        rgb[1] = g;
        rgb[2] = b;
    }

    private static float hue2RGB(float v1, float v2, float vH) {
        if (vH < 0.0f) {
            vH += 1.0f;
        }
        if (vH > 1.0f) {
            vH -= 1.0f;
        }
        if (6.0f * vH < 1.0f) {
            return v1 + (v2 - v1) * 6.0f * vH;
        }
        if (2.0f * vH < 1.0f) {
            return v2;
        }
        if (3.0f * vH < 2.0f) {
            return v1 + (v2 - v1) * (0.6666667f - vH) * 6.0f;
        }
        return v1;
    }

    public static void sRGBtoLinearRGBinPlace(int @NotNull [] argb) {
        argb[0] = SRGBtoLinearRGB[argb[0]];
        argb[1] = SRGBtoLinearRGB[argb[1]];
        argb[2] = SRGBtoLinearRGB[argb[2]];
    }

    public static void linearRGBtoSRGBinPlace(int @NotNull [] argb) {
        argb[0] = LinearRGBtoSRGB[argb[0]];
        argb[1] = LinearRGBtoSRGB[argb[1]];
        argb[2] = LinearRGBtoSRGB[argb[2]];
    }

    public static void sRGBtoLinearRGBPreInPlace(int @NotNull [] argb) {
        int alpha = argb[3];
        int[] table = SRGBtoLinearRGBPre[alpha];
        argb[0] = table[argb[0]];
        argb[1] = table[argb[1]];
        argb[2] = table[argb[2]];
    }

    public static void linearRGBtoSRGBPreInPlace(int @NotNull [] argb) {
        int alpha = argb[3];
        int[] table = LinearRGBtoSRGBPre[alpha];
        argb[0] = table[argb[0]];
        argb[1] = table[argb[1]];
        argb[2] = table[argb[2]];
    }

    public static int sRGBtoLinearRGBBand(int value) {
        return SRGBtoLinearRGB[value];
    }

    public static int sRGBtoLinearRGB(int argb) {
        int a = argb >>> 24;
        int r = SRGBtoLinearRGB[argb >> 16 & 0xFF];
        int g = SRGBtoLinearRGB[argb >> 8 & 0xFF];
        int b = SRGBtoLinearRGB[argb & 0xFF];
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    public static int linearRGBtoSRGBBand(int value) {
        return LinearRGBtoSRGB[value];
    }

    public static int linearRGBtoSRGB(int argb) {
        int a = argb >>> 24;
        int r = LinearRGBtoSRGB[argb >> 16 & 0xFF];
        int g = LinearRGBtoSRGB[argb >> 8 & 0xFF];
        int b = LinearRGBtoSRGB[argb & 0xFF];
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    private static int convertSRGBtoLinearRGB(int color, float alpha) {
        float factor = 255.0f * alpha;
        float input = (float)color / factor;
        float output = input <= 0.04045f ? input / 12.92f : (float)Math.pow(((double)input + 0.055) / 1.055, 2.4);
        return Math.round(output * factor);
    }

    private static int convertLinearRGBtoSRGB(int color, float alpha) {
        float factor = 255.0f * alpha;
        float input = (float)color / factor;
        float output = (double)input <= 0.0031308 ? input * 12.92f : 1.055f * (float)Math.pow(input, 0.4166666666666667) - 0.055f;
        return Math.round(output * factor);
    }

    public static int clampColor(int v) {
        return Math.max(Math.min(255, v), 0);
    }

    static {
        for (int k = 0; k < 256; ++k) {
            ColorUtil.SRGBtoLinearRGB[k] = ColorUtil.convertSRGBtoLinearRGB(k, 1.0f);
            ColorUtil.LinearRGBtoSRGB[k] = ColorUtil.convertLinearRGBtoSRGB(k, 1.0f);
        }
        ColorUtil.SRGBtoLinearRGBPre[255] = SRGBtoLinearRGB;
        ColorUtil.LinearRGBtoSRGBPre[255] = LinearRGBtoSRGB;
        for (int i = 0; i < 255; ++i) {
            int[] sRGBtoLinear = new int[256];
            int[] linearTosRGB = new int[256];
            float alpha = (float)i / 255.0f;
            for (int k = 0; k < 256; ++k) {
                sRGBtoLinear[k] = ColorUtil.convertSRGBtoLinearRGB(k, alpha);
                linearTosRGB[k] = ColorUtil.convertLinearRGBtoSRGB(k, alpha);
            }
            ColorUtil.SRGBtoLinearRGBPre[i] = sRGBtoLinear;
            ColorUtil.LinearRGBtoSRGBPre[i] = linearTosRGB;
        }
    }
}

