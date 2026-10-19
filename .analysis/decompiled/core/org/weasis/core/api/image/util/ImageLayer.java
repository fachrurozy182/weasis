/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.api.image.util;

import java.awt.geom.AffineTransform;
import org.weasis.core.api.image.OpManager;
import org.weasis.core.api.image.SimpleOpManager;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.opencv.data.PlanarImage;

public interface ImageLayer<E extends ImageElement>
extends MeasurableLayer {
    public E getSourceImage();

    public PlanarImage getDisplayImage();

    public void setImage(E var1, OpManager var2);

    public AffineTransform getTransform();

    public void setTransform(AffineTransform var1);

    public SimpleOpManager getDisplayOpManager();

    public void updateDisplayOperations();

    public boolean isEnableDispOperations();

    public void setEnableDispOperations(boolean var1);

    public void setVisible(Boolean var1);

    public Boolean getVisible();
}

