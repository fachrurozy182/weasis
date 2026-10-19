/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Optional;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.editor.image.Canvas;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.ReferencedSeries;
import org.weasis.core.ui.model.graphic.DragGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.GraphicSelectionListener;
import org.weasis.core.ui.model.graphic.imp.area.SelectGraphic;
import org.weasis.core.ui.model.layer.GraphicLayer;
import org.weasis.core.ui.model.layer.GraphicModelChangeListener;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.model.utils.UUIDable;
import org.weasis.core.ui.util.MouseEventDouble;

public interface GraphicModel
extends UUIDable {
    public List<Graphic> getModels();

    public void setModels(List<Graphic> var1);

    public void updateLabels(Object var1, ViewCanvas<? extends ImageElement> var2);

    public void addGraphic(Graphic var1);

    public Optional<GraphicLayer> findLayerByType(LayerType var1);

    public void deleteByLayerType(LayerType var1);

    public List<Graphic> getAllGraphics();

    public List<Graphic> getSelectedAllGraphicsIntersecting(Rectangle var1, AffineTransform var2);

    public List<Graphic> getSelectedAllGraphicsIntersecting(Rectangle var1, AffineTransform var2, boolean var3);

    public Optional<Graphic> getFirstGraphicIntersecting(MouseEventDouble var1);

    public List<DragGraphic> getSelectedDraggableGraphics();

    public List<Graphic> getSelectedGraphics();

    public Optional<SelectGraphic> getSelectGraphic();

    public void deleteSelectedGraphics(Canvas var1, Boolean var2);

    public void setSelectedGraphic(List<Graphic> var1);

    public void clear();

    public void setSelectedAllGraphics();

    public void fireGraphicsSelectionChanged(MeasurableLayer var1);

    public void draw(Graphics2D var1, AffineTransform var2, AffineTransform var3, Rectangle2D var4);

    public List<GraphicSelectionListener> getGraphicSelectionListeners();

    public void addGraphicSelectionListener(GraphicSelectionListener var1);

    public void removeGraphicSelectionListener(GraphicSelectionListener var1);

    public void dispose();

    public List<GraphicModelChangeListener> getChangeListeners();

    public void addChangeListener(GraphicModelChangeListener var1);

    public void removeChangeListener(GraphicModelChangeListener var1);

    public void fireChanged();

    public Boolean isChangeFiringSuspended();

    public void setChangeFiringSuspended(Boolean var1);

    public int getLayerCount();

    public void removeGraphic(Graphic var1);

    public void addGraphicChangeHandler(PropertyChangeListener var1);

    public void removeGraphicChangeHandler(PropertyChangeListener var1);

    public List<GraphicLayer> getLayers();

    public void setReferencedSeries(List<ReferencedSeries> var1);

    public List<ReferencedSeries> getReferencedSeries();

    public List<GraphicLayer> groupLayerByType();

    public void deleteByLayer(GraphicLayer var1);

    public void deleteNonSerializableGraphics();

    public boolean hasSerializableGraphics();

    public List<PropertyChangeListener> getGraphicsListeners();
}

