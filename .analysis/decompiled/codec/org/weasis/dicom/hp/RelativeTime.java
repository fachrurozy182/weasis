/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.annotations.Generated
 */
package org.weasis.dicom.hp;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import org.weasis.core.util.annotations.Generated;
import org.weasis.dicom.hp.enums.RelativeTimeUnits;

public class RelativeTime {
    private final int[] values;
    private final RelativeTimeUnits units;

    public RelativeTime() {
        this(0, 0, RelativeTimeUnits.SECONDS);
    }

    public RelativeTime(int start, int end, RelativeTimeUnits units) {
        this(new int[]{start, end}, units);
    }

    RelativeTime(int[] value, RelativeTimeUnits units) {
        this.values = Objects.requireNonNullElse(value, new int[2]);
        if (this.values.length != 2) {
            throw new IllegalArgumentException("values must have a length of 2");
        }
        this.units = units;
    }

    @Generated
    final int[] getValues() {
        return this.values;
    }

    @Generated
    public final int getStart() {
        return this.values[0];
    }

    @Generated
    public final int getEnd() {
        return this.values[1];
    }

    public LocalDateTime getStartDate() {
        return this.toDate(this.values[0]);
    }

    public LocalDateTime getEndDate() {
        return this.toDate(this.values[1]);
    }

    private LocalDateTime toDate(int value) {
        return LocalDateTime.now(ZoneId.of("CET")).minus(value, this.units.getChronoUnit());
    }

    @Generated
    public final boolean isCurrentTime() {
        return this.values[0] == 0 && this.values[1] == 0;
    }

    @Generated
    public final RelativeTimeUnits getUnits() {
        return this.units;
    }
}

