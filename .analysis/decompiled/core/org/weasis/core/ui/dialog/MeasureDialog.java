/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.EscapeChars
 */
package org.weasis.core.ui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.Objects;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.ui.dialog.PropertiesDialog;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.model.graphic.DragGraphic;
import org.weasis.core.ui.model.graphic.GraphicArea;
import org.weasis.core.ui.model.graphic.imp.AnnotationGraphic;
import org.weasis.core.ui.model.graphic.imp.PixelInfoGraphic;
import org.weasis.core.util.EscapeChars;

public class MeasureDialog
extends PropertiesDialog {
    private final List<DragGraphic> graphics;
    private final JTextPane textPane = new JTextPane();
    private final ViewCanvas<?> view2D;

    public MeasureDialog(ViewCanvas<?> view2d, List<DragGraphic> selectedGraphic) {
        super(SwingUtilities.getWindowAncestor(view2d.getJComponent()), Messages.getString("MeasureDialog.draw_props"));
        this.view2D = view2d;
        this.graphics = Objects.requireNonNull(selectedGraphic);
        this.iniGraphicDialog();
        this.pack();
    }

    public void iniGraphicDialog() {
        if (!this.graphics.isEmpty()) {
            DragGraphic graphic = (DragGraphic)this.graphics.getFirst();
            this.color = (Color)graphic.getColorPaint();
            int width = graphic.getLineThickness().intValue();
            boolean fill = graphic.getFilled();
            float opacity = graphic.getFillOpacity().floatValue();
            this.spinnerLineWidth.setValue(width);
            this.jCheckBoxFilled.setSelected(fill);
            this.slicerOpacity.setValue((int)(opacity * 100.0f));
            boolean areaGraphics = false;
            boolean mfill = false;
            boolean mcolor = false;
            boolean mwidth = false;
            boolean mopacity = false;
            for (DragGraphic g : this.graphics) {
                if (g instanceof GraphicArea) {
                    areaGraphics = true;
                    if (g.getFilled() != fill) {
                        mfill = true;
                    }
                }
                if (g.getLineThickness().intValue() != width) {
                    mwidth = true;
                }
                if (!this.color.equals(g.getColorPaint())) {
                    mcolor = true;
                }
                if (g.getFillOpacity().floatValue() == opacity) continue;
                mopacity = true;
            }
            this.checkBoxColor.setEnabled(mcolor);
            this.jLabelLineColor.setEnabled(!mcolor);
            this.jButtonColor.setEnabled(!mcolor);
            this.checkBoxWidth.setEnabled(mwidth);
            this.spinnerLineWidth.setEnabled(!mwidth);
            this.jLabelLineWidth.setEnabled(!mwidth);
            this.checkBoxFill.setEnabled(mfill);
            this.jCheckBoxFilled.setEnabled(areaGraphics && !mfill);
            this.checkBoxFillOpacity.setEnabled(mopacity);
            this.slicerOpacity.setEnabled(areaGraphics && !mopacity);
            if (this.graphics.size() == 1 || !mcolor && !mfill && !mwidth && !mopacity) {
                this.overrideMultipleValues.setVisible(false);
            } else {
                int size = this.overrideMultipleValues.getPreferredSize().width / 2;
                this.panelColor.add(GuiUtils.boxHorizontalStrut(size));
                this.panelColor.add(this.checkBoxColor);
                this.panelColor.add(GuiUtils.boxHorizontalStrut(size));
                this.panelLine.add(GuiUtils.boxHorizontalStrut(size));
                this.panelLine.add(this.checkBoxWidth);
                this.panelLine.add(GuiUtils.boxHorizontalStrut(size));
                this.panelFilled.add(GuiUtils.boxHorizontalStrut(size));
                this.panelFilled.add(this.checkBoxFill);
                this.panelFilled.add(GuiUtils.boxHorizontalStrut(size));
                this.panelOpacity.add(this.checkBoxFillOpacity);
                this.panelOpacity.add(GuiUtils.boxHorizontalStrut(size));
            }
            if (this.view2D != null && this.graphics.size() == 1 && graphic instanceof AnnotationGraphic) {
                String[] labels;
                AnnotationGraphic annotationGraphic = (AnnotationGraphic)graphic;
                JScrollPane panel = new JScrollPane();
                panel.setBorder(new CompoundBorder(GuiUtils.getEmptyBorder(10, 15, 5, 15), GuiUtils.getTitledBorder(Messages.getString("MeasureDialog.text"))));
                panel.setPreferredSize(GuiUtils.getDimension(400, 140));
                StringBuilder buf = new StringBuilder();
                for (String s : labels = annotationGraphic.getLabels()) {
                    buf.append(s);
                    buf.append("\n");
                }
                this.textPane.setEnabled(!(graphic instanceof PixelInfoGraphic));
                this.textPane.setText(buf.toString());
                panel.setViewportView(this.textPane);
                this.getContentPane().add((Component)panel, "North");
            }
        }
    }

    @Override
    protected void okAction() {
        DragGraphic graphic;
        for (DragGraphic graphic2 : this.graphics) {
            if (this.spinnerLineWidth.isEnabled()) {
                graphic2.setLineThickness(Float.valueOf(((Integer)this.spinnerLineWidth.getValue()).floatValue()));
            }
            if (this.jButtonColor.isEnabled()) {
                graphic2.setPaint(this.color);
            }
            if (this.jCheckBoxFilled.isEnabled()) {
                graphic2.setFilled(this.jCheckBoxFilled.isSelected());
            }
            if (!this.slicerOpacity.isEnabled()) continue;
            graphic2.setFillOpacity(Float.valueOf((float)this.slicerOpacity.getValue() / 100.0f));
        }
        if (this.graphics.size() == 1 && this.view2D != null && (graphic = (DragGraphic)this.graphics.getFirst()) instanceof AnnotationGraphic) {
            graphic.setLabel(EscapeChars.convertToLines((String)this.textPane.getText()), this.view2D);
        }
        this.dispose();
    }
}

