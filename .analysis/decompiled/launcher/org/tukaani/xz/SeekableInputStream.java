/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.IOException;
import java.io.InputStream;

public abstract class SeekableInputStream
extends InputStream {
    @Override
    public long skip(long n) throws IOException {
        if (n <= 0L) {
            return 0L;
        }
        long size = this.length();
        long pos = this.position();
        if (pos >= size) {
            return 0L;
        }
        if (size - pos < n) {
            n = size - pos;
        }
        this.seek(pos + n);
        return n;
    }

    public abstract long length() throws IOException;

    public abstract long position() throws IOException;

    public abstract void seek(long var1) throws IOException;
}

