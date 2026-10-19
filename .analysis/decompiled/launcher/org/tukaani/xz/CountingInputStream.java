/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.IOException;
import java.io.InputStream;
import org.tukaani.xz.CloseIgnoringInputStream;

class CountingInputStream
extends CloseIgnoringInputStream {
    private long size = 0L;

    public CountingInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int ret = this.in.read();
        if (ret != -1 && this.size >= 0L) {
            ++this.size;
        }
        return ret;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int ret = this.in.read(b, off, len);
        if (ret > 0 && this.size >= 0L) {
            this.size += (long)ret;
        }
        return ret;
    }

    public long getSize() {
        return this.size;
    }
}

