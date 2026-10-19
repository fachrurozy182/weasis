/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

public class DatePrecision {
    public int lastField;
    public boolean includeTimezone;

    public DatePrecision() {
        this(14, false);
    }

    public DatePrecision(int lastField) {
        this(lastField, false);
    }

    public DatePrecision(int lastField, boolean includeTimezone) {
        this.lastField = lastField;
        this.includeTimezone = includeTimezone;
    }
}

