/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlAccessType
 *  jakarta.xml.bind.annotation.XmlAccessorType
 *  jakarta.xml.bind.annotation.XmlElement
 *  jakarta.xml.bind.annotation.XmlElementWrapper
 *  jakarta.xml.bind.annotation.XmlElements
 *  jakarta.xml.bind.annotation.XmlType
 */
package org.weasis.core.ui.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.ui.editor.image.Canvas;
import org.weasis.core.ui.editor.image.MeasureToolBar;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.ReferencedSeries;
import org.weasis.core.ui.model.graphic.DragGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.GraphicLabel;
import org.weasis.core.ui.model.graphic.GraphicSelectionListener;
import org.weasis.core.ui.model.graphic.imp.AnnotationGraphic;
import org.weasis.core.ui.model.graphic.imp.PixelInfoGraphic;
import org.weasis.core.ui.model.graphic.imp.PointGraphic;
import org.weasis.core.ui.model.graphic.imp.angle.AngleToolGraphic;
import org.weasis.core.ui.model.graphic.imp.angle.CobbAngleToolGraphic;
import org.weasis.core.ui.model.graphic.imp.angle.FourPointsAngleToolGraphic;
import org.weasis.core.ui.model.graphic.imp.angle.OpenAngleToolGraphic;
import org.weasis.core.ui.model.graphic.imp.area.EllipseGraphic;
import org.weasis.core.ui.model.graphic.imp.area.ObliqueRectangleGraphic;
import org.weasis.core.ui.model.graphic.imp.area.PolygonGraphic;
import org.weasis.core.ui.model.graphic.imp.area.SelectGraphic;
import org.weasis.core.ui.model.graphic.imp.area.ThreePointsCircleGraphic;
import org.weasis.core.ui.model.graphic.imp.line.LineGraphic;
import org.weasis.core.ui.model.graphic.imp.line.LineWithGapGraphic;
import org.weasis.core.ui.model.graphic.imp.line.ParallelLineGraphic;
import org.weasis.core.ui.model.graphic.imp.line.PerpendicularLineGraphic;
import org.weasis.core.ui.model.graphic.imp.line.PolylineGraphic;
import org.weasis.core.ui.model.layer.GraphicLayer;
import org.weasis.core.ui.model.layer.GraphicModelChangeListener;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.model.layer.imp.DefaultLayer;
import org.weasis.core.ui.model.utils.imp.DefaultUUID;
import org.weasis.core.ui.util.MouseEventDouble;

@XmlType(propOrder={"referencedSeries", "layers", "models"})
@XmlAccessorType(value=XmlAccessType.NONE)
public abstract class AbstractGraphicModel
extends DefaultUUID
implements GraphicModel {
    private List<ReferencedSeries> referencedSeries;
    private List<GraphicLayer> layers;
    protected List<Graphic> models;
    private final List<GraphicSelectionListener> selectedGraphicsListeners = new ArrayList<GraphicSelectionListener>();
    private final List<GraphicModelChangeListener> modelListeners = new ArrayList<GraphicModelChangeListener>();
    private final List<PropertyChangeListener> graphicsListeners = new ArrayList<PropertyChangeListener>();
    private Boolean changeFiringSuspended = Boolean.FALSE;
    private final Function<Graphic, GraphicLayer> getLayer = Graphic::getLayer;
    private final Function<Graphic, DragGraphic> castToDragGraphic = DragGraphic.class::cast;
    private final Predicate<Graphic> isLayerVisible = g -> g.getLayer().getVisible();
    private final Predicate<Graphic> isGraphicSelected = Graphic::getSelected;

    protected AbstractGraphicModel() {
        this((List<ReferencedSeries>)null);
    }

    protected AbstractGraphicModel(List<ReferencedSeries> referencedSeries) {
        this.setReferencedSeries(referencedSeries);
        this.layers = Collections.synchronizedList(new ArrayList());
        this.models = Collections.synchronizedList(new ArrayList());
    }

    @Override
    @XmlElementWrapper(name="graphics")
    @XmlElements(value={@XmlElement(name="point", type=PointGraphic.class), @XmlElement(name="angle", type=AngleToolGraphic.class), @XmlElement(name="annotation", type=AnnotationGraphic.class), @XmlElement(name="pixelInfo", type=PixelInfoGraphic.class), @XmlElement(name="openAngle", type=OpenAngleToolGraphic.class), @XmlElement(name="cobbAngle", type=CobbAngleToolGraphic.class), @XmlElement(name="rectangle", type=ObliqueRectangleGraphic.class), @XmlElement(name="ellipse", type=EllipseGraphic.class), @XmlElement(name="fourPointsAngle", type=FourPointsAngleToolGraphic.class), @XmlElement(name="line", type=LineGraphic.class), @XmlElement(name="lineWithGap", type=LineWithGapGraphic.class), @XmlElement(name="perpendicularLine", type=PerpendicularLineGraphic.class), @XmlElement(name="parallelLine", type=ParallelLineGraphic.class), @XmlElement(name="polygon", type=PolygonGraphic.class), @XmlElement(name="polyline", type=PolylineGraphic.class), @XmlElement(name="threePointsCircle", type=ThreePointsCircleGraphic.class)})
    public List<Graphic> getModels() {
        return this.models;
    }

    @Override
    @XmlElementWrapper(name="layers")
    @XmlElements(value={@XmlElement(name="layer", type=DefaultLayer.class)})
    public List<GraphicLayer> getLayers() {
        return this.layers;
    }

    @Override
    @XmlElementWrapper(name="references")
    @XmlElement(name="series")
    public List<ReferencedSeries> getReferencedSeries() {
        return this.referencedSeries;
    }

    @Override
    public void setReferencedSeries(List<ReferencedSeries> referencedSeries) {
        if (referencedSeries != null && !referencedSeries.getClass().getSimpleName().startsWith("Synchronized")) {
            this.referencedSeries = Collections.synchronizedList(referencedSeries);
        }
        this.referencedSeries = Optional.ofNullable(referencedSeries).orElseGet(() -> Collections.synchronizedList(new ArrayList()));
    }

    @Override
    public void setModels(List<Graphic> models) {
        if (models != null) {
            this.models = Collections.synchronizedList(models);
            this.layers = Collections.synchronizedList(this.getLayerList());
        }
    }

    @Override
    public void addGraphic(Graphic graphic) {
        if (graphic != null) {
            GraphicLayer layer = graphic.getLayer();
            if (layer == null) {
                layer = this.findLayerByType(graphic.getLayerType()).orElseGet(() -> new DefaultLayer(graphic.getLayerType()));
                graphic.setLayer(layer);
            }
            if (!this.layers.contains(layer)) {
                this.layers.add(layer);
            }
            this.models.add(graphic);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeGraphic(Graphic graphic) {
        if (graphic != null) {
            this.models.remove(graphic);
            graphic.removeAllPropertyChangeListener();
            GraphicLayer layer = graphic.getLayer();
            if (layer != null) {
                boolean layerExist = false;
                List<Graphic> list = this.models;
                synchronized (list) {
                    for (Graphic g : this.models) {
                        if (!g.getLayer().equals(layer)) continue;
                        layerExist = true;
                        break;
                    }
                }
                if (!layerExist) {
                    this.layers.remove(layer);
                }
            }
        }
    }

    private List<GraphicLayer> getLayerList() {
        return this.models.parallelStream().map(this.getLayer).distinct().collect(Collectors.toList());
    }

    @Override
    public void addGraphicChangeHandler(PropertyChangeListener graphicsChangeHandler) {
        if (Objects.nonNull(graphicsChangeHandler) && !this.graphicsListeners.contains(graphicsChangeHandler)) {
            this.graphicsListeners.add(graphicsChangeHandler);
            this.models.forEach(g -> g.addPropertyChangeListener(graphicsChangeHandler));
        }
    }

    @Override
    public void removeGraphicChangeHandler(PropertyChangeListener graphicsChangeHandler) {
        if (Objects.nonNull(graphicsChangeHandler) && this.graphicsListeners.contains(graphicsChangeHandler)) {
            this.graphicsListeners.remove(graphicsChangeHandler);
            this.models.forEach(g -> g.removePropertyChangeListener(graphicsChangeHandler));
        }
    }

    @Override
    public List<PropertyChangeListener> getGraphicsListeners() {
        return this.graphicsListeners;
    }

    @Override
    public void updateLabels(Object source, ViewCanvas<? extends ImageElement> view) {
        this.models.forEach(g -> g.updateLabel(source, view));
    }

    @Override
    public Optional<GraphicLayer> findLayerByType(LayerType type) {
        Objects.requireNonNull(type);
        return this.layers.stream().filter(AbstractGraphicModel.isLayerTypeEquals(type)).findFirst();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public List<GraphicLayer> groupLayerByType() {
        if (this.models.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<GraphicLayer> layerType = new ArrayList<GraphicLayer>();
        List<Graphic> list = this.models;
        synchronized (list) {
            for (Graphic g : this.models) {
                LayerType type = g.getLayer().getType();
                boolean notInGroup = true;
                for (GraphicLayer glayer : layerType) {
                    if (!Objects.equals((Object)glayer.getType(), (Object)type)) continue;
                    notInGroup = false;
                    break;
                }
                if (!notInGroup) continue;
                layerType.add(g.getLayer());
            }
        }
        return layerType;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void deleteByLayer(GraphicLayer layer) {
        Objects.requireNonNull(layer);
        if (this.models.isEmpty()) {
            return;
        }
        List<Graphic> list = this.models;
        synchronized (list) {
            this.models.removeIf(g -> {
                boolean delete = layer.equals(g.getLayer());
                if (delete) {
                    g.removeAllPropertyChangeListener();
                }
                return delete;
            });
            this.layers.removeIf(l -> Objects.equals(l, layer));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void deleteByLayerType(LayerType type) {
        Objects.requireNonNull(type);
        if (this.models.isEmpty()) {
            return;
        }
        List<Graphic> list = this.models;
        synchronized (list) {
            for (Graphic g2 : this.models) {
                if (!g2.getLayer().getType().equals((Object)type)) continue;
                g2.removeAllPropertyChangeListener();
            }
            this.models.removeIf(g -> Objects.equals((Object)g.getLayer().getType(), (Object)type));
            this.layers.removeIf(l -> Objects.equals((Object)l.getType(), (Object)type));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void deleteNonSerializableGraphics() {
        if (this.models.isEmpty()) {
            return;
        }
        List<Graphic> list = this.models;
        synchronized (list) {
            for (Graphic g2 : this.models) {
                if (g2.getLayer().getSerializable().booleanValue()) continue;
                g2.removeAllPropertyChangeListener();
            }
            this.models.removeIf(g -> g.getLayer().getSerializable() == false);
            this.layers.removeIf(l -> l.getSerializable() == false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean hasSerializableGraphics() {
        if (this.models.isEmpty()) {
            return false;
        }
        List<Graphic> list = this.models;
        synchronized (list) {
            for (Graphic g : this.models) {
                if (!g.getLayer().getSerializable().booleanValue() || g.getPts().isEmpty()) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Graphic> getAllGraphics() {
        return this.models.stream().filter(this.isLayerVisible).collect(Collectors.toList());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public List<Graphic> getSelectedAllGraphicsIntersecting(Rectangle rectangle, AffineTransform transform) {
        ArrayList<Graphic> selectedGraphicList = new ArrayList<Graphic>();
        if (rectangle != null) {
            List<Graphic> list = this.models;
            synchronized (list) {
                for (int i = this.models.size() - 1; i >= 0; --i) {
                    Area selectionArea;
                    Area selectionArea2;
                    Graphic graphic = this.models.get(i);
                    GraphicLayer layer = graphic.getLayer();
                    if (!layer.getVisible().booleanValue() || !layer.getSelectable().booleanValue()) continue;
                    Rectangle graphBounds = graphic.getBounds(transform);
                    if (graphBounds != null && graphBounds.intersects(rectangle) && (selectionArea2 = graphic.getArea(transform)) != null && selectionArea2.intersects(rectangle)) {
                        selectedGraphicList.add(graphic);
                        continue;
                    }
                    GraphicLabel graphicLabel = graphic.getGraphicLabel();
                    if (!graphic.getLabelVisible().booleanValue() || graphicLabel == null || graphicLabel.getLabels() == null || (selectionArea = graphicLabel.getArea(transform)) == null || !selectionArea.intersects(rectangle)) continue;
                    selectedGraphicList.add(graphic);
                }
            }
        }
        return selectedGraphicList;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public List<Graphic> getSelectedAllGraphicsIntersecting(Rectangle rectangle, AffineTransform transform, boolean onlyFrontGraphic) {
        ArrayList<Graphic> selectedGraphicList = new ArrayList<Graphic>();
        if (rectangle != null) {
            List<Graphic> list = this.models;
            synchronized (list) {
                for (int i = this.models.size() - 1; i >= 0; --i) {
                    Area labelArea;
                    GraphicLabel graphicLabel;
                    Graphic graphic = this.models.get(i);
                    GraphicLayer layer = graphic.getLayer();
                    if (!layer.getVisible().booleanValue() || !layer.getSelectable().booleanValue()) continue;
                    ArrayList<Area> selectedAreaList = new ArrayList<Area>();
                    Area selectedArea = null;
                    Rectangle selectionBounds = graphic.getRepaintBounds(transform);
                    if (selectionBounds != null && selectionBounds.intersects(rectangle)) {
                        selectedArea = graphic.getArea(transform);
                    }
                    if ((graphicLabel = graphic.getGraphicLabel()) != null && graphicLabel.getLabels() != null && (labelArea = graphicLabel.getArea(transform)) != null) {
                        if (selectedArea != null) {
                            selectedArea.add(labelArea);
                        } else if (labelArea.intersects(rectangle)) {
                            selectedArea = graphic.getArea(transform);
                            selectedArea.add(labelArea);
                        }
                    }
                    if (selectedArea == null) continue;
                    if (onlyFrontGraphic) {
                        for (Area area : selectedAreaList) {
                            selectedArea.subtract(area);
                        }
                    }
                    if (!selectedArea.intersects(rectangle)) continue;
                    selectedAreaList.add(selectedArea);
                    selectedGraphicList.add(graphic);
                }
            }
        }
        return selectedGraphicList;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Optional<Graphic> getFirstGraphicIntersecting(MouseEventDouble mouseEvent) {
        Point2D.Double mousePt = mouseEvent.getImageCoordinates();
        Graphic firstSelectedGraph = null;
        List<Graphic> list = this.models;
        synchronized (list) {
            for (int i = this.models.size() - 1; i >= 0; --i) {
                Rectangle repaintBound;
                Graphic g = this.models.get(i);
                GraphicLayer l = g.getLayer();
                if (!l.getVisible().booleanValue() || !l.getSelectable().booleanValue()) continue;
                if (g.isOnGraphicLabel(mouseEvent)) {
                    if (g.getSelected().booleanValue()) {
                        return Optional.of(g);
                    }
                    if (firstSelectedGraph == null) {
                        firstSelectedGraph = g;
                    }
                }
                if ((repaintBound = g.getRepaintBounds(mouseEvent)) == null || !repaintBound.contains(mousePt) || g.getHandlePointIndex(mouseEvent) < 0 && !g.getArea(mouseEvent).contains(mousePt)) continue;
                if (g.getSelected().booleanValue()) {
                    return Optional.of(g);
                }
                if (firstSelectedGraph != null) continue;
                firstSelectedGraph = g;
            }
        }
        return Optional.ofNullable(firstSelectedGraph);
    }

    @Override
    public List<DragGraphic> getSelectedDraggableGraphics() {
        return this.models.stream().filter(this.isGraphicSelected).filter(DragGraphic.class::isInstance).map(this.castToDragGraphic).collect(Collectors.toList());
    }

    @Override
    public List<Graphic> getSelectedGraphics() {
        return this.models.stream().filter(this.isGraphicSelected).collect(Collectors.toList());
    }

    @Override
    public Optional<SelectGraphic> getSelectGraphic() {
        return this.models.stream().filter(SelectGraphic.class::isInstance).map(SelectGraphic.class::cast).findFirst();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setSelectedGraphic(List<Graphic> graphicList) {
        List<Graphic> list = this.models;
        synchronized (list) {
            for (Graphic g : this.models) {
                g.setSelected(false);
            }
        }
        if (graphicList != null) {
            for (Graphic g : graphicList) {
                g.setSelected(true);
            }
        }
    }

    @Override
    public void setSelectedAllGraphics() {
        this.setSelectedGraphic(this.getAllGraphics());
    }

    @Override
    public void deleteSelectedGraphics(Canvas canvas, Boolean warningMessage) {
        List<Graphic> list = this.getSelectedGraphics();
        if (list.isEmpty()) {
            return;
        }
        int response = 0;
        WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
        if (warningMessage.booleanValue() && preferences.getBooleanProperty("weasis.confirm.delete.measurement", true)) {
            JLabel label = new JLabel(String.format(Messages.getString("AbstractLayerModel.del_conf"), list.size()));
            JCheckBox dontAskAgain = new JCheckBox(Messages.getString("don.t.ask.again"));
            JPanel panel = GuiUtils.getVerticalBoxLayoutPanel(label, dontAskAgain);
            response = JOptionPane.showConfirmDialog(canvas.getJComponent(), panel, Messages.getString("AbstractLayerModel.del_graphs"), 0, 2);
            if (dontAskAgain.isSelected()) {
                preferences.putBooleanProperty("weasis.confirm.delete.measurement", false);
            }
        }
        if (response == 0) {
            list.forEach(Graphic::fireRemoveAction);
            canvas.getJComponent().repaint();
        }
    }

    @Override
    public void clear() {
        this.models.clear();
    }

    @Override
    public void fireGraphicsSelectionChanged(MeasurableLayer layer) {
        this.selectedGraphicsListeners.forEach(gl -> gl.handle(this.getSelectedGraphics(), layer));
    }

    @Override
    public void draw(Graphics2D g2d, AffineTransform transform, AffineTransform inverseTransform, Rectangle2D viewClip) {
        Shape area = inverseTransform.createTransformedShape(viewClip == null ? g2d.getClipBounds() : viewClip);
        Rectangle2D bound = area == null ? null : area.getBounds2D();
        g2d.translate(0.5, 0.5);
        this.models.forEach(g -> AbstractGraphicModel.applyPaint(g, g2d, transform, bound));
        g2d.translate(-0.5, -0.5);
    }

    private static void applyPaint(Graphic graphic, Graphics2D g2d, AffineTransform transform, Rectangle2D bounds) {
        if (graphic.getLayer().getVisible().booleanValue()) {
            if (bounds != null) {
                Rectangle repaintBounds = graphic.getRepaintBounds(transform);
                if (repaintBounds != null && repaintBounds.intersects(bounds)) {
                    graphic.paint(g2d, transform);
                } else {
                    Rectangle2D labelBounds;
                    GraphicLabel graphicLabel = graphic.getGraphicLabel();
                    if (graphicLabel != null && graphicLabel.getLabels() != null && (labelBounds = graphicLabel.getBounds(transform)).intersects(bounds)) {
                        graphic.paintLabel(g2d, transform);
                    }
                }
            } else {
                graphic.paint(g2d, transform);
                graphic.paintLabel(g2d, transform);
            }
        }
    }

    @Override
    public void addGraphicSelectionListener(GraphicSelectionListener listener) {
        if (Objects.nonNull(listener) && !this.selectedGraphicsListeners.contains(listener)) {
            this.selectedGraphicsListeners.add(listener);
        }
    }

    @Override
    public void removeGraphicSelectionListener(GraphicSelectionListener listener) {
        if (Objects.nonNull(listener)) {
            this.selectedGraphicsListeners.remove(listener);
        }
    }

    @Override
    public List<GraphicModelChangeListener> getChangeListeners() {
        return this.modelListeners;
    }

    @Override
    public void addChangeListener(GraphicModelChangeListener listener) {
        if (Objects.nonNull(listener) && !this.modelListeners.contains(listener)) {
            this.modelListeners.add(listener);
        }
    }

    @Override
    public void removeChangeListener(GraphicModelChangeListener listener) {
        Optional.ofNullable(listener).ifPresent(this.modelListeners::remove);
    }

    @Override
    public void fireChanged() {
        if (!this.changeFiringSuspended.booleanValue()) {
            this.modelListeners.stream().forEach(l -> l.handleModelChanged(this));
        }
    }

    @Override
    public Boolean isChangeFiringSuspended() {
        return this.changeFiringSuspended;
    }

    @Override
    public void setChangeFiringSuspended(Boolean change) {
        this.changeFiringSuspended = Optional.ofNullable(change).orElse(Boolean.FALSE);
    }

    @Override
    public void dispose() {
        this.modelListeners.clear();
        this.graphicsListeners.clear();
        this.selectedGraphicsListeners.clear();
    }

    @Override
    public int getLayerCount() {
        return this.models.stream().collect(Collectors.groupingBy(this.getLayer)).size();
    }

    @Override
    public List<GraphicSelectionListener> getGraphicSelectionListeners() {
        return this.selectedGraphicsListeners;
    }

    public static Graphic drawFromCurrentGraphic(ViewCanvas<?> canvas, Graphic graphicCreator) {
        Objects.requireNonNull(canvas);
        Graphic newGraphic = Optional.ofNullable(graphicCreator).orElse(MeasureToolBar.selectionGraphic);
        GraphicLayer layer = AbstractGraphicModel.getOrBuildLayer(canvas, newGraphic.getLayerType());
        if (!layer.getVisible().booleanValue() || !((Boolean)canvas.getActionValue(ActionW.DRAWINGS.cmd())).booleanValue()) {
            JOptionPane.showMessageDialog(canvas.getJComponent(), Messages.getString("AbstractLayerModel.msg_not_vis"), Messages.getString("AbstractLayerModel.draw"), 0);
            return null;
        }
        Graphic graph = (Graphic)newGraphic.copy();
        if (graph != null) {
            graph.updateLabel(Boolean.TRUE, canvas);
            for (PropertyChangeListener listener : canvas.getGraphicManager().getGraphicsListeners()) {
                graph.addPropertyChangeListener(listener);
            }
            graph.setLayer(layer);
            canvas.getGraphicManager().addGraphic(graph);
        }
        return graph;
    }

    public static void addGraphicToModel(ViewCanvas<?> canvas, GraphicLayer layer, Graphic graphic) {
        GraphicModel gm = canvas.getGraphicManager();
        graphic.setLayer(Optional.ofNullable(layer).orElseGet(() -> AbstractGraphicModel.getOrBuildLayer(canvas, graphic.getLayerType())));
        graphic.updateLabel(Boolean.TRUE, canvas);
        for (PropertyChangeListener listener : canvas.getGraphicManager().getGraphicsListeners()) {
            graphic.addPropertyChangeListener(listener);
        }
        gm.addGraphic(graphic);
    }

    public static void addGraphicToModel(ViewCanvas<?> canvas, Graphic graphic) {
        AbstractGraphicModel.addGraphicToModel(canvas, null, graphic);
    }

    public static GraphicLayer getOrBuildLayer(ViewCanvas<?> canvas, LayerType layerType) {
        return canvas.getGraphicManager().findLayerByType(layerType).orElseGet(() -> new DefaultLayer(layerType));
    }

    private static Predicate<GraphicLayer> isLayerTypeEquals(LayerType type) {
        return layer -> Objects.equals((Object)layer.getType(), (Object)type) && layer.getName() == null;
    }
}

