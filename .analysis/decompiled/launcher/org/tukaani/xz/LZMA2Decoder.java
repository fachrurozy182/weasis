/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.InputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.FilterDecoder;
import org.tukaani.xz.LZMA2Coder;
import org.tukaani.xz.LZMA2InputStream;
import org.tukaani.xz.UnsupportedOptionsException;

class LZMA2Decoder
extends LZMA2Coder
implements FilterDecoder {
    private int dictSize;

    LZMA2Decoder(byte[] props) throws UnsupportedOptionsException {
        if (props.length != 1 || (props[0] & 0xFF) > 37) {
            throw new UnsupportedOptionsException("Unsupported LZMA2 properties");
        }
        this.dictSize = 2 | props[0] & 1;
        this.dictSize <<= (props[0] >>> 1) + 11;
    }

    @Override
    public int getMemoryUsage() {
        return LZMA2InputStream.getMemoryUsage(this.dictSize);
    }

    @Override
    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new LZMA2InputStream(in, this.dictSize, null, arrayCache);
    }
}

