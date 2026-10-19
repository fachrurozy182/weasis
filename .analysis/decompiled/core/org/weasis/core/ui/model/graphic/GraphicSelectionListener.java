/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.graphic;

import java.util.List;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.utils.bean.MeasureItem;

public interface GraphicSelectionListener {
    public void handle(List<Graphic> var1, MeasurableLayer var2);

    public void updateMeasuredItems(List<MeasureItem> var1);
}

