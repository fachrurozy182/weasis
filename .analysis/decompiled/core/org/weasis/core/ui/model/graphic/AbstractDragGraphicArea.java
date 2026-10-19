/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.graphic;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.List;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.ui.editor.image.ImageRegionStatistics;
import org.weasis.core.ui.model.graphic.AbstractDragGraphic;
import org.weasis.core.ui.model.graphic.GraphicArea;
import org.weasis.core.ui.model.utils.bean.MeasureItem;

public abstract class AbstractDragGraphicArea
extends AbstractDragGraphic
implements GraphicArea {
    protected AbstractDragGraphicArea(Integer pointNumber) {
        super(pointNumber);
    }

    protected AbstractDragGraphicArea(AbstractDragGraphicArea graphic) {
        super(graphic);
    }

    @Override
    public Area getArea(AffineTransform transform) {
        if (this.shape == null) {
            return new Area();
        }
        Area area = super.getArea(transform);
        area.add(new Area(this.shape));
        return area;
    }

    @Override
    public List<MeasureItem> getImageStatistics(MeasurableLayer layer, Boolean releaseEvent) {
        return ImageRegionStatistics.getImageStatistics(this, layer, releaseEvent);
    }
}

