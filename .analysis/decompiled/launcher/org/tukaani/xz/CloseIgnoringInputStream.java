/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.FilterInputStream;
import java.io.InputStream;

public class CloseIgnoringInputStream
extends FilterInputStream {
    public CloseIgnoringInputStream(InputStream in) {
        super(in);
    }

    @Override
    public void close() {
    }
}

