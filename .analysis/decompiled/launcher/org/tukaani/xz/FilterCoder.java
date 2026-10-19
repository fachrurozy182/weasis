/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

interface FilterCoder {
    public boolean changesSize();

    public boolean nonLastOK();

    public boolean lastOK();
}

