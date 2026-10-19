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
import org.tukaani.xz.simple.ARM;

public final class ARMOptions
extends BCJOptions {
    private static final int ALIGNMENT = 4;

    public ARMOptions() {
        super(4);
    }

    @Override
    public FinishableOutputStream getOutputStream(FinishableOutputStream out, ArrayCache arrayCache) {
        return new SimpleOutputStream(out, new ARM(true, this.startOffset));
    }

    @Override
    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new SimpleInputStream(in, new ARM(false, this.startOffset));
    }

    @Override
    FilterEncoder getFilterEncoder() {
        return new BCJEncoder(this, 7L);
    }
}

