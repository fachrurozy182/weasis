/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.layer;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.editor.image.PixelInfo;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.layer.Layer;
import org.weasis.core.ui.model.layer.LayerItem;

public interface LayerAnnotation<E extends ImageElement>
extends Layer {
    public boolean getDisplayPreferences(LayerItem var1);

    public boolean setDisplayPreferencesValue(LayerItem var1, boolean var2);

    public Rectangle getPreloadingProgressBound();

    public Rectangle getPixelInfoBound();

    public void setPixelInfo(PixelInfo var1);

    public PixelInfo getPixelInfo();

    public int getBorder();

    public void setBorder(int var1);

    public void paint(Graphics2D var1);

    public LayerAnnotation<E> getLayerCopy(ViewCanvas<E> var1, boolean var2);

    public boolean isShowBottomScale();

    public void setShowBottomScale(Boolean var1);

    public void resetToDefault();

    public Point2D getPosition(Position var1);

    public void setPosition(Position var1, double var2, double var4);

    public static enum Position {
        TopLeft,
        TopRight,
        BottomLeft,
        BottomRight;

    }
}

