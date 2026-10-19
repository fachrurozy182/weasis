/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.graphic;

import java.util.List;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.utils.ImageStatistics;
import org.weasis.core.ui.model.utils.bean.MeasureItem;

public interface GraphicArea
extends Graphic,
ImageStatistics {
    public List<MeasureItem> getImageStatistics(MeasurableLayer var1, Boolean var2);
}

