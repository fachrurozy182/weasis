/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import org.tukaani.xz.FilterOptions;
import org.tukaani.xz.SimpleInputStream;
import org.tukaani.xz.SimpleOutputStream;
import org.tukaani.xz.UnsupportedOptionsException;

abstract class BCJOptions
extends FilterOptions {
    private final int alignment;
    int startOffset = 0;

    BCJOptions(int alignment) {
        this.alignment = alignment;
    }

    public void setStartOffset(int startOffset) throws UnsupportedOptionsException {
        if ((startOffset & this.alignment - 1) != 0) {
            throw new UnsupportedOptionsException("Start offset must be a multiple of " + this.alignment);
        }
        this.startOffset = startOffset;
    }

    public int getStartOffset() {
        return this.startOffset;
    }

    @Override
    public int getEncoderMemoryUsage() {
        return SimpleOutputStream.getMemoryUsage();
    }

    @Override
    public int getDecoderMemoryUsage() {
        return SimpleInputStream.getMemoryUsage();
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            assert (false);
            throw new RuntimeException();
        }
    }
}

