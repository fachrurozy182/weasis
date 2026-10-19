/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
 */
package org.weasis.core.ui.model.graphic;

import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import org.weasis.core.api.util.Copyable;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.graphic.AbstractGraphicLabel;

@XmlJavaTypeAdapter(value=AbstractGraphicLabel.Adapter.class)
public interface GraphicLabel
extends Copyable<GraphicLabel> {
    public static final int GROWING_BOUND = 3;
    public static final Double DEFAULT_OFFSET_X = 0.0;
    public static final Double DEFAULT_OFFSET_Y = 0.0;

    public void reset();

    public String[] getLabels();

    public Rectangle2D getLabelBounds();

    public Rectangle2D getBounds(AffineTransform var1);

    public Rectangle2D getTransformedBounds(AffineTransform var1);

    public void setLabel(ViewCanvas<?> var1, Double var2, Double var3, String ... var4);

    public void paint(Graphics2D var1, AffineTransform var2, boolean var3);

    public Area getArea(AffineTransform var1);

    public void move(Double var1, Double var2);

    public Double getOffsetX();

    public Double getOffsetY();
}

