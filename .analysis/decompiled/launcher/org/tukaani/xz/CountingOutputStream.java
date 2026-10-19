/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.IOException;
import java.io.OutputStream;
import org.tukaani.xz.FinishableOutputStream;

class CountingOutputStream
extends FinishableOutputStream {
    private final OutputStream out;
    private long size = 0L;

    public CountingOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
        this.out.write(b);
        if (this.size >= 0L) {
            ++this.size;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
        if (this.size >= 0L) {
            this.size += (long)len;
        }
    }

    @Override
    public void flush() throws IOException {
        this.out.flush();
    }

    @Override
    public void close() throws IOException {
        this.out.close();
    }

    public long getSize() {
        return this.size;
    }
}

