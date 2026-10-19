/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.vol;

import java.lang.reflect.Array;
import java.util.Arrays;

public final class ChunkedArray<A> {
    private static final int CHUNK_SHIFT = 27;
    static final int CHUNK_SIZE = 0x8000000;
    private static final long CHUNK_MASK = 0x7FFFFFFL;
    private final A[] chunks;
    private final long totalElements;
    private final ArrayOps<A> ops;
    private static final ArrayOps<byte[]> BYTE_OPS = new ArrayOps<byte[]>(){

        @Override
        public byte[] newArray(int size) {
            return new byte[size];
        }

        @Override
        public Class<byte[]> arrayClass() {
            return byte[].class;
        }

        @Override
        public void fill(byte[] array, Number value) {
            Arrays.fill(array, value.byteValue());
        }
    };
    private static final ArrayOps<short[]> SHORT_OPS = new ArrayOps<short[]>(){

        @Override
        public short[] newArray(int size) {
            return new short[size];
        }

        @Override
        public Class<short[]> arrayClass() {
            return short[].class;
        }

        @Override
        public void fill(short[] array, Number value) {
            Arrays.fill(array, value.shortValue());
        }
    };
    private static final ArrayOps<int[]> INT_OPS = new ArrayOps<int[]>(){

        @Override
        public int[] newArray(int size) {
            return new int[size];
        }

        @Override
        public Class<int[]> arrayClass() {
            return int[].class;
        }

        @Override
        public void fill(int[] array, Number value) {
            Arrays.fill(array, value.intValue());
        }
    };
    private static final ArrayOps<float[]> FLOAT_OPS = new ArrayOps<float[]>(){

        @Override
        public float[] newArray(int size) {
            return new float[size];
        }

        @Override
        public Class<float[]> arrayClass() {
            return float[].class;
        }

        @Override
        public void fill(float[] array, Number value) {
            Arrays.fill(array, value.floatValue());
        }
    };
    private static final ArrayOps<double[]> DOUBLE_OPS = new ArrayOps<double[]>(){

        @Override
        public double[] newArray(int size) {
            return new double[size];
        }

        @Override
        public Class<double[]> arrayClass() {
            return double[].class;
        }

        @Override
        public void fill(double[] array, Number value) {
            Arrays.fill(array, value.doubleValue());
        }
    };

    ChunkedArray(long totalElements, ArrayOps<A> ops) {
        if (totalElements < 0L) {
            throw new IllegalArgumentException("Negative total elements: " + totalElements);
        }
        this.totalElements = totalElements;
        this.ops = ops;
        int numChunks = (int)((totalElements + 0x8000000L - 1L) / 0x8000000L);
        this.chunks = (Object[])Array.newInstance(ops.arrayClass(), numChunks);
        for (int i = 0; i < numChunks; ++i) {
            long remaining = totalElements - (long)i * 0x8000000L;
            int chunkLen = (int)Math.min(remaining, 0x8000000L);
            this.chunks[i] = ops.newArray(chunkLen);
        }
    }

    public long size() {
        return this.totalElements;
    }

    public boolean isSingleChunk() {
        return this.chunks.length == 1;
    }

    public A singleChunk() {
        return this.chunks[0];
    }

    public A getChunk(int chunkIndex) {
        return this.chunks[chunkIndex];
    }

    public int chunkCount() {
        return this.chunks.length;
    }

    public int chunkOffset(long globalIndex) {
        return (int)(globalIndex & 0x7FFFFFFL);
    }

    public int chunkIndex(long globalIndex) {
        return (int)(globalIndex >>> 27);
    }

    public void fill(Number value) {
        for (A chunk : this.chunks) {
            this.ops.fill(chunk, value);
        }
    }

    public void copyTo(long srcPos, A dest, int destPos, long length) {
        long remaining = length;
        long gIdx = srcPos;
        int dIdx = destPos;
        while (remaining > 0L) {
            int ci = this.chunkIndex(gIdx);
            int co = this.chunkOffset(gIdx);
            int toCopy = (int)Math.min(remaining, 0x8000000L - (long)co);
            System.arraycopy(this.chunks[ci], co, dest, dIdx, toCopy);
            remaining -= (long)toCopy;
            gIdx += (long)toCopy;
            dIdx += toCopy;
        }
    }

    public void copyFrom(long destPos, A src, int srcPos, long length) {
        long remaining = length;
        long gIdx = destPos;
        int sIdx = srcPos;
        while (remaining > 0L) {
            int ci = this.chunkIndex(gIdx);
            int co = this.chunkOffset(gIdx);
            int toCopy = (int)Math.min(remaining, 0x8000000L - (long)co);
            System.arraycopy(src, sIdx, this.chunks[ci], co, toCopy);
            remaining -= (long)toCopy;
            gIdx += (long)toCopy;
            sIdx += toCopy;
        }
    }

    public void copyTo(long srcPos, ChunkedArray<A> dest, long destPos, long length) {
        long remaining = length;
        long sIdx = srcPos;
        long dIdx = destPos;
        while (remaining > 0L) {
            int sci = this.chunkIndex(sIdx);
            int sco = this.chunkOffset(sIdx);
            int dci = dest.chunkIndex(dIdx);
            int dco = dest.chunkOffset(dIdx);
            int srcAvail = 0x8000000 - sco;
            int destAvail = 0x8000000 - dco;
            int toCopy = (int)Math.min(remaining, (long)Math.min(srcAvail, destAvail));
            System.arraycopy(this.chunks[sci], sco, dest.chunks[dci], dco, toCopy);
            remaining -= (long)toCopy;
            sIdx += (long)toCopy;
            dIdx += (long)toCopy;
        }
    }

    public void copyFrom(long destPos, ChunkedArray<A> src, long srcPos, long length) {
        src.copyTo(srcPos, this, destPos, length);
    }

    public static ChunkedArray<byte[]> ofByte(long totalElements) {
        return new ChunkedArray<byte[]>(totalElements, BYTE_OPS);
    }

    public static ChunkedArray<short[]> ofShort(long totalElements) {
        return new ChunkedArray<short[]>(totalElements, SHORT_OPS);
    }

    public static ChunkedArray<int[]> ofInt(long totalElements) {
        return new ChunkedArray<int[]>(totalElements, INT_OPS);
    }

    public static ChunkedArray<float[]> ofFloat(long totalElements) {
        return new ChunkedArray<float[]>(totalElements, FLOAT_OPS);
    }

    public static ChunkedArray<double[]> ofDouble(long totalElements) {
        return new ChunkedArray<double[]>(totalElements, DOUBLE_OPS);
    }

    public static interface ArrayOps<A> {
        public A newArray(int var1);

        public Class<A> arrayClass();

        public void fill(A var1, Number var2);
    }
}

