/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.SystemFileChooser
 */
package org.weasis.core.ui.editor.image;

import com.formdev.flatlaf.util.SystemFileChooser;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.image.util.WindLevelParameters;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.DisplayByteLut;
import org.weasis.core.ui.editor.image.HistogramData;
import org.weasis.core.ui.editor.image.HistogramPanel;

public class ChannelHistogramPanel
extends JPanel {
    private final BorderLayout borderLayout1 = new BorderLayout();
    private final HistogramPanel jPanelHistogram;
    private final JButton jButtonHistoMinus;
    private final JButton jButtonHistoPlus;
    private final JCheckBox jCheckAccumulate;
    private final JCheckBox jCheckLogarithmic;
    private final JCheckBox jCheckShowIntensity;
    private final JButton jButtonReset = new JButton(Messages.getString("ChannelHistogramPanel.reset"));
    private final JPanel panel = new JPanel();

    public ChannelHistogramPanel(String name) {
        this(name, false, false, true);
    }

    public ChannelHistogramPanel(String name, boolean accumulate, boolean logarithmic, boolean showIntensity) {
        this.jCheckAccumulate = new JCheckBox(Messages.getString("ChannelHistogramPanel.accu"), accumulate);
        this.jCheckLogarithmic = new JCheckBox(Messages.getString("ChannelHistogramPanel.log"), logarithmic);
        this.jCheckShowIntensity = new JCheckBox(Messages.getString("ChannelHistogramPanel.ShowIntensity"), showIntensity);
        this.jPanelHistogram = new HistogramPanel();
        this.jButtonHistoMinus = new JButton((Icon)ResourceUtil.getIcon(ResourceUtil.ActionIcon.MINUS));
        this.jButtonHistoPlus = new JButton((Icon)ResourceUtil.getIcon(ResourceUtil.ActionIcon.PLUS));
        this.init(name);
    }

    private void init(String name) {
        this.setBorder(BorderFactory.createCompoundBorder(GuiUtils.getEmptyBorder(10, 5, 0, 5), GuiUtils.getTitledBorder(name)));
        this.setLayout(this.borderLayout1);
        this.add((Component)this.jPanelHistogram, "Center");
        this.add((Component)this.panel, "South");
        this.panel.setLayout(new FlowLayout(3));
        this.panel.add(this.jButtonHistoMinus);
        this.jButtonHistoMinus.setToolTipText(Messages.getString("ChannelHistogramPanel.shrink"));
        this.panel.add(this.jButtonHistoPlus);
        this.jButtonHistoPlus.setToolTipText(Messages.getString("ChannelHistogramPanel.strech"));
        this.panel.add(Box.createHorizontalStrut(GuiUtils.getScaleLength(15)));
        this.panel.add(this.jCheckAccumulate);
        this.jCheckAccumulate.addActionListener(e -> this.jPanelHistogram.setAccumulate(this.jCheckAccumulate.isSelected()));
        this.panel.add(this.jCheckLogarithmic);
        this.jCheckLogarithmic.addActionListener(e -> this.jPanelHistogram.setLogarithmic(this.jCheckLogarithmic.isSelected()));
        this.panel.add(this.jCheckShowIntensity);
        this.jCheckShowIntensity.addActionListener(e -> this.jPanelHistogram.setShowIntensity(this.jCheckShowIntensity.isSelected()));
        this.jButtonHistoPlus.addActionListener(e -> this.jPanelHistogram.updateZoom(true));
        this.jButtonHistoMinus.addActionListener(e -> this.jPanelHistogram.updateZoom(false));
        this.panel.add(this.jButtonReset);
        this.jButtonReset.addActionListener(e -> this.reset());
    }

    public boolean isAccumulate() {
        return this.jCheckAccumulate.isSelected();
    }

    public boolean isLogarithmic() {
        return this.jCheckLogarithmic.isSelected();
    }

    public boolean isShowIntensity() {
        return this.jCheckShowIntensity.isSelected();
    }

    public HistogramData getData() {
        return this.jPanelHistogram.getData();
    }

    private void save() {
        SystemFileChooser saveFC = new SystemFileChooser();
        int ret = saveFC.showSaveDialog((Component)this);
        if (ret == 0) {
            this.jPanelHistogram.saveHistogramInCSV(saveFC.getSelectedFile());
        }
    }

    private void reset() {
        this.jPanelHistogram.resetDisplay();
        this.jCheckLogarithmic.setSelected(this.jPanelHistogram.isLogarithmic());
        this.jCheckShowIntensity.setSelected(this.jPanelHistogram.isShowIntensity());
        this.jCheckAccumulate.setSelected(this.jPanelHistogram.isAccumulate());
    }

    public void setHistogramBins(HistogramData data) {
        this.jPanelHistogram.setHistogram(data, this.jCheckAccumulate.isSelected(), this.jCheckLogarithmic.isSelected(), this.jCheckShowIntensity.isSelected());
    }

    public void setWindLevelParameters(WindLevelParameters p) {
        this.jPanelHistogram.setWindLevelParameters(p);
        this.jPanelHistogram.repaint();
    }

    public void setLut(DisplayByteLut lut) {
        this.jPanelHistogram.getData().setLut(lut);
        this.jPanelHistogram.repaint();
    }
}

