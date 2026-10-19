/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.InputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.BCJEncoder;
import org.tukaani.xz.BCJOptions;
import org.tukaani.xz.FilterEncoder;
import org.tukaani.xz.FinishableOutputStream;
import org.tukaani.xz.SimpleInputStream;
import org.tukaani.xz.SimpleOutputStream;
import org.tukaani.xz.simple.RISCVDecoder;
import org.tukaani.xz.simple.RISCVEncoder;

public final class RISCVOptions
extends BCJOptions {
    private static final int ALIGNMENT = 2;

    public RISCVOptions() {
        super(2);
    }

    @Override
    public FinishableOutputStream getOutputStream(FinishableOutputStream out, ArrayCache arrayCache) {
        return new SimpleOutputStream(out, new RISCVEncoder(this.startOffset));
    }

    @Override
    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new SimpleInputStream(in, new RISCVDecoder(this.startOffset));
    }

    @Override
    FilterEncoder getFilterEncoder() {
        return new BCJEncoder(this, 11L);
    }
}

