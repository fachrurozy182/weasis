/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.BlockInputStream;
import org.tukaani.xz.CorruptedInputException;
import org.tukaani.xz.IndexIndicatorException;
import org.tukaani.xz.XZIOException;
import org.tukaani.xz.check.Check;
import org.tukaani.xz.common.DecoderUtil;
import org.tukaani.xz.common.StreamFlags;
import org.tukaani.xz.index.IndexHash;

public class SingleXZInputStream
extends InputStream {
    private InputStream in;
    private final ArrayCache arrayCache;
    private final int memoryLimit;
    private final StreamFlags streamHeaderFlags;
    private final Check check;
    private final boolean verifyCheck;
    private BlockInputStream blockDecoder = null;
    private final IndexHash indexHash = new IndexHash();
    private boolean endReached = false;
    private IOException exception = null;
    private final byte[] tempBuf = new byte[1];

    private static byte[] readStreamHeader(InputStream in) throws IOException {
        byte[] streamHeader = new byte[12];
        new DataInputStream(in).readFully(streamHeader);
        return streamHeader;
    }

    public SingleXZInputStream(InputStream in) throws IOException {
        this(in, -1);
    }

    public SingleXZInputStream(InputStream in, ArrayCache arrayCache) throws IOException {
        this(in, -1, arrayCache);
    }

    public SingleXZInputStream(InputStream in, int memoryLimit) throws IOException {
        this(in, memoryLimit, true);
    }

    public SingleXZInputStream(InputStream in, int memoryLimit, ArrayCache arrayCache) throws IOException {
        this(in, memoryLimit, true, arrayCache);
    }

    public SingleXZInputStream(InputStream in, int memoryLimit, boolean verifyCheck) throws IOException {
        this(in, memoryLimit, verifyCheck, ArrayCache.getDefaultCache());
    }

    public SingleXZInputStream(InputStream in, int memoryLimit, boolean verifyCheck, ArrayCache arrayCache) throws IOException {
        this(in, memoryLimit, verifyCheck, SingleXZInputStream.readStreamHeader(in), arrayCache);
    }

    SingleXZInputStream(InputStream in, int memoryLimit, boolean verifyCheck, byte[] streamHeader, ArrayCache arrayCache) throws IOException {
        this.arrayCache = arrayCache;
        this.in = in;
        this.memoryLimit = memoryLimit;
        this.verifyCheck = verifyCheck;
        this.streamHeaderFlags = DecoderUtil.decodeStreamHeader(streamHeader);
        this.check = Check.getInstance(this.streamHeaderFlags.checkType);
    }

    public int getCheckType() {
        return this.streamHeaderFlags.checkType;
    }

    public String getCheckName() {
        return this.check.getName();
    }

    @Override
    public int read() throws IOException {
        return this.read(this.tempBuf, 0, 1) == -1 ? -1 : this.tempBuf[0] & 0xFF;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int size;
        block12: {
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
            size = 0;
            try {
                while (len > 0) {
                    int ret;
                    if (this.blockDecoder == null) {
                        try {
                            this.blockDecoder = new BlockInputStream(this.in, this.check, this.verifyCheck, this.memoryLimit, -1L, -1L, this.arrayCache);
                        }
                        catch (IndexIndicatorException e) {
                            this.indexHash.validate(this.in);
                            this.validateStreamFooter();
                            this.endReached = true;
                            return size > 0 ? size : -1;
                        }
                    }
                    if ((ret = this.blockDecoder.read(buf, off, len)) > 0) {
                        size += ret;
                        off += ret;
                        len -= ret;
                        continue;
                    }
                    if (ret != -1) continue;
                    this.indexHash.add(this.blockDecoder.getUnpaddedSize(), this.blockDecoder.getUncompressedSize());
                    this.blockDecoder = null;
                }
            }
            catch (IOException e) {
                this.exception = e;
                if (size != 0) break block12;
                throw e;
            }
        }
        return size;
    }

    private void validateStreamFooter() throws IOException {
        byte[] buf = new byte[12];
        new DataInputStream(this.in).readFully(buf);
        StreamFlags streamFooterFlags = DecoderUtil.decodeStreamFooter(buf);
        if (!DecoderUtil.areStreamFlagsEqual(this.streamHeaderFlags, streamFooterFlags) || this.indexHash.getIndexSize() != streamFooterFlags.backwardSize) {
            throw new CorruptedInputException("XZ Stream Footer does not match Stream Header");
        }
    }

    @Override
    public int available() throws IOException {
        if (this.in == null) {
            throw new XZIOException("Stream closed");
        }
        if (this.exception != null) {
            throw this.exception;
        }
        return this.blockDecoder == null ? 0 : this.blockDecoder.available();
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
}

