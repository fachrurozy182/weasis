/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.index;

class IndexRecord {
    final long unpadded;
    final long uncompressed;

    IndexRecord(long unpadded, long uncompressed) {
        this.unpadded = unpadded;
        this.uncompressed = uncompressed;
    }
}

