/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import org.weasis.core.ui.model.graphic.imp.seg.SegRegion;

public interface SegRegionTool {
    public void computeStatistics(SegRegion<?> var1);

    public void updateVisibleNode();

    public void show(SegRegion<?> var1);
}

