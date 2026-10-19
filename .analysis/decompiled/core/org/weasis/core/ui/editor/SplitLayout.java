/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  bibliothek.gui.dock.common.CLocation
 *  bibliothek.gui.dock.common.CWorkingArea
 */
package org.weasis.core.ui.editor;

import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.CWorkingArea;
import org.weasis.core.ui.editor.SplitPosition;

public record SplitLayout(SplitPosition position, double ratio) {
    public static final double DEFAULT_RATIO = 0.5;
    public static final SplitLayout NONE = new SplitLayout(SplitPosition.NONE, 0.5);

    public SplitLayout {
        if (ratio < 0.0 || ratio > 1.0) {
            throw new IllegalArgumentException("ratio must be between 0.0 and 1.0, got: " + ratio);
        }
        if (position == null) {
            position = SplitPosition.NONE;
        }
    }

    public static SplitLayout of(SplitPosition position) {
        return new SplitLayout(position, 0.5);
    }

    public static SplitLayout of(SplitPosition position, double ratio) {
        return new SplitLayout(position, ratio);
    }

    public static SplitLayout auto() {
        return new SplitLayout(SplitPosition.AUTO, 0.5);
    }

    public static SplitLayout auto(double ratio) {
        return new SplitLayout(SplitPosition.AUTO, ratio);
    }

    public boolean isSplit() {
        return this.position.isSplit();
    }

    public CLocation toLocation(CWorkingArea area) {
        return this.position.toLocation(area, this.ratio);
    }
}

