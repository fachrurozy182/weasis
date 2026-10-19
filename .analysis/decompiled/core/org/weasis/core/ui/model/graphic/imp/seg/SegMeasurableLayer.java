/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.ui.model.graphic.imp.seg;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.util.Objects;
import org.weasis.core.api.image.measure.MeasurementsAdapter;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.TagW;
import org.weasis.opencv.data.PlanarImage;

public class SegMeasurableLayer<E extends ImageElement>
implements MeasurableLayer {
    private final double thickness;
    private Point offset;
    private final E sourceImage;

    public SegMeasurableLayer(E sourceImage) {
        this(sourceImage, 1.0);
    }

    public SegMeasurableLayer(E sourceImage, double thickness) {
        this.sourceImage = (ImageElement)Objects.requireNonNull(sourceImage);
        this.thickness = thickness;
    }

    @Override
    public boolean hasContent() {
        return true;
    }

    @Override
    public MeasurementsAdapter getMeasurementAdapter(Unit displayUnit) {
        return ((ImageElement)this.sourceImage).getMeasurementAdapter(displayUnit, this.offset);
    }

    @Override
    public AffineTransform getShapeTransform() {
        return null;
    }

    @Override
    public Object getSourceTagValue(TagW tagW) {
        return ((MediaElement)this.sourceImage).getTagValue(tagW);
    }

    @Override
    public String getPixelValueUnit() {
        return ((ImageElement)this.sourceImage).getPixelValueUnit();
    }

    @Override
    public Point getOffset() {
        return this.offset;
    }

    @Override
    public void setOffset(Point offset) {
        this.offset = offset;
    }

    @Override
    public PlanarImage getSourceRenderedImage() {
        return ((ImageElement)this.sourceImage).getImage(null);
    }

    @Override
    public double pixelToRealValue(Number pixelValue) {
        Number val = ((ImageElement)this.sourceImage).pixelToRealValue(pixelValue, null);
        return val == null ? 0.0 : val.doubleValue();
    }

    @Override
    public double getPixelMin() {
        return ((ImageElement)this.sourceImage).getPixelMin();
    }

    @Override
    public double getPixelMax() {
        return ((ImageElement)this.sourceImage).getPixelMax();
    }

    public E getSourceImage() {
        return this.sourceImage;
    }

    public double getThickness() {
        return this.thickness;
    }
}

