/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.CorruptedInputException;
import org.tukaani.xz.XZIOException;
import org.tukaani.xz.lz.LZDecoder;
import org.tukaani.xz.lzma.LZMADecoder;
import org.tukaani.xz.rangecoder.RangeDecoderFromBuffer;

public class LZMA2InputStream
extends InputStream {
    public static final int DICT_SIZE_MIN = 4096;
    public static final int DICT_SIZE_MAX = 0x7FFFFFF0;
    private static final int COMPRESSED_SIZE_MAX = 65536;
    private final ArrayCache arrayCache;
    private DataInputStream in;
    private LZDecoder lz;
    private RangeDecoderFromBuffer rc;
    private LZMADecoder lzma;
    private int uncompressedSize = 0;
    private boolean isLZMAChunk = false;
    private boolean needDictReset = true;
    private boolean needProps = true;
    private boolean endReached = false;
    private IOException exception = null;
    private final byte[] tempBuf = new byte[1];

    public static int getMemoryUsage(int dictSize) {
        return 104 + LZMA2InputStream.getDictSize(dictSize) / 1024;
    }

    private static int getDictSize(int dictSize) {
        if (dictSize < 4096 || dictSize > 0x7FFFFFF0) {
            throw new IllegalArgumentException("Unsupported dictionary size " + dictSize);
        }
        return dictSize + 15 & 0xFFFFFFF0;
    }

    public LZMA2InputStream(InputStream in, int dictSize) {
        this(in, dictSize, null);
    }

    public LZMA2InputStream(InputStream in, int dictSize, byte[] presetDict) {
        this(in, dictSize, presetDict, ArrayCache.getDefaultCache());
    }

    LZMA2InputStream(InputStream in, int dictSize, byte[] presetDict, ArrayCache arrayCache) {
        if (in == null) {
            throw new NullPointerException();
        }
        this.arrayCache = arrayCache;
        this.in = new DataInputStream(in);
        this.rc = new RangeDecoderFromBuffer(65536, arrayCache);
        this.lz = new LZDecoder(LZMA2InputStream.getDictSize(dictSize), presetDict, arrayCache);
        if (presetDict != null && presetDict.length > 0) {
            this.needDictReset = false;
        }
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
                if (this.uncompressedSize == 0) {
                    this.decodeChunkHeader();
                    if (this.endReached) {
                        return size == 0 ? -1 : size;
                    }
                }
                int copySizeMax = Math.min(this.uncompressedSize, len);
                if (!this.isLZMAChunk) {
                    this.lz.copyUncompressed(this.in, copySizeMax);
                } else {
                    this.lz.setLimit(copySizeMax);
                    this.lzma.decode();
                }
                int copiedSize = this.lz.flush(buf, off);
                off += copiedSize;
                len -= copiedSize;
                size += copiedSize;
                this.uncompressedSize -= copiedSize;
                if (this.uncompressedSize != 0 || this.rc.isFinished() && !this.lz.hasPending()) continue;
                throw new CorruptedInputException();
            }
            return size;
        }
        catch (IOException e) {
            this.exception = e;
            throw e;
        }
    }

    private void decodeChunkHeader() throws IOException {
        int control = this.in.readUnsignedByte();
        if (control == 0) {
            this.endReached = true;
            this.putArraysToCache();
            return;
        }
        if (control >= 224 || control == 1) {
            this.needProps = true;
            this.needDictReset = false;
            this.lz.reset();
        } else if (this.needDictReset) {
            throw new CorruptedInputException();
        }
        if (control >= 128) {
            this.isLZMAChunk = true;
            this.uncompressedSize = (control & 0x1F) << 16;
            this.uncompressedSize += this.in.readUnsignedShort() + 1;
            int compressedSize = this.in.readUnsignedShort() + 1;
            if (control >= 192) {
                this.needProps = false;
                this.decodeProps();
            } else {
                if (this.needProps) {
                    throw new CorruptedInputException();
                }
                if (control >= 160) {
                    this.lzma.reset();
                }
            }
            this.rc.prepareInputBuffer(this.in, compressedSize);
        } else {
            if (control > 2) {
                throw new CorruptedInputException();
            }
            this.isLZMAChunk = false;
            this.uncompressedSize = this.in.readUnsignedShort() + 1;
        }
    }

    private void decodeProps() throws IOException {
        int lp;
        int pb;
        int lc;
        int props = this.in.readUnsignedByte();
        if (props > 224) {
            throw new CorruptedInputException();
        }
        if ((lc = (props -= (pb = props / 45) * 9 * 5) - (lp = props / 9) * 9) + lp > 4) {
            throw new CorruptedInputException();
        }
        this.lzma = new LZMADecoder(this.lz, this.rc, lc, lp, pb);
    }

    @Override
    public int available() throws IOException {
        if (this.in == null) {
            throw new XZIOException("Stream closed");
        }
        if (this.exception != null) {
            throw this.exception;
        }
        return this.isLZMAChunk ? this.uncompressedSize : Math.min(this.uncompressedSize, this.in.available());
    }

    private void putArraysToCache() {
        if (this.lz != null) {
            this.lz.putArraysToCache(this.arrayCache);
            this.lz = null;
            this.rc.putArraysToCache(this.arrayCache);
            this.rc = null;
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

