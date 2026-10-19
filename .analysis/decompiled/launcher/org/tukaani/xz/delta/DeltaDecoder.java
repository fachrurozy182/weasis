/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.delta;

import org.tukaani.xz.delta.DeltaCoder;

public class DeltaDecoder
extends DeltaCoder {
    public DeltaDecoder(int distance) {
        super(distance);
    }

    public void decode(byte[] buf, int off, int len) {
        int i;
        int j = Math.min(len, this.distance);
        for (i = 0; i < j; ++i) {
            int n = off + i;
            buf[n] = (byte)(buf[n] + this.history[i]);
        }
        while (i < len) {
            int n = off + i;
            buf[n] = (byte)(buf[n] + buf[off + i - this.distance]);
            ++i;
        }
        if (len >= this.distance) {
            System.arraycopy(buf, off + len - this.distance, this.history, 0, this.distance);
        } else {
            assert (i == len);
            System.arraycopy(this.history, i, this.history, 0, this.distance - i);
            System.arraycopy(buf, off, this.history, this.distance - i, len);
        }
    }
}

