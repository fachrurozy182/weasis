/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.utils.bean;

import java.util.Objects;
import org.weasis.core.ui.model.utils.bean.Measurement;

public class MeasureItem {
    private final Measurement measurement;
    private final Object value;
    private final String unit;
    private final String labelExtension;

    public MeasureItem(Measurement measurement, Object value, String unit) {
        this(measurement, null, value, unit);
    }

    public MeasureItem(Measurement measurement, String labelExtension, Object value, String unit) {
        this.measurement = Objects.requireNonNull(measurement, "Measurement cannot be null!");
        this.value = value;
        this.unit = unit;
        this.labelExtension = labelExtension;
    }

    public Measurement getMeasurement() {
        return this.measurement;
    }

    public Object getValue() {
        return this.value;
    }

    public String getUnit() {
        return this.unit;
    }

    public String getLabelExtension() {
        return this.labelExtension;
    }
}

