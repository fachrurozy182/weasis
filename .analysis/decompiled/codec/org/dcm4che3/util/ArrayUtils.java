/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

public class ArrayUtils {
    public static int[] longsToInts(long[] in) {
        int[] out = new int[in.length];
        for (int i = 0; i < in.length; ++i) {
            out[i] = (int)in[i];
        }
        return out;
    }

    public static long[] intsToLong(int[] in) {
        long[] out = new long[in.length];
        for (int i = 0; i < in.length; ++i) {
            out[i] = in[i];
        }
        return out;
    }
}

