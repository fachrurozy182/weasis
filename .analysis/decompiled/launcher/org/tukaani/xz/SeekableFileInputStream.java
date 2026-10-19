/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.tukaani.xz.SeekableInputStream;

public class SeekableFileInputStream
extends SeekableInputStream {
    protected RandomAccessFile randomAccessFile;

    public SeekableFileInputStream(File file) throws FileNotFoundException {
        this.randomAccessFile = new RandomAccessFile(file, "r");
    }

    public SeekableFileInputStream(String name) throws FileNotFoundException {
        this.randomAccessFile = new RandomAccessFile(name, "r");
    }

    public SeekableFileInputStream(RandomAccessFile randomAccessFile) {
        this.randomAccessFile = randomAccessFile;
    }

    @Override
    public int read() throws IOException {
        return this.randomAccessFile.read();
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return this.randomAccessFile.read(buf);
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        return this.randomAccessFile.read(buf, off, len);
    }

    @Override
    public void close() throws IOException {
        this.randomAccessFile.close();
    }

    @Override
    public long length() throws IOException {
        return this.randomAccessFile.length();
    }

    @Override
    public long position() throws IOException {
        return this.randomAccessFile.getFilePointer();
    }

    @Override
    public void seek(long pos) throws IOException {
        this.randomAccessFile.seek(pos);
    }
}

