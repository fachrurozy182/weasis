/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.IOException;
import org.tukaani.xz.DeltaOptions;
import org.tukaani.xz.FinishableOutputStream;
import org.tukaani.xz.XZIOException;
import org.tukaani.xz.delta.DeltaEncoder;

class DeltaOutputStream
extends FinishableOutputStream {
    private static final int FILTER_BUF_SIZE = 4096;
    private FinishableOutputStream out;
    private final DeltaEncoder delta;
    private final byte[] filterBuf = new byte[4096];
    private boolean finished = false;
    private IOException exception = null;
    private final byte[] tempBuf = new byte[1];

    static int getMemoryUsage() {
        return 5;
    }

    DeltaOutputStream(FinishableOutputStream out, DeltaOptions options) {
        this.out = out;
        this.delta = new DeltaEncoder(options.getDistance());
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
            throw new XZIOException("Stream finished");
        }
        try {
            while (len > 4096) {
                this.delta.encode(buf, off, 4096, this.filterBuf);
                this.out.write(this.filterBuf);
                off += 4096;
                len -= 4096;
            }
            this.delta.encode(buf, off, len, this.filterBuf);
            this.out.write(this.filterBuf, 0, len);
        }
        catch (IOException e) {
            this.exception = e;
            throw e;
        }
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
            if (this.exception != null) {
                throw this.exception;
            }
            try {
                this.out.finish();
            }
            catch (IOException e) {
                this.exception = e;
                throw e;
            }
            this.finished = true;
        }
    }

    @Override
    public void close() throws IOException {
        if (this.out != null) {
            block4: {
                try {
                    this.out.close();
                }
                catch (IOException e) {
                    if (this.exception != null) break block4;
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

