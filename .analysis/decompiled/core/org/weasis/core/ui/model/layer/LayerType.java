/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.layer;

import org.weasis.core.Messages;

public enum LayerType {
    IMAGE(10, Messages.getString("LayerType.img"), Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE),
    CROSSLINES(20, Messages.getString("LayerType.crosslines"), Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE),
    IMAGE_ANNOTATION(10000, Messages.getString("LayerType.img_anont"), Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE),
    ANNOTATION(31, Messages.getString("LayerType.annot"), Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE),
    DRAW(40, Messages.getString("LayerType.drawings"), Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE),
    MEASURE(50, Messages.getString("LayerType.measurements"), Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE),
    TEMP_DRAW(60, "Drawings [Temp]", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE),
    ACQUIRE(70, "Dicomizer [Temp]", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE),
    BLOB(80, Messages.getString("LayerType.obj"), Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE),
    POINTS(90, Messages.getString("LayerType.pts"), Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE),
    DICOM_SEG(95, "DICOM SEG", Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE),
    DICOM_SR(100, "DICOM SR", Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE),
    DICOM_RT(110, "DICOM RT", Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE),
    DICOM_PR(120, "DICOM PR", Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE);

    private final Integer level;
    private final Boolean visible;
    private final Boolean locked;
    private final Boolean serializable;
    private final Boolean selectable;
    private final String defaultName;

    private LayerType(Integer level, String defaultName, Boolean visible, Boolean locked, Boolean serializable, Boolean selectable) {
        this.level = level;
        this.visible = visible;
        this.locked = locked;
        this.serializable = serializable;
        this.defaultName = defaultName;
        this.selectable = selectable;
    }

    public Integer getLevel() {
        return this.level;
    }

    public String toString() {
        return this.defaultName;
    }

    public String getDefaultName() {
        return this.defaultName;
    }

    public Boolean getVisible() {
        return this.visible;
    }

    public Boolean getLocked() {
        return this.locked;
    }

    public Boolean getSerializable() {
        return this.serializable;
    }

    public Boolean getSelectable() {
        return this.selectable;
    }
}

