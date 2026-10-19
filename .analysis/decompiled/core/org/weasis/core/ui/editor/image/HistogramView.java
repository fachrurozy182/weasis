/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Mat
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.lut.ByteLut
 *  org.weasis.opencv.op.lut.WlParams
 */
package org.weasis.core.ui.editor.image;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.image.ImageOpNode;
import org.weasis.core.api.image.OpManager;
import org.weasis.core.api.image.PseudoColorOp;
import org.weasis.core.api.image.WindowOp;
import org.weasis.core.api.image.util.MeasurableLayer;
import org.weasis.core.api.image.util.WindLevelParameters;
import org.weasis.core.ui.editor.SeriesViewer;
import org.weasis.core.ui.editor.SeriesViewerEvent;
import org.weasis.core.ui.editor.SeriesViewerListener;
import org.weasis.core.ui.editor.image.ChannelHistogramPanel;
import org.weasis.core.ui.editor.image.DisplayByteLut;
import org.weasis.core.ui.editor.image.HistogramData;
import org.weasis.core.ui.editor.image.ImageRegionStatistics;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.ViewCanvas;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.model.graphic.AbstractDragGraphicArea;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.model.graphic.GraphicSelectionListener;
import org.weasis.core.ui.model.graphic.imp.area.SelectGraphic;
import org.weasis.core.ui.model.utils.bean.MeasureItem;
import org.weasis.core.ui.util.SimpleTableModel;
import org.weasis.core.ui.util.TableNumberRenderer;
import org.weasis.core.util.StringUtil;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.lut.ByteLut;
import org.weasis.opencv.op.lut.WlParams;

public class HistogramView
extends JComponent
implements SeriesViewerListener,
GraphicSelectionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(HistogramView.class);
    private final JPanel view = new JPanel();
    private final JPanel histView = new JPanel();
    private final SeriesViewer<?> viewer;
    private final JSpinner spinnerBins = new JSpinner(new SpinnerNumberModel(256, 64, 4096, 8));
    private ViewCanvas<?> view2DPane;
    private final ItemListener modelListener = e -> {
        if (e.getStateChange() == 1) {
            this.buildHistogram();
            this.repaint();
        }
    };
    private final JComboBox<HistogramData.Model> jComboBoxImgChannel = new JComboBox();
    private AbstractDragGraphicArea selectedGraphic;

    public HistogramView(SeriesViewer<?> viewer) {
        this.viewer = viewer;
        this.setLayout(new BorderLayout());
        this.view.setLayout(new BorderLayout());
        this.add((Component)this.view, "Center");
        this.setPreferredSize(GuiUtils.getDimension(400, 300));
        this.setMinimumSize(GuiUtils.getDimension(150, 50));
    }

    @Override
    public void changingViewContentEvent(SeriesViewerEvent event) {
        block5: {
            SeriesViewerEvent.EVENT type;
            block7: {
                block6: {
                    type = event.getEventType();
                    if (event.getSeriesViewer() != this.viewer) break block5;
                    if (!SeriesViewerEvent.EVENT.SELECT.equals((Object)type) && !SeriesViewerEvent.EVENT.LAYOUT.equals((Object)type)) break block6;
                    this.updatePane(event);
                    break block5;
                }
                if (!SeriesViewerEvent.EVENT.WIN_LEVEL.equals((Object)type)) break block7;
                if (this.view2DPane == null) {
                    this.updatePane(event);
                } else {
                    WindLevelParameters p = this.getWinLeveParameters();
                    for (int i = 0; i < this.histView.getComponentCount(); ++i) {
                        Component c = this.histView.getComponent(i);
                        if (!(c instanceof ChannelHistogramPanel)) continue;
                        ChannelHistogramPanel histogramPanel = (ChannelHistogramPanel)c;
                        histogramPanel.setWindLevelParameters(p);
                        histogramPanel.getData().updateVoiLut(this.view2DPane);
                    }
                }
                break block5;
            }
            if (!SeriesViewerEvent.EVENT.LUT.equals((Object)type)) break block5;
            WindLevelParameters p = this.getWinLeveParameters();
            if (p == null) {
                return;
            }
            PlanarImage imageSource = this.view2DPane.getSourceImage();
            int channels = imageSource.channels();
            HistogramData.Model colorModel = this.getSelectedColorModel(channels);
            DisplayByteLut[] lut = this.getLut(p, colorModel);
            for (int i = 0; i < this.histView.getComponentCount(); ++i) {
                Component c = this.histView.getComponent(i);
                if (!(c instanceof ChannelHistogramPanel)) continue;
                ChannelHistogramPanel histogramPanel = (ChannelHistogramPanel)c;
                histogramPanel.setLut(lut[i]);
            }
        }
    }

    private void updatePane(SeriesViewerEvent event) {
        if (event.getSeriesViewer() instanceof ImageViewerPlugin) {
            this.view2DPane = ((ImageViewerPlugin)event.getSeriesViewer()).getSelectedViewCanvas();
            this.displayHistogram();
        } else {
            this.view2DPane = null;
        }
    }

    private void displayHistogram() {
        this.buildLayout();
        this.buildHistogram();
        this.repaint();
    }

    private void buildLayout() {
        this.view.removeAll();
        if (this.view2DPane != null && this.view2DPane.getSourceImage() != null) {
            int i;
            PlanarImage imageSource = this.view2DPane.getSourceImage();
            int channels = imageSource.channels();
            this.histView.setLayout(new BoxLayout(this.histView, 1));
            JPanel headerPanel = new JPanel();
            headerPanel.setLayout(new BoxLayout(headerPanel, 1));
            headerPanel.setBorder(BorderFactory.createCompoundBorder(GuiUtils.getEmptyBorder(10, 7, 7, 7), GuiUtils.getTitledBorder(Messages.getString("HistogramView.histoParams"))));
            JPanel row1 = new JPanel();
            row1.add(new JLabel(Messages.getString("HistogramView.channel") + ":"));
            this.jComboBoxImgChannel.removeItemListener(this.modelListener);
            HistogramData.Model oldColorModel = (HistogramData.Model)((Object)this.jComboBoxImgChannel.getSelectedItem());
            int oldModelNumber = this.jComboBoxImgChannel.getItemCount();
            this.jComboBoxImgChannel.removeAllItems();
            HistogramData.Model[] vals = HistogramData.Model.values();
            int limit = channels > 1 ? (imageSource.depth() > 1 ? 2 : vals.length) : 1;
            int n = i = channels > 1 ? 1 : 0;
            while (i < limit) {
                this.jComboBoxImgChannel.addItem(vals[i]);
                ++i;
            }
            if (channels > 1) {
                this.jComboBoxImgChannel.addItem(vals[0]);
            }
            if (oldColorModel != null && oldModelNumber == this.jComboBoxImgChannel.getItemCount()) {
                this.jComboBoxImgChannel.setSelectedItem((Object)oldColorModel);
            }
            this.jComboBoxImgChannel.addItemListener(this.modelListener);
            row1.add(this.jComboBoxImgChannel);
            headerPanel.add(row1);
            JPanel row2 = new JPanel();
            row2.add(new JLabel(Messages.getString("HistogramView.bins") + ":"));
            GuiUtils.formatCheckAction(this.spinnerBins);
            row2.add(this.spinnerBins);
            this.spinnerBins.addChangeListener(e -> this.buildHistogram());
            row2.add(GuiUtils.boxHorizontalStrut(15));
            JButton stats = new JButton(Messages.getString("HistogramView.stats"));
            stats.addActionListener(e -> this.showStatistics());
            row2.add(stats);
            headerPanel.add(row2);
            this.view.add((Component)headerPanel, "North");
            this.view.add((Component)this.histView, "Center");
        }
    }

    private void showStatistics() {
        if (this.view2DPane == null || this.histView.getComponentCount() == 0) {
            return;
        }
        ChannelHistogramPanel[] hist = new ChannelHistogramPanel[this.histView.getComponentCount()];
        for (int i = 0; i < hist.length; ++i) {
            ChannelHistogramPanel panel;
            Component c = this.histView.getComponent(i);
            if (!(c instanceof ChannelHistogramPanel)) continue;
            hist[i] = panel = (ChannelHistogramPanel)c;
        }
        if (hist.length == 0) {
            return;
        }
        ArrayList<MeasureItem> measList = new ArrayList<MeasureItem>();
        for (int i = 0; i < hist.length; ++i) {
            Integer channel = hist.length == 1 ? null : Integer.valueOf(i);
            measList.addAll(ImageRegionStatistics.getStatistics(hist[i].getData(), channel, i == 0));
        }
        JPanel tableContainer = HistogramView.buildStatisticsTable(measList);
        JOptionPane.showMessageDialog(this.spinnerBins, tableContainer, Messages.getString("HistogramView.stats"), -1, null);
    }

    public static JPanel buildStatisticsTable(List<MeasureItem> measList) {
        JPanel tableContainer = new JPanel();
        tableContainer.setLayout(new BorderLayout());
        JTable jtable = MeasureTool.createMultipleRenderingTable(new SimpleTableModel(new String[0], new Object[0][]));
        jtable.getTableHeader().setReorderingAllowed(false);
        String[] headers = new String[]{Messages.getString("MeasureTool.param"), Messages.getString("MeasureTool.val")};
        jtable.setModel(new SimpleTableModel(headers, MeasureTool.getLabels(measList)));
        jtable.getColumnModel().getColumn(1).setCellRenderer(new TableNumberRenderer());
        tableContainer.add((Component)jtable.getTableHeader(), "First");
        tableContainer.add((Component)jtable, "Center");
        jtable.setShowVerticalLines(true);
        jtable.getColumnModel().getColumn(0).setPreferredWidth(120);
        jtable.getColumnModel().getColumn(1).setPreferredWidth(80);
        return tableContainer;
    }

    private WindLevelParameters getWinLeveParameters() {
        ImageOpNode imageOpNode;
        OpManager dispOp;
        Optional<ImageOpNode> wlOp;
        if (this.view2DPane != null && (wlOp = (dispOp = this.view2DPane.getDisplayOpManager()).getNode(WindowOp.OP_NAME)).isPresent() && (imageOpNode = wlOp.get()) instanceof WindowOp) {
            WindowOp windowOp = (WindowOp)imageOpNode;
            return windowOp.getWindLevelParameters();
        }
        return null;
    }

    private DisplayByteLut[] getLut(WlParams p, HistogramData.Model colorModel) {
        int channels;
        DisplayByteLut[] lut = null;
        if (this.view2DPane != null && (channels = this.view2DPane.getSourceImage().channels()) == 1) {
            DisplayByteLut disLut = HistogramView.buildDisplayByteLut(this.view2DPane);
            if (disLut == null) {
                disLut = new DisplayByteLut(HistogramData.Model.GRAY.getByteLut()[0]);
            }
            disLut.setInvert(p.isInverseLut());
            lut = new DisplayByteLut[]{disLut};
        }
        if (lut == null) {
            ByteLut[] l = colorModel.getByteLut();
            lut = new DisplayByteLut[l.length];
            for (int i = 0; i < l.length; ++i) {
                lut[i] = new DisplayByteLut(l[i]);
            }
        }
        return lut;
    }

    public static DisplayByteLut buildDisplayByteLut(ViewCanvas<?> view2DPane) {
        PseudoColorOp pseudo;
        ByteLut lutTable;
        ImageOpNode imageOpNode;
        DisplayByteLut disLut = null;
        OpManager dispOp = view2DPane.getDisplayOpManager();
        Optional<ImageOpNode> lutOp = dispOp.getNode(PseudoColorOp.OP_NAME);
        if (lutOp.isPresent() && (imageOpNode = lutOp.get()) instanceof PseudoColorOp && (lutTable = (ByteLut)(pseudo = (PseudoColorOp)imageOpNode).getParam(PseudoColorOp.P_LUT)) != null && lutTable.lutTable() != null) {
            disLut = new DisplayByteLut(lutTable);
        }
        return disLut;
    }

    private HistogramData.Model getSelectedColorModel(int channels) {
        HistogramData.Model colorModel = (HistogramData.Model)((Object)this.jComboBoxImgChannel.getSelectedItem());
        if (colorModel == null) {
            colorModel = channels > 1 ? HistogramData.Model.RGB : HistogramData.Model.GRAY;
        }
        return colorModel;
    }

    private void buildHistogram() {
        if (this.view2DPane != null && this.view2DPane.getSourceImage() != null) {
            ChannelHistogramPanel[] old = new ChannelHistogramPanel[this.histView.getComponentCount()];
            for (int i = 0; i < old.length; ++i) {
                ChannelHistogramPanel channelHistogramPanel;
                Component c = this.histView.getComponent(i);
                if (!(c instanceof ChannelHistogramPanel)) continue;
                old[i] = channelHistogramPanel = (ChannelHistogramPanel)c;
            }
            this.histView.removeAll();
            WindLevelParameters p = this.getWinLeveParameters();
            if (p == null) {
                return;
            }
            MeasurableLayer layer = this.view2DPane.getMeasurableLayer();
            double pixMin = layer.getPixelMin();
            double pixMax = layer.getPixelMax();
            PlanarImage imageSource = this.view2DPane.getSourceImage();
            int channels = imageSource.channels();
            HistogramData.Model colorModel = this.getSelectedColorModel(channels);
            int[] selChannels = new int[channels];
            for (int i = 0; i < selChannels.length; ++i) {
                selChannels[i] = i;
            }
            try {
                int nbins = (Integer)this.spinnerBins.getValue();
                List<Mat> imgPr = ImageRegionStatistics.prepareInputImages(this.selectedGraphic, layer);
                if (imgPr.size() > 1) {
                    Mat srcImg = imgPr.get(0);
                    Mat mask = imgPr.get(1);
                    List<Mat> listHisto = HistogramData.computeHistogram(srcImg, mask, nbins, selChannels, colorModel, pixMin, pixMax);
                    ByteLut[] lut = colorModel.getByteLut();
                    DisplayByteLut[] displut = this.getLut(p, colorModel);
                    for (int i = 0; i < lut.length; ++i) {
                        StringBuilder name = new StringBuilder(lut[i].name());
                        name.append(" ");
                        name.append(Messages.getString("HistogramView.histo"));
                        if (StringUtil.hasText((CharSequence)layer.getPixelValueUnit())) {
                            name.append(" [");
                            name.append(layer.getPixelValueUnit());
                            name.append("]");
                        }
                        ChannelHistogramPanel chartPanel = i >= old.length || old[i] == null ? new ChannelHistogramPanel(name.toString()) : new ChannelHistogramPanel(name.toString(), old[i].isAccumulate(), old[i].isLogarithmic(), old[i].isShowIntensity());
                        this.histView.add(chartPanel);
                        Mat h = listHisto.get(i);
                        float[] histValues = new float[h.rows()];
                        h.get(0, 0, histValues);
                        HistogramData data = new HistogramData(histValues, displut[i], i, colorModel, p, pixMin, pixMax, layer);
                        data.updateVoiLut(this.view2DPane);
                        chartPanel.setHistogramBins(data);
                    }
                }
            }
            catch (Exception e) {
                LOGGER.error("Build histogram", (Throwable)e);
            }
            this.histView.revalidate();
            this.histView.repaint();
        }
    }

    @Override
    public void handle(List<Graphic> selectedGraphicList, MeasurableLayer layer) {
        AbstractDragGraphicArea sel;
        Graphic graphic;
        AbstractDragGraphicArea g = null;
        if (selectedGraphicList != null && selectedGraphicList.size() == 1 && (graphic = selectedGraphicList.get(0)) instanceof AbstractDragGraphicArea && !((sel = (AbstractDragGraphicArea)graphic) instanceof SelectGraphic)) {
            g = sel;
        }
        boolean update = !Objects.equals(this.selectedGraphic, g);
        this.selectedGraphic = g;
        if (update) {
            this.buildHistogram();
        }
    }

    @Override
    public void updateMeasuredItems(List<MeasureItem> measureList) {
        if (this.selectedGraphic != null) {
            this.buildHistogram();
        }
    }
}

