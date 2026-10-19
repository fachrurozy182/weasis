/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.IOException;
import java.io.OutputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.FinishableOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.XZIOException;
import org.tukaani.xz.lz.LZEncoder;
import org.tukaani.xz.lzma.LZMAEncoder;
import org.tukaani.xz.rangecoder.RangeEncoderToStream;

public class LZMAOutputStream
extends FinishableOutputStream {
    private OutputStream out;
    private final ArrayCache arrayCache;
    private LZEncoder lz;
    private final RangeEncoderToStream rc;
    private LZMAEncoder lzma;
    private final int props;
    private final boolean useEndMarker;
    private final long expectedUncompressedSize;
    private long currentUncompressedSize = 0L;
    private boolean finished = false;
    private IOException exception = null;
    private final byte[] tempBuf = new byte[1];

    private LZMAOutputStream(OutputStream out, LZMA2Options options, boolean useHeader, boolean useEndMarker, long expectedUncompressedSize, ArrayCache arrayCache) throws IOException {
        if (out == null) {
            throw new NullPointerException();
        }
        if (expectedUncompressedSize < -1L) {
            throw new IllegalArgumentException("Invalid expected input size (less than -1)");
        }
        this.useEndMarker = useEndMarker;
        this.expectedUncompressedSize = expectedUncompressedSize;
        this.arrayCache = arrayCache;
        this.out = out;
        this.rc = new RangeEncoderToStream(out);
        int dictSize = options.getDictSize();
        this.lzma = LZMAEncoder.getInstance(this.rc, options.getLc(), options.getLp(), options.getPb(), options.getMode(), dictSize, 0, options.getNiceLen(), options.getMatchFinder(), options.getDepthLimit(), arrayCache);
        this.lz = this.lzma.getLZEncoder();
        byte[] presetDict = options.getPresetDict();
        if (presetDict != null && presetDict.length > 0) {
            if (useHeader) {
                throw new UnsupportedOptionsException("Preset dictionary cannot be used in .lzma files (try a raw LZMA stream instead)");
            }
            this.lz.setPresetDict(dictSize, presetDict);
        }
        this.props = (options.getPb() * 5 + options.getLp()) * 9 + options.getLc();
        if (useHeader) {
            int i;
            out.write(this.props);
            for (i = 0; i < 4; ++i) {
                out.write(dictSize & 0xFF);
                dictSize >>>= 8;
            }
            for (i = 0; i < 8; ++i) {
                out.write((int)(expectedUncompressedSize >>> 8 * i) & 0xFF);
            }
        }
    }

    public LZMAOutputStream(OutputStream out, LZMA2Options options, long inputSize) throws IOException {
        this(out, options, inputSize, ArrayCache.getDefaultCache());
    }

    public LZMAOutputStream(OutputStream out, LZMA2Options options, long inputSize, ArrayCache arrayCache) throws IOException {
        this(out, options, true, inputSize == -1L, inputSize, arrayCache);
    }

    public LZMAOutputStream(OutputStream out, LZMA2Options options, boolean useEndMarker) throws IOException {
        this(out, options, useEndMarker, ArrayCache.getDefaultCache());
    }

    public LZMAOutputStream(OutputStream out, LZMA2Options options, boolean useEndMarker, ArrayCache arrayCache) throws IOException {
        this(out, options, false, useEndMarker, -1L, arrayCache);
    }

    public int getProps() {
        return this.props;
    }

    public long getUncompressedSize() {
        return this.currentUncompressedSize;
    }

    @Override
    public void write(int b) throws IOException {
        this.tempBuf[0] = (byte)b;
        this.write(this.tempBuf, 0, 1);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len < 0 || off + len > buf.length) {
            throw new IndexOutOfBoundsException();
        }
        if (this.exception != null) {
            throw this.exception;
        }
        if (this.finished) {
            throw new XZIOException("Stream finished or closed");
        }
        if (this.expectedUncompressedSize != -1L && this.expectedUncompressedSize - this.currentUncompressedSize < (long)len) {
            throw new XZIOException("Expected uncompressed input size (" + this.expectedUncompressedSize + " bytes) was exceeded");
        }
        this.currentUncompressedSize += (long)len;
        try {
            while (len > 0) {
                int used = this.lz.fillWindow(buf, off, len);
                off += used;
                len -= used;
                this.lzma.encodeForLZMA1();
            }
        }
        catch (IOException e) {
            this.exception = e;
            throw e;
        }
    }

    @Override
    public void flush() throws IOException {
        throw new XZIOException("LZMAOutputStream does not support flushing");
    }

    @Override
    public void finish() throws IOException {
        if (!this.finished) {
            if (this.exception != null) {
                throw this.exception;
            }
            try {
                if (this.expectedUncompressedSize != -1L && this.expectedUncompressedSize != this.currentUncompressedSize) {
                    throw new XZIOException("Expected uncompressed size (" + this.expectedUncompressedSize + ") doesn't equal the number of bytes written to the stream (" + this.currentUncompressedSize + ")");
                }
                this.lz.setFinishing();
                this.lzma.encodeForLZMA1();
                if (this.useEndMarker) {
                    this.lzma.encodeLZMA1EndMarker();
                }
                this.rc.finish();
            }
            catch (IOException e) {
                this.exception = e;
                throw e;
            }
            this.finished = true;
            this.lzma.putArraysToCache(this.arrayCache);
            this.lzma = null;
            this.lz = null;
        }
    }

    @Override
    public void close() throws IOException {
        if (this.out != null) {
            block6: {
                try {
                    this.finish();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                try {
                    this.out.close();
                }
                catch (IOException e) {
                    if (this.exception != null) break block6;
                    this.exception = e;
                }
            }
            this.out = null;
        }
        if (this.exception != null) {
            throw this.exception;
        }
    }
}

