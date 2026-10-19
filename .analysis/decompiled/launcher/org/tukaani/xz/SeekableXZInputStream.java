/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.BlockInputStream;
import org.tukaani.xz.CorruptedInputException;
import org.tukaani.xz.IndexIndicatorException;
import org.tukaani.xz.MemoryLimitException;
import org.tukaani.xz.SeekableInputStream;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZFormatException;
import org.tukaani.xz.XZIOException;
import org.tukaani.xz.check.Check;
import org.tukaani.xz.common.DecoderUtil;
import org.tukaani.xz.common.StreamFlags;
import org.tukaani.xz.index.BlockInfo;
import org.tukaani.xz.index.IndexDecoder;

public class SeekableXZInputStream
extends SeekableInputStream {
    private final ArrayCache arrayCache;
    private SeekableInputStream in;
    private final int memoryLimit;
    private int indexMemoryUsage = 0;
    private final ArrayList<IndexDecoder> streams = new ArrayList();
    private int checkTypes = 0;
    private long uncompressedSize = 0L;
    private long largestBlockSize = 0L;
    private int blockCount = 0;
    private final BlockInfo curBlockInfo;
    private final BlockInfo queriedBlockInfo;
    private Check check;
    private final boolean verifyCheck;
    private BlockInputStream blockDecoder = null;
    private long curPos = 0L;
    private long seekPos;
    private boolean seekNeeded = false;
    private boolean endReached = false;
    private IOException exception = null;
    private final byte[] tempBuf = new byte[1];

    public SeekableXZInputStream(SeekableInputStream in) throws IOException {
        this(in, -1);
    }

    public SeekableXZInputStream(SeekableInputStream in, ArrayCache arrayCache) throws IOException {
        this(in, -1, arrayCache);
    }

    public SeekableXZInputStream(SeekableInputStream in, int memoryLimit) throws IOException {
        this(in, memoryLimit, true);
    }

    public SeekableXZInputStream(SeekableInputStream in, int memoryLimit, ArrayCache arrayCache) throws IOException {
        this(in, memoryLimit, true, arrayCache);
    }

    public SeekableXZInputStream(SeekableInputStream in, int memoryLimit, boolean verifyCheck) throws IOException {
        this(in, memoryLimit, verifyCheck, ArrayCache.getDefaultCache());
    }

    public SeekableXZInputStream(SeekableInputStream in, int memoryLimit, boolean verifyCheck, ArrayCache arrayCache) throws IOException {
        this.arrayCache = arrayCache;
        this.verifyCheck = verifyCheck;
        this.in = in;
        DataInputStream inData = new DataInputStream(in);
        in.seek(0L);
        byte[] buf = new byte[XZ.HEADER_MAGIC.length];
        inData.readFully(buf);
        if (!Arrays.equals(buf, XZ.HEADER_MAGIC)) {
            throw new XZFormatException();
        }
        long pos = in.length();
        if ((pos & 3L) != 0L) {
            throw new CorruptedInputException("XZ file size is not a multiple of 4 bytes");
        }
        byte[] buf2 = new byte[12];
        long streamPadding = 0L;
        while (pos > 0L) {
            long off;
            IndexDecoder index;
            if (pos < 12L) {
                throw new CorruptedInputException();
            }
            in.seek(pos - 12L);
            inData.readFully(buf2);
            if (buf2[8] == 0 && buf2[9] == 0 && buf2[10] == 0 && buf2[11] == 0) {
                streamPadding += 4L;
                pos -= 4L;
                continue;
            }
            StreamFlags streamFooter = DecoderUtil.decodeStreamFooter(buf2);
            if (streamFooter.backwardSize >= (pos -= 12L)) {
                throw new CorruptedInputException("Backward Size in XZ Stream Footer is too big");
            }
            this.check = Check.getInstance(streamFooter.checkType);
            this.checkTypes |= 1 << streamFooter.checkType;
            in.seek(pos - streamFooter.backwardSize);
            try {
                index = new IndexDecoder(in, streamFooter, streamPadding, memoryLimit);
            }
            catch (MemoryLimitException e) {
                assert (memoryLimit >= 0);
                throw new MemoryLimitException(e.getMemoryNeeded() + this.indexMemoryUsage, memoryLimit + this.indexMemoryUsage);
            }
            this.indexMemoryUsage += index.getMemoryUsage();
            if (memoryLimit >= 0) assert ((memoryLimit -= index.getMemoryUsage()) >= 0);
            if (this.largestBlockSize < index.getLargestBlockSize()) {
                this.largestBlockSize = index.getLargestBlockSize();
            }
            if (pos < (off = index.getStreamSize() - 12L)) {
                throw new CorruptedInputException("XZ Index indicates too big compressed size for the XZ Stream");
            }
            in.seek(pos -= off);
            inData.readFully(buf2);
            StreamFlags streamHeader = DecoderUtil.decodeStreamHeader(buf2);
            if (!DecoderUtil.areStreamFlagsEqual(streamHeader, streamFooter)) {
                throw new CorruptedInputException("XZ Stream Footer does not match Stream Header");
            }
            this.uncompressedSize += index.getUncompressedSize();
            if (this.uncompressedSize < 0L) {
                throw new UnsupportedOptionsException("XZ file is too big");
            }
            this.blockCount += index.getRecordCount();
            if (this.blockCount < 0) {
                throw new UnsupportedOptionsException("XZ file has over 2147483647 Blocks");
            }
            this.streams.add(index);
            streamPadding = 0L;
        }
        assert (pos == 0L);
        this.memoryLimit = memoryLimit;
        IndexDecoder prev = this.streams.get(this.streams.size() - 1);
        for (int i = this.streams.size() - 2; i >= 0; --i) {
            IndexDecoder cur = this.streams.get(i);
            cur.setOffsets(prev);
            prev = cur;
        }
        IndexDecoder first = this.streams.get(this.streams.size() - 1);
        this.curBlockInfo = new BlockInfo(first);
        this.queriedBlockInfo = new BlockInfo(first);
    }

    public int getCheckTypes() {
        return this.checkTypes;
    }

    public int getIndexMemoryUsage() {
        return this.indexMemoryUsage;
    }

    public long getLargestBlockSize() {
        return this.largestBlockSize;
    }

    public int getStreamCount() {
        return this.streams.size();
    }

    public int getBlockCount() {
        return this.blockCount;
    }

    public long getBlockPos(int blockNumber) {
        this.locateBlockByNumber(this.queriedBlockInfo, blockNumber);
        return this.queriedBlockInfo.uncompressedOffset;
    }

    public long getBlockSize(int blockNumber) {
        this.locateBlockByNumber(this.queriedBlockInfo, blockNumber);
        return this.queriedBlockInfo.uncompressedSize;
    }

    public long getBlockCompPos(int blockNumber) {
        this.locateBlockByNumber(this.queriedBlockInfo, blockNumber);
        return this.queriedBlockInfo.compressedOffset;
    }

    public long getBlockCompSize(int blockNumber) {
        this.locateBlockByNumber(this.queriedBlockInfo, blockNumber);
        return this.queriedBlockInfo.unpaddedSize + 3L & 0xFFFFFFFFFFFFFFFCL;
    }

    public int getBlockCheckType(int blockNumber) {
        this.locateBlockByNumber(this.queriedBlockInfo, blockNumber);
        return this.queriedBlockInfo.getCheckType();
    }

    public int getBlockNumber(long pos) {
        this.locateBlockByPos(this.queriedBlockInfo, pos);
        return this.queriedBlockInfo.blockNumber;
    }

    @Override
    public int read() throws IOException {
        return this.read(this.tempBuf, 0, 1) == -1 ? -1 : this.tempBuf[0] & 0xFF;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int size;
        block13: {
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
            size = 0;
            try {
                if (this.seekNeeded) {
                    this.seek();
                }
                if (this.endReached) {
                    return -1;
                }
                while (len > 0) {
                    int ret;
                    if (this.blockDecoder == null) {
                        this.seek();
                        if (this.endReached) break;
                    }
                    if ((ret = this.blockDecoder.read(buf, off, len)) > 0) {
                        this.curPos += (long)ret;
                        size += ret;
                        off += ret;
                        len -= ret;
                        continue;
                    }
                    if (ret != -1) continue;
                    this.blockDecoder = null;
                }
            }
            catch (IOException e2) {
                CorruptedInputException e2;
                if (e2 instanceof EOFException) {
                    e2 = new CorruptedInputException();
                }
                this.exception = e2;
                if (size != 0) break block13;
                throw e2;
            }
        }
        return size;
    }

    @Override
    public int available() throws IOException {
        if (this.in == null) {
            throw new XZIOException("Stream closed");
        }
        if (this.exception != null) {
            throw this.exception;
        }
        if (this.endReached || this.seekNeeded || this.blockDecoder == null) {
            return 0;
        }
        return this.blockDecoder.available();
    }

    @Override
    public void close() throws IOException {
        this.close(true);
    }

    public void close(boolean closeInput) throws IOException {
        if (this.in != null) {
            if (this.blockDecoder != null) {
                this.blockDecoder.close();
                this.blockDecoder = null;
            }
            try {
                if (closeInput) {
                    this.in.close();
                }
            }
            finally {
                this.in = null;
            }
        }
    }

    @Override
    public long length() {
        return this.uncompressedSize;
    }

    @Override
    public long position() throws IOException {
        if (this.in == null) {
            throw new XZIOException("Stream closed");
        }
        return this.seekNeeded ? this.seekPos : this.curPos;
    }

    @Override
    public void seek(long pos) throws IOException {
        if (this.in == null) {
            throw new XZIOException("Stream closed");
        }
        if (pos < 0L) {
            throw new XZIOException("Negative seek position: " + pos);
        }
        this.seekPos = pos;
        this.seekNeeded = true;
    }

    public void seekToBlock(int blockNumber) throws IOException {
        if (this.in == null) {
            throw new XZIOException("Stream closed");
        }
        if (blockNumber < 0 || blockNumber >= this.blockCount) {
            throw new XZIOException("Invalid XZ Block number: " + blockNumber);
        }
        this.locateBlockByNumber(this.queriedBlockInfo, blockNumber);
        this.seekPos = this.queriedBlockInfo.uncompressedOffset;
        this.seekNeeded = true;
    }

    private void seek() throws IOException {
        if (!this.seekNeeded) {
            if (this.curBlockInfo.hasNext()) {
                this.curBlockInfo.setNext();
                this.initBlockDecoder();
                return;
            }
            this.seekPos = this.curPos;
        }
        this.seekNeeded = false;
        if (this.seekPos >= this.uncompressedSize) {
            this.curPos = this.seekPos;
            if (this.blockDecoder != null) {
                this.blockDecoder.close();
                this.blockDecoder = null;
            }
            this.endReached = true;
            return;
        }
        this.endReached = false;
        this.locateBlockByPos(this.curBlockInfo, this.seekPos);
        if (this.curPos <= this.curBlockInfo.uncompressedOffset || this.curPos > this.seekPos) {
            this.in.seek(this.curBlockInfo.compressedOffset);
            this.check = Check.getInstance(this.curBlockInfo.getCheckType());
            this.initBlockDecoder();
            this.curPos = this.curBlockInfo.uncompressedOffset;
        }
        if (this.seekPos > this.curPos) {
            long skipAmount = this.seekPos - this.curPos;
            if (this.blockDecoder.skip(skipAmount) != skipAmount) {
                throw new CorruptedInputException();
            }
            this.curPos = this.seekPos;
        }
    }

    private void locateBlockByPos(BlockInfo info, long pos) {
        IndexDecoder index;
        if (pos < 0L || pos >= this.uncompressedSize) {
            throw new IndexOutOfBoundsException("Invalid uncompressed position: " + pos);
        }
        int i = 0;
        while (!(index = this.streams.get(i)).hasUncompressedOffset(pos)) {
            ++i;
        }
        index.locateBlock(info, pos);
        assert ((info.compressedOffset & 3L) == 0L);
        assert (info.uncompressedSize > 0L);
        assert (pos >= info.uncompressedOffset);
        assert (pos < info.uncompressedOffset + info.uncompressedSize);
    }

    private void locateBlockByNumber(BlockInfo info, int blockNumber) {
        if (blockNumber < 0 || blockNumber >= this.blockCount) {
            throw new IndexOutOfBoundsException("Invalid XZ Block number: " + blockNumber);
        }
        if (info.blockNumber == blockNumber) {
            return;
        }
        int i = 0;
        while (true) {
            IndexDecoder index;
            if ((index = this.streams.get(i)).hasRecord(blockNumber)) {
                index.setBlockInfo(info, blockNumber);
                return;
            }
            ++i;
        }
    }

    private void initBlockDecoder() throws IOException {
        try {
            if (this.blockDecoder != null) {
                this.blockDecoder.close();
                this.blockDecoder = null;
            }
            this.blockDecoder = new BlockInputStream(this.in, this.check, this.verifyCheck, this.memoryLimit, this.curBlockInfo.unpaddedSize, this.curBlockInfo.uncompressedSize, this.arrayCache);
        }
        catch (MemoryLimitException e) {
            assert (this.memoryLimit >= 0);
            throw new MemoryLimitException(e.getMemoryNeeded() + this.indexMemoryUsage, this.memoryLimit + this.indexMemoryUsage);
        }
        catch (IndexIndicatorException e) {
            throw new CorruptedInputException();
        }
    }
}

