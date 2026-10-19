/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import org.tukaani.xz.FilterCoder;

abstract class LZMA2Coder
implements FilterCoder {
    public static final long FILTER_ID = 33L;

    LZMA2Coder() {
    }

    @Override
    public boolean changesSize() {
        return true;
    }

    @Override
    public boolean nonLastOK() {
        return false;
    }

    @Override
    public boolean lastOK() {
        return true;
    }
}

