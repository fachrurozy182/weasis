/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.index;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import org.tukaani.xz.CorruptedInputException;
import org.tukaani.xz.MemoryLimitException;
import org.tukaani.xz.SeekableInputStream;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.common.DecoderUtil;
import org.tukaani.xz.common.StreamFlags;
import org.tukaani.xz.index.BlockInfo;
import org.tukaani.xz.index.IndexBase;

public final class IndexDecoder
extends IndexBase {
    private final StreamFlags streamFlags;
    private final long streamPadding;
    private final int memoryUsage;
    private final long[] unpadded;
    private final long[] uncompressed;
    private long largestBlockSize = 0L;
    private int recordOffset = 0;
    private long compressedOffset = 0L;
    private long uncompressedOffset = 0L;

    public IndexDecoder(SeekableInputStream in, StreamFlags streamFooterFlags, long streamPadding, int memoryLimit) throws IOException {
        super(new CorruptedInputException("XZ Index is corrupt"));
        int i;
        this.streamFlags = streamFooterFlags;
        this.streamPadding = streamPadding;
        long endPos = in.position() + streamFooterFlags.backwardSize - 4L;
        CRC32 crc32 = new CRC32();
        CheckedInputStream inChecked = new CheckedInputStream(in, crc32);
        if (inChecked.read() != 0) {
            throw new CorruptedInputException("XZ Index is corrupt");
        }
        try {
            long count = DecoderUtil.decodeVLI(inChecked);
            if (count >= streamFooterFlags.backwardSize / 2L) {
                throw new CorruptedInputException("XZ Index is corrupt");
            }
            if (count > Integer.MAX_VALUE) {
                throw new UnsupportedOptionsException("XZ Index has over 2147483647 Records");
            }
            this.memoryUsage = 1 + (int)((16L * count + 1023L) / 1024L);
            if (memoryLimit >= 0 && this.memoryUsage > memoryLimit) {
                throw new MemoryLimitException(this.memoryUsage, memoryLimit);
            }
            this.unpadded = new long[(int)count];
            this.uncompressed = new long[(int)count];
            int record = 0;
            for (i = (int)count; i > 0; --i) {
                long unpaddedSize = DecoderUtil.decodeVLI(inChecked);
                long uncompressedSize = DecoderUtil.decodeVLI(inChecked);
                if (in.position() > endPos) {
                    throw new CorruptedInputException("XZ Index is corrupt");
                }
                this.unpadded[record] = this.blocksSum + unpaddedSize;
                this.uncompressed[record] = this.uncompressedSum + uncompressedSize;
                super.add(unpaddedSize, uncompressedSize);
                assert ((long)(++record) == this.recordCount);
                if (this.largestBlockSize >= uncompressedSize) continue;
                this.largestBlockSize = uncompressedSize;
            }
        }
        catch (EOFException e) {
            throw new CorruptedInputException("XZ Index is corrupt");
        }
        int indexPaddingSize = this.getIndexPaddingSize();
        if (in.position() + (long)indexPaddingSize != endPos) {
            throw new CorruptedInputException("XZ Index is corrupt");
        }
        while (indexPaddingSize-- > 0) {
            if (inChecked.read() == 0) continue;
            throw new CorruptedInputException("XZ Index is corrupt");
        }
        long value = crc32.getValue();
        for (i = 0; i < 4; ++i) {
            if ((value >>> i * 8 & 0xFFL) == (long)in.read()) continue;
            throw new CorruptedInputException("XZ Index is corrupt");
        }
    }

    public void setOffsets(IndexDecoder prev) {
        this.recordOffset = prev.recordOffset + (int)prev.recordCount;
        this.compressedOffset = prev.compressedOffset + prev.getStreamSize() + prev.streamPadding;
        assert ((this.compressedOffset & 3L) == 0L);
        this.uncompressedOffset = prev.uncompressedOffset + prev.uncompressedSum;
    }

    public int getMemoryUsage() {
        return this.memoryUsage;
    }

    public StreamFlags getStreamFlags() {
        return this.streamFlags;
    }

    public int getRecordCount() {
        return (int)this.recordCount;
    }

    public long getUncompressedSize() {
        return this.uncompressedSum;
    }

    public long getLargestBlockSize() {
        return this.largestBlockSize;
    }

    public boolean hasUncompressedOffset(long pos) {
        return pos >= this.uncompressedOffset && pos < this.uncompressedOffset + this.uncompressedSum;
    }

    public boolean hasRecord(int blockNumber) {
        return blockNumber >= this.recordOffset && (long)blockNumber < (long)this.recordOffset + this.recordCount;
    }

    public void locateBlock(BlockInfo info, long target) {
        assert (target >= this.uncompressedOffset);
        assert ((target -= this.uncompressedOffset) < this.uncompressedSum);
        int left = 0;
        int right = this.unpadded.length - 1;
        while (left < right) {
            int i = left + (right - left) / 2;
            if (this.uncompressed[i] <= target) {
                left = i + 1;
                continue;
            }
            right = i;
        }
        this.setBlockInfo(info, this.recordOffset + left);
    }

    public void setBlockInfo(BlockInfo info, int blockNumber) {
        assert (blockNumber >= this.recordOffset);
        assert ((long)(blockNumber - this.recordOffset) < this.recordCount);
        info.index = this;
        info.blockNumber = blockNumber;
        int pos = blockNumber - this.recordOffset;
        if (pos == 0) {
            info.compressedOffset = 0L;
            info.uncompressedOffset = 0L;
        } else {
            info.compressedOffset = this.unpadded[pos - 1] + 3L & 0xFFFFFFFFFFFFFFFCL;
            info.uncompressedOffset = this.uncompressed[pos - 1];
        }
        info.unpaddedSize = this.unpadded[pos] - info.compressedOffset;
        info.uncompressedSize = this.uncompressed[pos] - info.uncompressedOffset;
        info.compressedOffset += this.compressedOffset + 12L;
        info.uncompressedOffset += this.uncompressedOffset;
    }
}

