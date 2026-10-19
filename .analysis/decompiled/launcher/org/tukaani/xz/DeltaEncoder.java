/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.DeltaCoder;
import org.tukaani.xz.DeltaOptions;
import org.tukaani.xz.FilterEncoder;
import org.tukaani.xz.FinishableOutputStream;

class DeltaEncoder
extends DeltaCoder
implements FilterEncoder {
    private final DeltaOptions options;
    private final byte[] props = new byte[1];

    DeltaEncoder(DeltaOptions options) {
        this.props[0] = (byte)(options.getDistance() - 1);
        this.options = (DeltaOptions)options.clone();
    }

    @Override
    public long getFilterID() {
        return 3L;
    }

    @Override
    public byte[] getFilterProps() {
        return this.props;
    }

    @Override
    public boolean supportsFlushing() {
        return true;
    }

    @Override
    public FinishableOutputStream getOutputStream(FinishableOutputStream out, ArrayCache arrayCache) {
        return this.options.getOutputStream(out, arrayCache);
    }
}

