/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

public class DicomEncodingOptions {
    public static final DicomEncodingOptions DEFAULT = new DicomEncodingOptions(false, true, false, true, false);
    public final boolean groupLength;
    public final boolean undefSequenceLength;
    public final boolean undefEmptySequenceLength;
    public final boolean undefItemLength;
    public final boolean undefEmptyItemLength;

    public DicomEncodingOptions(boolean groupLength, boolean undefSeqLength, boolean undefEmptySeqLength, boolean undefItemLength, boolean undefEmptyItemLength) {
        if (undefEmptySeqLength && !undefSeqLength) {
            throw new IllegalArgumentException("undefEmptySeqLength && !undefSeqLength");
        }
        if (undefEmptyItemLength && !undefItemLength) {
            throw new IllegalArgumentException("undefEmptyItemLength && !undefItemLength");
        }
        this.groupLength = groupLength;
        this.undefSequenceLength = undefSeqLength;
        this.undefEmptySequenceLength = undefEmptySeqLength;
        this.undefItemLength = undefItemLength;
        this.undefEmptyItemLength = undefEmptyItemLength;
    }
}

