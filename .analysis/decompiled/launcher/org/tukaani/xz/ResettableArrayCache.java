/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.util.ArrayList;
import java.util.List;
import org.tukaani.xz.ArrayCache;

public class ResettableArrayCache
extends ArrayCache {
    private final ArrayCache arrayCache;
    private final List<byte[]> byteArrays;
    private final List<int[]> intArrays;

    public ResettableArrayCache(ArrayCache arrayCache) {
        this.arrayCache = arrayCache;
        if (arrayCache == ArrayCache.getDummyCache()) {
            this.byteArrays = null;
            this.intArrays = null;
        } else {
            this.byteArrays = new ArrayList<byte[]>();
            this.intArrays = new ArrayList<int[]>();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public byte[] getByteArray(int size, boolean fillWithZeros) {
        byte[] array = this.arrayCache.getByteArray(size, fillWithZeros);
        if (this.byteArrays != null) {
            List<byte[]> list = this.byteArrays;
            synchronized (list) {
                this.byteArrays.add(array);
            }
        }
        return array;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void putArray(byte[] array) {
        if (this.byteArrays != null) {
            List<byte[]> list = this.byteArrays;
            synchronized (list) {
                int i = this.byteArrays.lastIndexOf(array);
                if (i != -1) {
                    this.byteArrays.remove(i);
                }
            }
            this.arrayCache.putArray(array);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int[] getIntArray(int size, boolean fillWithZeros) {
        int[] array = this.arrayCache.getIntArray(size, fillWithZeros);
        if (this.intArrays != null) {
            List<int[]> list = this.intArrays;
            synchronized (list) {
                this.intArrays.add(array);
            }
        }
        return array;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void putArray(int[] array) {
        if (this.intArrays != null) {
            List<int[]> list = this.intArrays;
            synchronized (list) {
                int i = this.intArrays.lastIndexOf(array);
                if (i != -1) {
                    this.intArrays.remove(i);
                }
            }
            this.arrayCache.putArray(array);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void reset() {
        if (this.byteArrays != null) {
            int i;
            List<byte[]> list = this.byteArrays;
            synchronized (list) {
                for (i = this.byteArrays.size() - 1; i >= 0; --i) {
                    this.arrayCache.putArray(this.byteArrays.get(i));
                }
                this.byteArrays.clear();
            }
            list = this.intArrays;
            synchronized (list) {
                for (i = this.intArrays.size() - 1; i >= 0; --i) {
                    this.arrayCache.putArray(this.intArrays.get(i));
                }
                this.intArrays.clear();
            }
        }
    }
}

