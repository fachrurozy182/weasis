/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.Filter
 *  org.weasis.core.api.image.cv.CvUtil
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.media.data.SeriesEvent
 *  org.weasis.core.api.media.data.SeriesEvent$Action
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagView
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.MathUtil
 */
package org.weasis.dicom.codec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.Filter;
import org.weasis.core.api.image.cv.CvUtil;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.SeriesEvent;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagView;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.MathUtil;
import org.weasis.dicom.codec.DcmMediaReader;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSpecialElement;
import org.weasis.dicom.codec.DicomSpecialElementFactory;
import org.weasis.dicom.codec.HiddenSeriesManager;
import org.weasis.dicom.codec.HiddenSpecialElement;
import org.weasis.dicom.codec.Messages;
import org.weasis.dicom.codec.SortSeriesStack;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.seg.SegSpecialElement;
import org.weasis.dicom.codec.utils.SeriesInstanceList;

public class DicomSeries
extends Series<DicomImageElement> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomSeries.class);
    static final TagView defaultTagView = new TagView(TagD.getTagFromIDs(528446, 0x200011, 524337));
    private static PreloadingTask preloadingTask;

    public DicomSeries(String subseriesInstanceUID) {
        this(subseriesInstanceUID, null, defaultTagView);
    }

    public DicomSeries(String subseriesInstanceUID, List<DicomImageElement> c, TagView displayTag) {
        super(TagD.getUID(TagD.Level.SERIES), (Object)subseriesInstanceUID, displayTag, c, SortSeriesStack.instanceNumber);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean[] getImageInMemoryList() {
        boolean[] list;
        DicomSeries dicomSeries = this;
        synchronized (dicomSeries) {
            list = new boolean[this.medias.size()];
            for (int i = 0; i < this.medias.size(); ++i) {
                if (!((DicomImageElement)this.medias.get(i)).isImageInCache()) continue;
                list[i] = true;
            }
        }
        return list;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addMedia(DicomImageElement media) {
        if (media != null && media.getMediaReader() != null) {
            DicomSeries dicomSeries = this;
            synchronized (dicomSeries) {
                int index = Collections.binarySearch(this.medias, media, SortSeriesStack.instanceNumber);
                int insertIndex = index < 0 ? -(index + 1) : index + 1;
                if (insertIndex < 0 || insertIndex > this.medias.size()) {
                    insertIndex = this.medias.size();
                }
                this.add(insertIndex, (MediaElement)media);
            }
            DataExplorerModel model = (DataExplorerModel)this.getTagValue(TagW.ExplorerModel);
            if (model != null) {
                model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.ADD, (Object)model, null, (Object)new SeriesEvent(SeriesEvent.Action.ADD_IMAGE, (Object)this, (Object)media)));
            }
        }
    }

    public String getToolTips() {
        StringBuilder toolTips = DicomSeries.getToolTips(this);
        if ((double)this.getFileSize() > 0.0) {
            toolTips.append(Messages.getString("DicomSeries.size"));
            toolTips.append(": ");
            toolTips.append(FileUtil.humanReadableByte((long)this.getFileSize(), (boolean)false));
            toolTips.append("<br>");
        }
        toolTips.append("</html>");
        return toolTips.toString();
    }

    public static StringBuilder getToolTips(Series<?> series) {
        StringBuilder toolTips = new StringBuilder("<html>");
        series.addToolTipsElement(toolTips, Messages.getString("DicomSeries.pat"), TagD.get(0x100010));
        series.addToolTipsElement(toolTips, Messages.getString("DicomSeries.mod"), TagD.get(524384));
        series.addToolTipsElement(toolTips, Messages.getString("DicomSeries.series_nb"), TagD.get(0x200011));
        series.addToolTipsElement(toolTips, Messages.getString("DicomSeries.study"), TagD.get(528432));
        series.addToolTipsElement(toolTips, Messages.getString("DicomSeries.series"), TagD.get(528446));
        DicomSeries.addToolTipsDateTime(toolTips, Messages.getString("DicomSeries.date"), series, 524321, 524337);
        return toolTips;
    }

    public static void addToolTipsDateTime(StringBuilder toolTips, String title, Series<?> series, int dateTag, int timeTag) {
        toolTips.append(title);
        toolTips.append(": ");
        toolTips.append(DcmMediaReader.buildDateTimeWithTimeZone(series, dateTag, timeTag));
        toolTips.append("<br>");
    }

    public String getSeriesNumber() {
        Integer splitNb = (Integer)this.getTagValue(TagW.SplitSeriesNumber);
        Integer val = TagD.getTagValue((TagReadable)this, 0x200011, Integer.class);
        String result = val == null ? "" : val.toString();
        return splitNb == null ? result : result + "-" + splitNb;
    }

    public String getMimeType() {
        String modality = TagD.getTagValue((TagReadable)this, 524384, String.class);
        DicomSpecialElementFactory factory = DicomMediaIO.DCM_ELEMENT_FACTORIES.get(modality);
        if (factory != null) {
            return factory.getSeriesMimeType();
        }
        return "series/dicom";
    }

    public synchronized void dispose() {
        DicomSeries.stopPreloading(this);
        String seriesUID = (String)this.getTagValue(this.getTagID());
        String modality = TagD.getTagValue((TagReadable)this, 524384, String.class);
        if (DicomMediaIO.isHiddenModality(modality)) {
            HiddenSeriesManager manager = HiddenSeriesManager.getInstance();
            Set<HiddenSpecialElement> removed = manager.series2Elements.remove(seriesUID);
            if (removed != null && !removed.isEmpty()) {
                Set<String> list;
                String patientPseudoUID = (String)removed.iterator().next().getTagValue(TagW.PatientPseudoUID);
                if (patientPseudoUID != null && (list = manager.patient2Series.get(patientPseudoUID)) != null) {
                    list.remove(seriesUID);
                    if (list.isEmpty()) {
                        manager.patient2Series.remove(patientPseudoUID);
                    }
                }
                manager.reference2Series.entrySet().removeIf(entry -> {
                    Set referencingSeries = (Set)entry.getValue();
                    referencingSeries.remove(seriesUID);
                    return referencingSeries.isEmpty();
                });
                for (HiddenSpecialElement element : removed) {
                    Set<String> referencingSeries;
                    String sopUID;
                    if (element instanceof SegSpecialElement) {
                        SegSpecialElement seg = (SegSpecialElement)element;
                        seg.disposeSegmentationVolume();
                    }
                    if ((sopUID = TagD.getTagValue((TagReadable)element, 524312, String.class)) == null || (referencingSeries = manager.sopRef2Series.get(sopUID)) == null) continue;
                    referencingSeries.remove(seriesUID);
                    if (!referencingSeries.isEmpty()) continue;
                    manager.sopRef2Series.remove(sopUID);
                }
            }
        }
        super.dispose();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DicomImageElement getNearestImage(double location, int offset, Filter<DicomImageElement> filter, Comparator<DicomImageElement> sort) {
        Iterable mediaList = this.getMedias(filter, sort);
        DicomImageElement nearest = null;
        int index = 0;
        int bestIndex = -1;
        DicomSeries dicomSeries = this;
        synchronized (dicomSeries) {
            double bestDiff = Double.MAX_VALUE;
            for (DicomImageElement dcm : mediaList) {
                double diff;
                Double val = (Double)dcm.getTagValue(TagW.SlicePosition);
                if (val != null && (diff = Math.abs(location - val)) < bestDiff) {
                    bestDiff = diff;
                    nearest = dcm;
                    bestIndex = index;
                    if (MathUtil.isEqualToZero((double)diff)) break;
                }
                ++index;
            }
        }
        if (offset > 0) {
            return (DicomImageElement)this.getMedia(bestIndex + offset, filter, sort);
        }
        return nearest;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getNearestImageIndex(double location, int offset, Filter<DicomImageElement> filter, Comparator<DicomImageElement> sort) {
        Iterable mediaList = this.getMedias(filter, sort);
        int index = 0;
        int bestIndex = -1;
        DicomSeries dicomSeries = this;
        synchronized (dicomSeries) {
            double bestDiff = Double.MAX_VALUE;
            for (DicomImageElement dcm : mediaList) {
                double diff;
                Double val = (Double)dcm.getTagValue(TagW.SlicePosition);
                if (val != null && (diff = Math.abs(location - val)) < bestDiff) {
                    bestDiff = diff;
                    bestIndex = index;
                    if (MathUtil.isEqualToZero((double)diff)) break;
                }
                ++index;
            }
        }
        return offset > 0 ? bestIndex + offset : bestIndex;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean hasMediaContains(TagW tag, Object val) {
        if (val != null) {
            List<DicomSpecialElement> list;
            Object val2;
            DicomSeries dicomSeries = this;
            synchronized (dicomSeries) {
                for (DicomImageElement media : this.medias) {
                    val2 = media.getTagValue(tag);
                    if (!val.equals(val2)) continue;
                    return true;
                }
            }
            if (this.medias.isEmpty() && (list = this.getAllDicomSpecialElement()) != null) {
                for (DicomSpecialElement specialElement : list) {
                    val2 = specialElement.getTagValue(tag);
                    if (!val.equals(val2)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public List<DicomSpecialElement> getAllDicomSpecialElement() {
        List specialElements = (List)this.getTagValue(TagW.DicomSpecialElementList);
        if (specialElements != null) {
            return specialElements;
        }
        String seriesUID = (String)this.getTagValue(this.getTagID());
        Set<HiddenSpecialElement> list = HiddenSeriesManager.getInstance().series2Elements.get(seriesUID);
        if (list != null && !list.isEmpty()) {
            return new ArrayList<DicomSpecialElement>(list);
        }
        return Collections.emptyList();
    }

    public DicomSpecialElement getFirstSpecialElement() {
        List specialElements = (List)this.getTagValue(TagW.DicomSpecialElementList);
        if (specialElements != null && !specialElements.isEmpty()) {
            return (DicomSpecialElement)specialElements.getFirst();
        }
        return null;
    }

    public boolean isSuitableFor3d() {
        int size = this.size(null);
        SeriesInstanceList seriesInstanceList = (SeriesInstanceList)this.getTagValue(TagW.WadoInstanceReferenceList);
        if (seriesInstanceList != null) {
            size = Math.max(size, seriesInstanceList.size());
        }
        return size >= 5;
    }

    public static synchronized void startPreloading(DicomSeries series, List<DicomImageElement> imageList, int currentIndex) {
        if (series != null && imageList != null) {
            if (preloadingTask != null) {
                if (preloadingTask.getSeries() == series) {
                    return;
                }
                DicomSeries.stopPreloading(preloadingTask.getSeries());
            }
            preloadingTask = new PreloadingTask(series, imageList, currentIndex);
            preloadingTask.start();
        }
    }

    public static synchronized void stopPreloading(DicomSeries series) {
        if (preloadingTask != null && preloadingTask.getSeries() == series) {
            PreloadingTask moribund = preloadingTask;
            preloadingTask = null;
            moribund.setPreloading(false);
            moribund.interrupt();
        }
    }

    static class PreloadingTask
    extends Thread {
        private volatile boolean preloading = true;
        private final int index;
        private final List<DicomImageElement> imageList;
        private final DicomSeries series;

        public PreloadingTask(DicomSeries series, List<DicomImageElement> imageList, int currentIndex) {
            this.series = series;
            this.imageList = imageList;
            this.index = currentIndex;
        }

        public synchronized boolean isPreloading() {
            return this.preloading;
        }

        public DicomSeries getSeries() {
            return this.series;
        }

        public List<DicomImageElement> getImageList() {
            return this.imageList;
        }

        public synchronized void setPreloading(boolean preloading) {
            this.preloading = preloading;
        }

        private static long evaluateImageSize(DicomImageElement image) {
            Integer allocated = TagD.getTagValue((TagReadable)image, 2621696, Integer.class);
            Integer sample = TagD.getTagValue((TagReadable)image, 0x280002, Integer.class);
            Integer rows = TagD.getTagValue((TagReadable)image, 2621456, Integer.class);
            Integer columns = TagD.getTagValue((TagReadable)image, 2621457, Integer.class);
            if (allocated != null && sample != null && rows != null && columns != null) {
                return (long)rows.intValue() * (long)columns.intValue() * (long)sample.intValue() * (long)allocated.intValue() / 8L;
            }
            return 0L;
        }

        private void loadArrays(DicomImageElement img, DataExplorerModel model) {
            Boolean cache;
            if (!(!this.preloading || img.isLoading() || (cache = (Boolean)img.getTagValue(TagW.ImageCache)) != null && cache.booleanValue())) {
                long start = System.currentTimeMillis();
                try {
                    img.getImage();
                }
                catch (OutOfMemoryError e) {
                    LOGGER.error("Out of memory when loading image: {}", (Object)img, (Object)e);
                    CvUtil.runGarbageCollectorAndWait((long)50L);
                    return;
                }
                long stop = System.currentTimeMillis();
                LOGGER.debug("Reading time: {} ms of image: {}", (Object)(stop - start), (Object)img);
                if (model != null) {
                    model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.ADD, (Object)model, null, (Object)new SeriesEvent(SeriesEvent.Action.PRELOADING, (Object)this.series, (Object)img)));
                }
            }
        }

        @Override
        public void run() {
            block9: {
                if (this.imageList == null) break block9;
                DataExplorerModel model = (DataExplorerModel)this.series.getTagValue(TagW.ExplorerModel);
                int size = this.imageList.size();
                if (model == null || this.index < 0 || this.index >= size) {
                    return;
                }
                long imgSize = PreloadingTask.evaluateImageSize(this.imageList.get(this.index)) * (long)size + 5000L;
                long heapSize = Runtime.getRuntime().totalMemory();
                long heapFreeSize = Runtime.getRuntime().freeMemory();
                if (imgSize > heapSize / 3L) {
                    double val;
                    int adjustSize;
                    int start;
                    if (imgSize > heapFreeSize) {
                        CvUtil.runGarbageCollectorAndWait((long)50L);
                    }
                    if ((start = this.index - (adjustSize = (int)((double)size * (val = (double)heapFreeSize / (double)imgSize)) / 2)) < 0) {
                        adjustSize -= start;
                        start = 0;
                    }
                    if (adjustSize > size) {
                        adjustSize = size;
                    }
                    for (int i = start; i < adjustSize; ++i) {
                        this.loadArrays(this.imageList.get(i), model);
                    }
                } else {
                    if (imgSize > heapFreeSize) {
                        CvUtil.runGarbageCollectorAndWait((long)50L);
                    }
                    for (DicomImageElement img : this.imageList) {
                        this.loadArrays(img, model);
                    }
                }
            }
        }
    }
}

