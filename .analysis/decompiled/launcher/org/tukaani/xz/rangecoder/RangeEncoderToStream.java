/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.rangecoder;

import java.io.IOException;
import java.io.OutputStream;
import org.tukaani.xz.rangecoder.RangeEncoder;

public final class RangeEncoderToStream
extends RangeEncoder {
    private final OutputStream out;

    public RangeEncoderToStream(OutputStream out) {
        this.out = out;
        this.reset();
    }

    @Override
    void writeByte(int b) throws IOException {
        this.out.write(b);
    }
}

