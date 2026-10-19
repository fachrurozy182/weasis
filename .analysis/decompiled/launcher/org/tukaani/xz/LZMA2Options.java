/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.IOException;
import java.io.InputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.FilterEncoder;
import org.tukaani.xz.FilterOptions;
import org.tukaani.xz.FinishableOutputStream;
import org.tukaani.xz.LZMA2Encoder;
import org.tukaani.xz.LZMA2InputStream;
import org.tukaani.xz.LZMA2OutputStream;
import org.tukaani.xz.UncompressedLZMA2OutputStream;
import org.tukaani.xz.UnsupportedOptionsException;

public final class LZMA2Options
extends FilterOptions {
    public static final int PRESET_MIN = 0;
    public static final int PRESET_MAX = 9;
    public static final int PRESET_DEFAULT = 6;
    public static final int DICT_SIZE_MIN = 4096;
    public static final int DICT_SIZE_MAX = 0x30000000;
    public static final int DICT_SIZE_DEFAULT = 0x800000;
    public static final int LC_LP_MAX = 4;
    public static final int LC_DEFAULT = 3;
    public static final int LP_DEFAULT = 0;
    public static final int PB_MAX = 4;
    public static final int PB_DEFAULT = 2;
    public static final int MODE_UNCOMPRESSED = 0;
    public static final int MODE_FAST = 1;
    public static final int MODE_NORMAL = 2;
    public static final int NICE_LEN_MIN = 8;
    public static final int NICE_LEN_MAX = 273;
    public static final int MF_HC4 = 4;
    public static final int MF_BT4 = 20;
    private static final int[] presetToDictSize = new int[]{262144, 0x100000, 0x200000, 0x400000, 0x400000, 0x800000, 0x800000, 0x1000000, 0x2000000, 0x4000000};
    private static final int[] presetToDepthLimit = new int[]{4, 8, 24, 48};
    private int dictSize;
    private byte[] presetDict = null;
    private int lc;
    private int lp;
    private int pb;
    private int mode;
    private int niceLen;
    private int mf;
    private int depthLimit;

    public LZMA2Options() {
        try {
            this.setPreset(6);
        }
        catch (UnsupportedOptionsException e) {
            assert (false);
            throw new RuntimeException();
        }
    }

    public LZMA2Options(int preset) throws UnsupportedOptionsException {
        this.setPreset(preset);
    }

    public LZMA2Options(int dictSize, int lc, int lp, int pb, int mode, int niceLen, int mf, int depthLimit) throws UnsupportedOptionsException {
        this.setDictSize(dictSize);
        this.setLcLp(lc, lp);
        this.setPb(pb);
        this.setMode(mode);
        this.setNiceLen(niceLen);
        this.setMatchFinder(mf);
        this.setDepthLimit(depthLimit);
    }

    public void setPreset(int preset) throws UnsupportedOptionsException {
        if (preset < 0 || preset > 9) {
            throw new UnsupportedOptionsException("Unsupported preset: " + preset);
        }
        this.lc = 3;
        this.lp = 0;
        this.pb = 2;
        this.dictSize = presetToDictSize[preset];
        if (preset <= 3) {
            this.mode = 1;
            this.mf = 4;
            this.niceLen = preset <= 1 ? 128 : 273;
            this.depthLimit = presetToDepthLimit[preset];
        } else {
            this.mode = 2;
            this.mf = 20;
            this.niceLen = preset == 4 ? 16 : (preset == 5 ? 32 : 64);
            this.depthLimit = 0;
        }
    }

    public void setDictSize(int dictSize) throws UnsupportedOptionsException {
        if (dictSize < 4096) {
            throw new UnsupportedOptionsException("LZMA2 dictionary size must be at least 4 KiB: " + dictSize + " B");
        }
        if (dictSize > 0x30000000) {
            throw new UnsupportedOptionsException("LZMA2 dictionary size must not exceed 768 MiB: " + dictSize + " B");
        }
        this.dictSize = dictSize;
    }

    public int getDictSize() {
        return this.dictSize;
    }

    public void setPresetDict(byte[] presetDict) {
        this.presetDict = presetDict;
    }

    public byte[] getPresetDict() {
        return this.presetDict;
    }

    public void setLcLp(int lc, int lp) throws UnsupportedOptionsException {
        if (lc < 0 || lp < 0 || lc > 4 || lp > 4 || lc + lp > 4) {
            throw new UnsupportedOptionsException("lc + lp must not exceed 4: " + lc + " + " + lp);
        }
        this.lc = lc;
        this.lp = lp;
    }

    public void setLc(int lc) throws UnsupportedOptionsException {
        this.setLcLp(lc, this.lp);
    }

    public void setLp(int lp) throws UnsupportedOptionsException {
        this.setLcLp(this.lc, lp);
    }

    public int getLc() {
        return this.lc;
    }

    public int getLp() {
        return this.lp;
    }

    public void setPb(int pb) throws UnsupportedOptionsException {
        if (pb < 0 || pb > 4) {
            throw new UnsupportedOptionsException("pb must not exceed 4: " + pb);
        }
        this.pb = pb;
    }

    public int getPb() {
        return this.pb;
    }

    public void setMode(int mode) throws UnsupportedOptionsException {
        if (mode < 0 || mode > 2) {
            throw new UnsupportedOptionsException("Unsupported compression mode: " + mode);
        }
        this.mode = mode;
    }

    public int getMode() {
        return this.mode;
    }

    public void setNiceLen(int niceLen) throws UnsupportedOptionsException {
        if (niceLen < 8) {
            throw new UnsupportedOptionsException("Minimum nice length of matches is 8 bytes: " + niceLen);
        }
        if (niceLen > 273) {
            throw new UnsupportedOptionsException("Maximum nice length of matches is 273: " + niceLen);
        }
        this.niceLen = niceLen;
    }

    public int getNiceLen() {
        return this.niceLen;
    }

    public void setMatchFinder(int mf) throws UnsupportedOptionsException {
        if (mf != 4 && mf != 20) {
            throw new UnsupportedOptionsException("Unsupported match finder: " + mf);
        }
        this.mf = mf;
    }

    public int getMatchFinder() {
        return this.mf;
    }

    public void setDepthLimit(int depthLimit) throws UnsupportedOptionsException {
        if (depthLimit < 0) {
            throw new UnsupportedOptionsException("Depth limit cannot be negative: " + depthLimit);
        }
        this.depthLimit = depthLimit;
    }

    public int getDepthLimit() {
        return this.depthLimit;
    }

    @Override
    public int getEncoderMemoryUsage() {
        return this.mode == 0 ? UncompressedLZMA2OutputStream.getMemoryUsage() : LZMA2OutputStream.getMemoryUsage(this);
    }

    @Override
    public FinishableOutputStream getOutputStream(FinishableOutputStream out, ArrayCache arrayCache) {
        if (this.mode == 0) {
            return new UncompressedLZMA2OutputStream(out, arrayCache);
        }
        return new LZMA2OutputStream(out, this, arrayCache);
    }

    @Override
    public int getDecoderMemoryUsage() {
        int d = this.dictSize - 1;
        d |= d >>> 2;
        d |= d >>> 3;
        d |= d >>> 4;
        d |= d >>> 8;
        d |= d >>> 16;
        return LZMA2InputStream.getMemoryUsage(d + 1);
    }

    @Override
    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) throws IOException {
        return new LZMA2InputStream(in, this.dictSize, this.presetDict, arrayCache);
    }

    @Override
    FilterEncoder getFilterEncoder() {
        return new LZMA2Encoder(this);
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            assert (false);
            throw new RuntimeException();
        }
    }
}

