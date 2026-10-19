/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.CorruptedInputException;
import org.tukaani.xz.MemoryLimitException;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.XZIOException;
import org.tukaani.xz.lz.LZDecoder;
import org.tukaani.xz.lzma.LZMADecoder;
import org.tukaani.xz.rangecoder.RangeDecoderFromStream;

public class LZMAInputStream
extends InputStream {
    public static final int DICT_SIZE_MAX = 0x7FFFFFF0;
    private InputStream in;
    private ArrayCache arrayCache;
    private LZDecoder lz;
    private RangeDecoderFromStream rc;
    private LZMADecoder lzma;
    private boolean endReached = false;
    private boolean relaxedEndCondition = false;
    private final byte[] tempBuf = new byte[1];
    private long remainingSize;
    private IOException exception = null;

    public static int getMemoryUsage(int dictSize, byte propsByte) throws UnsupportedOptionsException, CorruptedInputException {
        if (dictSize < 0 || dictSize > 0x7FFFFFF0) {
            throw new UnsupportedOptionsException("LZMA dictionary is too big for this implementation");
        }
        int props = propsByte & 0xFF;
        if (props > 224) {
            throw new CorruptedInputException("Invalid LZMA properties byte");
        }
        int lp = (props %= 45) / 9;
        int lc = props - lp * 9;
        return LZMAInputStream.getMemoryUsage(dictSize, lc, lp);
    }

    public static int getMemoryUsage(int dictSize, int lc, int lp) {
        if (lc < 0 || lc > 8 || lp < 0 || lp > 4) {
            throw new IllegalArgumentException("Invalid lc or lp");
        }
        return 10 + LZMAInputStream.getDictSize(dictSize) / 1024 + (1536 << lc + lp) / 1024;
    }

    private static int getDictSize(int dictSize) {
        if (dictSize < 0 || dictSize > 0x7FFFFFF0) {
            throw new IllegalArgumentException("LZMA dictionary is too big for this implementation");
        }
        if (dictSize < 4096) {
            dictSize = 4096;
        }
        return dictSize + 15 & 0xFFFFFFF0;
    }

    public LZMAInputStream(InputStream in) throws IOException {
        this(in, -1);
    }

    public LZMAInputStream(InputStream in, ArrayCache arrayCache) throws IOException {
        this(in, -1, arrayCache);
    }

    public LZMAInputStream(InputStream in, int memoryLimit) throws IOException {
        this(in, memoryLimit, ArrayCache.getDefaultCache());
    }

    public LZMAInputStream(InputStream in, int memoryLimit, ArrayCache arrayCache) throws IOException {
        DataInputStream inData = new DataInputStream(in);
        byte propsByte = inData.readByte();
        int dictSize = 0;
        for (int i = 0; i < 4; ++i) {
            dictSize |= inData.readUnsignedByte() << 8 * i;
        }
        long uncompSize = 0L;
        for (int i = 0; i < 8; ++i) {
            uncompSize |= (long)inData.readUnsignedByte() << 8 * i;
        }
        int memoryNeeded = LZMAInputStream.getMemoryUsage(dictSize, propsByte);
        if (memoryLimit != -1 && memoryNeeded > memoryLimit) {
            throw new MemoryLimitException(memoryNeeded, memoryLimit);
        }
        this.relaxedEndCondition = true;
        this.initialize(in, uncompSize, propsByte, dictSize, null, arrayCache);
    }

    public LZMAInputStream(InputStream in, long uncompSize, byte propsByte, int dictSize) throws IOException {
        this.initialize(in, uncompSize, propsByte, dictSize, null, ArrayCache.getDefaultCache());
    }

    public LZMAInputStream(InputStream in, long uncompSize, byte propsByte, int dictSize, byte[] presetDict) throws IOException {
        this.initialize(in, uncompSize, propsByte, dictSize, presetDict, ArrayCache.getDefaultCache());
    }

    public LZMAInputStream(InputStream in, long uncompSize, byte propsByte, int dictSize, byte[] presetDict, ArrayCache arrayCache) throws IOException {
        this.initialize(in, uncompSize, propsByte, dictSize, presetDict, arrayCache);
    }

    public LZMAInputStream(InputStream in, long uncompSize, int lc, int lp, int pb, int dictSize, byte[] presetDict) throws IOException {
        this.initialize(in, uncompSize, lc, lp, pb, dictSize, presetDict, ArrayCache.getDefaultCache());
    }

    public LZMAInputStream(InputStream in, long uncompSize, int lc, int lp, int pb, int dictSize, byte[] presetDict, ArrayCache arrayCache) throws IOException {
        this.initialize(in, uncompSize, lc, lp, pb, dictSize, presetDict, arrayCache);
    }

    private void initialize(InputStream in, long uncompSize, byte propsByte, int dictSize, byte[] presetDict, ArrayCache arrayCache) throws IOException {
        if (uncompSize < -1L) {
            throw new UnsupportedOptionsException("Uncompressed size is too big");
        }
        int props = propsByte & 0xFF;
        if (props > 224) {
            throw new CorruptedInputException("Invalid LZMA properties byte");
        }
        int pb = props / 45;
        int lp = (props -= pb * 9 * 5) / 9;
        int lc = props - lp * 9;
        if (dictSize < 0 || dictSize > 0x7FFFFFF0) {
            throw new UnsupportedOptionsException("LZMA dictionary is too big for this implementation");
        }
        this.initialize(in, uncompSize, lc, lp, pb, dictSize, presetDict, arrayCache);
    }

    private void initialize(InputStream in, long uncompSize, int lc, int lp, int pb, int dictSize, byte[] presetDict, ArrayCache arrayCache) throws IOException {
        if (uncompSize < -1L || lc < 0 || lc > 8 || lp < 0 || lp > 4 || pb < 0 || pb > 4) {
            throw new IllegalArgumentException();
        }
        this.in = in;
        this.arrayCache = arrayCache;
        dictSize = LZMAInputStream.getDictSize(dictSize);
        if (uncompSize >= 0L && (long)dictSize > uncompSize) {
            dictSize = LZMAInputStream.getDictSize((int)uncompSize);
        }
        this.lz = new LZDecoder(LZMAInputStream.getDictSize(dictSize), presetDict, arrayCache);
        this.rc = new RangeDecoderFromStream(in);
        this.lzma = new LZMADecoder(this.lz, this.rc, lc, lp, pb);
        this.remainingSize = uncompSize;
    }

    public void enableRelaxedEndCondition() {
        this.relaxedEndCondition = true;
    }

    @Override
    public int read() throws IOException {
        return this.read(this.tempBuf, 0, 1) == -1 ? -1 : this.tempBuf[0] & 0xFF;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len < 0 || off + len > buf.length) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        if (this.in == null) {
            throw new XZIOException("Stream closed");
        }
        if (this.exception != null) {
            throw this.exception;
        }
        if (this.endReached) {
            return -1;
        }
        try {
            int size = 0;
            while (len > 0) {
                int copySizeMax = len;
                if (this.remainingSize >= 0L && this.remainingSize < (long)len) {
                    copySizeMax = (int)this.remainingSize;
                }
                this.lz.setLimit(copySizeMax);
                try {
                    this.lzma.decode();
                }
                catch (CorruptedInputException e) {
                    if (this.remainingSize != -1L || !this.lzma.endMarkerDetected()) {
                        throw e;
                    }
                    this.endReached = true;
                    this.rc.normalize();
                }
                int copiedSize = this.lz.flush(buf, off);
                off += copiedSize;
                len -= copiedSize;
                size += copiedSize;
                if (this.remainingSize >= 0L) {
                    this.remainingSize -= (long)copiedSize;
                    assert (this.remainingSize >= 0L);
                    if (this.remainingSize == 0L) {
                        this.endReached = true;
                    }
                }
                if (!this.endReached) continue;
                if (this.lz.hasPending()) {
                    throw new CorruptedInputException();
                }
                if (!this.rc.isFinished()) {
                    if (this.remainingSize == -1L || !this.relaxedEndCondition) {
                        throw new CorruptedInputException();
                    }
                    this.lz.setLimit(1);
                    boolean endMarkerDetected = false;
                    try {
                        this.lzma.decode();
                    }
                    catch (CorruptedInputException e) {
                        if (!this.lzma.endMarkerDetected()) {
                            throw e;
                        }
                        endMarkerDetected = true;
                        this.rc.normalize();
                    }
                    if (!endMarkerDetected || !this.rc.isFinished()) {
                        throw new CorruptedInputException();
                    }
                    assert (!this.lz.hasPending());
                    assert (this.lz.hasSpace());
                }
                this.putArraysToCache();
                return size == 0 ? -1 : size;
            }
            return size;
        }
        catch (IOException e) {
            this.exception = e;
            throw e;
        }
    }

    private void putArraysToCache() {
        if (this.lz != null) {
            this.lz.putArraysToCache(this.arrayCache);
            this.lz = null;
        }
    }

    @Override
    public void close() throws IOException {
        if (this.in != null) {
            this.putArraysToCache();
            try {
                this.in.close();
            }
            finally {
                this.in = null;
            }
        }
    }
}

