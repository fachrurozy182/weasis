/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.IOException;
import java.io.InputStream;
import org.tukaani.xz.XZIOException;
import org.tukaani.xz.delta.DeltaDecoder;

public class DeltaInputStream
extends InputStream {
    public static final int DISTANCE_MIN = 1;
    public static final int DISTANCE_MAX = 256;
    private InputStream in;
    private final DeltaDecoder delta;
    private IOException exception = null;
    private final byte[] tempBuf = new byte[1];

    public DeltaInputStream(InputStream in, int distance) {
        if (in == null) {
            throw new NullPointerException();
        }
        this.in = in;
        this.delta = new DeltaDecoder(distance);
    }

    @Override
    public int read() throws IOException {
        return this.read(this.tempBuf, 0, 1) == -1 ? -1 : this.tempBuf[0] & 0xFF;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int size;
        if (len == 0) {
            return 0;
        }
        if (this.in == null) {
            throw new XZIOException("Stream closed");
        }
        if (this.exception != null) {
            throw this.exception;
        }
        try {
            size = this.in.read(buf, off, len);
        }
        catch (IOException e) {
            this.exception = e;
            throw e;
        }
        if (size == -1) {
            return -1;
        }
        this.delta.decode(buf, off, size);
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
        return this.in.available();
    }

    @Override
    public void close() throws IOException {
        if (this.in != null) {
            try {
                this.in.close();
            }
            finally {
                this.in = null;
            }
        }
    }
}

