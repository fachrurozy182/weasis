/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.graphic.imp.area;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import org.weasis.core.Messages;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.graphic.imp.area.RectangleGraphic;
import org.weasis.core.ui.model.utils.Draggable;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.imp.SelectedDragSequence;

public class SelectGraphic
extends RectangleGraphic {
    public static final Icon ICON = ResourceUtil.getIcon(ResourceUtil.ActionIcon.DRAW_SELECTION);

    public SelectGraphic() {
        this.setPaint(Color.WHITE);
    }

    public SelectGraphic(SelectGraphic graphic) {
        super(graphic);
    }

    @Override
    public SelectGraphic copy() {
        return new SelectGraphic(this);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getUIName() {
        return Messages.getString("MeasureToolBar.sel");
    }

    @Override
    public void paint(Graphics2D g2d, AffineTransform transform) {
        Color oldColor = g2d.getColor();
        Stroke oldStroke = g2d.getStroke();
        g2d.setPaint(Color.white);
        float[] dash = new float[]{5.0f};
        Shape transformedShape = transform == null ? this.shape : transform.createTransformedShape(this.shape);
        g2d.setStroke(new BasicStroke(1.0f, 0, 0, 5.0f, dash, 0.0f));
        g2d.draw(transformedShape);
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(1.0f, 0, 0, 5.0f, dash, 5.0f));
        g2d.draw(transformedShape);
        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }

    @Override
    public Draggable createResizeDrag(Integer i) {
        return new SelectedDragSequence(this);
    }

    @Override
    public Boolean isGraphicComplete() {
        return this.pts.size() > 1;
    }

    @Override
    public Boolean getSelected() {
        return Boolean.TRUE;
    }

    @Override
    public void updateLabel(Object source, ViewCanvas<?> view2d) {
    }

    @Override
    public List<MeasureItem> computeMeasurements(MeasurableLayer layer, boolean releaseEvent, Unit displayUnit) {
        return Collections.emptyList();
    }
}

