/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.InputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.DeltaCoder;
import org.tukaani.xz.DeltaInputStream;
import org.tukaani.xz.FilterDecoder;
import org.tukaani.xz.UnsupportedOptionsException;

class DeltaDecoder
extends DeltaCoder
implements FilterDecoder {
    private final int distance;

    DeltaDecoder(byte[] props) throws UnsupportedOptionsException {
        if (props.length != 1) {
            throw new UnsupportedOptionsException("Unsupported Delta filter properties");
        }
        this.distance = (props[0] & 0xFF) + 1;
    }

    @Override
    public int getMemoryUsage() {
        return 1;
    }

    @Override
    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new DeltaInputStream(in, this.distance);
    }
}

