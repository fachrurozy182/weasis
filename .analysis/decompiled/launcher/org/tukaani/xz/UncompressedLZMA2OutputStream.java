/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.DataOutputStream;
import java.io.IOException;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.FinishableOutputStream;
import org.tukaani.xz.XZIOException;

class UncompressedLZMA2OutputStream
extends FinishableOutputStream {
    private final ArrayCache arrayCache;
    private FinishableOutputStream out;
    private final DataOutputStream outData;
    private final byte[] uncompBuf;
    private int uncompPos = 0;
    private boolean dictResetNeeded = true;
    private boolean finished = false;
    private IOException exception = null;
    private final byte[] tempBuf = new byte[1];

    static int getMemoryUsage() {
        return 70;
    }

    UncompressedLZMA2OutputStream(FinishableOutputStream out, ArrayCache arrayCache) {
        if (out == null) {
            throw new NullPointerException();
        }
        this.out = out;
        this.outData = new DataOutputStream(out);
        this.arrayCache = arrayCache;
        this.uncompBuf = arrayCache.getByteArray(65536, false);
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
        try {
            while (len > 0) {
                int copySize = Math.min(65536 - this.uncompPos, len);
                if (copySize == 65536) {
                    assert (this.uncompPos == 0);
                    this.writeChunk(buf, off, copySize);
                } else {
                    System.arraycopy(buf, off, this.uncompBuf, this.uncompPos, copySize);
                    this.uncompPos += copySize;
                    if (this.uncompPos == 65536) {
                        this.writeChunk();
                    }
                }
                off += copySize;
                len -= copySize;
            }
        }
        catch (IOException e) {
            this.exception = e;
            throw e;
        }
    }

    private void writeChunk() throws IOException {
        this.writeChunk(this.uncompBuf, 0, this.uncompPos);
        this.uncompPos = 0;
    }

    private void writeChunk(byte[] buf, int off, int len) throws IOException {
        this.outData.writeByte(this.dictResetNeeded ? 1 : 2);
        this.outData.writeShort(len - 1);
        this.outData.write(buf, off, len);
        this.dictResetNeeded = false;
    }

    private void writeEndMarker() throws IOException {
        if (this.exception != null) {
            throw this.exception;
        }
        if (this.finished) {
            throw new XZIOException("Stream finished or closed");
        }
        try {
            if (this.uncompPos > 0) {
                this.writeChunk();
            }
            this.out.write(0);
        }
        catch (IOException e) {
            this.exception = e;
            throw e;
        }
        this.finished = true;
        this.arrayCache.putArray(this.uncompBuf);
    }

    @Override
    public void flush() throws IOException {
        if (this.exception != null) {
            throw this.exception;
        }
        if (this.finished) {
            throw new XZIOException("Stream finished or closed");
        }
        try {
            if (this.uncompPos > 0) {
                this.writeChunk();
            }
            this.out.flush();
        }
        catch (IOException e) {
            this.exception = e;
            throw e;
        }
    }

    @Override
    public void finish() throws IOException {
        if (!this.finished) {
            this.writeEndMarker();
            try {
                this.out.finish();
            }
            catch (IOException e) {
                this.exception = e;
                throw e;
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (this.out != null) {
            block7: {
                if (!this.finished) {
                    try {
                        this.writeEndMarker();
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                }
                try {
                    this.out.close();
                }
                catch (IOException e) {
                    if (this.exception != null) break block7;
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

