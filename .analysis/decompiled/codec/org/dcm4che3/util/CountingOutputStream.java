/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class CountingOutputStream
extends FilterOutputStream {
    private volatile long count;

    public CountingOutputStream(OutputStream out) {
        super(Objects.requireNonNull(out));
    }

    public long getCount() {
        return this.count;
    }

    @Override
    public void write(int b) throws IOException {
        this.out.write(b);
        ++this.count;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
        this.count += (long)len;
    }
}

