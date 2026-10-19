/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import org.tukaani.xz.FilterCoder;

abstract class DeltaCoder
implements FilterCoder {
    public static final long FILTER_ID = 3L;

    DeltaCoder() {
    }

    @Override
    public boolean changesSize() {
        return false;
    }

    @Override
    public boolean nonLastOK() {
        return true;
    }

    @Override
    public boolean lastOK() {
        return false;
    }
}

