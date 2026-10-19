/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.img.stream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.Objects;

public final class SeekableInMemoryByteChannel
implements SeekableByteChannel {
    private static final int EXPONENTIAL_GROWTH_LIMIT = 0x3FFFFFFF;
    private static final int INITIAL_CAPACITY = 32;
    private byte[] data;
    private volatile boolean closed;
    private int position;
    private int size;

    public SeekableInMemoryByteChannel(byte[] data) {
        this.data = Objects.requireNonNull(data, "Data array cannot be null");
        this.size = data.length;
        this.position = 0;
    }

    public SeekableInMemoryByteChannel() {
        this(32);
    }

    public SeekableInMemoryByteChannel(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity cannot be negative: " + initialCapacity);
        }
        this.data = new byte[initialCapacity];
        this.size = 0;
        this.position = 0;
    }

    public byte[] array() {
        return this.data;
    }

    @Override
    public long position() {
        return this.position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        this.ensureOpen();
        this.validatePosition(newPosition);
        this.position = (int)newPosition;
        return this;
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public SeekableByteChannel truncate(long newSize) {
        this.validateSize(newSize);
        int truncatedSize = (int)newSize;
        this.size = Math.min(this.size, truncatedSize);
        this.position = Math.min(this.position, truncatedSize);
        return this;
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException {
        this.ensureOpen();
        int bytesToRead = Math.min(buffer.remaining(), this.size - this.position);
        if (bytesToRead <= 0) {
            return -1;
        }
        buffer.put(this.data, this.position, bytesToRead);
        this.position += bytesToRead;
        return bytesToRead;
    }

    @Override
    public int write(ByteBuffer buffer) throws IOException {
        this.ensureOpen();
        int bytesToWrite = buffer.remaining();
        int requiredCapacity = this.position + bytesToWrite;
        if (requiredCapacity < 0) {
            bytesToWrite = Integer.MAX_VALUE - this.position;
            requiredCapacity = Integer.MAX_VALUE;
        }
        this.ensureCapacity(requiredCapacity);
        buffer.get(this.data, this.position, bytesToWrite);
        this.position += bytesToWrite;
        this.size = Math.max(this.size, this.position);
        return bytesToWrite;
    }

    @Override
    public boolean isOpen() {
        return !this.closed;
    }

    @Override
    public void close() {
        this.closed = true;
    }

    private void validatePosition(long position) {
        if (position < 0L || position > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Position must be in range [0, %d], got: %d".formatted(new Object[]{Integer.MAX_VALUE, position}));
        }
    }

    private void validateSize(long size) {
        if (size < 0L || size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Size must be in range [0, %d], got: %d".formatted(new Object[]{Integer.MAX_VALUE, size}));
        }
    }

    private void ensureOpen() throws ClosedChannelException {
        if (!this.isOpen()) {
            throw new ClosedChannelException();
        }
    }

    private void ensureCapacity(int requiredCapacity) {
        if (requiredCapacity <= this.data.length) {
            return;
        }
        this.data = Arrays.copyOf(this.data, this.calculateNewCapacity(this.data.length, requiredCapacity));
    }

    private int calculateNewCapacity(int currentCapacity, int requiredCapacity) {
        int newCapacity;
        if (requiredCapacity < 0x3FFFFFFF) {
            for (newCapacity = Math.max(currentCapacity, 1); newCapacity < requiredCapacity; newCapacity <<= 1) {
            }
        } else {
            newCapacity = requiredCapacity;
        }
        return newCapacity;
    }
}

