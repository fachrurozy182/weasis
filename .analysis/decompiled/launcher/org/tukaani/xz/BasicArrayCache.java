/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.tukaani.xz.ArrayCache;

public class BasicArrayCache
extends ArrayCache {
    private static final int CACHEABLE_SIZE_MIN = 32768;
    private static final int STACKS_MAX = 32;
    private static final int ELEMENTS_PER_STACK = 512;
    private final CacheMap<byte[]> byteArrayCache = new CacheMap();
    private final CacheMap<int[]> intArrayCache = new CacheMap();

    public static BasicArrayCache getInstance() {
        return LazyHolder.INSTANCE;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static <T> T getArray(CacheMap<T> cache, int size) {
        Reference r;
        Object array;
        CyclicStack stack;
        if (size < 32768) {
            return null;
        }
        CacheMap<T> cacheMap = cache;
        synchronized (cacheMap) {
            stack = (CyclicStack)cache.get(size);
        }
        if (stack == null) {
            return null;
        }
        do {
            if ((r = (Reference)stack.pop()) != null) continue;
            return null;
        } while ((array = r.get()) == null);
        return array;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static <T> void putArray(CacheMap<T> cache, T array, int size) {
        CyclicStack<SoftReference<T>> stack;
        if (size < 32768) {
            return;
        }
        CacheMap<T> cacheMap = cache;
        synchronized (cacheMap) {
            stack = (CyclicStack<SoftReference<T>>)cache.get(size);
            if (stack == null) {
                stack = new CyclicStack<SoftReference<T>>();
                cache.put(size, stack);
            }
        }
        stack.push(new SoftReference<T>(array));
    }

    @Override
    public byte[] getByteArray(int size, boolean fillWithZeros) {
        byte[] array = BasicArrayCache.getArray(this.byteArrayCache, size);
        if (array == null) {
            array = new byte[size];
        } else if (fillWithZeros) {
            Arrays.fill(array, (byte)0);
        }
        return array;
    }

    @Override
    public void putArray(byte[] array) {
        BasicArrayCache.putArray(this.byteArrayCache, array, array.length);
    }

    @Override
    public int[] getIntArray(int size, boolean fillWithZeros) {
        int[] array = BasicArrayCache.getArray(this.intArrayCache, size);
        if (array == null) {
            array = new int[size];
        } else if (fillWithZeros) {
            Arrays.fill(array, 0);
        }
        return array;
    }

    @Override
    public void putArray(int[] array) {
        BasicArrayCache.putArray(this.intArrayCache, array, array.length);
    }

    private static class CacheMap<T>
    extends LinkedHashMap<Integer, CyclicStack<Reference<T>>> {
        private static final long serialVersionUID = 1L;

        public CacheMap() {
            super(64, 0.75f, true);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, CyclicStack<Reference<T>>> eldest) {
            return this.size() > 32;
        }
    }

    private static final class LazyHolder {
        static final BasicArrayCache INSTANCE = new BasicArrayCache();

        private LazyHolder() {
        }
    }

    private static class CyclicStack<T> {
        private final T[] elements = new Object[512];
        private int pos = 0;

        private CyclicStack() {
        }

        public synchronized T pop() {
            T e = this.elements[this.pos];
            this.elements[this.pos] = null;
            this.pos = this.pos - 1 & 0x1FF;
            return e;
        }

        public synchronized void push(T e) {
            this.pos = this.pos + 1 & 0x1FF;
            this.elements[this.pos] = e;
        }
    }
}

