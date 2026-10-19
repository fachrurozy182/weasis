/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import org.tukaani.xz.BasicArrayCache;

public class ArrayCache {
    private static final ArrayCache dummyCache = new ArrayCache();
    private static volatile ArrayCache defaultCache;

    public static ArrayCache getDummyCache() {
        return dummyCache;
    }

    public static ArrayCache getDefaultCache() {
        return defaultCache;
    }

    public static void setDefaultCache(ArrayCache arrayCache) {
        if (arrayCache == null) {
            throw new NullPointerException();
        }
        defaultCache = arrayCache;
    }

    public byte[] getByteArray(int size, boolean fillWithZeros) {
        return new byte[size];
    }

    public void putArray(byte[] array) {
    }

    public int[] getIntArray(int size, boolean fillWithZeros) {
        return new int[size];
    }

    public void putArray(int[] array) {
    }

    static {
        String cacheType = System.getProperty("org.tukaani.xz.ArrayCache");
        if (cacheType == null) {
            cacheType = "Dummy";
        }
        switch (cacheType) {
            case "Dummy": {
                defaultCache = dummyCache;
                break;
            }
            case "Basic": {
                defaultCache = BasicArrayCache.getInstance();
                break;
            }
            default: {
                throw new Error("Unsupported value '" + cacheType + "' in the system property org.tukaani.xz.ArrayCache. Supported values: Dummy, Basic");
            }
        }
    }
}

