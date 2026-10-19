/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.LangUtil
 */
package org.weasis.core.ui.editor.image;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.Image2DViewer;
import org.weasis.core.api.gui.model.ViewModel;
import org.weasis.core.api.gui.model.ViewModelChangeListener;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.image.ImageOpNode;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.editor.image.Canvas;
import org.weasis.core.ui.editor.image.DrawingsKeyListeners;
import org.weasis.core.ui.editor.image.PropertyChangeHandler;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.imp.XmlGraphicModel;
import org.weasis.core.ui.model.layer.GraphicModelChangeListener;
import org.weasis.core.ui.model.layer.imp.RenderedImageLayer;
import org.weasis.core.ui.model.utils.imp.DefaultViewModel;
import org.weasis.core.util.LangUtil;

public abstract class GraphicsPane
extends JComponent
implements Canvas {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphicsPane.class);
    protected GraphicModel graphicManager;
    protected ViewModel viewModel;
    protected final LayerModelHandler layerModelHandler;
    protected final ViewModelHandler viewModelHandler;
    protected final DrawingsKeyListeners drawingsKeyListeners;
    protected final HashMap<String, Object> actionsInView = new HashMap();
    protected final AffineTransform affineTransform = new AffineTransform();
    protected final AffineTransform inverseTransform = new AffineTransform();
    protected final PropertyChangeListener graphicsChangeHandler;

    public GraphicsPane(ViewModel viewModel) {
        this.setOpaque(true);
        this.layerModelHandler = new LayerModelHandler(this);
        this.graphicManager = new XmlGraphicModel();
        this.graphicManager.addChangeListener(this.layerModelHandler);
        this.viewModelHandler = new ViewModelHandler(this);
        this.viewModel = Optional.ofNullable(viewModel).orElseGet(DefaultViewModel::new);
        this.viewModel.addViewModelChangeListener(this.viewModelHandler);
        this.drawingsKeyListeners = new DrawingsKeyListeners(this);
        this.graphicsChangeHandler = new PropertyChangeHandler(this);
    }

    @Override
    public void setGraphicManager(GraphicModel graphicManager) {
        Objects.requireNonNull(graphicManager);
        GraphicModel graphicManagerOld = this.graphicManager;
        if (!Objects.equals(graphicManager, graphicManagerOld)) {
            this.removeGraphicManager(graphicManagerOld, this.layerModelHandler);
            this.graphicManager = graphicManager;
            graphicManager.addGraphicChangeHandler(this.graphicsChangeHandler);
            GraphicsPane graphicsPane = this;
            if (graphicsPane instanceof ViewCanvas) {
                ViewCanvas viewCanvas = (ViewCanvas)((Object)graphicsPane);
                graphicManager.updateLabels(Boolean.TRUE, viewCanvas);
            }
            graphicManager.addChangeListener(this.layerModelHandler);
            this.firePropertyChange("graphicManager", graphicManagerOld, this.graphicManager);
        }
    }

    @Override
    public DrawingsKeyListeners getDrawingsKeyListeners() {
        return this.drawingsKeyListeners;
    }

    @Override
    public GraphicModel getGraphicManager() {
        return this.graphicManager;
    }

    @Override
    public AffineTransform getAffineTransform() {
        return this.affineTransform;
    }

    @Override
    public AffineTransform getInverseTransform() {
        return this.inverseTransform;
    }

    @Override
    public PropertyChangeListener getGraphicsChangeHandler() {
        return this.graphicsChangeHandler;
    }

    @Override
    public void disposeView() {
        Optional.ofNullable(this.viewModel).ifPresent(model -> model.removeViewModelChangeListener(this.viewModelHandler));
        this.removeGraphicManager(this.graphicManager, this.layerModelHandler);
    }

    @Override
    public ViewModel getViewModel() {
        return this.viewModel;
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        ViewModel viewModelOld = this.viewModel;
        if (viewModelOld != viewModel) {
            if (viewModelOld != null) {
                viewModelOld.removeViewModelChangeListener(this.viewModelHandler);
            }
            this.viewModel = viewModel;
            if (this.viewModel != null) {
                this.viewModel.addViewModelChangeListener(this.viewModelHandler);
            }
            this.firePropertyChange("viewModel", viewModelOld, this.viewModel);
        }
    }

    @Override
    public Object getActionValue(String action) {
        if (action == null) {
            return null;
        }
        return this.actionsInView.get(action);
    }

    @Override
    public Map<String, Object> getActionsInView() {
        return this.actionsInView;
    }

    @Override
    public JComponent getJComponent() {
        return this;
    }

    @Override
    public void zoom(Double viewScale) {
        this.getViewModel().setModelOffset(this.viewModel.getModelOffsetX(), this.viewModel.getModelOffsetY(), this.cropViewScale(viewScale));
    }

    public void zoom(Rectangle2D zoomRect) {
        Rectangle2D modelArea = this.viewModel.getModelArea();
        this.getViewModel().setModelOffset(modelArea.getCenterX() - zoomRect.getCenterX(), modelArea.getCenterY() - zoomRect.getCenterY(), Math.min((double)this.getWidth() / zoomRect.getWidth(), (double)this.getHeight() / zoomRect.getHeight()));
    }

    @Override
    public double getBestFitViewScale() {
        Rectangle2D modelArea = this.viewModel.getModelArea();
        return this.cropViewScale(Math.min((double)this.getWidth() / modelArea.getWidth(), (double)this.getHeight() / modelArea.getHeight()));
    }

    @Override
    public Point2D viewToModel(Double viewX, Double viewY) {
        Point2D p = this.getViewCoordinatesOffset();
        p.setLocation(viewX - p.getX(), viewY - p.getY());
        this.inverseTransform.transform(p, p);
        return p;
    }

    @Override
    public double viewToModelLength(Double viewLength) {
        return viewLength / this.viewModel.getViewScale();
    }

    @Override
    public Point2D modelToView(Double modelX, Double modelY) {
        Point2D.Double p2 = new Point2D.Double(modelX, modelY);
        this.affineTransform.transform(p2, p2);
        Point2D p = this.getViewCoordinatesOffset();
        ((Point2D)p2).setLocation(((Point2D)p2).getX() + p.getX(), ((Point2D)p2).getY() + p.getY());
        return p2;
    }

    @Override
    public double modelToViewLength(Double modelLength) {
        return modelLength * this.viewModel.getViewScale();
    }

    @Override
    public Point2D getViewCoordinatesOffset() {
        Rectangle2D b = this.getImageViewBounds(this.getWidth(), this.getHeight());
        return new Point2D.Double(b.getX(), b.getY());
    }

    public Path2D getVisibleImageViewBounds() {
        Point2D p = this.getClipViewCoordinatesOffset();
        Rectangle2D.Double bounds = new Rectangle2D.Double(-p.getX(), -p.getY(), this.getWidth(), this.getHeight());
        Shape path = this.inverseTransform.createTransformedShape(bounds);
        if (path instanceof Path2D) {
            Path2D path2D = (Path2D)path;
            return path2D;
        }
        Path2D.Double path2D = new Path2D.Double();
        path2D.append(bounds, false);
        return path2D;
    }

    @Override
    public Rectangle2D getImageViewBounds() {
        return this.getImageViewBounds(this.getWidth(), this.getHeight());
    }

    @Override
    public Rectangle2D getImageViewBounds(double viewportWidth, double viewportHeight) {
        Rectangle2D b = this.affineTransform.createTransformedShape(this.viewModel.getModelArea()).getBounds2D();
        ViewModel m = this.getViewModel();
        double viewOffsetX = (viewportWidth - b.getWidth()) * 0.5;
        double viewOffsetY = (viewportHeight - b.getHeight()) * 0.5;
        double offsetX = viewOffsetX - m.getModelOffsetX() * m.getViewScale();
        double offsetY = viewOffsetY - m.getModelOffsetY() * m.getViewScale();
        b.setRect(offsetX, offsetY, b.getWidth(), b.getHeight());
        return b;
    }

    @Override
    public Point2D getClipViewCoordinatesOffset() {
        Point2D p = this.getViewCoordinatesOffset();
        p.setLocation(Math.max(p.getX(), 0.0), Math.max(p.getY(), 0.0));
        return p;
    }

    @Override
    public Point2D getImageCoordinatesFromMouse(Integer x, Integer y) {
        Point2D p = this.getClipViewCoordinatesOffset();
        p.setLocation((double)x.intValue() - p.getX(), (double)y.intValue() - p.getY());
        this.inverseTransform.transform(p, p);
        return p;
    }

    @Override
    public Point getMouseCoordinatesFromImage(Double x, Double y) {
        Point2D.Double p2 = new Point2D.Double(x, y);
        this.affineTransform.transform(p2, p2);
        Point2D p = this.getClipViewCoordinatesOffset();
        return new Point((int)Math.floor(((Point2D)p2).getX() + p.getX() + 0.5), (int)Math.floor(((Point2D)p2).getY() + p.getY() + 0.5));
    }

    protected void updateAffineTransform(Image2DViewer<? extends ImageElement> view2d, ImageOpNode node, RenderedImageLayer<?> imageLayer, double viewSizeShift) {
        Point offset;
        Rectangle2D modelArea = this.getViewModel().getModelArea();
        double viewScale = this.getViewModel().getViewScale();
        double rWidth = modelArea.getWidth();
        double rHeight = modelArea.getHeight();
        boolean flip = LangUtil.nullToFalse((Boolean)((Boolean)view2d.getActionValue(ActionW.FLIP.cmd())));
        Integer rotationAngle = (Integer)view2d.getActionValue(ActionW.ROTATION.cmd());
        this.affineTransform.setToScale(flip ? -viewScale : viewScale, viewScale);
        if (rotationAngle != null && rotationAngle > 0) {
            this.affineTransform.rotate(Math.toRadians(rotationAngle.intValue()), rWidth / 2.0, rHeight / 2.0);
        }
        if (flip) {
            this.affineTransform.translate(-rWidth, 0.0);
        }
        if (node != null) {
            Rectangle2D dstBounds;
            Rectangle2D srcBounds;
            Rectangle2D imgBounds = this.affineTransform.createTransformedShape(modelArea).getBounds2D();
            double diffX = 0.0;
            double diffY = 0.0;
            Rectangle2D.Double viewBounds = new Rectangle2D.Double(0.0, 0.0, (double)this.getWidth() + viewSizeShift, (double)this.getHeight() + viewSizeShift);
            if (viewBounds.contains(srcBounds = this.getImageViewBounds(((RectangularShape)viewBounds).getWidth(), ((RectangularShape)viewBounds).getHeight()))) {
                dstBounds = srcBounds;
            } else {
                dstBounds = ((Rectangle2D)viewBounds).createIntersection(srcBounds);
                if (srcBounds.getX() < 0.0) {
                    diffX += srcBounds.getX();
                }
                if (srcBounds.getY() < 0.0) {
                    diffY += srcBounds.getY();
                }
            }
            double[] fmx = new double[6];
            this.affineTransform.getMatrix(fmx);
            fmx[4] = fmx[4] - (imgBounds.getX() - diffX);
            fmx[5] = fmx[5] - (imgBounds.getY() - diffY);
            this.affineTransform.setTransform(fmx[0], fmx[1], fmx[2], fmx[3], fmx[4], fmx[5]);
            List<Double> m = List.of(Double.valueOf(fmx[0]), Double.valueOf(fmx[2]), Double.valueOf(fmx[4]), Double.valueOf(fmx[1]), Double.valueOf(fmx[3]), Double.valueOf(fmx[5]));
            node.setParam("affine.matrix", m);
            node.setParam("dest.bounds", dstBounds);
            imageLayer.updateDisplayOperations();
        }
        if ((offset = view2d.getImageLayer().getOffset()) != null) {
            this.affineTransform.translate(-offset.getX(), -offset.getY());
        }
        try {
            this.inverseTransform.setTransform(this.affineTransform.createInverse());
        }
        catch (NoninvertibleTransformException e) {
            LOGGER.error("Create inverse transform", (Throwable)e);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (this.isOpaque()) {
            g.setColor(this.getBackground());
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }

    private double cropViewScale(double viewScale) {
        return DefaultViewModel.cropViewScale(viewScale, this.viewModel.getViewScaleMin(), this.viewModel.getViewScaleMax());
    }

    public static void repaint(Canvas canvas, Rectangle rectangle) {
        if (rectangle != null) {
            Point2D p = canvas.getClipViewCoordinatesOffset();
            int x = (int)((double)rectangle.x + p.getX());
            int y = (int)((double)rectangle.y + p.getY());
            canvas.getJComponent().repaint(new Rectangle(x, y, rectangle.width, rectangle.height));
        }
    }

    private class LayerModelHandler
    implements GraphicModelChangeListener {
        final /* synthetic */ GraphicsPane this$0;

        private LayerModelHandler(GraphicsPane graphicsPane) {
            GraphicsPane graphicsPane2 = graphicsPane;
            Objects.requireNonNull(graphicsPane2);
            this.this$0 = graphicsPane2;
        }

        @Override
        public void handleModelChanged(GraphicModel modelList) {
            this.this$0.repaint();
        }
    }

    private class ViewModelHandler
    implements ViewModelChangeListener {
        final /* synthetic */ GraphicsPane this$0;

        private ViewModelHandler(GraphicsPane graphicsPane) {
            GraphicsPane graphicsPane2 = graphicsPane;
            Objects.requireNonNull(graphicsPane2);
            this.this$0 = graphicsPane2;
        }

        @Override
        public void handleViewModelChanged(ViewModel viewModel) {
            this.this$0.repaint();
        }
    }
}

