/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.ui.FlatUIUtils
 *  net.miginfocom.swing.MigLayout
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.SeriesImporter
 *  org.weasis.core.api.media.data.SeriesThumbnail
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.util.FontItem
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.HiddenSeriesManager
 *  org.weasis.dicom.codec.TagD
 */
package org.weasis.dicom.explorer.main;

import com.formdev.flatlaf.ui.FlatUIUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.SeriesImporter;
import org.weasis.core.api.media.data.SeriesThumbnail;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.util.FontItem;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.HiddenSeriesManager;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.main.ThumbnailMouseAndKeyAdapter;
import org.weasis.dicom.explorer.wado.LoadSeries;

public class SeriesPane
extends JPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeriesPane.class);
    private static final int LABEL_GAP = 1;
    private static final String LAYOUT_CONSTRAINTS = "wrap 1, insets 0, gapy 1lp";
    private static final String COLUMN_CONSTRAINTS = "[center]";
    private final DicomSeries dicomSeries;
    private final JLabel label;
    private final DicomModel model;
    private SeriesThumbnail thumbnail;
    private int currentThumbnailSize;

    public SeriesPane(DicomSeries dicomSeries, DicomModel model) {
        this.dicomSeries = Objects.requireNonNull(dicomSeries);
        this.model = Objects.requireNonNull(model);
        this.label = this.createDescriptionLabel();
        this.initializeLayout();
        this.initializeComponents();
    }

    private void initializeLayout() {
        this.setLayout((LayoutManager)new MigLayout(LAYOUT_CONSTRAINTS, COLUMN_CONSTRAINTS));
        this.setBackground(FlatUIUtils.getUIColor((String)"List.background", (Color)Color.LIGHT_GRAY));
        this.setFocusable(false);
    }

    private void initializeComponents() {
        this.currentThumbnailSize = SeriesThumbnail.getThumbnailSizeFromPreferences();
        this.thumbnail = this.getOrCreateThumbnail();
        if (this.thumbnail != null) {
            this.add((Component)this.thumbnail);
        }
        this.updateSize(this.currentThumbnailSize);
        this.add(this.label);
    }

    public void updateThumbnail() {
        SeriesThumbnail newThumb = (SeriesThumbnail)this.dicomSeries.getTagValue(TagW.Thumbnail);
        if (newThumb != this.thumbnail) {
            this.thumbnail = newThumb;
            this.removeAll();
            if (this.thumbnail != null) {
                this.add((Component)this.thumbnail);
            }
            this.add(this.label);
            this.updateSize(this.currentThumbnailSize);
            this.revalidate();
            this.repaint();
        }
    }

    private SeriesThumbnail getOrCreateThumbnail() {
        SeriesThumbnail thumb = (SeriesThumbnail)this.dicomSeries.getTagValue(TagW.Thumbnail);
        if (thumb == null && (thumb = SeriesPane.createThumbnail(this.dicomSeries, this.model, this.currentThumbnailSize)) != null) {
            this.dicomSeries.setTag(TagW.Thumbnail, (Object)thumb);
        }
        return thumb;
    }

    private JLabel createDescriptionLabel() {
        String description = this.getSeriesDescription();
        JLabel descLabel = new JLabel(description, 0);
        descLabel.setFont(FontItem.MINI.getFont());
        descLabel.setFocusable(false);
        return descLabel;
    }

    private String getSeriesDescription() {
        String desc = (String)TagD.getTagValue((TagReadable)this.dicomSeries, (int)528446, String.class);
        return desc == null ? "" : desc;
    }

    public void updateSize(int thumbnailSize) {
        if (thumbnailSize <= 0) {
            LOGGER.warn("Invalid thumbnail size: {}", (Object)thumbnailSize);
            return;
        }
        if (this.currentThumbnailSize == thumbnailSize && this.label.getMaximumSize() != null && this.label.getMaximumSize().width == thumbnailSize) {
            return;
        }
        this.currentThumbnailSize = thumbnailSize;
        this.updateThumbnailSize(thumbnailSize);
        this.updateLabelSize(thumbnailSize);
        SwingUtilities.invokeLater(() -> {
            this.revalidate();
            this.repaint();
        });
    }

    private void updateThumbnailSize(int thumbnailSize) {
        SeriesThumbnail thumb = (SeriesThumbnail)this.dicomSeries.getTagValue(TagW.Thumbnail);
        if (thumb != null) {
            thumb.setThumbnailSize(thumbnailSize);
        }
    }

    private void updateLabelSize(int thumbnailSize) {
        try {
            FontRenderContext frc = new FontRenderContext(null, false, false);
            int width = GuiUtils.getScaleLength((int)thumbnailSize);
            int height = (int)Math.ceil(this.label.getFont().getStringBounds("0", frc).getHeight()) + GuiUtils.getScaleLength((int)1);
            Dimension dimension = new Dimension(width, height);
            this.label.setPreferredSize(dimension);
            this.label.setMaximumSize(dimension);
        }
        catch (Exception e) {
            LOGGER.warn("Error updating label size of thumbnail: {}", (Object)thumbnailSize, (Object)e);
        }
    }

    public void updateText() {
        String description = this.getSeriesDescription();
        SwingUtilities.invokeLater(() -> this.label.setText(description));
    }

    public boolean isSeries(MediaSeriesGroup sequence) {
        return this.dicomSeries.equals((Object)sequence);
    }

    public DicomSeries getDicomSeries() {
        return this.dicomSeries;
    }

    public JLabel getLabel() {
        return this.label;
    }

    public DicomModel getModel() {
        return this.model;
    }

    public int getCurrentThumbnailSize() {
        return this.currentThumbnailSize;
    }

    public static SeriesThumbnail createThumbnail(DicomSeries series, DicomModel dicomModel, int thumbnailSize) {
        if (series == null || dicomModel == null || thumbnailSize <= 0) {
            LOGGER.warn("Invalid parameters for thumbnail creation: series={}, model={}, size={}", new Object[]{series, dicomModel, thumbnailSize});
            return null;
        }
        Callable<SeriesThumbnail> callable = () -> SeriesPane.createThumbnailInternal(series, dicomModel, thumbnailSize);
        FutureTask<SeriesThumbnail> future = new FutureTask<SeriesThumbnail>(callable);
        try {
            GuiExecutor.invokeAndWait(future);
            return future.get();
        }
        catch (InterruptedException e) {
            LOGGER.warn("Thumbnail creation interrupted for series: {}", (Object)series);
            Thread.currentThread().interrupt();
            return null;
        }
        catch (ExecutionException e) {
            LOGGER.error("Error creating thumbnail for series: {}", (Object)series, (Object)e);
            return null;
        }
    }

    private static SeriesThumbnail createThumbnailInternal(DicomSeries series, DicomModel dicomModel, int thumbnailSize) {
        Function<String, Set> drawIcons = HiddenSeriesManager::getRelatedIcons;
        SeriesThumbnail thumb = new SeriesThumbnail((MediaSeries)series, thumbnailSize, drawIcons);
        SeriesImporter seriesImporter = series.getSeriesLoader();
        if (seriesImporter instanceof LoadSeries) {
            LoadSeries loader = (LoadSeries)seriesImporter;
            thumb.setProgressBar(loader.isDone() ? null : loader.getProgressBar());
        }
        thumb.registerListeners();
        ThumbnailMouseAndKeyAdapter thumbAdapter = new ThumbnailMouseAndKeyAdapter(series, dicomModel, null);
        thumb.addMouseListener((MouseListener)thumbAdapter);
        thumb.addKeyListener((KeyListener)thumbAdapter);
        return thumb;
    }

    public void dispose() {
        SeriesThumbnail thumb = (SeriesThumbnail)this.dicomSeries.getTagValue(TagW.Thumbnail);
        if (thumb != null) {
            thumb.removeMouseAndKeyListener();
        }
    }
}

