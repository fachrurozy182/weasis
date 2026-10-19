/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
 */
package org.weasis.core.ui.model.graphic;

import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import org.weasis.core.api.gui.util.GUIEntry;
import org.weasis.core.api.gui.util.KeyActionValue;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.util.Copyable;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.graphic.AbstractGraphic;
import org.weasis.core.ui.model.graphic.GraphicLabel;
import org.weasis.core.ui.model.layer.GraphicLayer;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.model.utils.UUIDable;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.util.MouseEventDouble;

@XmlJavaTypeAdapter(value=AbstractGraphic.Adapter.class)
public interface Graphic
extends UUIDable,
GUIEntry,
KeyActionValue,
Copyable<Graphic> {
    public static final Color DEFAULT_COLOR = Color.YELLOW;
    public static final Float DEFAULT_LINE_THICKNESS = Float.valueOf(1.0f);
    public static final Float DEFAULT_FILL_OPACITY = Float.valueOf(1.0f);
    public static final Boolean DEFAULT_LABEL_VISIBLE = Boolean.TRUE;
    public static final Boolean DEFAULT_FILLED = Boolean.FALSE;
    public static final Boolean DEFAULT_SELECTED = Boolean.FALSE;
    public static final Integer DEFAULT_PTS_SIZE = 10;
    public static final String ACTION_TO_FRONT = "toFront";
    public static final String ACTION_TO_BACK = "toBack";
    public static final String ACTION_REMOVE = "remove";
    public static final String ACTION_REMOVE_REPAINT = "remove.repaint";
    public static final Integer HANDLE_SIZE = 6;
    public static final Integer SELECTION_SIZE = 10;
    public static final Integer UNDEFINED = -1;

    default public List<MeasureItem> computeMeasurements(MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {
        return Collections.emptyList();
    }

    default public List<Measurement> getMeasurementList() {
        return Collections.emptyList();
    }

    public Rectangle getBounds(AffineTransform var1);

    public Rectangle getTransformedBounds(Shape var1, AffineTransform var2);

    public Float getLineThickness();

    public Float getFillOpacity();

    public Stroke getStroke(Float var1);

    public Paint getColorPaint();

    public Boolean getFilled();

    public Integer getHandleSize();

    public Graphic deepCopy();

    public void buildShape();

    public Area getArea(AffineTransform var1);

    public void setLabel(String[] var1, ViewCanvas<?> var2);

    public void setSelected(Boolean var1);

    public Boolean getSelected();

    public void paint(Graphics2D var1, AffineTransform var2);

    public void paintLabel(Graphics2D var1, AffineTransform var2);

    public void updateLabel(Object var1, ViewCanvas<?> var2);

    public Rectangle getRepaintBounds(AffineTransform var1);

    public GraphicLabel getGraphicLabel();

    public Boolean getLabelVisible();

    public ViewCanvas getDefaultView2d(MouseEvent var1);

    public Boolean intersects(Rectangle var1, AffineTransform var2);

    public void addPropertyChangeListener(PropertyChangeListener var1);

    public void removePropertyChangeListener(PropertyChangeListener var1);

    public void fireRemoveAction();

    public void toFront();

    public void toBack();

    public boolean isShapeValid();

    default public Boolean isLastPointValid() {
        List<Point2D> pts = this.getPts();
        int size = pts.size();
        Point2D lastPt = size > 0 ? pts.get(size - 1) : null;
        Point2D previousPt = size > 1 ? pts.get(size - 2) : null;
        return lastPt == null || !lastPt.equals(previousPt);
    }

    public Shape getShape();

    public Rectangle getTransformedBounds(GraphicLabel var1, AffineTransform var2);

    public void moveLabel(Double var1, Double var2);

    public Boolean isGraphicComplete();

    public List<Point2D> getPts();

    public Integer getPtsNumber();

    public boolean isOnGraphicLabel(MouseEventDouble var1);

    public int getHandlePointIndex(MouseEventDouble var1);

    public Rectangle getRepaintBounds(MouseEvent var1);

    public Area getArea(MouseEvent var1);

    public void fireRemoveAndRepaintAction();

    public void setLineThickness(Float var1);

    public void setFillOpacity(Float var1);

    public void setPaint(Color var1);

    public void setPointNumber(Integer var1);

    public void setPts(List<Point2D> var1);

    public void setLabelVisible(Boolean var1);

    public void setFilled(Boolean var1);

    public void setShape(Shape var1, MouseEvent var2);

    public Boolean getVariablePointsNumber();

    public void setVariablePointsNumber(Boolean var1);

    public LayerType getLayerType();

    public void removeAllPropertyChangeListener();

    public void setLayer(GraphicLayer var1);

    public GraphicLayer getLayer();

    public Graphic buildGraphic(List<Point2D> var1) throws InvalidShapeException;

    public void setLayerType(LayerType var1);

    public void setClassID(Integer var1);

    public Integer getClassID();

    public void setLabel(GraphicLabel var1);
}

