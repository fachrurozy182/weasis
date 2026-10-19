/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.IOException;
import org.tukaani.xz.FinishableOutputStream;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.XZIOException;
import org.tukaani.xz.simple.SimpleFilter;

class SimpleOutputStream
extends FinishableOutputStream {
    private static final int FILTER_BUF_SIZE = 4096;
    private FinishableOutputStream out;
    private final SimpleFilter simpleFilter;
    private final byte[] filterBuf = new byte[4096];
    private int pos = 0;
    private int unfiltered = 0;
    private IOException exception = null;
    private boolean finished = false;
    private final byte[] tempBuf = new byte[1];

    static int getMemoryUsage() {
        return 5;
    }

    SimpleOutputStream(FinishableOutputStream out, SimpleFilter simpleFilter) {
        if (out == null) {
            throw new NullPointerException();
        }
        this.out = out;
        this.simpleFilter = simpleFilter;
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
        while (len > 0) {
            int copySize = Math.min(len, 4096 - (this.pos + this.unfiltered));
            System.arraycopy(buf, off, this.filterBuf, this.pos + this.unfiltered, copySize);
            off += copySize;
            len -= copySize;
            this.unfiltered += copySize;
            int filtered = this.simpleFilter.code(this.filterBuf, this.pos, this.unfiltered);
            assert (filtered <= this.unfiltered);
            this.unfiltered -= filtered;
            try {
                this.out.write(this.filterBuf, this.pos, filtered);
            }
            catch (IOException e) {
                this.exception = e;
                throw e;
            }
            this.pos += filtered;
            if (this.pos + this.unfiltered != 4096) continue;
            System.arraycopy(this.filterBuf, this.pos, this.filterBuf, 0, this.unfiltered);
            this.pos = 0;
        }
    }

    private void writePending() throws IOException {
        assert (!this.finished);
        if (this.exception != null) {
            throw this.exception;
        }
        try {
            this.out.write(this.filterBuf, this.pos, this.unfiltered);
        }
        catch (IOException e) {
            this.exception = e;
            throw e;
        }
        this.finished = true;
    }

    @Override
    public void flush() throws IOException {
        throw new UnsupportedOptionsException("Flushing is not supported");
    }

    @Override
    public void finish() throws IOException {
        if (!this.finished) {
            this.writePending();
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
                        this.writePending();
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

