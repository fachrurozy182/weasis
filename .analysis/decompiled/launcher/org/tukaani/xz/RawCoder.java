/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import org.tukaani.xz.FilterCoder;
import org.tukaani.xz.UnsupportedOptionsException;

class RawCoder {
    RawCoder() {
    }

    static void validate(FilterCoder[] filters) throws UnsupportedOptionsException {
        for (int i = 0; i < filters.length - 1; ++i) {
            if (filters[i].nonLastOK()) continue;
            throw new UnsupportedOptionsException("Unsupported XZ filter chain");
        }
        if (!filters[filters.length - 1].lastOK()) {
            throw new UnsupportedOptionsException("Unsupported XZ filter chain");
        }
        int changesSizeCount = 0;
        for (int i = 0; i < filters.length; ++i) {
            if (!filters[i].changesSize()) continue;
            ++changesSizeCount;
        }
        if (changesSizeCount > 3) {
            throw new UnsupportedOptionsException("Unsupported XZ filter chain");
        }
    }
}

