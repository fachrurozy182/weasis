/*
 * Decompiled with CFR 0.152.
 */
package org.tukaani.xz.index;

import org.tukaani.xz.index.IndexDecoder;

public class BlockInfo {
    public int blockNumber = -1;
    public long compressedOffset = -1L;
    public long uncompressedOffset = -1L;
    public long unpaddedSize = -1L;
    public long uncompressedSize = -1L;
    IndexDecoder index;

    public BlockInfo(IndexDecoder indexOfFirstStream) {
        this.index = indexOfFirstStream;
    }

    public int getCheckType() {
        return this.index.getStreamFlags().checkType;
    }

    public boolean hasNext() {
        return this.index.hasRecord(this.blockNumber + 1);
    }

    public void setNext() {
        this.index.setBlockInfo(this, this.blockNumber + 1);
    }
}

