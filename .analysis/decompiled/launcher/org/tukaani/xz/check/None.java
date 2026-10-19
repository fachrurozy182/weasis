/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.check;

import org.tukaani.xz.check.Check;

public class None
extends Check {
    public None() {
        this.size = 0;
        this.name = "None";
    }

    @Override
    public void update(byte[] buf, int off, int len) {
    }

    @Override
    public byte[] finish() {
        byte[] empty = new byte[]{};
        return empty;
    }
}

