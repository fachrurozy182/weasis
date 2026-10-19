/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlAccessType
 *  jakarta.xml.bind.annotation.XmlAccessorType
 *  jakarta.xml.bind.annotation.XmlAttribute
 *  jakarta.xml.bind.annotation.XmlElement
 *  jakarta.xml.bind.annotation.XmlElementWrapper
 *  jakarta.xml.bind.annotation.XmlIDREF
 *  jakarta.xml.bind.annotation.adapters.XmlAdapter
 *  jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.ui.model.graphic;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.BasicStroke;
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
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.Image2DViewer;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.DecFormatter;
import org.weasis.core.api.gui.util.GeomUtil;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.util.Copyable;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.GraphicArea;
import org.weasis.core.ui.model.graphic.GraphicLabel;
import org.weasis.core.ui.model.graphic.GraphicSelectionListener;
import org.weasis.core.ui.model.layer.GraphicLayer;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.model.utils.bean.AdvancedShape;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.model.utils.exceptions.InvalidShapeException;
import org.weasis.core.ui.model.utils.imp.DefaultGraphicLabel;
import org.weasis.core.ui.model.utils.imp.DefaultUUID;
import org.weasis.core.ui.serialize.ColorModelAdapter;
import org.weasis.core.ui.serialize.PointAdapter;
import org.weasis.core.ui.util.MouseEventDouble;
import org.weasis.core.util.MathUtil;

@XmlAccessorType(value=XmlAccessType.NONE)
public abstract class AbstractGraphic
extends DefaultUUID
implements Graphic {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGraphic.class);
    protected static final String NULL_MSG = "Null is not allowed";
    protected Integer pointNumber;
    protected List<Point2D> pts;
    protected Paint colorPaint = DEFAULT_COLOR;
    protected Float lineThickness = DEFAULT_LINE_THICKNESS;
    protected Float fillOpacity = DEFAULT_FILL_OPACITY;
    protected Boolean labelVisible = DEFAULT_LABEL_VISIBLE;
    protected Boolean filled = DEFAULT_FILLED;
    protected Integer classID;
    protected GraphicLabel graphicLabel;
    protected LayerType layerType = LayerType.DRAW;
    protected Shape shape;
    protected Boolean selected = DEFAULT_SELECTED;
    protected Boolean variablePointsNumber;
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private GraphicLayer layer;

    protected AbstractGraphic(Integer pointNumber) {
        this.setPointNumber(pointNumber);
        this.variablePointsNumber = Objects.isNull(pointNumber) || pointNumber < 0;
        this.setPts(null);
    }

    protected AbstractGraphic(AbstractGraphic graphic) {
        this.layerType = graphic.layerType;
        this.setPointNumber(graphic.pointNumber);
        this.setColorPaint(graphic.colorPaint);
        this.setLineThickness(graphic.lineThickness);
        this.setFillOpacity(graphic.fillOpacity);
        this.setLabelVisible(graphic.labelVisible);
        this.setFilled(graphic.filled);
        this.setClassID(graphic.classID);
        this.graphicLabel = graphic.graphicLabel == null ? null : (GraphicLabel)graphic.graphicLabel.copy();
        this.variablePointsNumber = Objects.isNull(graphic.pointNumber) || graphic.pointNumber < 0;
        List<Point2D> ptsList = graphic.pts.stream().filter(Objects::nonNull).map(g -> (Point2D)g.clone()).collect(Collectors.toList());
        try {
            this.initCopy(graphic);
            this.buildGraphic(ptsList);
        }
        catch (InvalidShapeException e) {
            LOGGER.error("Building graphic", (Throwable)e);
        }
    }

    @Override
    public String toString() {
        return this.getUIName();
    }

    @Override
    public Integer getPtsNumber() {
        return this.pointNumber;
    }

    @Override
    public void setPointNumber(Integer pointNumber) {
        Objects.requireNonNull(pointNumber, NULL_MSG);
        this.pointNumber = pointNumber;
    }

    @Override
    @XmlElementWrapper(name="pts")
    @XmlElement(name="pt")
    @XmlJavaTypeAdapter(value=PointAdapter.Point2DAdapter.class)
    public List<Point2D> getPts() {
        return this.pts;
    }

    @Override
    public void setPts(List<Point2D> pts) {
        if (pts == null) {
            int capacity = Optional.ofNullable(this.getPtsNumber()).filter(n -> n > 0).orElse(DEFAULT_PTS_SIZE);
            this.pts = new ArrayList<Point2D>(capacity);
        } else {
            this.pts = new ArrayList<Point2D>(pts);
        }
    }

    @Override
    public Graphic buildGraphic(List<Point2D> pts) throws InvalidShapeException {
        this.setPts(pts);
        if (!this.pts.isEmpty()) {
            this.prepareShape();
        }
        return this;
    }

    protected abstract void prepareShape() throws InvalidShapeException;

    protected void initCopy(Graphic graphic) {
    }

    @Override
    public Boolean getVariablePointsNumber() {
        return this.variablePointsNumber;
    }

    @Override
    public void setVariablePointsNumber(Boolean variablePointsNumber) {
        Objects.requireNonNull(variablePointsNumber, NULL_MSG);
        this.variablePointsNumber = variablePointsNumber;
    }

    @Override
    @XmlElement(name="paint")
    @XmlJavaTypeAdapter(value=ColorModelAdapter.PaintAdapter.class)
    public Paint getColorPaint() {
        return this.colorPaint;
    }

    public void setColorPaint(Paint colorPaint) {
        this.colorPaint = Optional.ofNullable(colorPaint).orElse(DEFAULT_COLOR);
    }

    @Override
    @XmlAttribute(name="thickness")
    public Float getLineThickness() {
        return this.lineThickness;
    }

    @Override
    public void setLineThickness(Float lineThickness) {
        if (!Objects.equals(this.lineThickness, lineThickness)) {
            this.lineThickness = Optional.ofNullable(lineThickness).orElse(DEFAULT_LINE_THICKNESS);
            Shape shape = this.shape;
            if (shape instanceof AdvancedShape) {
                AdvancedShape advancedShape = (AdvancedShape)shape;
                advancedShape.getShapeList().stream().forEachOrdered(bs -> bs.changeLineThickness(lineThickness.floatValue()));
            }
            this.fireDrawingChanged();
        }
    }

    @Override
    @XmlAttribute(name="fillOpacity")
    public Float getFillOpacity() {
        return this.fillOpacity;
    }

    @Override
    public void setFillOpacity(Float fillOpacity) {
        if (!Objects.equals(this.fillOpacity, fillOpacity)) {
            this.fillOpacity = Optional.ofNullable(fillOpacity).orElse(DEFAULT_FILL_OPACITY);
            this.fireDrawingChanged();
        }
    }

    @Override
    @XmlAttribute(name="showLabel")
    public Boolean getLabelVisible() {
        return this.labelVisible;
    }

    @Override
    public void setLabelVisible(Boolean labelVisible) {
        if (!Objects.equals(this.labelVisible, labelVisible)) {
            this.labelVisible = Optional.ofNullable(labelVisible).orElse(DEFAULT_LABEL_VISIBLE);
            this.fireLabelChanged();
        }
    }

    @Override
    @XmlAttribute(name="fill")
    public Boolean getFilled() {
        return this.filled;
    }

    @Override
    public void setFilled(Boolean filled) {
        if (!Objects.equals(this.filled, filled) && this instanceof GraphicArea) {
            this.filled = Optional.ofNullable(filled).orElse(DEFAULT_FILLED);
            this.fireDrawingChanged();
        }
    }

    @Override
    @XmlAttribute(name="classId")
    public Integer getClassID() {
        return this.classID;
    }

    @Override
    public void setClassID(Integer classID) {
        this.classID = classID;
    }

    @Override
    public Shape getShape() {
        return this.shape;
    }

    @Override
    public Boolean getSelected() {
        return this.selected;
    }

    @Override
    public void setSelected(Boolean selected) {
        if (!Objects.equals(this.selected, selected)) {
            this.selected = Optional.ofNullable(selected).orElse(DEFAULT_SELECTED);
            this.fireDrawingChanged();
            this.fireLabelChanged();
        }
    }

    @Override
    @XmlElement(name="graphicLabel")
    public GraphicLabel getGraphicLabel() {
        return this.graphicLabel;
    }

    public void setGraphicLabel(GraphicLabel graphicLabel) {
        this.graphicLabel = graphicLabel;
    }

    @Override
    public void setLayer(GraphicLayer layer) {
        Objects.requireNonNull(layer, NULL_MSG);
        this.layer = layer;
        this.setLayerType(layer.getType());
    }

    @Override
    @XmlIDREF
    @XmlElement(name="layer")
    public GraphicLayer getLayer() {
        return this.layer;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Area getArea(AffineTransform transform) {
        if (Objects.isNull(this.shape)) {
            return new Area();
        }
        Shape shape = this.shape;
        if (shape instanceof AdvancedShape) {
            AdvancedShape advancedShape = (AdvancedShape)shape;
            return advancedShape.getArea(transform);
        }
        double growingSize = Math.max(SELECTION_SIZE, HANDLE_SIZE);
        growingSize = Math.max(growingSize, (double)this.lineThickness.floatValue());
        BasicStroke boundingStroke = new BasicStroke((float)(growingSize /= GeomUtil.extractScalingFactor(transform)), 1, 1);
        return new Area(boundingStroke.createStrokedShape(this.shape));
    }

    @Override
    public Boolean intersects(Rectangle rectangle, AffineTransform transform) {
        return Optional.ofNullable(rectangle).map(this.getArea(transform)::intersects).orElse(false);
    }

    @Override
    public Rectangle getBounds(AffineTransform transform) {
        if (Objects.isNull(this.shape)) {
            return null;
        }
        Shape shape = this.shape;
        if (shape instanceof AdvancedShape) {
            AdvancedShape advancedShape = (AdvancedShape)shape;
            advancedShape.setAffineTransform(transform);
        }
        Rectangle2D bounds = this.shape.getBounds2D();
        double growingSize = (double)this.lineThickness.floatValue() / 2.0;
        GeomUtil.growRectangle(bounds, growingSize /= GeomUtil.extractScalingFactor(transform));
        return Optional.ofNullable(bounds).map(RectangularShape::getBounds).orElse(null);
    }

    @Override
    public Rectangle getRepaintBounds(AffineTransform transform) {
        return this.getRepaintBounds(this.shape, transform);
    }

    @Override
    public Rectangle getTransformedBounds(Shape shape, AffineTransform transform) {
        Rectangle rectangle = this.getRepaintBounds(shape, transform);
        if (Objects.nonNull(transform) && Objects.nonNull(rectangle)) {
            rectangle = transform.createTransformedShape(rectangle).getBounds();
        }
        return rectangle;
    }

    @Override
    public Rectangle getTransformedBounds(GraphicLabel label, AffineTransform transform) {
        return Optional.ofNullable(label).map(l -> l.getTransformedBounds(transform).getBounds()).orElse(null);
    }

    @Override
    public void setLabel(String[] labels, ViewCanvas<?> view2d) {
        Consumer<Shape> applyShape = arg_0 -> this.lambda$setLabel$0(labels, view2d, arg_0);
        Optional.ofNullable(this.shape).ifPresent(applyShape);
    }

    @Override
    public void updateLabel(Object source, ViewCanvas<?> view2d) {
        boolean releasedEvent = false;
        if (source instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent)source;
            releasedEvent = mouseEvent.getID() == 502;
        } else if (source instanceof Boolean) {
            Boolean boolVal = (Boolean)source;
            releasedEvent = boolVal;
        }
        this.updateLabel(view2d, null, releasedEvent);
    }

    @Override
    public void paint(Graphics2D g2d, AffineTransform transform) {
        Paint oldPaint = g2d.getPaint();
        Stroke oldStroke = g2d.getStroke();
        Shape shape = this.shape;
        if (shape instanceof AdvancedShape) {
            AdvancedShape advancedShape = (AdvancedShape)shape;
            advancedShape.paint(g2d, transform);
        } else if (this.shape != null) {
            Shape drawingShape = transform == null ? this.shape : transform.createTransformedShape(this.shape);
            g2d.setPaint(this.colorPaint);
            g2d.setStroke(this.getStroke(this.lineThickness));
            g2d.draw(drawingShape);
            if (this.getFilled().booleanValue()) {
                Paint paint;
                if (this.fillOpacity.floatValue() < 1.0f && (paint = this.colorPaint) instanceof Color) {
                    Color color = (Color)paint;
                    int alpha = (int)(this.fillOpacity.floatValue() * (float)color.getAlpha());
                    g2d.setPaint(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
                }
                g2d.fill(drawingShape);
            }
        }
        g2d.setStroke(oldStroke);
        g2d.setPaint(oldPaint);
        if (this.getSelected().booleanValue()) {
            this.paintHandles(g2d, transform);
        }
        this.paintLabel(g2d, transform);
    }

    @Override
    public void paintLabel(Graphics2D g2d, AffineTransform transform) {
        if (this.isLabelDisplayable().booleanValue()) {
            this.graphicLabel.paint(g2d, transform, this.selected);
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertychangelistener) {
        for (PropertyChangeListener listener : this.pcs.getPropertyChangeListeners()) {
            if (listener != propertychangelistener) continue;
            return;
        }
        this.pcs.addPropertyChangeListener(propertychangelistener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertychangelistener) {
        this.pcs.removePropertyChangeListener(propertychangelistener);
    }

    @Override
    public void removeAllPropertyChangeListener() {
        for (PropertyChangeListener listener : this.pcs.getPropertyChangeListeners()) {
            this.pcs.removePropertyChangeListener(listener);
        }
    }

    @Override
    public void toFront() {
        if (this.isGraphicComplete().booleanValue()) {
            this.firePropertyChange("toFront", null, this);
        }
    }

    @Override
    public void toBack() {
        if (this.isGraphicComplete().booleanValue()) {
            this.firePropertyChange("toBack", null, this);
        }
    }

    @Override
    public void fireRemoveAction() {
        if (this.isGraphicComplete().booleanValue()) {
            this.firePropertyChange("remove", null, this);
        }
    }

    @Override
    public int getKeyCode() {
        return 0;
    }

    @Override
    public int getModifier() {
        return 0;
    }

    @Override
    public Graphic deepCopy() {
        Graphic newGraphic = (Graphic)this.copy();
        if (newGraphic == null) {
            return null;
        }
        for (Point2D p : this.pts) {
            newGraphic.getPts().add(p != null ? (Point2D)p.clone() : null);
        }
        newGraphic.buildShape();
        return newGraphic;
    }

    @Override
    public void fireRemoveAndRepaintAction() {
        if (this.isGraphicComplete().booleanValue()) {
            this.firePropertyChange("remove.repaint", null, this);
        }
    }

    @Override
    public Stroke getStroke(Float lineThickness) {
        return new BasicStroke(Optional.ofNullable(lineThickness).orElse(DEFAULT_LINE_THICKNESS).floatValue(), 0, 1);
    }

    public Stroke getDashStroke(Float lineThickness) {
        return new BasicStroke(Optional.ofNullable(lineThickness).orElse(DEFAULT_LINE_THICKNESS).floatValue(), 0, 0, 10.0f, new float[]{5.0f, 5.0f}, 0.0f);
    }

    protected Boolean isLabelDisplayable() {
        return this.labelVisible != false && this.graphicLabel != null && this.graphicLabel.getLabelBounds() != null;
    }

    @Override
    public Boolean isGraphicComplete() {
        return Objects.equals(this.pts.size(), this.pointNumber);
    }

    public Point2D getHandlePoint(int index) {
        Predicate<List> validateIndex = list -> list.size() > index;
        Function<List, Point2D> getPoint = list -> (Point2D)list.get(index);
        Function<Point2D, Point2D> cloneValue = point -> (Point2D)point.clone();
        return Optional.of(this.pts).filter(validateIndex).map(getPoint).map(cloneValue).orElse(null);
    }

    public List<Point2D> getHandlePointList() {
        return this.pts.stream().map(p -> (Point2D)p.clone()).collect(Collectors.toList());
    }

    public void setHandlePoint(int index, Point2D newPoint) {
        Optional.ofNullable(this.pts).ifPresent(list -> {
            if (index >= 0 && index <= list.size()) {
                if (index == list.size()) {
                    list.add(newPoint);
                } else {
                    list.set(index, newPoint);
                }
            }
        });
    }

    public Integer getHandlePointListSize() {
        return this.pts.size();
    }

    @Override
    public Integer getHandleSize() {
        return HANDLE_SIZE;
    }

    @Override
    public Area getArea(MouseEvent mouseEvent) {
        AffineTransform transform = this.getAffineTransform(mouseEvent);
        return this.getArea(transform);
    }

    protected AffineTransform getAffineTransform(MouseEvent mouseevent) {
        if (mouseevent != null && mouseevent.getSource() instanceof Image2DViewer) {
            return ((Image2DViewer)mouseevent.getSource()).getAffineTransform();
        }
        return null;
    }

    public Rectangle getBounds(MouseEvent mouseEvent) {
        AffineTransform transform = this.getAffineTransform(mouseEvent);
        return this.getBounds(transform);
    }

    public Rectangle getRepaintBounds(Shape shape, AffineTransform transform) {
        if (Objects.isNull(shape)) {
            return null;
        }
        if (shape instanceof AdvancedShape) {
            AdvancedShape advancedShape = (AdvancedShape)shape;
            advancedShape.setAffineTransform(transform);
        }
        Rectangle2D bounds = shape.getBounds2D();
        double growingSize = Math.max((double)HANDLE_SIZE.intValue() * 1.5 / 2.0, (double)this.lineThickness.floatValue() / 2.0) + 2.0;
        GeomUtil.growRectangle(bounds, growingSize /= GeomUtil.extractScalingFactor(transform));
        return bounds != null ? bounds.getBounds() : null;
    }

    @Override
    public Rectangle getRepaintBounds(MouseEvent mouseEvent) {
        AffineTransform transform = this.getAffineTransform(mouseEvent);
        return this.getRepaintBounds(this.shape, transform);
    }

    @Override
    public int getHandlePointIndex(MouseEventDouble mouseEvent) {
        int nearestHandlePtIndex = -1;
        Point2D mousePoint = Optional.ofNullable(mouseEvent).map(MouseEventDouble::getImageCoordinates).orElse(null);
        if (mousePoint != null && !this.pts.isEmpty() && !this.layer.getLocked().booleanValue()) {
            double minHandleDistance = Double.MAX_VALUE;
            double maxHandleDistance = (double)HANDLE_SIZE.intValue() * 1.5 / GeomUtil.extractScalingFactor(this.getAffineTransform(mouseEvent));
            for (int index = 0; index < this.pts.size(); ++index) {
                Point2D handlePoint = this.pts.get(index);
                double handleDistance = Optional.ofNullable(handlePoint).map(mousePoint::distance).orElse((Double)Double.MAX_VALUE);
                if (!(handleDistance <= maxHandleDistance) || !(handleDistance < minHandleDistance)) continue;
                minHandleDistance = handleDistance;
                nearestHandlePtIndex = index;
            }
        }
        return nearestHandlePtIndex;
    }

    public List<Integer> getHandlePointIndexList(MouseEventDouble mouseEvent) {
        TreeMap<Double, Integer> indexByDistanceMap = new TreeMap<Double, Integer>();
        Point2D mousePoint = Optional.ofNullable(mouseEvent).map(MouseEventDouble::getImageCoordinates).orElse(null);
        if (mousePoint != null && !this.pts.isEmpty() && !this.layer.getLocked().booleanValue()) {
            double maxHandleDistance = (double)HANDLE_SIZE.intValue() * 1.5 / GeomUtil.extractScalingFactor(this.getAffineTransform(mouseEvent));
            for (int index = 0; index < this.pts.size(); ++index) {
                double handleDistance;
                Point2D handlePoint = this.pts.get(index);
                double d = handleDistance = handlePoint != null ? mousePoint.distance(handlePoint) : Double.MAX_VALUE;
                if (!(handleDistance <= maxHandleDistance)) continue;
                indexByDistanceMap.put(handleDistance, index);
            }
        }
        return !indexByDistanceMap.isEmpty() ? new ArrayList(indexByDistanceMap.values()) : null;
    }

    @Override
    public boolean isOnGraphicLabel(MouseEventDouble mouseevent) {
        if (Objects.isNull(mouseevent)) {
            return false;
        }
        AffineTransform transform = this.getAffineTransform(mouseevent);
        if (transform != null && this.isLabelDisplayable().booleanValue()) {
            Area labelArea = this.graphicLabel.getArea(transform);
            return labelArea != null && labelArea.contains(mouseevent.getImageCoordinates());
        }
        return false;
    }

    @Override
    public ViewCanvas getDefaultView2d(MouseEvent mouseevent) {
        Object object;
        if (mouseevent != null && (object = mouseevent.getSource()) instanceof ViewCanvas) {
            ViewCanvas viewCanvas = (ViewCanvas)object;
            return viewCanvas;
        }
        return null;
    }

    @Override
    public void setShape(Shape newShape, MouseEvent mouseevent) {
        Shape oldShape = this.shape;
        this.shape = newShape;
        this.fireDrawingChanged(oldShape);
    }

    @Override
    public void setPaint(Color newPaintColor) {
        if (this.colorPaint == null || !this.colorPaint.equals(newPaintColor)) {
            this.colorPaint = newPaintColor;
            this.fireDrawingChanged();
        }
    }

    protected void fireDrawingChanged() {
        this.fireDrawingChanged(null);
    }

    protected void fireDrawingChanged(Shape oldShape) {
        this.firePropertyChange("bounds", oldShape, this.shape);
    }

    protected void firePropertyChange(String s, Object obj, Object obj1) {
        this.pcs.firePropertyChange(s, obj, obj1);
    }

    protected void firePropertyChange(String s, int i, int j) {
        this.pcs.firePropertyChange(s, i, j);
    }

    protected void firePropertyChange(String s, boolean flag, boolean flag1) {
        this.pcs.firePropertyChange(s, flag, flag1);
    }

    protected void fireLabelChanged() {
        this.fireLabelChanged(null);
    }

    protected void fireLabelChanged(GraphicLabel oldLabel) {
        this.firePropertyChange("graphicLabel", oldLabel, this.graphicLabel);
    }

    @Override
    public void setLabel(GraphicLabel label) {
        GraphicLabel oldLabel = Optional.ofNullable(this.graphicLabel).map(Copyable::copy).orElse(null);
        this.graphicLabel = label;
        this.fireLabelChanged(oldLabel);
    }

    public void setLabel(String[] labels, ViewCanvas<?> view2d, Point2D pos) {
        GraphicLabel oldLabel = Optional.ofNullable(this.graphicLabel).map(Copyable::copy).orElse(null);
        if (labels == null || labels.length == 0) {
            this.graphicLabel = null;
            this.fireLabelChanged(oldLabel);
        } else if (pos == null) {
            this.setLabel(labels, view2d);
        } else {
            if (this.graphicLabel == null) {
                this.graphicLabel = new DefaultGraphicLabel();
            }
            this.graphicLabel.setLabel(view2d, pos.getX(), pos.getY(), labels);
            this.fireLabelChanged(oldLabel);
        }
    }

    @Override
    public void moveLabel(Double deltaX, Double deltaY) {
        if (this.isLabelDisplayable().booleanValue() && (MathUtil.isDifferentFromZero((double)deltaX) || MathUtil.isDifferentFromZero((double)deltaY))) {
            GraphicLabel oldLabel = (GraphicLabel)this.graphicLabel.copy();
            this.graphicLabel.move(deltaX, deltaY);
            this.fireLabelChanged(oldLabel);
        }
    }

    public void updateLabel(ViewCanvas<?> view2d, Point2D pos, boolean releasedEvent) {
        List selectedGraphics = view2d == null ? Collections.emptyList() : view2d.getGraphicManager().getSelectedGraphics();
        boolean isMultiSelection = selectedGraphics.size() > 1;
        List<MeasureItem> measList = null;
        String[] labels = null;
        if ((this.labelVisible.booleanValue() || !isMultiSelection) && this.getLayerType() == LayerType.MEASURE) {
            Unit displayUnit = view2d == null ? null : (Unit)((Object)view2d.getActionValue(ActionW.SPATIAL_UNIT.cmd()));
            measList = this.computeMeasurements(view2d == null ? null : view2d.getMeasurableLayer(), releasedEvent, displayUnit);
        }
        if (this.labelVisible.booleanValue() && measList != null && !measList.isEmpty()) {
            ArrayList<String> labelList = new ArrayList<String>(measList.size());
            for (MeasureItem item : measList) {
                Measurement measurement;
                if (item == null || (measurement = item.getMeasurement()) == null || !measurement.getGraphicLabel().booleanValue()) continue;
                StringBuilder sb = new StringBuilder();
                String name = measurement.getName();
                Object value = item.getValue();
                String unit = item.getUnit();
                if (name != null) {
                    sb.append(name);
                    if (item.getLabelExtension() != null) {
                        sb.append(item.getLabelExtension());
                    }
                    sb.append(" : ");
                    if (value instanceof Number) {
                        Number number = (Number)value;
                        sb.append(DecFormatter.allNumber(number));
                        if (unit != null) {
                            sb.append(" ").append(unit);
                        }
                    } else if (value != null) {
                        sb.append(value);
                    }
                }
                labelList.add(sb.toString());
            }
            if (!labelList.isEmpty()) {
                labels = labelList.toArray(new String[0]);
            }
        }
        if (labels == null && this.graphicLabel != null && (view2d == null || this.getLayerType() != LayerType.MEASURE)) {
            labels = this.graphicLabel.getLabels();
        }
        this.setLabel(labels, view2d, pos);
        if (selectedGraphics.size() == 1 && this.equals(selectedGraphics.get(0)) && view2d != null) {
            for (GraphicSelectionListener gfxListener : view2d.getGraphicManager().getGraphicSelectionListeners()) {
                gfxListener.updateMeasuredItems(measList);
            }
        }
    }

    protected void paintHandles(Graphics2D g2d, AffineTransform transform) {
        if (!this.pts.isEmpty()) {
            double size = HANDLE_SIZE.intValue();
            double halfSize = size / 2.0;
            ArrayList<Point2D.Double> handlePts = new ArrayList<Point2D.Double>(this.pts.size());
            for (Point2D pt : this.pts) {
                if (pt == null) continue;
                handlePts.add(new Point2D.Double(pt.getX(), pt.getY()));
            }
            Point2D[] handlePtArray = handlePts.toArray(new Point2D[0]);
            transform.transform(handlePtArray, 0, handlePtArray, 0, handlePtArray.length);
            Paint oldPaint = g2d.getPaint();
            Stroke oldStroke = g2d.getStroke();
            g2d.setPaint(Color.black);
            for (Point2D point : handlePtArray) {
                g2d.fill(new Rectangle2D.Double(point.getX() - halfSize, point.getY() - halfSize, size, size));
            }
            g2d.setPaint(Color.white);
            g2d.setStroke(new BasicStroke(1.0f));
            for (Point2D point : handlePtArray) {
                g2d.draw(new Rectangle2D.Double(point.getX() - halfSize, point.getY() - halfSize, size, size));
            }
            g2d.setPaint(oldPaint);
            g2d.setStroke(oldStroke);
        }
    }

    @Override
    public boolean isShapeValid() {
        if (!this.isGraphicComplete().booleanValue()) {
            return false;
        }
        int lastPointIndex = this.pts.size() - 1;
        while (lastPointIndex > 0) {
            Point2D checkPoint = this.pts.get(lastPointIndex);
            ListIterator<Point2D> listIt = this.pts.listIterator(lastPointIndex--);
            while (listIt.hasPrevious()) {
                if (checkPoint == null || !checkPoint.equals(listIt.previous())) continue;
                return false;
            }
        }
        return true;
    }

    protected void fireMoveAction() {
        if (this.isGraphicComplete().booleanValue()) {
            this.firePropertyChange("move", null, this);
        }
    }

    @Override
    public LayerType getLayerType() {
        return this.layerType;
    }

    @Override
    public void setLayerType(LayerType layerType) {
        this.layerType = Objects.requireNonNull(layerType, NULL_MSG);
    }

    /*
     * Unable to fully structure code
     */
    private /* synthetic */ void lambda$setLabel$0(String[] labels, ViewCanvas view2d, Shape s) {
        if (!(s instanceof AdvancedShape)) ** GOTO lbl-1000
        advancedShape = (AdvancedShape)s;
        if (!advancedShape.shapeList.isEmpty()) {
            generalPath = advancedShape.shapeList.get(0).getShape();
            rect = generalPath.getBounds2D();
        } else lbl-1000:
        // 2 sources

        {
            rect = s.getBounds2D();
        }
        xPos = rect.getX() + rect.getWidth() + 3.0;
        yPos = rect.getY() + rect.getHeight() * 0.5;
        this.setLabel(labels, view2d, new Point2D.Double(xPos, yPos));
    }

    static class Adapter
    extends XmlAdapter<AbstractGraphic, Graphic> {
        Adapter() {
        }

        public Graphic unmarshal(AbstractGraphic v) throws Exception {
            v.buildGraphic(v.getPts());
            return v;
        }

        public AbstractGraphic marshal(Graphic v) throws Exception {
            return (AbstractGraphic)v;
        }
    }
}

