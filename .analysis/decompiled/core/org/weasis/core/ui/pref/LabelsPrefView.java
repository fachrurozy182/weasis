/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.pref;

import java.awt.GridLayout;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.util.FontItem;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.MeasureToolBar;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.utils.ImageStatistics;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.pref.ViewSetting;

public class LabelsPrefView
extends AbstractItemDialogPage {
    private final JPanel panelList = new JPanel();
    private final JComboBox<Graphic> comboBoxTool;
    private final JComboBox<FontItem> fontItemJComboBox;
    private final Map<JCheckBox, Measurement> map = HashMap.newHashMap((int)ImageStatistics.ALL_MEASUREMENTS.length);

    public LabelsPrefView() {
        super(MeasureTool.LABEL_PREF_NAME, 710);
        ArrayList<Graphic> tools = new ArrayList<Graphic>(MeasureToolBar.getMeasureGraphicList());
        tools.removeFirst();
        this.comboBoxTool = new JComboBox<Graphic>((Graphic[])tools.toArray(Graphic[]::new));
        this.fontItemJComboBox = new JComboBox<FontItem>(FontItem.values());
        this.jbInit();
        this.initialize();
    }

    private void jbInit() {
        JLabel jLabelSize = new JLabel(Messages.getString("LabelPrefView.size") + ":");
        JPanel panelFont = GuiUtils.getFlowLayoutPanel(2, 10, jLabelSize, this.fontItemJComboBox);
        panelFont.setBorder(GuiUtils.getTitledBorder(Messages.getString("LabelPrefView.font")));
        this.add(panelFont);
        JPanel panelShape = new JPanel();
        panelShape.setLayout(new BoxLayout(panelShape, 1));
        panelShape.setBorder(GuiUtils.getTitledBorder(Messages.getString("LabelsPrefView.geometric1")));
        JLabel shapeLabel = new JLabel(Messages.getString("LabelsPrefView.geometricshape") + ":");
        JPanel panelCombo = GuiUtils.getFlowLayoutPanel(2, 5, shapeLabel, this.comboBoxTool);
        panelShape.add(panelCombo);
        this.add(GuiUtils.boxVerticalStrut(15));
        this.panelList.setLayout(new GridLayout(0, 2));
        panelShape.add(this.panelList);
        ItemListener toolsListener = e -> {
            if (e.getStateChange() == 1) {
                this.selectTool((Graphic)e.getItem());
            }
        };
        this.comboBoxTool.setMaximumRowCount(12);
        this.comboBoxTool.addItemListener(toolsListener);
        this.add(panelShape);
        this.add(GuiUtils.boxVerticalStrut(15));
        JPanel panel = new JPanel();
        panel.setBorder(GuiUtils.getTitledBorder(Messages.getString("MeasureTool.pix_stats")));
        panel.setLayout(new GridLayout(0, 2));
        for (Measurement m : ImageStatistics.ALL_MEASUREMENTS) {
            JCheckBox box = new JCheckBox(m.getName(), (boolean)m.getGraphicLabel());
            panel.add(box);
            this.map.put(box, m);
            box.addActionListener(e -> {
                JCheckBox checkBox;
                Measurement measure;
                Object source = e.getSource();
                if (source instanceof JCheckBox && (measure = this.map.get(checkBox = (JCheckBox)source)) != null) {
                    measure.setGraphicLabel(((JCheckBox)source).isSelected());
                }
            });
        }
        this.add(panel);
        this.add(GuiUtils.boxYLastElement(5));
        this.getProperties().setProperty("show.apply", Boolean.TRUE.toString());
        this.getProperties().setProperty("show.restore", Boolean.TRUE.toString());
        this.getProperties().setProperty("help.item", "draw-measure/#preferences");
    }

    private void selectTool(Graphic graph) {
        if (graph != null) {
            this.panelList.removeAll();
            List<Measurement> list = graph.getMeasurementList();
            if (list != null) {
                for (Measurement m : list) {
                    JCheckBox box = new JCheckBox(m.getName(), (boolean)m.getGraphicLabel());
                    box.addActionListener(e -> {
                        Object source = e.getSource();
                        if (source instanceof JCheckBox) {
                            JCheckBox checkBox = (JCheckBox)source;
                            m.setGraphicLabel(checkBox.isSelected());
                        }
                    });
                    this.panelList.add(box);
                }
            }
            this.panelList.revalidate();
            this.panelList.repaint();
        }
    }

    private void initialize() {
        FontItem item = MeasureTool.viewSetting.getFontItem();
        this.fontItemJComboBox.setSelectedItem((Object)(item == null ? ViewSetting.DEFAULT_FONT : item));
        this.selectTool((Graphic)this.comboBoxTool.getSelectedItem());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void closeAdditionalWindow() {
        List<ViewerPlugin<?>> viewerPlugins;
        ViewSetting settings = MeasureTool.viewSetting;
        settings.setFontItem((FontItem)((Object)Objects.requireNonNull(this.fontItemJComboBox.getSelectedItem())));
        List<ViewerPlugin<?>> list = viewerPlugins = GuiUtils.getUICore().getViewerPlugins();
        synchronized (list) {
            for (int i = viewerPlugins.size() - 1; i >= 0; --i) {
                ViewerPlugin<?> p = viewerPlugins.get(i);
                if (!(p instanceof ImageViewerPlugin)) continue;
                ImageViewerPlugin viewerPlugin = (ImageViewerPlugin)p;
                for (ViewCanvas v : viewerPlugin.getImagePanels()) {
                    if (!(v instanceof ViewCanvas)) continue;
                    ViewCanvas view = v;
                    GraphicModel graphicList = view.getGraphicManager();
                    graphicList.updateLabels(true, view);
                }
            }
        }
    }

    @Override
    public void resetToDefaultValues() {
        MeasureTool.viewSetting.setFontItem(ViewSetting.DEFAULT_FONT);
        this.initialize();
        MeasureToolBar.getMeasureGraphicList().forEach(g -> {
            List<Measurement> list = g.getMeasurementList();
            Optional.ofNullable(list).ifPresent(l -> l.forEach(Measurement::resetToGraphicLabelValue));
        });
        this.selectTool((Graphic)this.comboBoxTool.getSelectedItem());
        Arrays.stream(ImageStatistics.ALL_MEASUREMENTS).forEach(Measurement::resetToGraphicLabelValue);
        this.map.forEach((key, value) -> key.setSelected(value.getGraphicLabel()));
    }
}

