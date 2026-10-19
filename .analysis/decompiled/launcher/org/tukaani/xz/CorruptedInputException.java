/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import org.tukaani.xz.XZIOException;

public class CorruptedInputException
extends XZIOException {
    private static final long serialVersionUID = 3L;

    public CorruptedInputException() {
        super("Compressed data is corrupt");
    }

    public CorruptedInputException(String s) {
        super(s);
    }
}

