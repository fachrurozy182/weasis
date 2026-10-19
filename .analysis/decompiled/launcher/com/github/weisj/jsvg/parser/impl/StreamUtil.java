/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import org.jetbrains.annotations.NotNull;

public final class StreamUtil {
    private StreamUtil() {
    }

    @NotNull
    public static InputStream createDocumentInputStream(@NotNull InputStream is) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(is);
        bin.mark(2);
        int b0 = bin.read();
        int b1 = bin.read();
        bin.reset();
        if ((b1 << 8 | b0) == 35615) {
            return new GZIPInputStream(bin);
        }
        return bin;
    }
}

