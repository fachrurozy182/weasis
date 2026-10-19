/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class RAFInputStreamAdapter
extends InputStream {
    private final RandomAccessFile raf;
    private long markedPos;
    private IOException markException;

    public RAFInputStreamAdapter(RandomAccessFile raf) {
        if (raf == null) {
            throw new NullPointerException();
        }
        this.raf = raf;
    }

    @Override
    public int read() throws IOException {
        return this.raf.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.raf.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return this.raf.skipBytes((int)n);
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            this.markedPos = this.raf.getFilePointer();
            this.markException = null;
        }
        catch (IOException e) {
            this.markException = e;
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        if (this.markException != null) {
            throw this.markException;
        }
        this.raf.seek(this.markedPos);
    }

    @Override
    public boolean markSupported() {
        return true;
    }
}

