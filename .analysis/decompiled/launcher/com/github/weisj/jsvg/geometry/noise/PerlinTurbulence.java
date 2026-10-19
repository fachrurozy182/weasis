/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.geometry.noise;

import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.Nullable;

public final class PerlinTurbulence {
    private static final int RAND_m = Integer.MAX_VALUE;
    private static final int RAND_a = 16807;
    private static final int RAND_q = 127773;
    private static final int RAND_r = 2836;
    private static final int BSize = 256;
    private static final int BM = 255;
    private static final double PerlinN = 4096.0;
    private final int[] uLatticeSelector = new int[257];
    private final double[] fGradient = new double[2056];
    private final int numOctaves;
    private final double xFrequency;
    private final double yFrequency;

    public PerlinTurbulence(int seed, int numOctaves, double xFrequency, double yFrequency) {
        this.numOctaves = numOctaves;
        this.xFrequency = xFrequency;
        this.yFrequency = yFrequency;
        this.init(seed);
    }

    private static int setupSeed(int seed) {
        if (seed <= 0) {
            seed = -(seed % 0x7FFFFFFE) + 1;
        }
        if (seed > 0x7FFFFFFE) {
            seed = 0x7FFFFFFE;
        }
        return seed;
    }

    private static int random(int seed) {
        int result = 16807 * (seed % 127773) - 2836 * (seed / 127773);
        if (result <= 0) {
            result += Integer.MAX_VALUE;
        }
        return result;
    }

    private void init(int seed) {
        int j;
        int i;
        int k;
        int lSeed = PerlinTurbulence.setupSeed(seed);
        for (k = 0; k < 4; ++k) {
            for (i = 0; i < 256; ++i) {
                double v;
                double u;
                do {
                    lSeed = PerlinTurbulence.random(lSeed);
                    u = (double)(lSeed % 512) - 256.0;
                    lSeed = PerlinTurbulence.random(lSeed);
                    v = (double)(lSeed % 512) - 256.0;
                } while (u == 0.0 && v == 0.0);
                double s = Math.sqrt(u * u + v * v);
                double si = 1.0 / s;
                this.fGradient[i * 8 + k * 2] = u * si;
                this.fGradient[i * 8 + k * 2 + 1] = v * si;
            }
        }
        for (i = 0; i < 256; ++i) {
            this.uLatticeSelector[i] = i;
        }
        while (--i > 0) {
            k = this.uLatticeSelector[i];
            lSeed = PerlinTurbulence.random(lSeed);
            j = lSeed % 256;
            this.uLatticeSelector[i] = this.uLatticeSelector[j];
            this.uLatticeSelector[j] = k;
            int s1 = i << 3;
            int s2 = j << 3;
            for (j = 0; j < 8; ++j) {
                double s = this.fGradient[s1 + j];
                this.fGradient[s1 + j] = this.fGradient[s2 + j];
                this.fGradient[s2 + j] = s;
            }
        }
        this.uLatticeSelector[256] = this.uLatticeSelector[0];
        for (j = 0; j < 8; ++j) {
            this.fGradient[2048 + j] = this.fGradient[j];
        }
    }

    private static double curve(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private void noise2(double[] noiseChannels, double vec0, double vec1, @Nullable StitchInfo stitchInfo) {
        double t = vec0 + 4096.0;
        int bx0 = (int)t;
        int bx1 = bx0 + 1;
        double rx0 = t - (double)bx0;
        double rx1 = rx0 - 1.0;
        double sx = PerlinTurbulence.curve(rx0);
        t = vec1 + 4096.0;
        int by0 = (int)t;
        int by1 = by0 + 1;
        double ry0 = t - (double)((int)t);
        double ry1 = ry0 - 1.0;
        double sy = PerlinTurbulence.curve(ry0);
        if (stitchInfo != null) {
            if (bx0 >= stitchInfo.wrapX) {
                bx0 -= stitchInfo.width;
            }
            if (bx1 >= stitchInfo.wrapX) {
                bx1 -= stitchInfo.width;
            }
            if (by0 >= stitchInfo.wrapY) {
                by0 -= stitchInfo.height;
            }
            if (by1 >= stitchInfo.wrapY) {
                by1 -= stitchInfo.height;
            }
        }
        int i = this.uLatticeSelector[bx0 &= 0xFF];
        int j = this.uLatticeSelector[bx1 &= 0xFF];
        int b00 = (i + (by0 &= 0xFF) & 0xFF) << 3;
        int b10 = (j + by0 & 0xFF) << 3;
        int b01 = (i + (by1 &= 0xFF) & 0xFF) << 3;
        int b11 = (j + by1 & 0xFF) << 3;
        for (int channelIndex = 0; channelIndex < noiseChannels.length; ++channelIndex) {
            int offset = 2 * channelIndex;
            noiseChannels[channelIndex] = PerlinTurbulence.lerp(sy, PerlinTurbulence.lerp(sx, rx0 * this.fGradient[b00 + offset] + ry0 * this.fGradient[b00 + offset + 1], rx1 * this.fGradient[b10 + offset] + ry0 * this.fGradient[b10 + offset + 1]), PerlinTurbulence.lerp(sx, rx0 * this.fGradient[b01 + offset] + ry1 * this.fGradient[b01 + offset + 1], rx1 * this.fGradient[b11 + offset] + ry1 * this.fGradient[b11 + offset + 1]));
        }
    }

    public void turbulence(double[] turbulenceChannels, double pointX, double pointY, boolean fractalSum, @Nullable StitchInfo stitchInfo, @Nullable Rectangle2D.Double tile) {
        double[] dArray;
        double baseFrequencyX = this.xFrequency;
        double baseFrequencyY = this.yFrequency;
        if (stitchInfo != null) {
            assert (tile != null);
            if (baseFrequencyX != 0.0) {
                baseFrequencyX = this.adjustFrequency(baseFrequencyX, tile.width);
            }
            if (baseFrequencyY != 0.0) {
                baseFrequencyY = this.adjustFrequency(baseFrequencyY, tile.height);
            }
            stitchInfo.width = (int)(tile.width * baseFrequencyX + 0.5);
            stitchInfo.wrapX = (int)(tile.x * baseFrequencyX + 4096.0 + (double)stitchInfo.width);
            stitchInfo.height = (int)(tile.height * baseFrequencyY + 0.5);
            stitchInfo.wrapY = (int)(tile.y * baseFrequencyY + 4096.0 + (double)stitchInfo.height);
        }
        if (fractalSum) {
            double[] dArray2 = new double[4];
            dArray2[0] = 127.5;
            dArray2[1] = 127.5;
            dArray2[2] = 127.5;
            dArray = dArray2;
            dArray2[3] = 127.5;
        } else {
            double[] dArray3 = new double[4];
            dArray3[0] = 0.0;
            dArray3[1] = 0.0;
            dArray3[2] = 0.0;
            dArray = dArray3;
            dArray3[3] = 0.0;
        }
        double[] fSum = dArray;
        double vec0 = pointX * baseFrequencyX;
        double vec1 = pointY * baseFrequencyY;
        double ratio = fractalSum ? 127.5 : 255.0;
        for (int nOctave = 0; nOctave < this.numOctaves; ++nOctave) {
            int i;
            this.noise2(turbulenceChannels, vec0, vec1, stitchInfo);
            if (fractalSum) {
                for (i = 0; i < turbulenceChannels.length; ++i) {
                    int n = i;
                    fSum[n] = fSum[n] + turbulenceChannels[i] * ratio;
                }
            } else {
                for (i = 0; i < turbulenceChannels.length; ++i) {
                    int n = i;
                    fSum[n] = fSum[n] + Math.abs(turbulenceChannels[i]) * ratio;
                }
            }
            vec0 *= 2.0;
            vec1 *= 2.0;
            ratio *= 0.5;
            if (stitchInfo == null) continue;
            stitchInfo.width *= 2;
            stitchInfo.wrapX *= 2;
            stitchInfo.wrapX += 4096;
            stitchInfo.height *= 2;
            stitchInfo.wrapY *= 2;
            stitchInfo.wrapY += 4096;
        }
        System.arraycopy(fSum, 0, turbulenceChannels, 0, fSum.length);
    }

    private double adjustFrequency(double frequency, double tileSize) {
        double fHiFreq;
        double fLoFreq = Math.floor(tileSize * frequency) / tileSize;
        if (frequency / fLoFreq < (fHiFreq = Math.ceil(tileSize * frequency) / tileSize) / frequency) {
            return fLoFreq;
        }
        return fHiFreq;
    }

    public static final class StitchInfo {
        private int width;
        private int height;
        private int wrapX;
        private int wrapY;
    }
}

