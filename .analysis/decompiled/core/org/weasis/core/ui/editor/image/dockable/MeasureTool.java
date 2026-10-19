/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  bibliothek.gui.dock.common.CLocation
 */
package org.weasis.core.ui.editor.image.dockable;

import bibliothek.gui.dock.common.CLocation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.JToggleButtonGroup;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.Unit;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.util.FontItem;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.docking.PluginTool;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.MeasureToolBar;
import org.weasis.core.ui.editor.image.MouseActions;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.ViewerPlugin;
import org.weasis.core.ui.editor.image.ViewerToolBar;
import org.weasis.core.ui.model.graphic.DragGraphic;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.GraphicSelectionListener;
import org.weasis.core.ui.model.graphic.imp.AnnotationGraphic;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.model.utils.ImageStatistics;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.model.utils.bean.Measurement;
import org.weasis.core.ui.pref.PreferenceDialog;
import org.weasis.core.ui.pref.ViewSetting;
import org.weasis.core.ui.util.ColorLayerUI;
import org.weasis.core.ui.util.SimpleTableModel;
import org.weasis.core.ui.util.TableColumnAdjuster;
import org.weasis.core.ui.util.TableNumberRenderer;

public class MeasureTool
extends PluginTool
implements GraphicSelectionListener {
    public static final String BUTTON_NAME = String.valueOf(ActionW.DRAW) + " & " + String.valueOf(ActionW.MEASURE);
    public static final String LABEL_PREF_NAME = Messages.getString("MeasureTool.lab_img");
    public static final ViewSetting viewSetting = new ViewSetting();
    protected final ImageViewerEventManager<? extends ImageElement> eventManager;
    private final JScrollPane rootPane;
    private JPanel tableContainer;
    private JTable jtable;
    private List<DragGraphic> selectedGraphic;

    public MeasureTool(ImageViewerEventManager<? extends ImageElement> eventManager) {
        super(BUTTON_NAME, Insertable.Type.TOOL, 30);
        this.eventManager = eventManager;
        this.rootPane = new JScrollPane();
        this.dockable.setTitleIcon((Icon)ResourceUtil.getIcon(ResourceUtil.ActionIcon.MEASURE));
        this.setDockableWidth(220);
        this.jbInit();
    }

    private void jbInit() {
        this.rootPane.setBorder(BorderFactory.createEmptyBorder());
        this.setLayout(new BoxLayout(this, 1));
        this.add(this.getIconsPanel());
        this.add(this.getSelectedMeasurePanel());
    }

    public final JPanel getIconsPanel() {
        JPanel transform = new JPanel();
        transform.setLayout(new BoxLayout(transform, 1));
        transform.setBorder(GuiUtils.getEmptyBorder(5, 5, 5, 5));
        MeasureTool.buildIconPanel(transform, this.eventManager, ActionW.MEASURE, ActionW.DRAW_MEASURE, 5);
        MeasureTool.buildIconPanel(transform, this.eventManager, ActionW.DRAW, ActionW.DRAW_GRAPHICS, 5);
        transform.add(Box.createVerticalStrut(5));
        JLabel label = new JLabel(Messages.getString("MeasureToolBar.line") + ":");
        JButton button = MeasureTool.buildLineColorButton(this);
        JSpinner spinner = new JSpinner();
        viewSetting.initLineWidthSpinner(spinner);
        transform.add(GuiUtils.getFlowLayoutPanel(label, button, spinner));
        this.eventManager.getAction(ActionW.DRAW_ONLY_ONCE).ifPresent(b -> {
            JCheckBox checkDraw = b.createCheckBox(ActionW.DRAW_ONLY_ONCE.getTitle());
            checkDraw.setSelected(viewSetting.isDrawOnlyOnce());
            transform.add(GuiUtils.getFlowLayoutPanel(checkDraw));
        });
        JCheckBox checkboxBasicImageStatistics = new JCheckBox(Messages.getString("MeasureTool.pix_stats"), viewSetting.isBasicStatistics());
        checkboxBasicImageStatistics.addActionListener(e -> {
            List<ViewerPlugin<?>> viewerPlugins;
            JCheckBox box = (JCheckBox)e.getSource();
            boolean sel = box.isSelected();
            viewSetting.setBasicStatistics(sel);
            viewSetting.setMoreStatistics(sel);
            for (Measurement m : ImageStatistics.ALL_MEASUREMENTS) {
                m.setComputed(sel);
            }
            List<ViewerPlugin<?>> list = viewerPlugins = GuiUtils.getUICore().getViewerPlugins();
            synchronized (list) {
                for (int i = viewerPlugins.size() - 1; i >= 0; --i) {
                    ViewerPlugin<?> p = viewerPlugins.get(i);
                    if (!(p instanceof ImageViewerPlugin)) continue;
                    for (ViewCanvas view : ((ImageViewerPlugin)p).getImagePanels()) {
                        if (view == null) continue;
                        view.getGraphicManager().updateLabels(true, view);
                    }
                }
            }
        });
        transform.add(GuiUtils.getFlowLayoutPanel(checkboxBasicImageStatistics));
        this.eventManager.getAction(ActionW.SPATIAL_UNIT).ifPresent(b -> {
            JLabel labelUnit = new JLabel(Messages.getString("MeasureTool.unit") + ":");
            JComboBox unitComboBox = b.createCombo(120);
            unitComboBox.setSelectedItem((Object)Unit.PIXEL);
            transform.add(GuiUtils.getFlowLayoutPanel(labelUnit, unitComboBox));
        });
        JButton btnGeneralOptions = new JButton(Messages.getString("MeasureTool.more_options"));
        btnGeneralOptions.addActionListener(e -> {
            Window win = SwingUtilities.getWindowAncestor(this);
            ColorLayerUI layer = ColorLayerUI.createTransparentLayerUI(win.getParent());
            PreferenceDialog dialog = new PreferenceDialog(win);
            dialog.showPage(BUTTON_NAME);
            ColorLayerUI.showCenterScreen(dialog, layer);
        });
        transform.add(GuiUtils.getFlowLayoutPanel(btnGeneralOptions));
        return transform;
    }

    public static void updateMeasureProperties() {
        MeasureToolBar.getMeasureGraphicList().forEach(g -> MeasureToolBar.applyDefaultSetting(viewSetting, g));
        MeasureToolBar.getDrawGraphicList().forEach(g -> MeasureToolBar.applyDefaultSetting(viewSetting, g));
    }

    public JPanel getSelectedMeasurePanel() {
        this.jtable = MeasureTool.createMultipleRenderingTable(new SimpleTableModel(new String[0], new Object[0][]));
        this.jtable.setFont(FontItem.MINI.getFont());
        this.jtable.getTableHeader().setReorderingAllowed(false);
        this.tableContainer = new JPanel(new BorderLayout());
        this.tableContainer.setPreferredSize(GuiUtils.getDimension(50, 50));
        this.tableContainer.setBorder(BorderFactory.createCompoundBorder(GuiUtils.getEmptyBorder(10, 3, 0, 3), GuiUtils.getTitledBorder(Messages.getString("MeasureTool.sel"))));
        return this.tableContainer;
    }

    @Override
    public Component getToolComponent() {
        return this.getToolComponentFromJScrollPane(this.rootPane);
    }

    @Override
    protected void changeToolWindowAnchor(CLocation clocation) {
    }

    public static JButton buildLineColorButton(Component c) {
        JButton button = new JButton((Icon)ResourceUtil.getIcon(ResourceUtil.ActionIcon.PIPETTE));
        button.setToolTipText(Messages.getString("MeasureTool.pick"));
        button.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(WinUtil.getValidComponent(c), Messages.getString("MeasureTool.pick_color"), viewSetting.getLineColor());
            if (newColor != null) {
                viewSetting.setLineColor(newColor);
                MeasureTool.updateMeasureProperties();
            }
        });
        return button;
    }

    public static JTable createMultipleRenderingTable(TableModel model) {
        JTable table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.getColumnModel().setColumnMargin(GuiUtils.getScaleLength(3));
        return table;
    }

    public void setSelectedGraphic(Graphic graph, MeasurableLayer layer) {
        List<MeasureItem> measList = null;
        if (graph != null && layer != null && graph.getLayerType() == LayerType.MEASURE) {
            Unit unit = this.eventManager.getAction(ActionW.SPATIAL_UNIT).map(c -> (Unit)((Object)((Object)c.getSelectedItem()))).orElse(null);
            measList = graph.computeMeasurements(layer, true, unit);
        }
        this.updateMeasuredItems(measList);
    }

    @Override
    public void updateMeasuredItems(List<MeasureItem> measList) {
        this.tableContainer.removeAll();
        if (measList != null) {
            String[] headers = new String[]{Messages.getString("MeasureTool.param"), Messages.getString("MeasureTool.val")};
            this.jtable.setModel(new SimpleTableModel(headers, MeasureTool.getLabels(measList)));
            this.jtable.getColumnModel().getColumn(1).setCellRenderer(new TableNumberRenderer());
            TableColumnAdjuster.adjustPreferredSizeForViewPort(this.jtable, this.tableContainer);
        } else {
            this.tableContainer.setPreferredSize(GuiUtils.getDimension(50, 50));
        }
        this.tableContainer.revalidate();
        this.tableContainer.repaint();
    }

    public static Object[][] getLabels(List<MeasureItem> measList) {
        if (measList != null) {
            Object[][] labels = new Object[measList.size()][];
            for (int i = 0; i < labels.length; ++i) {
                MeasureItem m = measList.get(i);
                Object[] row = new Object[2];
                StringBuilder buffer = new StringBuilder(m.getMeasurement().getName());
                if (m.getLabelExtension() != null) {
                    buffer.append(m.getLabelExtension());
                }
                if (m.getUnit() != null) {
                    buffer.append(" [");
                    buffer.append(m.getUnit());
                    buffer.append("]");
                }
                row[0] = buffer.toString();
                row[1] = m.getValue();
                labels[i] = row;
            }
            return labels;
        }
        return null;
    }

    public static int getNumberOfMeasures(boolean[] select) {
        int k = 0;
        for (boolean b : select) {
            if (!b) continue;
            ++k;
        }
        return k;
    }

    @Override
    public void handle(List<Graphic> selectedGraphicList, MeasurableLayer layer) {
        Graphic g = null;
        ArrayList<DragGraphic> list = null;
        if (selectedGraphicList != null) {
            if (selectedGraphicList.size() == 1) {
                g = selectedGraphicList.get(0);
            }
            list = new ArrayList<DragGraphic>();
            for (Graphic graphic : selectedGraphicList) {
                if (!(graphic instanceof DragGraphic)) continue;
                DragGraphic dragGraphic = (DragGraphic)graphic;
                list.add(dragGraphic);
            }
        }
        boolean computeAllMeasures = true;
        if (this.selectedGraphic != null) {
            if (g != null && this.selectedGraphic.size() == 1 && g == this.selectedGraphic.get(0) && !(g instanceof AnnotationGraphic)) {
                computeAllMeasures = false;
            }
            this.selectedGraphic.clear();
        }
        this.selectedGraphic = list;
        if (computeAllMeasures) {
            this.setSelectedGraphic(g, layer);
        }
    }

    public static void buildIconPanel(JPanel rootPanel, ImageViewerEventManager<?> eventManager, Feature<?> action, Feature.ComboItemListenerValue<Graphic> graphicAction, int lineLength) {
        Optional<Graphic> actionState = eventManager.getAction(graphicAction);
        if (actionState.isEmpty()) {
            return;
        }
        JPanel pIcons = new JPanel();
        pIcons.setBorder(BorderFactory.createCompoundBorder(GuiUtils.getEmptyBorder(10, 5, 0, 5), GuiUtils.getTitledBorder(graphicAction.getTitle())));
        JToggleButtonGroup measures = ((ComboItemListener)((Object)actionState.get())).createButtonGroup();
        JToggleButton[] items = measures.getJToggleButtonList();
        pIcons.setLayout(new GridLayout(0, lineLength));
        for (JToggleButton item : items) {
            item.addActionListener(e -> {
                String cmd;
                ViewerToolBar toolBar;
                ImageViewerPlugin view = eventManager.getSelectedView2dContainer();
                if (view != null && (toolBar = view.getViewerToolBar()) != null && !toolBar.isCommandActive(cmd = action.cmd())) {
                    MouseActions mouseActions = eventManager.getMouseActions();
                    mouseActions.setAction("left", cmd);
                    view.setMouseActions(mouseActions);
                    toolBar.changeButtonState("left", cmd);
                }
            });
            pIcons.add(item);
        }
        rootPanel.add(GuiUtils.getFlowLayoutPanel(3, 0, 0, pIcons));
    }
}

