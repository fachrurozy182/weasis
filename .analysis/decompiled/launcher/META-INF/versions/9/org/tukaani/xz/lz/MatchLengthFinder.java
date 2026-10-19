/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lz;

interface MatchLengthFinder {
    public int getExtraSize();

    public int getLen(byte[] var1, int var2, int var3, int var4, int var5);
}

