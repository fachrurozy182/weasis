/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.lz;

public final class Matches {
    public final int[] len;
    public final int[] dist;
    public int count = 0;

    Matches(int countMax) {
        this.len = new int[countMax];
        this.dist = new int[countMax];
    }
}

