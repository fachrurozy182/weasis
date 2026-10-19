/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.api.image.util;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Optional;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.media.data.TagW;
import org.weasis.opencv.data.PlanarImage;

public interface MeasurableLayer {
    public boolean hasContent();

    default public Optional<Rectangle2D> getContentBounds() {
        return Optional.ofNullable(this.getSourceRenderedImage()).map(image -> new Rectangle2D.Double(0.0, 0.0, image.width(), image.height()));
    }

    public MeasurementsAdapter getMeasurementAdapter(Unit var1);

    default public MeasurementsAdapter getMeasurementAdapter() {
        return this.getMeasurementAdapter(Unit.PIXEL);
    }

    public AffineTransform getShapeTransform();

    public Object getSourceTagValue(TagW var1);

    public String getPixelValueUnit();

    public Point getOffset();

    public void setOffset(Point var1);

    public PlanarImage getSourceRenderedImage();

    public double pixelToRealValue(Number var1);

    public double getPixelMin();

    public double getPixelMax();

    default public List<MeasurableLayer> getSecondaryLayers() {
        return List.of();
    }

    default public String getStatLabel() {
        return null;
    }
}

