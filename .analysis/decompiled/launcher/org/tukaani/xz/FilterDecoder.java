/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import java.io.InputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.FilterCoder;

interface FilterDecoder
extends FilterCoder {
    public int getMemoryUsage();

    public InputStream getInputStream(InputStream var1, ArrayCache var2);
}

