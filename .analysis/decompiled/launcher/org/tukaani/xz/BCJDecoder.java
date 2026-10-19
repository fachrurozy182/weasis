/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.InputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.BCJCoder;
import org.tukaani.xz.FilterDecoder;
import org.tukaani.xz.SimpleInputStream;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.simple.ARM;
import org.tukaani.xz.simple.ARM64;
import org.tukaani.xz.simple.ARMThumb;
import org.tukaani.xz.simple.IA64;
import org.tukaani.xz.simple.PowerPC;
import org.tukaani.xz.simple.RISCVDecoder;
import org.tukaani.xz.simple.SPARC;
import org.tukaani.xz.simple.SimpleFilter;
import org.tukaani.xz.simple.X86;

class BCJDecoder
extends BCJCoder
implements FilterDecoder {
    private final long filterID;
    private final int startOffset;

    BCJDecoder(long filterID, byte[] props) throws UnsupportedOptionsException {
        assert (BCJDecoder.isBCJFilterID(filterID));
        this.filterID = filterID;
        if (props.length == 0) {
            this.startOffset = 0;
        } else if (props.length == 4) {
            int n = 0;
            for (int i = 0; i < 4; ++i) {
                n |= (props[i] & 0xFF) << i * 8;
            }
            this.startOffset = n;
        } else {
            throw new UnsupportedOptionsException("Unsupported BCJ filter properties");
        }
    }

    @Override
    public int getMemoryUsage() {
        return SimpleInputStream.getMemoryUsage();
    }

    @Override
    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        SimpleFilter simpleFilter = null;
        if (this.filterID == 4L) {
            simpleFilter = new X86(false, this.startOffset);
        } else if (this.filterID == 5L) {
            simpleFilter = new PowerPC(false, this.startOffset);
        } else if (this.filterID == 6L) {
            simpleFilter = new IA64(false, this.startOffset);
        } else if (this.filterID == 7L) {
            simpleFilter = new ARM(false, this.startOffset);
        } else if (this.filterID == 8L) {
            simpleFilter = new ARMThumb(false, this.startOffset);
        } else if (this.filterID == 9L) {
            simpleFilter = new SPARC(false, this.startOffset);
        } else if (this.filterID == 10L) {
            simpleFilter = new ARM64(false, this.startOffset);
        } else if (this.filterID == 11L) {
            simpleFilter = new RISCVDecoder(this.startOffset);
        } else assert (false);
        return new SimpleInputStream(in, simpleFilter);
    }
}

