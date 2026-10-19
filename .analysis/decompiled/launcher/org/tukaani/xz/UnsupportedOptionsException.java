/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import org.tukaani.xz.XZIOException;

public class UnsupportedOptionsException
extends XZIOException {
    private static final long serialVersionUID = 3L;

    public UnsupportedOptionsException() {
    }

    public UnsupportedOptionsException(String s) {
        super(s);
    }
}

