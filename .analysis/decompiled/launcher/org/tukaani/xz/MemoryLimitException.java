/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz;

import org.tukaani.xz.XZIOException;

public class MemoryLimitException
extends XZIOException {
    private static final long serialVersionUID = 3L;
    private final int memoryNeeded;
    private final int memoryLimit;

    public MemoryLimitException(int memoryNeeded, int memoryLimit) {
        super("" + memoryNeeded + " KiB of memory would be needed; limit was " + memoryLimit + " KiB");
        this.memoryNeeded = memoryNeeded;
        this.memoryLimit = memoryLimit;
    }

    public int getMemoryNeeded() {
        return this.memoryNeeded;
    }

    public int getMemoryLimit() {
        return this.memoryLimit;
    }
}

