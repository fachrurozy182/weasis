/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Thumbnail
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.TagD
 */
package org.weasis.dicom.explorer.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Thumbnail;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.DicomSorter;
import org.weasis.dicom.explorer.main.DicomExplorer;
import org.weasis.dicom.explorer.main.DicomPaneManager;
import org.weasis.dicom.explorer.main.SeriesPane;
import org.weasis.dicom.explorer.main.StudyPane;

public class SplitSeriesManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SplitSeriesManager.class);
    private static final int REPLACEMENT_SERIES_NUMBER = -1;
    private static final int INITIAL_SPLIT_NUMBER = 1;
    private final DicomExplorer explorer;
    private final DicomModel model;
    private final ConcurrentHashMap<String, List<DicomSeries>> splitSeriesCache = new ConcurrentHashMap();
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();

    public SplitSeriesManager(DicomExplorer explorer) {
        this.explorer = Objects.requireNonNull(explorer, "DicomExplorer cannot be null");
        this.model = explorer.getDataExplorerModel();
    }

    public void updateSplitSeries(DicomSeries dcmSeries) {
        String seriesUID = this.getSeriesInstanceUID(dcmSeries);
        if (seriesUID == null) {
            LOGGER.warn("Cannot update split series: Series is null");
            return;
        }
        try {
            this.processSplitSeriesUpdate(dcmSeries, seriesUID);
        }
        catch (Exception e) {
            LOGGER.error("Error updating split series for: {}", (Object)seriesUID, (Object)e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void processSplitSeriesUpdate(DicomSeries dcmSeries, String seriesUID) {
        this.cacheLock.writeLock().lock();
        try {
            List<DicomSeries> splitSeries = this.discoverSplitSeries(dcmSeries, seriesUID);
            if (splitSeries.isEmpty()) {
                this.splitSeriesCache.remove(seriesUID);
                return;
            }
            List previous = this.splitSeriesCache.put(seriesUID, new ArrayList<DicomSeries>(splitSeries));
            if (splitSeries.size() > 1) {
                this.ensureThumbnails(splitSeries);
                if (!SplitSeriesManager.isSameSplitSet(previous, splitSeries)) {
                    this.updateSplitSeriesNumbers(splitSeries);
                    SwingUtilities.invokeLater(() -> this.refreshAffectedStudyPanes(splitSeries));
                }
            }
        }
        finally {
            this.cacheLock.writeLock().unlock();
        }
    }

    private List<DicomSeries> discoverSplitSeries(DicomSeries dcmSeries, String seriesUID) {
        MediaSeriesGroup study = this.model.getParent((MediaSeriesGroup)dcmSeries, DicomModel.study);
        if (study == null) {
            return Collections.emptyList();
        }
        ArrayList<DicomSeries> splitSeries = new ArrayList<DicomSeries>();
        for (MediaSeriesGroup series : this.model.getChildren(study)) {
            DicomSeries s;
            String currentSeriesUID;
            if (!(series instanceof DicomSeries) || !seriesUID.equals(currentSeriesUID = this.getSeriesInstanceUID(s = (DicomSeries)series))) continue;
            splitSeries.add(s);
        }
        return splitSeries;
    }

    private void updateSplitSeriesNumbers(List<DicomSeries> splitSeries) {
        if (splitSeries.size() <= 1) {
            return;
        }
        splitSeries.sort((s1, s2) -> {
            Integer split1 = (Integer)TagD.getTagValue((TagReadable)s1, (TagW)TagD.SplitSeriesNumber, Integer.class);
            Integer split2 = (Integer)TagD.getTagValue((TagReadable)s2, (TagW)TagD.SplitSeriesNumber, Integer.class);
            if (split1 != null && split2 != null) {
                return Integer.compare(split1, split2);
            }
            if (split1 != null) {
                return -1;
            }
            if (split2 != null) {
                return 1;
            }
            return DicomSorter.SERIES_COMPARATOR.compare(s1, s2);
        });
        for (int i = 0; i < splitSeries.size(); ++i) {
            DicomSeries series = splitSeries.get(i);
            int splitNumber = i + 1;
            series.setTag(TagD.SplitSeriesNumber, (Object)splitNumber);
            LOGGER.trace("Updated split series number: {} for series: {}", (Object)splitNumber, (Object)series);
        }
    }

    private static boolean isSameSplitSet(List<DicomSeries> previous, List<DicomSeries> current) {
        return previous != null && previous.size() == current.size() && previous.containsAll(current);
    }

    private void ensureThumbnails(List<DicomSeries> splitSeries) {
        splitSeries.forEach(this.model::buildThumbnail);
    }

    private void refreshAffectedStudyPanes(List<DicomSeries> splitSeries) {
        HashSet<StudyPane> studyPanes = new HashSet<StudyPane>();
        DicomPaneManager paneManager = this.explorer.getPaneManager();
        for (DicomSeries series : splitSeries) {
            StudyPane studyPane;
            MediaSeriesGroup study;
            Object object;
            SeriesPane seriesPane = paneManager.getSeriesPane((MediaSeriesGroup)series);
            if (seriesPane != null) {
                seriesPane.updateThumbnail();
            }
            if ((object = series.getTagValue(TagW.Thumbnail)) instanceof Thumbnail) {
                Thumbnail thumbnail = (Thumbnail)object;
                thumbnail.repaint();
            }
            if ((study = this.model.getParent((MediaSeriesGroup)series, DicomModel.study)) == null || (studyPane = paneManager.getStudyPane(study)) == null) continue;
            studyPanes.add(studyPane);
        }
        for (StudyPane studyPane : studyPanes) {
            studyPane.refreshLayoutAsync();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public List<DicomSeries> getSplitSeries(String seriesInstanceUID) {
        if (seriesInstanceUID == null) {
            return new ArrayList<DicomSeries>();
        }
        this.cacheLock.readLock().lock();
        try {
            List<DicomSeries> cached = this.splitSeriesCache.get(seriesInstanceUID);
            if (cached != null) {
                ArrayList<DicomSeries> arrayList = new ArrayList<DicomSeries>(cached);
                return arrayList;
            }
            MediaSeriesGroup series = this.model.getSeriesNode(seriesInstanceUID);
            if (series instanceof DicomSeries) {
                DicomSeries dcmSeries = (DicomSeries)series;
                List<DicomSeries> discovered = this.discoverSplitSeries(dcmSeries, seriesInstanceUID);
                if (!discovered.isEmpty()) {
                    this.splitSeriesCache.put(seriesInstanceUID, new ArrayList<DicomSeries>(discovered));
                }
                List<DicomSeries> list = discovered;
                return list;
            }
        }
        finally {
            this.cacheLock.readLock().unlock();
        }
        return Collections.emptyList();
    }

    public List<DicomSeries> getSplitSeries(DicomSeries series) {
        if (series == null) {
            return new ArrayList<DicomSeries>();
        }
        String seriesUID = this.getSeriesInstanceUID(series);
        return this.getSplitSeries(seriesUID);
    }

    public void markSeriesAsReplacement(DicomSeries series) {
        if (series != null) {
            series.setTag(TagD.SplitSeriesNumber, (Object)-1);
            LOGGER.trace("Marked series as replacement: {}", (Object)series);
        }
    }

    public boolean isReplacementSeries(DicomSeries series) {
        if (series == null) {
            return false;
        }
        Integer splitNumber = (Integer)TagD.getTagValue((TagReadable)series, (TagW)TagD.SplitSeriesNumber, Integer.class);
        return splitNumber != null && splitNumber == -1;
    }

    public void invalidateSplitSeriesCache(String seriesInstanceUID) {
        if (seriesInstanceUID != null) {
            this.cacheLock.writeLock().lock();
            try {
                this.splitSeriesCache.remove(seriesInstanceUID);
                LOGGER.debug("Invalidated split series cache for: {}", (Object)seriesInstanceUID);
            }
            finally {
                this.cacheLock.writeLock().unlock();
            }
        }
    }

    public void invalidateSplitSeriesCache(DicomSeries series) {
        String seriesUID = this.getSeriesInstanceUID(series);
        this.invalidateSplitSeriesCache(seriesUID);
    }

    private String getSeriesInstanceUID(DicomSeries series) {
        if (series == null) {
            return null;
        }
        return (String)TagD.getTagValue((TagReadable)series, (int)0x20000E, String.class);
    }

    public void dispose() {
        this.cacheLock.writeLock().lock();
        try {
            this.splitSeriesCache.clear();
            LOGGER.debug("SplitSeriesManager disposed");
        }
        finally {
            this.cacheLock.writeLock().unlock();
        }
    }
}

