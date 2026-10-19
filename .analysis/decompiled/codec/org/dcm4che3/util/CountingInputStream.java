/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class CountingInputStream
extends FilterInputStream {
    private volatile long count;
    private volatile long mark;

    public CountingInputStream(InputStream in) {
        super(Objects.requireNonNull(in));
    }

    public long getCount() {
        return this.count;
    }

    @Override
    public int read() throws IOException {
        return this.incCount(this.in.read());
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.addCount(this.in.read(b, off, len));
    }

    @Override
    public long skip(long n) throws IOException {
        return this.addCount(this.in.skip(n));
    }

    @Override
    public synchronized void mark(int readlimit) {
        this.in.mark(readlimit);
        this.mark = this.count;
    }

    @Override
    public synchronized void reset() throws IOException {
        this.in.reset();
        this.count = this.mark;
    }

    private int incCount(int read) {
        if (read >= 0) {
            ++this.count;
        }
        return read;
    }

    private int addCount(int read) {
        if (read > 0) {
            this.count += (long)read;
        }
        return read;
    }

    private long addCount(long skip) {
        if (skip > 0L) {
            this.count += skip;
        }
        return skip;
    }
}

