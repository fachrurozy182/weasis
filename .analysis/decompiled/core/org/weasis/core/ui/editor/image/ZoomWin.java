/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.LangUtil
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.core.ui.editor.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.swing.UIManager;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.image.AffineTransformOp;
import org.weasis.core.api.image.ImageOpEvent;
import org.weasis.core.api.image.ImageOpNode;
import org.weasis.core.api.image.OpManager;
import org.weasis.core.api.image.SimpleOpManager;
import org.weasis.core.api.image.ZoomOp;
import org.weasis.core.api.image.util.ImageLayer;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.GraphicsPane;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.PopUpMenuOnZoom;
import org.weasis.core.ui.editor.image.SynchCineEvent;
import org.weasis.core.ui.editor.image.SynchData;
import org.weasis.core.ui.editor.image.SynchEvent;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.model.layer.imp.RenderedImageLayer;
import org.weasis.core.ui.model.utils.ImageLayerChangeListener;
import org.weasis.core.ui.pref.ZoomSetting;
import org.weasis.core.util.LangUtil;
import org.weasis.opencv.data.PlanarImage;

public class ZoomWin<E extends ImageElement>
extends GraphicsPane
implements ImageLayerChangeListener<E> {
    public static final String SYNCH_CMD = "synchronize";
    public static final String FREEZE_CMD = "freeze";
    private final DefaultView2d<E> view2d;
    private RectangularShape shape;
    private int borderOffset = 2;
    private Color lineColor;
    private Color backgroundColor;
    private Stroke stroke;
    private final PopUpMenuOnZoom popup;
    private final RenderedImageLayer<E> imageLayer;
    private final MouseHandler mouseHandler;
    private SimpleOpManager freezeOperations;
    private final HashMap<String, Object> freezeActionsInView = new HashMap();

    public ZoomWin(DefaultView2d<E> view2d) {
        super(null);
        this.view2d = view2d;
        this.setOpaque(false);
        ImageViewerEventManager<E> manager = view2d.getEventManager();
        this.imageLayer = new RenderedImageLayer();
        SimpleOpManager operations = this.imageLayer.getDisplayOpManager();
        operations.addImageOperationAction(new AffineTransformOp());
        manager.getAction(ActionW.LENS_ZOOM).ifPresent(c -> this.actionsInView.put(ActionW.ZOOM.cmd(), c.getRealValue()));
        this.popup = new PopUpMenuOnZoom(this);
        this.popup.setInvoker(this);
        this.setCursor(DefaultView2d.MOVE_CURSOR);
        ZoomSetting z = manager.getZoomSetting();
        OpManager disOp = this.getDisplayOpManager();
        disOp.setParamValue(AffineTransformOp.OP_NAME, "interpolation", (Object)ZoomOp.Interpolation.fromPosition(z.getInterpolation()));
        disOp.setParamValue(AffineTransformOp.OP_NAME, "affine.matrix", null);
        this.actionsInView.put(SYNCH_CMD, z.isLensSynchronize());
        this.actionsInView.put(ActionW.DRAWINGS.cmd(), z.isLensShowDrawings());
        this.actionsInView.put(FREEZE_CMD, SyncType.NONE);
        Color bckColor = UIManager.getColor("Panel.background");
        this.setLensDecoration(z.getLensLineWidth(), z.getLensLineColor(), bckColor, z.isLensRound());
        this.setSize(z.getLensWidth(), z.getLensHeight());
        this.setLocation(-1, -1);
        this.imageLayer.addLayerChangeListener(this);
        this.mouseHandler = new MouseHandler(this);
    }

    public void setActionInView(String action, Object value) {
        Optional.ofNullable(action).ifPresent(a -> this.actionsInView.put(a, value));
    }

    private void refreshZoomWin() {
        Point loc = this.getLocation();
        if (loc.x == -1 && loc.y == -1) {
            this.centerZoomWin();
            return;
        }
        Rectangle rect = this.view2d.getBounds();
        rect.x = 0;
        rect.y = 0;
        Rectangle2D r = rect.createIntersection(this.getBounds());
        if (r.getWidth() < 25.0 || r.getHeight() < 25.0) {
            this.centerZoomWin();
        }
    }

    public void updateImage() {
        this.view2d.graphicManager.addGraphicChangeHandler(this.graphicsChangeHandler);
        this.imageLayer.setImage(this.view2d.getImage(), (OpManager)this.actionsInView.get(ActionW.PREPROCESSING.cmd()));
        this.getViewModel().setModelArea(this.view2d.getViewModel().getModelArea());
        SyncType type = (SyncType)((Object)this.actionsInView.get(FREEZE_CMD));
        if (SyncType.PARENT_PARAMETERS.equals((Object)type)) {
            this.freezeOperations.setFirstNode(this.imageLayer.getSourceRenderedImage());
            this.freezeOperations.handleImageOpEvent(new ImageOpEvent(ImageOpEvent.OpEvent.IMAGE_CHANGE, (MediaSeries<? extends ImageElement>)this.view2d.getSeries(), (ImageElement)this.view2d.getImage(), null));
            this.freezeOperations.process();
        }
    }

    public void showLens(boolean val) {
        if (val) {
            this.updateImage();
            this.refreshZoomWin();
            this.updateZoom();
            this.enableMouseListener();
            this.setVisible(true);
        } else {
            this.setVisible(false);
            this.view2d.graphicManager.removeGraphicChangeHandler(this.graphicsChangeHandler);
            this.disableMouseAndKeyListener();
        }
    }

    public void centerZoomWin() {
        int magPosx = this.view2d.getWidth() / 2 - this.getWidth() / 2;
        int magPosy = this.view2d.getHeight() / 2 - this.getHeight() / 2;
        this.setLocation(magPosx, magPosy);
    }

    public void hideZoom() {
        if (Objects.equals(this.view2d.getEventManager().getSelectedViewPane(), this.view2d)) {
            this.view2d.getEventManager().getAction(ActionW.LENS).ifPresent(b -> b.setSelected(false));
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        if (g instanceof Graphics2D) {
            Graphics2D graphics2D = (Graphics2D)g;
            this.draw(graphics2D);
        }
    }

    protected void draw(Graphics2D g2d) {
        Stroke oldStroke = g2d.getStroke();
        Paint oldColor = g2d.getPaint();
        Shape oldClip = g2d.getClip();
        g2d.clip(this.shape);
        g2d.setBackground(this.backgroundColor);
        this.drawBackground(g2d);
        g2d.setFont(MeasureTool.viewSetting.getFont());
        Point2D p = this.getClipViewCoordinatesOffset();
        g2d.translate(p.getX(), p.getY());
        this.imageLayer.drawImage(g2d);
        this.drawLayers(g2d, this.affineTransform, this.inverseTransform);
        g2d.translate(-p.getX(), -p.getY());
        g2d.setClip(oldClip);
        g2d.setStroke(this.stroke);
        g2d.setPaint(this.lineColor);
        Rectangle bound = this.getBounds();
        int size = GuiUtils.getScaleLength(12);
        g2d.drawRect(bound.width - size - this.borderOffset, bound.height - size - this.borderOffset, size, size);
        g2d.draw(this.shape);
        g2d.setPaint(oldColor);
        g2d.setStroke(oldStroke);
    }

    public void drawLayers(Graphics2D g2d, AffineTransform transform, AffineTransform inverseTransform) {
        if (LangUtil.nullToTrue((Boolean)((Boolean)this.actionsInView.get(ActionW.DRAWINGS.cmd())))) {
            Object[] oldRenderingHints = GuiUtils.setRenderingHints(g2d, true, false, this.view2d.requiredTextAntialiasing());
            Rectangle2D.Double b = new Rectangle2D.Double(0.0, 0.0, this.getWidth(), this.getHeight());
            this.view2d.getGraphicManager().draw(g2d, transform, inverseTransform, b);
            GuiUtils.resetRenderingHints(g2d, oldRenderingHints);
        }
    }

    private void drawBackground(Graphics2D g2d) {
        g2d.clearRect(0, 0, this.getWidth(), this.getHeight());
    }

    protected void updateAffineTransform() {
        this.getViewModel().setModelOffset(this.getOffsetCenterX(), this.getOffsetCenterY());
        Optional<ImageOpNode> node = this.getDisplayOpManager().getNode(AffineTransformOp.OP_NAME);
        super.updateAffineTransform(this.view2d, node.orElse(null), this.imageLayer, -2.0);
    }

    public void setLensDecoration(int lineWidth, Color lineColor, Color backgroundColor, boolean roundShape) {
        this.borderOffset = lineWidth / 2 + 1;
        this.stroke = new BasicStroke(lineWidth);
        this.lineColor = lineColor;
        this.backgroundColor = backgroundColor;
        this.setBackground(backgroundColor);
        this.upateShape(roundShape);
    }

    @Override
    public void setSize(int width, int height) {
        this.shape.setFrame(this.borderOffset, this.borderOffset, (double)width - (double)this.borderOffset, (double)height - (double)this.borderOffset);
        super.setSize(width + this.borderOffset, height + this.borderOffset);
    }

    private void upateShape(boolean round) {
        this.shape = round ? new Ellipse2D.Double(this.getX(), this.getY(), this.getWidth(), this.getHeight()) : new Rectangle2D.Double(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    public double getOffsetCenterX() {
        double magPosx = (double)this.getX() + (double)this.getWidth() * 0.5 - (double)this.view2d.getWidth() * 0.5;
        return this.view2d.viewToModelLength(magPosx) + this.view2d.getViewModel().getModelOffsetX();
    }

    public double getOffsetCenterY() {
        double magPosy = (double)this.getY() + (double)this.getHeight() * 0.5 - (double)this.view2d.getHeight() * 0.5;
        return this.view2d.viewToModelLength(magPosy) + this.view2d.getViewModel().getModelOffsetY();
    }

    @Override
    public void zoom(Double viewScale) {
        E img = this.imageLayer.getSourceImage();
        Optional<ImageOpNode> node = this.imageLayer.getDisplayOpManager().getNode(AffineTransformOp.OP_NAME);
        if (img != null && node.isPresent()) {
            node.get().setParam("op.input.img", this.getSourceImage());
            this.actionsInView.put(ActionW.ZOOM.cmd(), viewScale);
            super.zoom(Math.abs(viewScale));
            this.updateAffineTransform();
        }
    }

    @Override
    public double getRealWorldViewScale() {
        return this.view2d.getRealWorldViewScale();
    }

    @Override
    public double adjustViewScale(double viewScale) {
        return this.view2d.adjustViewScale(viewScale);
    }

    public void updateZoom() {
        double zoomFactor = (Boolean)this.actionsInView.get(SYNCH_CMD) != false ? this.view2d.getViewModel().getViewScale() : ((Double)this.actionsInView.get(ActionW.ZOOM.cmd())).doubleValue();
        this.zoom(zoomFactor);
    }

    protected PlanarImage getSourceImage() {
        SyncType type = (SyncType)((Object)this.actionsInView.get(FREEZE_CMD));
        if (SyncType.PARENT_PARAMETERS.equals((Object)type) || SyncType.PARENT_IMAGE.equals((Object)type)) {
            return this.freezeOperations.getLastNodeOutputImage().orElse(null);
        }
        Optional<ImageOpNode> node = ((RenderedImageLayer)this.view2d.getImageLayer()).getDisplayOpManager().getNode(AffineTransformOp.OP_NAME);
        if (node.isPresent()) {
            return (PlanarImage)node.get().getParam("op.input.img");
        }
        return ((RenderedImageLayer)this.view2d.getImageLayer()).getDisplayOpManager().getLastNodeOutputImage().orElse(null);
    }

    public void setFreezeImage(SyncType type) {
        this.actionsInView.put(FREEZE_CMD, type);
        if (Objects.isNull((Object)type) || SyncType.NONE.equals((Object)type)) {
            this.freezeActionsInView.clear();
            this.freezeOperations = null;
            this.actionsInView.put(FREEZE_CMD, SyncType.NONE);
        } else {
            this.freezeParentParameters();
        }
        this.imageLayer.updateDisplayOperations();
        this.updateZoom();
    }

    void freezeParentParameters() {
        SimpleOpManager pManager = ((RenderedImageLayer)this.view2d.getImageLayer()).getDisplayOpManager();
        this.freezeActionsInView.clear();
        this.view2d.copyActionWState(this.freezeActionsInView);
        this.freezeOperations = new SimpleOpManager();
        for (ImageOpNode op : pManager.getOperations()) {
            if (AffineTransformOp.OP_NAME.equals(op.getParam("op.display.name"))) break;
            ImageOpNode operation = (ImageOpNode)op.copy();
            this.freezeOperations.addImageOperationAction(operation);
        }
        this.freezeOperations.setFirstNode(this.imageLayer.getSourceRenderedImage());
        this.freezeOperations.process();
    }

    public void disableMouseAndKeyListener() {
        this.removeMouseListener(this.mouseHandler);
        this.removeMouseMotionListener(this.mouseHandler);
        this.removeMouseWheelListener(this.view2d.getEventManager().getAction(ActionW.LENS_ZOOM).orElse(null));
    }

    public void enableMouseListener() {
        this.disableMouseAndKeyListener();
        this.addMouseListener(this.mouseHandler);
        this.addMouseMotionListener(this.mouseHandler);
        this.addMouseWheelListener(this.view2d.getEventManager().getAction(ActionW.LENS_ZOOM).orElse(null));
    }

    public ViewCanvas<E> getView2d() {
        return this.view2d;
    }

    @Override
    public void handleLayerChanged(ImageLayer layer) {
        this.repaint();
    }

    public void setCommandFromParentView(String command, Object value) {
        String cmd = null;
        if (ActionW.SYNCH.cmd().equals(command) && value instanceof SynchEvent) {
            SynchEvent synchEvent = (SynchEvent)value;
            if (!(value instanceof SynchCineEvent)) {
                SynchData synchData = (SynchData)this.view2d.getActionValue(ActionW.SYNCH_LINK.cmd());
                if (synchData != null && !synchData.isSynchActivated()) {
                    return;
                }
                for (Map.Entry<String, Object> entry : synchEvent.getEvents().entrySet()) {
                    if (synchData != null && !synchData.isActionEnable(entry.getKey())) continue;
                    cmd = entry.getKey();
                    break;
                }
            }
        } else {
            cmd = command;
        }
        if (cmd != null) {
            if (command.equals(ActionW.PROGRESSION.cmd())) {
                this.updateImage();
            } else if (command.equals(ActionW.ROTATION.cmd()) || command.equals(ActionW.FLIP.cmd())) {
                this.refreshZoomWin();
            }
        }
    }

    public OpManager getDisplayOpManager() {
        return this.imageLayer.getDisplayOpManager();
    }

    public static enum SyncType {
        NONE,
        PARENT_IMAGE,
        PARENT_PARAMETERS;

    }

    class MouseHandler
    extends MouseAdapter {
        private Point pickPoint;
        private int pickWidth;
        private int pickHeight;
        private int cursor;
        final /* synthetic */ ZoomWin this$0;

        MouseHandler(ZoomWin this$0) {
            ZoomWin zoomWin = this$0;
            Objects.requireNonNull(zoomWin);
            this.this$0 = zoomWin;
            this.pickPoint = null;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            ImageViewerPlugin pane = this.this$0.view2d.getEventManager().getSelectedView2dContainer();
            if (pane == null) {
                return;
            }
            if (pane.isContainingView(this.this$0.view2d).booleanValue()) {
                pane.setSelectedImagePane(this.this$0.view2d);
            }
            if (e.isPopupTrigger()) {
                this.this$0.popup.enableMenuItem();
                this.this$0.popup.show(e.getComponent(), e.getX(), e.getY());
            }
            this.pickPoint = e.getPoint();
            this.pickWidth = this.this$0.getWidth();
            this.pickHeight = this.this$0.getHeight();
            this.cursor = this.getCursor(e);
        }

        @Override
        public void mouseReleased(MouseEvent mouseevent) {
            this.pickPoint = null;
            if (mouseevent.isPopupTrigger()) {
                this.this$0.popup.enableMenuItem();
                this.this$0.popup.show(mouseevent.getComponent(), mouseevent.getX(), mouseevent.getY());
            } else if (mouseevent.getClickCount() == 2) {
                ImageViewerEventManager manager = this.this$0.view2d.getEventManager();
                manager.getAction(ActionW.LENS_ZOOM).ifPresent(c -> c.setRealValue(this.this$0.view2d.getViewModel().getViewScale()));
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            int mods = e.getModifiers();
            if (this.pickPoint != null && (mods & 0x10) != 0) {
                Point p = e.getPoint();
                int dx = p.x - this.pickPoint.x;
                int dy = p.y - this.pickPoint.y;
                if (this.cursor == 5) {
                    int nw = this.pickWidth + dx;
                    int nh = this.pickHeight + dy;
                    nw = nw < 50 ? 50 : Math.min(nw, 500);
                    nh = nh < 50 ? 50 : Math.min(nh, 500);
                    this.this$0.setSize(nw, nh);
                    this.this$0.updateAffineTransform();
                } else {
                    this.this$0.setLocation(this.this$0.getX() + dx, this.this$0.getY() + dy);
                    this.this$0.updateAffineTransform();
                }
                this.this$0.setCursor(Cursor.getPredefinedCursor(this.cursor));
            }
        }

        @Override
        public void mouseMoved(MouseEvent me) {
            this.this$0.setCursor(Cursor.getPredefinedCursor(this.getCursor(me)));
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {
            this.this$0.setCursor(Cursor.getDefaultCursor());
        }

        public int getCursor(MouseEvent me) {
            Component c = me.getComponent();
            int w = c.getWidth();
            int h = c.getHeight();
            int size = GuiUtils.getScaleLength(12);
            Rectangle rect = new Rectangle(w - size - this.this$0.borderOffset, h - size - this.this$0.borderOffset, size, size);
            if (rect.contains(me.getPoint())) {
                return 5;
            }
            return 13;
        }
    }
}

