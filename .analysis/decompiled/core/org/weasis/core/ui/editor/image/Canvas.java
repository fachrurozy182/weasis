/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.core.ui.editor.image;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Optional;
import javax.swing.JComponent;
import org.weasis.core.api.gui.model.ViewModel;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.DrawingsKeyListeners;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.layer.GraphicModelChangeListener;
import org.weasis.core.util.MathUtil;

public interface Canvas {
    public JComponent getJComponent();

    public AffineTransform getAffineTransform();

    public AffineTransform getInverseTransform();

    public void disposeView();

    public ViewModel getViewModel();

    public void setViewModel(ViewModel var1);

    public Object getActionValue(String var1);

    public Map<String, Object> getActionsInView();

    public void zoom(Double var1);

    public double getRealWorldViewScale();

    public double getBestFitViewScale();

    public Point2D viewToModel(Double var1, Double var2);

    public double viewToModelLength(Double var1);

    public Point2D modelToView(Double var1, Double var2);

    public double modelToViewLength(Double var1);

    public Point2D getImageCoordinatesFromMouse(Integer var1, Integer var2);

    public Point getMouseCoordinatesFromImage(Double var1, Double var2);

    public void setGraphicManager(GraphicModel var1);

    public GraphicModel getGraphicManager();

    public PropertyChangeListener getGraphicsChangeHandler();

    public Point2D getClipViewCoordinatesOffset();

    public Point2D getViewCoordinatesOffset();

    public Rectangle2D getImageViewBounds();

    public Rectangle2D getImageViewBounds(double var1, double var3);

    public DrawingsKeyListeners getDrawingsKeyListeners();

    public double adjustViewScale(double var1);

    default public double adjustViewScale(ImageViewerEventManager<?> eventManager, double viewScale) {
        double ratio = viewScale;
        Optional<SliderChangeListener> zoom = eventManager.getAction(ActionW.ZOOM);
        if (zoom.isPresent()) {
            SliderChangeListener z = zoom.get();
            if (eventManager.getSelectedViewPane() == this) {
                z.setRealValue(ratio, false);
                ratio = z.getRealValue();
            } else {
                ratio = z.toModelValue(z.toSliderValue(ratio));
            }
        }
        return ratio;
    }

    default public void removeGraphicManager(GraphicModel graphicManager, GraphicModelChangeListener layerModelHandler) {
        if (graphicManager != null) {
            graphicManager.removeChangeListener(layerModelHandler);
            graphicManager.removeGraphicChangeHandler(this.getGraphicsChangeHandler());
            graphicManager.deleteNonSerializableGraphics();
        }
    }

    default public double getZoomRatioFromType(Double viewScale) {
        boolean defSize = MathUtil.isEqualToZero((double)viewScale);
        double ratio = viewScale;
        if (defSize) {
            DefaultView2d.ZoomType type = (DefaultView2d.ZoomType)((Object)this.getActionsInView().get("zoom.type"));
            if (DefaultView2d.ZoomType.BEST_FIT.equals((Object)type)) {
                ratio = -this.getBestFitViewScale();
            } else if (DefaultView2d.ZoomType.REAL.equals((Object)type)) {
                ratio = -this.getRealWorldViewScale();
            }
            if (MathUtil.isEqualToZero((double)ratio)) {
                ratio = -this.adjustViewScale(1.0);
            }
        }
        return ratio;
    }
}

