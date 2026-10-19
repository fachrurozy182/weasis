/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.miginfocom.swing.MigLayout
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.SeriesThumbnail
 */
package org.weasis.dicom.explorer.main;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import net.miginfocom.swing.MigLayout;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.SeriesThumbnail;
import org.weasis.dicom.explorer.main.DicomPaneManager;
import org.weasis.dicom.explorer.main.SeriesFilter;
import org.weasis.dicom.explorer.main.SeriesPane;

public class StudyPane
extends JPanel {
    private static final String LAYOUT_CONSTRAINTS = "fillx, flowy, insets 0";
    private static final String COLUMN_CONSTRAINTS = "[fill]";
    private static final String SUB_PANEL_CONSTRAINTS = "shrinky 100";
    private static final int GAP = 5;
    private static final int SUB_PANEL_INSET = 3;
    private static final int BORDER_INSET = 3;
    private static final int SEPARATION = 4;
    private final JPanel subPanel;
    private final MediaSeriesGroup dicomStudy;
    private final TitledBorder titleBorder;
    private final ComponentResizeHandler resizeHandler;

    public StudyPane(MediaSeriesGroup dicomStudy) {
        super((LayoutManager)new MigLayout(LAYOUT_CONSTRAINTS, COLUMN_CONSTRAINTS));
        this.dicomStudy = Objects.requireNonNull(dicomStudy);
        this.subPanel = new JPanel((LayoutManager)new MigLayout("insets 0, gap 5 5, flowx"));
        this.subPanel.setBorder(GuiUtils.getEmptyBorder((int)3));
        this.titleBorder = GuiUtils.getTitledBorder((String)dicomStudy.toString());
        this.resizeHandler = new ComponentResizeHandler(this);
        this.initializeComponent();
    }

    private void initializeComponent() {
        this.setBorder(BorderFactory.createCompoundBorder(GuiUtils.getEmptyBorder((int)0, (int)3, (int)0, (int)3), this.titleBorder));
        this.setFocusable(false);
        this.add((Component)this.subPanel, SUB_PANEL_CONSTRAINTS);
        this.addComponentListener(this.resizeHandler);
    }

    public static int getHorizontalChrome(Component c) {
        Insets border = BorderFactory.createCompoundBorder(GuiUtils.getEmptyBorder((int)0, (int)3, (int)0, (int)3), GuiUtils.getTitledBorder((String)" ")).getBorderInsets(c);
        Insets subPanelPadding = GuiUtils.getEmptyBorder((int)3).getBorderInsets(c);
        return border.left + border.right + subPanelPadding.left + subPanelPadding.right;
    }

    public int getSingleColumnWidth() {
        if (this.subPanel.getComponentCount() == 0) {
            return 0;
        }
        int itemWidth = this.subPanel.getComponent((int)0).getPreferredSize().width;
        Insets sub = this.subPanel.getInsets();
        Insets study = this.getInsets();
        return itemWidth + sub.left + sub.right + study.left + study.right;
    }

    @Override
    public void remove(int index) {
        if (this.isValidIndex(index)) {
            this.subPanel.remove(index);
            this.refreshLayoutAsync();
        }
    }

    @Override
    public void remove(Component comp) {
        if (comp != null && this.subPanel.isAncestorOf(comp)) {
            this.subPanel.remove(comp);
            this.refreshLayoutAsync();
        }
    }

    @Override
    public void removeAll() {
        this.subPanel.removeAll();
    }

    public void refreshLayout() {
        LayoutManager layoutManager = this.subPanel.getLayout();
        if (layoutManager instanceof MigLayout) {
            MigLayout migLayout = (MigLayout)layoutManager;
            int width = this.subPanel.getWidth();
            Container parent = SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
            if (parent instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)parent;
                width = scrollPane.getViewport().getWidth();
                Insets insets = this.getInsets();
                width -= insets.left + insets.right;
            }
            Insets subInsets = this.subPanel.getInsets();
            if ((width -= subInsets.left + subInsets.right + GuiUtils.getScaleLength((int)4)) > 0 && this.subPanel.getComponentCount() > 0) {
                int itemWidth = this.subPanel.getComponent((int)0).getPreferredSize().width;
                int count = (width + 5) / (itemWidth + 5);
                migLayout.setLayoutConstraints((Object)("insets 0, gap 5 5, flowx, wrap " + Math.max(1, count)));
            }
        }
    }

    public void refreshLayoutAsync() {
        SwingUtilities.invokeLater(() -> {
            this.refreshLayout();
            this.revalidate();
            Optional.ofNullable(this.getParent()).ifPresent(Component::repaint);
        });
    }

    public boolean isSeriesVisible(MediaSeriesGroup series) {
        if (series == null) {
            return false;
        }
        return this.getSeriesPaneStream().anyMatch(seriesPane -> seriesPane.isSeries(series));
    }

    public List<SeriesPane> getSeriesPaneList() {
        return this.getSeriesPaneStream().toList();
    }

    public void showAllSeries(DicomPaneManager paneManager) {
        Objects.requireNonNull(paneManager);
        this.removeAll();
        List<SeriesPane> seriesList = paneManager.getSeriesList(this.dicomStudy);
        if (seriesList.isEmpty()) {
            this.revalidate();
            return;
        }
        SeriesFilter filter = paneManager.getSeriesFilter();
        int thumbnailSize = SeriesThumbnail.getThumbnailSizeFromPreferences();
        int index = 0;
        for (SeriesPane series : seriesList) {
            if (series == null || !filter.test((MediaSeriesGroup)series.getDicomSeries(), this.dicomStudy)) continue;
            this.addPane(series, index++, thumbnailSize);
        }
        this.revalidate();
    }

    public void addPane(SeriesPane seriesPane, int index, int thumbnailSize) {
        if (seriesPane == null) {
            return;
        }
        seriesPane.updateSize(thumbnailSize);
        if (index >= 0 && index <= this.subPanel.getComponentCount()) {
            this.subPanel.add((Component)seriesPane, index);
        } else {
            this.subPanel.add(seriesPane);
        }
        this.updateText();
    }

    public void addPane(SeriesPane seriesPane) {
        int thumbnailSize = SeriesThumbnail.getThumbnailSizeFromPreferences();
        this.addPane(seriesPane, this.subPanel.getComponentCount(), thumbnailSize);
    }

    public MediaSeriesGroup getDicomStudy() {
        return this.dicomStudy;
    }

    public void updateText() {
        if (this.titleBorder != null) {
            this.titleBorder.setTitle(this.dicomStudy.toString());
            this.repaint();
        }
    }

    public boolean isStudy(MediaSeriesGroup study) {
        return Objects.equals(this.dicomStudy, study);
    }

    public int getSeriesCount() {
        return (int)this.getSeriesPaneStream().count();
    }

    public boolean hasSeriesPanes() {
        return this.getSeriesCount() > 0;
    }

    public void updateThumbnailSize(int thumbnailSize) {
        if (thumbnailSize <= 0) {
            return;
        }
        this.getSeriesPaneStream().forEach(seriesPane -> seriesPane.updateSize(thumbnailSize));
        this.refreshLayoutAsync();
    }

    private Stream<SeriesPane> getSeriesPaneStream() {
        return Stream.of(this.subPanel.getComponents()).filter(SeriesPane.class::isInstance).map(SeriesPane.class::cast);
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < this.subPanel.getComponentCount();
    }

    private class ComponentResizeHandler
    extends ComponentAdapter {
        final /* synthetic */ StudyPane this$0;

        private ComponentResizeHandler(StudyPane studyPane) {
            StudyPane studyPane2 = studyPane;
            Objects.requireNonNull(studyPane2);
            this.this$0 = studyPane2;
        }

        @Override
        public void componentResized(ComponentEvent e) {
            this.this$0.refreshLayoutAsync();
        }
    }
}

