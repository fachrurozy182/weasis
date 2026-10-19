/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.hp.enums;

import java.time.temporal.ChronoUnit;

public enum RelativeTimeUnits {
    SECONDS("SECONDS", ChronoUnit.SECONDS),
    MINUTES("MINUTES", ChronoUnit.MINUTES),
    HOURS("HOURS", ChronoUnit.HOURS),
    DAYS("DAYS", ChronoUnit.DAYS),
    WEEKS("WEEKS", ChronoUnit.WEEKS),
    MONTHS("MONTHS", ChronoUnit.MONTHS),
    YEARS("YEARS", ChronoUnit.YEARS);

    private final ChronoUnit chronoUnit;
    private final String codeString;

    private RelativeTimeUnits(String codeString, ChronoUnit chronoUnit) {
        this.codeString = codeString;
        this.chronoUnit = chronoUnit;
    }

    public ChronoUnit getChronoUnit() {
        return this.chronoUnit;
    }

    public String getCodeString() {
        return this.codeString;
    }

    public String toString() {
        return this.codeString;
    }
}

