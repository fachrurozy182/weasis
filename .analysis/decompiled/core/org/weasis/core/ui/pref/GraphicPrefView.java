/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.ui.pref;

import java.awt.LayoutManager;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.JSliderW;
import org.weasis.core.ui.dialog.PropertiesDialog;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.pref.ViewSetting;

public class GraphicPrefView
extends AbstractItemDialogPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphicPrefView.class);
    public static final String PAGE_NAME = ActionW.DRAW_GRAPHICS.getTitle();
    private final JSpinner spinner = new JSpinner();
    private final JCheckBox checkboxFilled = new JCheckBox(Messages.getString("PropertiesDialog.fill_shape"));
    private final JSliderW sliderOpacity = PropertiesDialog.createOpacitySlider(PropertiesDialog.FILL_OPACITY);

    public GraphicPrefView() {
        super(PAGE_NAME, 702);
        try {
            this.jbInit();
            this.initialize();
        }
        catch (Exception e) {
            LOGGER.error("Cannot initialize", (Throwable)e);
        }
    }

    private void jbInit() {
        this.add(GuiUtils.getFlowLayoutPanel(0, 10, this.checkboxFilled));
        JButton button = MeasureTool.buildLineColorButton(this);
        MeasureTool.viewSetting.initLineWidthSpinner(this.spinner);
        JPanel linePane = GuiUtils.getFlowLayoutPanel(button, this.spinner);
        linePane.setBorder(GuiUtils.getTitledBorder(Messages.getString("MeasureToolBar.line")));
        this.add(linePane);
        this.add(GuiUtils.boxVerticalStrut(15));
        JPanel shapePane = GuiUtils.getVerticalBoxLayoutPanel(new JComponent[0]);
        shapePane.add(GuiUtils.getFlowLayoutPanel(2, 5, this.checkboxFilled));
        MigLayout layout2 = new MigLayout("fillx, ins 5lp", "[fill]", "");
        new JPanel((LayoutManager)layout2).add(this.sliderOpacity);
        shapePane.add(GuiUtils.getHorizontalBoxLayoutPanel(2, this.sliderOpacity));
        shapePane.setBorder(GuiUtils.getTitledBorder(Messages.getString("closed.shape")));
        this.add(shapePane);
        this.add(GuiUtils.boxYLastElement(5));
        this.sliderOpacity.setValue((int)(MeasureTool.viewSetting.getFillOpacity() * 100.0f));
        PropertiesDialog.updateSlider(this.sliderOpacity, PropertiesDialog.FILL_OPACITY);
        this.sliderOpacity.addChangeListener(changeEvent -> PropertiesDialog.updateSlider(this.sliderOpacity, PropertiesDialog.FILL_OPACITY));
        this.getProperties().setProperty("show.restore", Boolean.TRUE.toString());
        this.getProperties().setProperty("help.item", "draw-measure/#preferences");
    }

    protected void initialize() {
        ViewSetting settings = MeasureTool.viewSetting;
        this.spinner.setValue(settings.getLineWidth());
        this.checkboxFilled.setSelected(settings.isFilled());
        int opacity = (int)(settings.getFillOpacity() * 100.0f);
        this.sliderOpacity.setValue(opacity);
        PropertiesDialog.updateSlider(this.sliderOpacity, PropertiesDialog.FILL_OPACITY);
    }

    @Override
    public void closeAdditionalWindow() {
        ViewSetting settings = MeasureTool.viewSetting;
        settings.setFilled(this.checkboxFilled.isSelected());
        settings.setFillOpacity((float)this.sliderOpacity.getValue() / 100.0f);
        MeasureTool.updateMeasureProperties();
    }

    @Override
    public void resetToDefaultValues() {
        ViewSetting settings = MeasureTool.viewSetting;
        settings.setLineWidth(Graphic.DEFAULT_LINE_THICKNESS.intValue());
        settings.setLineColor(Graphic.DEFAULT_COLOR);
        settings.setFilled(Graphic.DEFAULT_FILLED);
        settings.setFillOpacity(Graphic.DEFAULT_FILL_OPACITY.floatValue());
        this.initialize();
    }
}

