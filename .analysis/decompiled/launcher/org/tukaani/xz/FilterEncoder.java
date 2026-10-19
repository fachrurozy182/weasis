/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.FilterCoder;
import org.tukaani.xz.FinishableOutputStream;

interface FilterEncoder
extends FilterCoder {
    public long getFilterID();

    public byte[] getFilterProps();

    public boolean supportsFlushing();

    public FinishableOutputStream getOutputStream(FinishableOutputStream var1, ArrayCache var2);
}

