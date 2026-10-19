/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.utils.bean;

import java.util.Objects;

public class Measurement {
    private final String name;
    private final int id;
    private final boolean quickComputing;
    private boolean computed;
    private boolean graphicLabel;
    private final boolean defaultGraphicLabel;

    public Measurement(String name, Integer id, Boolean quickComputing) {
        this(name, id, quickComputing, Boolean.TRUE, Boolean.TRUE);
    }

    public Measurement(String name, Integer id, Boolean quickComputing, Boolean computed, Boolean graphicLabel) {
        this.name = Objects.requireNonNull(name, "Name cannot be null!");
        this.id = id;
        this.quickComputing = quickComputing;
        this.computed = computed;
        this.graphicLabel = graphicLabel;
        this.defaultGraphicLabel = graphicLabel;
    }

    public String getName() {
        return this.name;
    }

    public Integer getId() {
        return this.id;
    }

    public Boolean getComputed() {
        return this.computed;
    }

    public void setComputed(Boolean computed) {
        this.computed = computed;
    }

    public Boolean getGraphicLabel() {
        return this.graphicLabel;
    }

    public void setGraphicLabel(Boolean graphicLabel) {
        this.graphicLabel = graphicLabel;
    }

    public void resetToGraphicLabelValue() {
        this.graphicLabel = this.defaultGraphicLabel;
    }

    public Boolean getQuickComputing() {
        return this.quickComputing;
    }
}

