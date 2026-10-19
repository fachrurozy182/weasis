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
import org.tukaani.xz.simple.X86;

public final class X86Options
extends BCJOptions {
    private static final int ALIGNMENT = 1;

    public X86Options() {
        super(1);
    }

    @Override
    public FinishableOutputStream getOutputStream(FinishableOutputStream out, ArrayCache arrayCache) {
        return new SimpleOutputStream(out, new X86(true, this.startOffset));
    }

    @Override
    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new SimpleInputStream(in, new X86(false, this.startOffset));
    }

    @Override
    FilterEncoder getFilterEncoder() {
        return new BCJEncoder(this, 4L);
    }
}

