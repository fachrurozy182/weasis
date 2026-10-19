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
import org.tukaani.xz.simple.SPARC;

public final class SPARCOptions
extends BCJOptions {
    private static final int ALIGNMENT = 4;

    public SPARCOptions() {
        super(4);
    }

    @Override
    public FinishableOutputStream getOutputStream(FinishableOutputStream out, ArrayCache arrayCache) {
        return new SimpleOutputStream(out, new SPARC(true, this.startOffset));
    }

    @Override
    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new SimpleInputStream(in, new SPARC(false, this.startOffset));
    }

    @Override
    FilterEncoder getFilterEncoder() {
        return new BCJEncoder(this, 9L);
    }
}

