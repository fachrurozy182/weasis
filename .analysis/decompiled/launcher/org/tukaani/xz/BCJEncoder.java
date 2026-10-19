/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.BCJCoder;
import org.tukaani.xz.BCJOptions;
import org.tukaani.xz.FilterEncoder;
import org.tukaani.xz.FinishableOutputStream;

class BCJEncoder
extends BCJCoder
implements FilterEncoder {
    private final BCJOptions options;
    private final long filterID;
    private final byte[] props;

    BCJEncoder(BCJOptions options, long filterID) {
        assert (BCJEncoder.isBCJFilterID(filterID));
        int startOffset = options.getStartOffset();
        if (startOffset == 0) {
            this.props = new byte[0];
        } else {
            this.props = new byte[4];
            for (int i = 0; i < 4; ++i) {
                this.props[i] = (byte)(startOffset >>> i * 8);
            }
        }
        this.filterID = filterID;
        this.options = (BCJOptions)options.clone();
    }

    @Override
    public long getFilterID() {
        return this.filterID;
    }

    @Override
    public byte[] getFilterProps() {
        return this.props;
    }

    @Override
    public boolean supportsFlushing() {
        return false;
    }

    @Override
    public FinishableOutputStream getOutputStream(FinishableOutputStream out, ArrayCache arrayCache) {
        return this.options.getOutputStream(out, arrayCache);
    }
}

