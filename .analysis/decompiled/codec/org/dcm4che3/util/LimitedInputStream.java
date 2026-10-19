/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public final class LimitedInputStream
extends FilterInputStream {
    private long remaining;
    private long mark = -1L;
    private final boolean closeSource;

    public LimitedInputStream(InputStream in, long limit, boolean closeSource) {
        super(Objects.requireNonNull(in));
        if (limit <= 0L) {
            throw new IllegalArgumentException("limit must be > 0");
        }
        this.remaining = limit;
        this.closeSource = closeSource;
    }

    @Override
    public int read() throws IOException {
        int result;
        if (this.remaining == 0L || (result = this.in.read()) < 0) {
            return -1;
        }
        --this.remaining;
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result;
        if (this.remaining == 0L || (result = this.in.read(b, off, (int)Math.min((long)len, this.remaining))) < 0) {
            return -1;
        }
        this.remaining -= (long)result;
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        long result = this.in.skip(Math.min(n, this.remaining));
        this.remaining -= result;
        return result;
    }

    @Override
    public int available() throws IOException {
        return (int)Math.min((long)this.in.available(), this.remaining);
    }

    @Override
    public synchronized void mark(int readlimit) {
        this.in.mark(readlimit);
        this.mark = this.remaining;
    }

    @Override
    public synchronized void reset() throws IOException {
        this.in.reset();
        this.remaining = this.mark;
    }

    @Override
    public void close() throws IOException {
        if (this.closeSource) {
            this.in.close();
        }
    }

    public long getRemaining() {
        return this.remaining;
    }
}

