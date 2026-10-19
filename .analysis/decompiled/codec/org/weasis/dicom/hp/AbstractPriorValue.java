/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.annotations.Generated
 */
package org.weasis.dicom.hp;

import org.weasis.core.util.annotations.Generated;

public class AbstractPriorValue {
    private final int start;
    private final int end;

    public AbstractPriorValue(int start, int end) {
        this.start = start;
        this.end = end;
    }

    AbstractPriorValue(int[] value) {
        this(value[0], value[1]);
    }

    @Generated
    final int[] getValues() {
        return new int[]{this.start, this.end};
    }

    @Generated
    public final int getStart() {
        return this.start;
    }

    @Generated
    public final int getEnd() {
        return this.end;
    }
}

