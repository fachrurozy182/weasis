/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.InputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.DeltaEncoder;
import org.tukaani.xz.DeltaInputStream;
import org.tukaani.xz.DeltaOutputStream;
import org.tukaani.xz.FilterEncoder;
import org.tukaani.xz.FilterOptions;
import org.tukaani.xz.FinishableOutputStream;
import org.tukaani.xz.UnsupportedOptionsException;

public final class DeltaOptions
extends FilterOptions {
    public static final int DISTANCE_MIN = 1;
    public static final int DISTANCE_MAX = 256;
    private int distance = 1;

    public DeltaOptions() {
    }

    public DeltaOptions(int distance) throws UnsupportedOptionsException {
        this.setDistance(distance);
    }

    public void setDistance(int distance) throws UnsupportedOptionsException {
        if (distance < 1 || distance > 256) {
            throw new UnsupportedOptionsException("Delta distance must be in the range [1, 256]: " + distance);
        }
        this.distance = distance;
    }

    public int getDistance() {
        return this.distance;
    }

    @Override
    public int getEncoderMemoryUsage() {
        return DeltaOutputStream.getMemoryUsage();
    }

    @Override
    public FinishableOutputStream getOutputStream(FinishableOutputStream out, ArrayCache arrayCache) {
        return new DeltaOutputStream(out, this);
    }

    @Override
    public int getDecoderMemoryUsage() {
        return 1;
    }

    @Override
    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new DeltaInputStream(in, this.distance);
    }

    @Override
    FilterEncoder getFilterEncoder() {
        return new DeltaEncoder(this);
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            assert (false);
            throw new RuntimeException();
        }
    }
}

