/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import org.tukaani.xz.XZIOException;

public class XZFormatException
extends XZIOException {
    private static final long serialVersionUID = 3L;

    public XZFormatException() {
        super("Input is not in the XZ format");
    }
}

