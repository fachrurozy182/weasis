/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.delta;

abstract class DeltaCoder {
    static final int DISTANCE_MIN = 1;
    static final int DISTANCE_MAX = 256;
    final int distance;
    final byte[] history;

    DeltaCoder(int distance) {
        if (distance < 1 || distance > 256) {
            throw new IllegalArgumentException("Invalid distance: " + distance);
        }
        this.distance = distance;
        this.history = new byte[distance];
    }
}

