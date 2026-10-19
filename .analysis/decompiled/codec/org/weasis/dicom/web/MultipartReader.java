/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.dicom.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.web.MultipartConstants;
import org.weasis.dicom.web.MultipartStreamException;

public final class MultipartReader
implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipartReader.class);
    public static final int MAX_HEADER_SIZE = 16384;
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private static final int MIN_BUFFER_SIZE = 256;
    private static final int MAX_BUFFER_SIZE = 0x100000;
    private final InputStream inputStream;
    private final byte[] boundary;
    private final StreamBuffer buffer;
    private Charset headerEncoding = StandardCharsets.UTF_8;
    private int currentBoundaryLength;

    public MultipartReader(InputStream inputStream, byte[] boundary) {
        this(inputStream, boundary, 4096);
    }

    public MultipartReader(InputStream inputStream, byte[] boundary, int bufferSize) {
        this.inputStream = Objects.requireNonNull(inputStream, "Input stream cannot be null");
        Objects.requireNonNull(boundary, "Boundary cannot be null");
        this.validateBufferSize(bufferSize);
        this.buffer = new StreamBuffer(inputStream, bufferSize);
        this.boundary = this.createBoundaryWithPrefix(boundary);
        this.currentBoundaryLength = this.boundary.length;
    }

    public Charset getHeaderEncoding() {
        return this.headerEncoding;
    }

    public void setHeaderEncoding(String encoding) {
        try {
            this.headerEncoding = encoding != null ? Charset.forName(encoding) : StandardCharsets.UTF_8;
        }
        catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid encoding '{}', using UTF-8", (Object)encoding);
            this.headerEncoding = StandardCharsets.UTF_8;
        }
    }

    public boolean skipFirstBoundary() throws IOException {
        return this.processBoundary(true);
    }

    public boolean readBoundary() throws IOException {
        return this.processBoundary(false);
    }

    public String readHeaders() throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();){
            this.readHeadersToStream(output);
            String string = output.toString(this.headerEncoding);
            return string;
        }
    }

    public PartInputStream newPartInputStream() {
        return new PartInputStream();
    }

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }

    private void validateBufferSize(int size) {
        if (size < 256 || size > 0x100000) {
            throw new IllegalArgumentException("Buffer size must be between %d and %d bytes".formatted(new Object[]{256, 0x100000}));
        }
    }

    private byte[] createBoundaryWithPrefix(byte[] boundary) {
        byte[] prefix = MultipartConstants.Separator.BOUNDARY.getBytes();
        byte[] fullBoundary = new byte[boundary.length + prefix.length];
        System.arraycopy(prefix, 0, fullBoundary, 0, prefix.length);
        System.arraycopy(boundary, 0, fullBoundary, prefix.length, boundary.length);
        return fullBoundary;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean processBoundary(boolean isFirst) throws IOException {
        byte[] originalBoundary = (byte[])this.boundary.clone();
        int originalLength = this.currentBoundaryLength;
        try {
            if (isFirst) {
                this.adjustBoundaryForFirst();
                this.discardDataUntilBoundary();
                this.buffer.skip(this.currentBoundaryLength);
            } else {
                this.buffer.skip(this.currentBoundaryLength);
            }
            boolean bl = this.readBoundaryTerminator();
            return bl;
        }
        finally {
            if (isFirst) {
                this.restoreBoundary(originalBoundary, originalLength);
            }
        }
    }

    private void adjustBoundaryForFirst() {
        System.arraycopy(this.boundary, 2, this.boundary, 0, this.boundary.length - 2);
        this.currentBoundaryLength -= 2;
    }

    private void restoreBoundary(byte[] original, int originalLength) {
        System.arraycopy(original, 0, this.boundary, 0, original.length);
        this.currentBoundaryLength = originalLength;
    }

    private boolean readBoundaryTerminator() throws IOException {
        byte[] terminator = new byte[]{this.buffer.readByte(), this.buffer.readByte()};
        if (Arrays.equals(terminator, MultipartConstants.Separator.STREAM.getBytes())) {
            return false;
        }
        if (Arrays.equals(terminator, MultipartConstants.Separator.FIELD.getBytes())) {
            return true;
        }
        throw new MultipartStreamException("Invalid boundary terminator: " + Arrays.toString(terminator));
    }

    private void readHeadersToStream(ByteArrayOutputStream output) throws IOException {
        byte[] separator = MultipartConstants.Separator.HEADER.getBytes();
        int matchCount = 0;
        int totalBytes = 0;
        while (matchCount < separator.length) {
            byte currentByte = this.buffer.readByte();
            if (++totalBytes > 16384) {
                throw MultipartStreamException.headerSizeExceeded(totalBytes, 16384);
            }
            matchCount = currentByte == separator[matchCount] ? matchCount + 1 : 0;
            output.write(currentByte);
        }
    }

    private void discardDataUntilBoundary() throws IOException {
        try (PartInputStream partStream = this.newPartInputStream();){
            partStream.transferTo(OutputStream.nullOutputStream());
        }
    }

    private static class StreamBuffer {
        private final InputStream inputStream;
        private final byte[] buffer;
        private final int bufferSize;
        private int position = 0;
        private int limit = 0;

        StreamBuffer(InputStream inputStream, int bufferSize) {
            this.inputStream = inputStream;
            this.bufferSize = bufferSize;
            this.buffer = new byte[bufferSize];
        }

        byte readByte() throws IOException {
            if (this.position >= this.limit) {
                this.fillBuffer();
            }
            return this.buffer[this.position++];
        }

        void readBytes(byte[] dest, int offset, int length) throws IOException {
            int available;
            int destOffset = offset;
            for (int remaining = length; remaining > 0; remaining -= available) {
                if (this.position >= this.limit) {
                    this.fillBuffer();
                }
                available = Math.min(remaining, this.limit - this.position);
                System.arraycopy(this.buffer, this.position, dest, destOffset, available);
                this.position += available;
                destOffset += available;
            }
        }

        void skip(int bytes) throws IOException {
            int available;
            for (int remaining = bytes; remaining > 0; remaining -= available) {
                if (this.position >= this.limit) {
                    this.fillBuffer();
                }
                available = Math.min(remaining, this.limit - this.position);
                this.position += available;
            }
        }

        int available() {
            return this.limit - this.position;
        }

        int getPosition() {
            return this.position;
        }

        int findPattern(byte[] pattern, int patternLength) {
            for (int i = this.position; i <= this.limit - patternLength; ++i) {
                if (!this.matchesPattern(i, pattern, patternLength)) continue;
                return i;
            }
            return -1;
        }

        private boolean matchesPattern(int start, byte[] pattern, int patternLength) {
            for (int i = 0; i < patternLength; ++i) {
                if (this.buffer[start + i] == pattern[i]) continue;
                return false;
            }
            return true;
        }

        private void fillBuffer() throws IOException {
            if (this.position < this.limit) {
                int remaining = this.limit - this.position;
                System.arraycopy(this.buffer, this.position, this.buffer, 0, remaining);
                this.limit = remaining;
                this.position = 0;
            } else {
                this.limit = 0;
                this.position = 0;
            }
            int bytesRead = this.inputStream.read(this.buffer, this.limit, this.bufferSize - this.limit);
            if (bytesRead == -1) {
                throw MultipartStreamException.unexpectedEndOfStream();
            }
            this.limit += bytesRead;
        }
    }

    public final class PartInputStream
    extends InputStream {
        private final BoundaryDetector detector;
        private boolean closed = false;

        PartInputStream() {
            this.detector = new BoundaryDetector();
        }

        @Override
        public int read() throws IOException {
            this.validateNotClosed();
            while (!this.detector.hasReachedBoundary()) {
                if (this.detector.getSafeAvailable() > 0) {
                    int b = MultipartReader.this.buffer.readByte() & 0xFF;
                    this.detector.refresh();
                    return b;
                }
                MultipartReader.this.buffer.fillBuffer();
                this.detector.refresh();
            }
            return -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            this.validateNotClosed();
            Objects.checkFromIndexSize(off, len, b.length);
            if (len == 0) {
                return 0;
            }
            while (!this.detector.hasReachedBoundary()) {
                int safe = this.detector.getSafeAvailable();
                if (safe > 0) {
                    int bytesToRead = Math.min(len, safe);
                    MultipartReader.this.buffer.readBytes(b, off, bytesToRead);
                    this.detector.refresh();
                    return bytesToRead;
                }
                MultipartReader.this.buffer.fillBuffer();
                this.detector.refresh();
            }
            return -1;
        }

        @Override
        public int available() throws IOException {
            this.validateNotClosed();
            return this.detector.hasReachedBoundary() ? 0 : this.detector.getSafeAvailable();
        }

        @Override
        public long skip(long n) throws IOException {
            this.validateNotClosed();
            if (n <= 0L) {
                return 0L;
            }
            long total = 0L;
            while (total < n && !this.detector.hasReachedBoundary()) {
                int safe = this.detector.getSafeAvailable();
                if (safe > 0) {
                    int step = (int)Math.min(n - total, (long)safe);
                    MultipartReader.this.buffer.skip(step);
                    this.detector.refresh();
                    total += (long)step;
                    continue;
                }
                MultipartReader.this.buffer.fillBuffer();
                this.detector.refresh();
            }
            return total;
        }

        @Override
        public void close() throws IOException {
            if (!this.closed) {
                try {
                    while (!this.detector.hasReachedBoundary()) {
                        int safe = this.detector.getSafeAvailable();
                        if (safe > 0) {
                            MultipartReader.this.buffer.skip(safe);
                            this.detector.refresh();
                            continue;
                        }
                        MultipartReader.this.buffer.fillBuffer();
                        this.detector.refresh();
                    }
                }
                catch (IOException iOException) {
                }
                finally {
                    this.closed = true;
                }
            }
        }

        public boolean isClosed() {
            return this.closed;
        }

        private void validateNotClosed() throws IOException {
            if (this.closed) {
                throw new IOException("PartInputStream is closed");
            }
        }
    }

    private class BoundaryDetector {
        private int boundaryPosition = -1;
        private boolean boundaryFound;

        BoundaryDetector() {
            this.refresh();
        }

        boolean hasReachedBoundary() {
            return this.boundaryFound && MultipartReader.this.buffer.getPosition() >= this.boundaryPosition;
        }

        int getSafeAvailable() {
            if (this.boundaryFound) {
                return Math.max(0, this.boundaryPosition - MultipartReader.this.buffer.getPosition());
            }
            return Math.max(0, MultipartReader.this.buffer.available() - MultipartReader.this.currentBoundaryLength);
        }

        void refresh() {
            int found = MultipartReader.this.buffer.findPattern(MultipartReader.this.boundary, MultipartReader.this.currentBoundaryLength);
            if (found != -1) {
                this.boundaryPosition = found;
                this.boundaryFound = true;
            } else {
                this.boundaryFound = false;
            }
        }
    }
}

