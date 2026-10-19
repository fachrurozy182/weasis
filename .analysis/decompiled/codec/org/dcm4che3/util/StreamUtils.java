/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.ResourceLocator;

public class StreamUtils {
    private static final int COPY_BUFFER_SIZE = 2048;

    public static int readAvailable(InputStream in, byte[] b, int off, int len) throws IOException {
        int count;
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        int wpos = off;
        while (len > 0 && (count = in.read(b, wpos, len)) >= 0) {
            wpos += count;
            len -= count;
        }
        return wpos - off;
    }

    public static void readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        if (StreamUtils.readAvailable(in, b, off, len) < len) {
            throw new EOFException();
        }
    }

    public static void skipFully(InputStream in, long n) throws IOException {
        while (n > 0L) {
            long count = in.skip(n);
            if (count == 0L) {
                if (in.read() == -1) {
                    throw new EOFException();
                }
                count = 1L;
            }
            n -= count;
        }
    }

    public static void copy(InputStream in, OutputStream out, byte[] buf) throws IOException {
        int count;
        while ((count = in.read(buf, 0, buf.length)) > 0) {
            if (out == null) continue;
            out.write(buf, 0, count);
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        StreamUtils.copy(in, out, new byte[2048]);
    }

    public static void copy(InputStream in, OutputStream out, int len, byte[] buf) throws IOException {
        StreamUtils.copy(in, out, StreamUtils.unsignedInt(len), buf);
    }

    public static void copy(InputStream in, OutputStream out, long len, byte[] buf) throws IOException {
        if (len < 0L) {
            throw new IndexOutOfBoundsException();
        }
        while (len > 0L) {
            int count = in.read(buf, 0, (int)Math.min(len, (long)buf.length));
            if (count < 0) {
                throw new EOFException();
            }
            out.write(buf, 0, count);
            len -= (long)count;
        }
    }

    public static void copy(InputStream in, OutputStream out, int len) throws IOException {
        StreamUtils.copy(in, out, StreamUtils.unsignedInt(len));
    }

    public static void copy(InputStream in, OutputStream out, long len) throws IOException {
        StreamUtils.copy(in, out, len & 0xFFFFFFFFL, new byte[(int)Math.min(len, 2048L)]);
    }

    public static void copy(InputStream in, OutputStream out, int len, int swapBytes, byte[] buf) throws IOException {
        StreamUtils.copy(in, out, StreamUtils.unsignedInt(len), swapBytes, buf);
    }

    public static void copy(InputStream in, OutputStream out, long len, int swapBytes, byte[] buf) throws IOException {
        if (swapBytes == 1) {
            StreamUtils.copy(in, out, len, buf);
            return;
        }
        if (swapBytes != 2 && swapBytes != 4) {
            throw new IllegalArgumentException("swapBytes: " + swapBytes);
        }
        if (len < 0L || len % (long)swapBytes != 0L) {
            throw new IllegalArgumentException("length: " + len);
        }
        int off = 0;
        while (len > 0L) {
            int count = in.read(buf, off, (int)Math.min(len, (long)(buf.length - off)));
            if (count < 0) {
                throw new EOFException();
            }
            len -= (long)count;
            off = (count += off) % swapBytes;
            count -= off;
            switch (swapBytes) {
                case 2: {
                    ByteUtils.swapShorts(buf, 0, count);
                    break;
                }
                case 4: {
                    ByteUtils.swapInts(buf, 0, count);
                    break;
                }
                case 8: {
                    ByteUtils.swapLongs(buf, 0, count);
                }
            }
            out.write(buf, 0, count);
            if (off <= 0) continue;
            System.arraycopy(buf, count, buf, 0, off);
        }
    }

    public static void copy(InputStream in, OutputStream out, int len, int swapBytes) throws IOException {
        StreamUtils.copy(in, out, StreamUtils.unsignedInt(len), swapBytes);
    }

    private static long unsignedInt(int len) {
        return (long)len & 0xFFFFFFFFL;
    }

    public static void copy(InputStream in, OutputStream out, long len, int swapBytes) throws IOException {
        StreamUtils.copy(in, out, len, swapBytes, new byte[(int)Math.min(len, 2048L)]);
    }

    public static InputStream openFileOrURL(String name) throws IOException {
        if (name.startsWith("resource:")) {
            URL url = ResourceLocator.getResourceURL(name.substring(9), StreamUtils.class);
            if (url == null) {
                throw new FileNotFoundException(name);
            }
            return url.openStream();
        }
        if (name.indexOf(58) < 2) {
            return new FileInputStream(name);
        }
        return new URL(name).openStream();
    }

    public static void skipAll(InputStream in) throws IOException {
        StreamUtils.copy(in, StreamUtils.nullOutputStream());
    }

    public static OutputStream nullOutputStream() {
        return new OutputStream(){
            private volatile boolean closed;

            private void ensureOpen() throws IOException {
                if (this.closed) {
                    throw new IOException("Stream closed");
                }
            }

            @Override
            public void write(int b) throws IOException {
                this.ensureOpen();
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                this.ensureOpen();
            }

            @Override
            public void close() {
                this.closed = true;
            }
        };
    }

    public static InputStream readSkippedInputStream(InputStream in) {
        return StreamUtils.readSkippedInputStream(in, new byte[2048]);
    }

    public static InputStream readSkippedInputStream(InputStream in, final byte[] buf) {
        return new FilterInputStream(in){

            @Override
            public long skip(long n) throws IOException {
                long remaining;
                int read;
                for (remaining = n; remaining > 0L && (read = this.read(buf, 0, (int)Math.min(remaining, (long)buf.length))) > 0; remaining -= (long)read) {
                }
                return n - remaining;
            }
        };
    }
}

