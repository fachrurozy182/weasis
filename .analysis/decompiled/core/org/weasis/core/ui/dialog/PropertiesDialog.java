/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.DecFormatter;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.JSliderW;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.api.util.FontItem;
import org.weasis.core.api.util.ResourceUtil;

public abstract class PropertiesDialog
extends JDialog {
    public static final String FILL_OPACITY = Messages.getString("fill.opacity");
    protected Color color;
    private final JPanel panel1 = new JPanel();
    private final JButton jButtonOk = new JButton();
    private final JButton jButtonCancel = new JButton();
    protected final JSpinner spinnerLineWidth = new JSpinner();
    protected final JLabel jLabelLineWidth = new JLabel();
    protected final JLabel jLabelLineColor = new JLabel();
    protected final JButton jButtonColor = new JButton((Icon)ResourceUtil.getIcon(ResourceUtil.ActionIcon.PIPETTE));
    protected final JCheckBox jCheckBoxFilled = new JCheckBox();
    protected final JSliderW slicerOpacity = PropertiesDialog.createOpacitySlider(FILL_OPACITY);
    protected final JButton overrideMultipleValues = new JButton(Messages.getString("PropertiesDialog.header_override"));
    protected final JCheckBox checkBoxColor = new JCheckBox();
    protected final JCheckBox checkBoxFillOpacity = new JCheckBox();
    protected final JCheckBox checkBoxWidth = new JCheckBox();
    protected final JCheckBox checkBoxFill = new JCheckBox();
    protected final JPanel panelColor = GuiUtils.getFlowLayoutPanel(4, 2, 5, new JComponent[0]);
    protected final JPanel panelLine = GuiUtils.getFlowLayoutPanel(4, 2, 5, new JComponent[0]);
    protected final JPanel panelFilled = GuiUtils.getFlowLayoutPanel(4, 2, 5, new JComponent[0]);
    protected final JPanel panelOpacity = GuiUtils.getFlowLayoutPanel(4, 2, 5, new JComponent[0]);

    protected PropertiesDialog(Window parent, String title) {
        super(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        this.init();
    }

    private void init() {
        this.panel1.setBorder(GuiUtils.getEmptyBorder(10, 15, 0, 15));
        this.panel1.setLayout(new BorderLayout());
        this.jButtonOk.setText(Messages.getString("PropertiesDialog.ok"));
        this.jButtonOk.addActionListener(e -> this.okAction());
        this.jButtonCancel.setText(Messages.getString("PropertiesDialog.cancel"));
        this.jButtonCancel.addActionListener(e -> this.quitWithoutSaving());
        GuiUtils.setNumberModel(this.spinnerLineWidth, 1, 1, 8, 1);
        this.jLabelLineWidth.setText(Messages.getString("PropertiesDialog.line_width") + ":");
        this.jLabelLineColor.setText(Messages.getString("PropertiesDialog.line_color") + ":");
        this.jButtonColor.setToolTipText(Messages.getString("MeasureTool.pick"));
        this.jButtonColor.addActionListener(e -> this.openColorChooser((JButton)e.getSource()));
        this.jCheckBoxFilled.setText(Messages.getString("PropertiesDialog.fill_shape"));
        this.slicerOpacity.addChangeListener(changeEvent -> PropertiesDialog.updateSlider(this.slicerOpacity, FILL_OPACITY));
        this.getContentPane().add(this.panel1);
        JPanel jPanelFooter = GuiUtils.getFlowLayoutPanel(4, 15, 15, this.jButtonOk, this.jButtonCancel);
        this.panel1.add((Component)jPanelFooter, "South");
        this.overrideMultipleValues.setEnabled(false);
        this.checkBoxColor.addActionListener(e -> {
            JCheckBox box = (JCheckBox)e.getSource();
            this.jLabelLineColor.setEnabled(box.isSelected());
            this.jButtonColor.setEnabled(box.isSelected());
        });
        this.checkBoxWidth.addActionListener(e -> {
            JCheckBox box = (JCheckBox)e.getSource();
            this.jLabelLineWidth.setEnabled(box.isSelected());
            this.spinnerLineWidth.setEnabled(box.isSelected());
        });
        this.checkBoxFill.addActionListener(e -> {
            JCheckBox box = (JCheckBox)e.getSource();
            this.jCheckBoxFilled.setEnabled(box.isSelected());
        });
        this.checkBoxFillOpacity.addActionListener(e -> {
            JCheckBox box = (JCheckBox)e.getSource();
            this.slicerOpacity.setEnabled(box.isSelected());
        });
        JPanel centerPanel = GuiUtils.getVerticalBoxLayoutPanel(new JComponent[0]);
        centerPanel.add(GuiUtils.getFlowLayoutPanel(4, 2, 5, this.overrideMultipleValues));
        this.panelColor.add(this.jLabelLineColor);
        this.panelColor.add(this.jButtonColor);
        this.panelLine.add(this.jLabelLineWidth);
        this.panelLine.add(this.spinnerLineWidth);
        this.panelFilled.add(this.jCheckBoxFilled);
        this.panelOpacity.add(this.slicerOpacity);
        centerPanel.add(this.panelColor);
        centerPanel.add(this.panelLine);
        centerPanel.add(this.panelFilled);
        centerPanel.add(this.panelOpacity);
        centerPanel.add(GuiUtils.boxVerticalStrut(10));
        this.panel1.add((Component)centerPanel, "Center");
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == 201) {
            this.quitWithoutSaving();
        }
        super.processWindowEvent(e);
    }

    public static float updateSlider(JSliderW slider, String titlePrefix) {
        float value = (float)slider.getValue() / 100.0f;
        SliderChangeListener.updateSliderProperties(slider, PropertiesDialog.getOpacityTitle(titlePrefix, value));
        return value;
    }

    private static String getOpacityTitle(String titlePrefix, float value) {
        return titlePrefix + ": " + DecFormatter.percentTwoDecimal(Float.valueOf(value));
    }

    public static JSliderW createOpacitySlider(String titlePrefix) {
        DefaultBoundedRangeModel model = new DefaultBoundedRangeModel(100, 0, 0, 100);
        TitledBorder titledBorder = new TitledBorder(BorderFactory.createEmptyBorder(), PropertiesDialog.getOpacityTitle(titlePrefix, (float)model.getValue() / 100.0f), 4, 0, FontItem.MEDIUM.getFont(), null);
        JSliderW s = new JSliderW(model);
        s.setDisplayValueInTitle(true);
        s.setBorder(titledBorder);
        return s;
    }

    protected abstract void okAction();

    protected void quitWithoutSaving() {
        this.dispose();
    }

    public void openColorChooser(JButton button) {
        Color newColor;
        if (button != null && (newColor = JColorChooser.showDialog(button, Messages.getString("MeasureTool.pick_color"), this.color)) != null) {
            this.color = newColor;
        }
    }
}

