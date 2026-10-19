/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.api.gui;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import org.weasis.core.api.gui.model.ViewModel;
import org.weasis.core.api.image.util.ImageLayer;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.opencv.data.PlanarImage;

public interface Image2DViewer<E extends ImageElement> {
    public MediaSeries<E> getSeries();

    public int getFrameIndex();

    public void drawLayers(Graphics2D var1, AffineTransform var2, AffineTransform var3);

    public ViewModel getViewModel();

    public ImageLayer<E> getImageLayer();

    public MeasurableLayer getMeasurableLayer();

    public AffineTransform getAffineTransform();

    public E getImage();

    public PlanarImage getSourceImage();

    public Object getActionValue(String var1);
}

