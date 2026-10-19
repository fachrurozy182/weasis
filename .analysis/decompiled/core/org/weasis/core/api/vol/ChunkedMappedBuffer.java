/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.runtime.SwitchBootstraps
 *  org.weasis.core.util.FileUtil
 */
package org.weasis.core.api.vol;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.runtime.SwitchBootstraps;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import org.weasis.core.api.vol.ChunkedArray;
import org.weasis.core.util.FileUtil;

public final class ChunkedMappedBuffer {
    static final long CHUNK_BYTE_SIZE = 0x40000000L;
    private static final int CHUNK_SHIFT = 30;
    private static final long CHUNK_MASK = 0x3FFFFFFFL;
    private final MappedByteBuffer[] chunks;
    private final long totalBytes;
    private final File backingFile;

    public ChunkedMappedBuffer(File file, long totalBytes) throws IOException {
        if (totalBytes < 0L) {
            throw new IllegalArgumentException("Negative total bytes: " + totalBytes);
        }
        this.totalBytes = totalBytes;
        this.backingFile = file;
        int numChunks = (int)((totalBytes + 0x40000000L - 1L) / 0x40000000L);
        this.chunks = new MappedByteBuffer[Math.max(numChunks, 1)];
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");){
            raf.setLength(totalBytes);
            FileChannel channel = raf.getChannel();
            for (int i = 0; i < this.chunks.length; ++i) {
                long offset = (long)i * 0x40000000L;
                long remaining = totalBytes - offset;
                long chunkLen = Math.min(remaining, 0x40000000L);
                this.chunks[i] = channel.map(FileChannel.MapMode.READ_WRITE, offset, chunkLen);
            }
        }
    }

    public long totalBytes() {
        return this.totalBytes;
    }

    private int chunkIndex(long byteOffset) {
        return (int)(byteOffset >>> 30);
    }

    private int chunkOffset(long byteOffset) {
        return (int)(byteOffset & 0x3FFFFFFFL);
    }

    public <A> void readInto(ChunkedArray<A> raster, long byteOffset, int totalElements, int byteDepth) {
        int count;
        long globalIndex = 0L;
        for (int remaining = totalElements; remaining > 0; remaining -= count) {
            A a;
            int ci = raster.chunkIndex(globalIndex);
            int co = raster.chunkOffset(globalIndex);
            A chunk = raster.getChunk(ci);
            int chunkCapacity = Array.getLength(chunk) - co;
            count = Math.min(remaining, chunkCapacity);
            long bufferPos = byteOffset + globalIndex * (long)byteDepth;
            Objects.requireNonNull(chunk);
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{byte[].class, short[].class, int[].class, float[].class, double[].class}, a, (int)n)) {
                case 0: {
                    byte[] a2 = (byte[])a;
                    for (int i = 0; i < count; ++i) {
                        a2[co + i] = this.get(bufferPos + (long)i * (long)byteDepth);
                    }
                    break;
                }
                case 1: {
                    short[] a3 = (short[])a;
                    for (int i = 0; i < count; ++i) {
                        a3[co + i] = this.getShort(bufferPos + (long)i * (long)byteDepth);
                    }
                    break;
                }
                case 2: {
                    int[] a4 = (int[])a;
                    for (int i = 0; i < count; ++i) {
                        a4[co + i] = this.getInt(bufferPos + (long)i * (long)byteDepth);
                    }
                    break;
                }
                case 3: {
                    float[] a5 = (float[])a;
                    for (int i = 0; i < count; ++i) {
                        a5[co + i] = this.getFloat(bufferPos + (long)i * (long)byteDepth);
                    }
                    break;
                }
                case 4: {
                    double[] a6 = (double[])a;
                    for (int i = 0; i < count; ++i) {
                        a6[co + i] = this.getDouble(bufferPos + (long)i * (long)byteDepth);
                    }
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected array type: " + String.valueOf(chunk.getClass()));
                }
            }
            globalIndex += (long)count;
        }
    }

    public byte get(long byteOffset) {
        return this.chunks[this.chunkIndex(byteOffset)].get(this.chunkOffset(byteOffset));
    }

    public void put(long byteOffset, byte value) {
        this.chunks[this.chunkIndex(byteOffset)].put(this.chunkOffset(byteOffset), value);
    }

    public short getShort(long byteOffset) {
        return this.chunks[this.chunkIndex(byteOffset)].getShort(this.chunkOffset(byteOffset));
    }

    public void putShort(long byteOffset, short value) {
        this.chunks[this.chunkIndex(byteOffset)].putShort(this.chunkOffset(byteOffset), value);
    }

    public int getInt(long byteOffset) {
        return this.chunks[this.chunkIndex(byteOffset)].getInt(this.chunkOffset(byteOffset));
    }

    public void putInt(long byteOffset, int value) {
        this.chunks[this.chunkIndex(byteOffset)].putInt(this.chunkOffset(byteOffset), value);
    }

    public float getFloat(long byteOffset) {
        return this.chunks[this.chunkIndex(byteOffset)].getFloat(this.chunkOffset(byteOffset));
    }

    public void putFloat(long byteOffset, float value) {
        this.chunks[this.chunkIndex(byteOffset)].putFloat(this.chunkOffset(byteOffset), value);
    }

    public double getDouble(long byteOffset) {
        return this.chunks[this.chunkIndex(byteOffset)].getDouble(this.chunkOffset(byteOffset));
    }

    public void putDouble(long byteOffset, double value) {
        this.chunks[this.chunkIndex(byteOffset)].putDouble(this.chunkOffset(byteOffset), value);
    }

    public void getFloats(long byteOffset, float[] dst, int dstOff, int count) {
        while (count > 0) {
            int ci = this.chunkIndex(byteOffset);
            int co = this.chunkOffset(byteOffset);
            MappedByteBuffer chunk = this.chunks[ci];
            int floatsInChunk = chunk.limit() - co >>> 2;
            int n = Math.min(count, floatsInChunk);
            ByteBuffer slice = chunk.duplicate();
            slice.position(co);
            slice.asFloatBuffer().get(dst, dstOff, n);
            byteOffset += (long)n * 4L;
            dstOff += n;
            count -= n;
        }
    }

    public void putBytes(long byteOffset, byte[] src, int srcOff, int count) {
        while (count > 0) {
            int ci = this.chunkIndex(byteOffset);
            int co = this.chunkOffset(byteOffset);
            MappedByteBuffer chunk = this.chunks[ci];
            int bytesInChunk = chunk.limit() - co;
            int n = Math.min(count, bytesInChunk);
            ByteBuffer slice = chunk.duplicate();
            slice.position(co);
            slice.put(src, srcOff, n);
            byteOffset += (long)n;
            srcOff += n;
            count -= n;
        }
    }

    public void putShorts(long byteOffset, short[] src, int srcOff, int count) {
        while (count > 0) {
            int ci = this.chunkIndex(byteOffset);
            int co = this.chunkOffset(byteOffset);
            MappedByteBuffer chunk = this.chunks[ci];
            int shortsInChunk = chunk.limit() - co >>> 1;
            int n = Math.min(count, shortsInChunk);
            ByteBuffer slice = chunk.duplicate();
            slice.position(co);
            slice.asShortBuffer().put(src, srcOff, n);
            byteOffset += (long)n * 2L;
            srcOff += n;
            count -= n;
        }
    }

    public void putInts(long byteOffset, int[] src, int srcOff, int count) {
        while (count > 0) {
            int ci = this.chunkIndex(byteOffset);
            int co = this.chunkOffset(byteOffset);
            MappedByteBuffer chunk = this.chunks[ci];
            int intsInChunk = chunk.limit() - co >>> 2;
            int n = Math.min(count, intsInChunk);
            ByteBuffer slice = chunk.duplicate();
            slice.position(co);
            slice.asIntBuffer().put(src, srcOff, n);
            byteOffset += (long)n * 4L;
            srcOff += n;
            count -= n;
        }
    }

    public void putFloats(long byteOffset, float[] src, int srcOff, int count) {
        while (count > 0) {
            int ci = this.chunkIndex(byteOffset);
            int co = this.chunkOffset(byteOffset);
            MappedByteBuffer chunk = this.chunks[ci];
            int floatsInChunk = chunk.limit() - co >>> 2;
            int n = Math.min(count, floatsInChunk);
            ByteBuffer slice = chunk.duplicate();
            slice.position(co);
            slice.asFloatBuffer().put(src, srcOff, n);
            byteOffset += (long)n * 4L;
            srcOff += n;
            count -= n;
        }
    }

    public void putDoubles(long byteOffset, double[] src, int srcOff, int count) {
        while (count > 0) {
            int ci = this.chunkIndex(byteOffset);
            int co = this.chunkOffset(byteOffset);
            MappedByteBuffer chunk = this.chunks[ci];
            int doublesInChunk = chunk.limit() - co >>> 3;
            int n = Math.min(count, doublesInChunk);
            ByteBuffer slice = chunk.duplicate();
            slice.position(co);
            slice.asDoubleBuffer().put(src, srcOff, n);
            byteOffset += (long)n * 8L;
            srcOff += n;
            count -= n;
        }
    }

    public void force() {
        for (MappedByteBuffer chunk : this.chunks) {
            if (chunk == null) continue;
            chunk.force();
        }
    }

    public void close() {
        Arrays.fill(this.chunks, null);
        if (this.backingFile != null) {
            FileUtil.delete((Path)this.backingFile.toPath());
        }
    }
}

