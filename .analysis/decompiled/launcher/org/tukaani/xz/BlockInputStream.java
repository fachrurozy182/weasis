/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.BCJDecoder;
import org.tukaani.xz.CorruptedInputException;
import org.tukaani.xz.CountingInputStream;
import org.tukaani.xz.DeltaDecoder;
import org.tukaani.xz.FilterCoder;
import org.tukaani.xz.FilterDecoder;
import org.tukaani.xz.IndexIndicatorException;
import org.tukaani.xz.LZMA2Decoder;
import org.tukaani.xz.MemoryLimitException;
import org.tukaani.xz.RawCoder;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.check.Check;
import org.tukaani.xz.common.DecoderUtil;

class BlockInputStream
extends InputStream {
    private final DataInputStream inData;
    private final CountingInputStream inCounted;
    private InputStream filterChain;
    private final Check check;
    private final boolean verifyCheck;
    private long uncompressedSizeInHeader = -1L;
    private long compressedSizeInHeader = -1L;
    private long compressedSizeLimit;
    private final int headerSize;
    private long uncompressedSize = 0L;
    private boolean endReached = false;
    private final byte[] tempBuf = new byte[1];

    public BlockInputStream(InputStream in, Check check, boolean verifyCheck, int memoryLimit, long unpaddedSizeInIndex, long uncompressedSizeInIndex, ArrayCache arrayCache) throws IOException, IndexIndicatorException {
        int i;
        int i2;
        this.check = check;
        this.verifyCheck = verifyCheck;
        this.inData = new DataInputStream(in);
        int b = this.inData.readUnsignedByte();
        if (b == 0) {
            throw new IndexIndicatorException();
        }
        this.headerSize = 4 * (b + 1);
        byte[] buf = new byte[this.headerSize];
        buf[0] = (byte)b;
        this.inData.readFully(buf, 1, this.headerSize - 1);
        if (!DecoderUtil.isCRC32Valid(buf, 0, this.headerSize - 4, this.headerSize - 4)) {
            throw new CorruptedInputException("XZ Block Header is corrupt");
        }
        if ((buf[1] & 0x3C) != 0) {
            throw new UnsupportedOptionsException("Unsupported options in XZ Block Header");
        }
        int filterCount = (buf[1] & 3) + 1;
        long[] filterIDs = new long[filterCount];
        byte[][] filterProps = new byte[filterCount][];
        ByteArrayInputStream bufStream = new ByteArrayInputStream(buf, 2, this.headerSize - 6);
        try {
            this.compressedSizeLimit = 0x7FFFFFFFFFFFFFFCL - (long)this.headerSize - (long)check.getSize();
            if ((buf[1] & 0x40) != 0) {
                this.compressedSizeInHeader = DecoderUtil.decodeVLI(bufStream);
                if (this.compressedSizeInHeader == 0L || this.compressedSizeInHeader > this.compressedSizeLimit) {
                    throw new CorruptedInputException();
                }
                this.compressedSizeLimit = this.compressedSizeInHeader;
            }
            if ((buf[1] & 0x80) != 0) {
                this.uncompressedSizeInHeader = DecoderUtil.decodeVLI(bufStream);
            }
            for (i2 = 0; i2 < filterCount; ++i2) {
                filterIDs[i2] = DecoderUtil.decodeVLI(bufStream);
                long filterPropsSize = DecoderUtil.decodeVLI(bufStream);
                if (filterPropsSize > (long)bufStream.available()) {
                    throw new CorruptedInputException();
                }
                filterProps[i2] = new byte[(int)filterPropsSize];
                bufStream.read(filterProps[i2]);
            }
        }
        catch (IOException e) {
            throw new CorruptedInputException("XZ Block Header is corrupt");
        }
        for (i2 = bufStream.available(); i2 > 0; --i2) {
            if (bufStream.read() == 0) continue;
            throw new UnsupportedOptionsException("Unsupported options in XZ Block Header");
        }
        if (unpaddedSizeInIndex != -1L) {
            int headerAndCheckSize = this.headerSize + check.getSize();
            if ((long)headerAndCheckSize >= unpaddedSizeInIndex) {
                throw new CorruptedInputException("XZ Index does not match a Block Header");
            }
            long compressedSizeFromIndex = unpaddedSizeInIndex - (long)headerAndCheckSize;
            if (compressedSizeFromIndex > this.compressedSizeLimit || this.compressedSizeInHeader != -1L && this.compressedSizeInHeader != compressedSizeFromIndex) {
                throw new CorruptedInputException("XZ Index does not match a Block Header");
            }
            if (this.uncompressedSizeInHeader != -1L && this.uncompressedSizeInHeader != uncompressedSizeInIndex) {
                throw new CorruptedInputException("XZ Index does not match a Block Header");
            }
            this.compressedSizeLimit = compressedSizeFromIndex;
            this.compressedSizeInHeader = compressedSizeFromIndex;
            this.uncompressedSizeInHeader = uncompressedSizeInIndex;
        }
        FilterCoder[] filters = new FilterDecoder[filterIDs.length];
        for (i = 0; i < filters.length; ++i) {
            if (filterIDs[i] == 33L) {
                filters[i] = new LZMA2Decoder(filterProps[i]);
                continue;
            }
            if (filterIDs[i] == 3L) {
                filters[i] = new DeltaDecoder(filterProps[i]);
                continue;
            }
            if (BCJDecoder.isBCJFilterID(filterIDs[i])) {
                filters[i] = new BCJDecoder(filterIDs[i], filterProps[i]);
                continue;
            }
            throw new UnsupportedOptionsException("Unknown Filter ID " + filterIDs[i]);
        }
        RawCoder.validate(filters);
        if (memoryLimit >= 0) {
            int memoryNeeded = 0;
            for (int i3 = 0; i3 < filters.length; ++i3) {
                memoryNeeded += filters[i3].getMemoryUsage();
            }
            if (memoryNeeded > memoryLimit) {
                throw new MemoryLimitException(memoryNeeded, memoryLimit);
            }
        }
        this.inCounted = new CountingInputStream(in);
        this.filterChain = this.inCounted;
        for (i = filters.length - 1; i >= 0; --i) {
            this.filterChain = filters[i].getInputStream(this.filterChain, arrayCache);
        }
    }

    @Override
    public int read() throws IOException {
        return this.read(this.tempBuf, 0, 1) == -1 ? -1 : this.tempBuf[0] & 0xFF;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        if (this.endReached) {
            return -1;
        }
        int ret = this.filterChain.read(buf, off, len);
        if (ret > 0) {
            if (this.verifyCheck) {
                this.check.update(buf, off, ret);
            }
            this.uncompressedSize += (long)ret;
            long compressedSize = this.inCounted.getSize();
            if (compressedSize < 0L || compressedSize > this.compressedSizeLimit || this.uncompressedSize < 0L || this.uncompressedSizeInHeader != -1L && this.uncompressedSize > this.uncompressedSizeInHeader) {
                throw new CorruptedInputException();
            }
            if (ret < len || this.uncompressedSize == this.uncompressedSizeInHeader) {
                if (this.filterChain.read() != -1) {
                    throw new CorruptedInputException();
                }
                this.validate();
                this.endReached = true;
            }
        } else if (ret == -1) {
            this.validate();
            this.endReached = true;
        }
        return ret;
    }

    private void validate() throws IOException {
        long compressedSize = this.inCounted.getSize();
        if (this.compressedSizeInHeader != -1L && this.compressedSizeInHeader != compressedSize || this.uncompressedSizeInHeader != -1L && this.uncompressedSizeInHeader != this.uncompressedSize) {
            throw new CorruptedInputException();
        }
        while ((compressedSize++ & 3L) != 0L) {
            if (this.inData.readUnsignedByte() == 0) continue;
            throw new CorruptedInputException();
        }
        byte[] storedCheck = new byte[this.check.getSize()];
        this.inData.readFully(storedCheck);
        if (this.verifyCheck && !Arrays.equals(this.check.finish(), storedCheck)) {
            throw new CorruptedInputException("Integrity check (" + this.check.getName() + ") does not match");
        }
    }

    @Override
    public int available() throws IOException {
        return this.filterChain.available();
    }

    @Override
    public void close() {
        block2: {
            try {
                this.filterChain.close();
            }
            catch (IOException e) {
                if ($assertionsDisabled) break block2;
                throw new AssertionError();
            }
        }
        this.filterChain = null;
    }

    public long getUnpaddedSize() {
        return (long)this.headerSize + this.inCounted.getSize() + (long)this.check.getSize();
    }

    public long getUncompressedSize() {
        return this.uncompressedSize;
    }
}

