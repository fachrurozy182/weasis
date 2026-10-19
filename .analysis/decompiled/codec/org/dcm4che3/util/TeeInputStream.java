/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TeeInputStream
extends FilterInputStream {
    private final OutputStream out;

    public TeeInputStream(InputStream in, OutputStream out) {
        super(in);
        if (out == null) {
            throw new NullPointerException("out");
        }
        this.out = out;
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b >= 0) {
            this.out.write(b);
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if ((len = super.read(b, off, len)) > 0) {
            this.out.write(b, off, len);
        }
        return len;
    }
}

