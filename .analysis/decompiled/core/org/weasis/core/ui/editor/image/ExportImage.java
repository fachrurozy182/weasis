/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import org.weasis.core.api.gui.model.ViewModel;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.MouseActionAdapter;
import org.weasis.core.api.image.ImageOpNode;
import org.weasis.core.api.image.SimpleOpManager;
import org.weasis.core.api.image.util.ImageLayer;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.util.FontItem;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.MouseActions;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.layer.LayerItem;

public class ExportImage<E extends ImageElement>
extends DefaultView2d<E> {
    private final ViewCanvas<E> sourceView;
    private Graphics2D currentG2d;
    private double imagePrintingResolution = 1.0;

    public ExportImage(ViewCanvas<E> sourceView) {
        super(sourceView.getEventManager(), null);
        this.sourceView = sourceView;
        this.imageLayer.removeEventListener(this.imageLayer.getDisplayOpManager());
        this.setFont(FontItem.MINI.getFont());
        this.infoLayer = sourceView.getInfoLayer().getLayerCopy(this, false);
        this.infoLayer.setVisible(sourceView.getInfoLayer().getVisible());
        this.infoLayer.setShowBottomScale(false);
        this.infoLayer.setDisplayPreferencesValue(LayerItem.PIXEL, false);
        this.infoLayer.setDisplayPreferencesValue(LayerItem.PRELOADING_BAR, false);
        SimpleOpManager operations = this.imageLayer.getDisplayOpManager();
        for (ImageOpNode op : sourceView.getImageLayer().getDisplayOpManager().getOperations()) {
            operations.addImageOperationAction((ImageOpNode)op.copy());
        }
        sourceView.copyActionWState(this.actionsInView);
        this.setPreferredSize(new Dimension(1024, 1024));
        ViewModel model = sourceView.getViewModel();
        Rectangle2D.Double canvas = new Rectangle2D.Double(0.0, 0.0, sourceView.getJComponent().getWidth(), sourceView.getJComponent().getHeight());
        this.actionsInView.put("origin.image.bound", canvas);
        this.actionsInView.put("origin.zoom", sourceView.getActionValue(ActionW.ZOOM.cmd()));
        this.actionsInView.put("origin.center.offset", new Point2D.Double(model.getModelOffsetX(), model.getModelOffsetY()));
        this.series = sourceView.getSeries();
        this.setImage(sourceView.getImage());
    }

    public double getImagePrintingResolution() {
        return this.imagePrintingResolution;
    }

    public void setImagePrintingResolution(double imagePrintingResolution) {
        this.imagePrintingResolution = imagePrintingResolution;
    }

    @Override
    public void disposeView() {
        this.disableMouseAndKeyListener();
        this.removeFocusListener(this);
        ToolTipManager.sharedInstance().unregisterComponent(this);
        this.imageLayer.removeLayerChangeListener(this);
        this.removeGraphicManager(this.graphicManager, this.layerModelHandler);
        this.setViewModel(null);
    }

    @Override
    public Graphics getGraphics() {
        if (this.currentG2d != null) {
            return this.currentG2d;
        }
        return super.getGraphics();
    }

    @Override
    public void paintComponent(Graphics g) {
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D)g;
            this.draw(g2d);
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        this.currentG2d = g2d;
        Stroke oldStroke = g2d.getStroke();
        Paint oldColor = g2d.getPaint();
        g2d.setFont(this.getLayerFont());
        Object[] oldRenderingHints = GuiUtils.setRenderingHints(g2d, true, true, true);
        this.graphicManager.updateLabels(Boolean.TRUE, this);
        Point2D offset = this.getClipViewCoordinatesOffset();
        g2d.translate(offset.getX(), offset.getY());
        Integer rotationAngle = (Integer)this.actionsInView.get(ActionW.ROTATION.cmd());
        if ((rotationAngle == null || rotationAngle == 0) && g2d.getClass().getName().contains("print")) {
            this.imageLayer.drawImageForPrinter(g2d, this.imagePrintingResolution, this);
        } else {
            this.imageLayer.drawImage(g2d);
        }
        this.drawLayers(g2d, this.affineTransform, this.inverseTransform);
        g2d.translate(-offset.getX(), -offset.getY());
        if (this.infoLayer != null) {
            this.infoLayer.paint(g2d);
        }
        GuiUtils.resetRenderingHints(g2d, oldRenderingHints);
        g2d.setPaint(oldColor);
        g2d.setStroke(oldStroke);
        this.graphicManager.updateLabels(Boolean.TRUE, this.sourceView);
        this.currentG2d = null;
    }

    @Override
    public void handleLayerChanged(ImageLayer<E> layer) {
    }

    @Override
    public void enableMouseAndKeyListener(MouseActions mouseActions) {
    }

    @Override
    public MouseActionAdapter getMouseAdapter(String command) {
        return null;
    }

    @Override
    public JPopupMenu buildGraphicContextMenu(MouseEvent evt, List<Graphic> selected) {
        return null;
    }

    @Override
    public JPopupMenu buildContextMenu(MouseEvent evt) {
        return null;
    }

    @Override
    public boolean hasValidContent() {
        return this.getSourceImage() != null;
    }
}

