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

public enum SplitPosition {
    NONE,
    AUTO,
    LEFT,
    RIGHT,
    TOP,
    BOTTOM;


    public boolean isSplit() {
        return this != NONE;
    }

    public CLocation toLocation(CWorkingArea area, double ratio) {
        return switch (this.ordinal()) {
            case 2 -> CLocation.working((CWorkingArea)area).west(ratio);
            case 3 -> CLocation.working((CWorkingArea)area).east(ratio);
            case 4 -> CLocation.working((CWorkingArea)area).north(ratio);
            case 5 -> CLocation.working((CWorkingArea)area).south(ratio);
            default -> null;
        };
    }
}

