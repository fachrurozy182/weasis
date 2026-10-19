/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.MediaSeries$MEDIA_POSITION
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.media.data.SeriesImporter
 *  org.weasis.core.api.media.data.SeriesThumbnail
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.TagW$TagType
 *  org.weasis.core.api.media.data.Thumbnail
 *  org.weasis.core.api.model.PerformanceModel
 *  org.weasis.core.api.net.ClosableURLConnection
 *  org.weasis.core.api.net.NetworkUtil
 *  org.weasis.core.api.net.URLParameters
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.HiddenSeriesManager
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.TagD$Level
 *  org.weasis.dicom.codec.seg.SegVisibilityPolicy
 *  org.weasis.dicom.codec.utils.SeriesInstanceList
 *  org.weasis.dicom.mf.HttpTag
 *  org.weasis.dicom.mf.SopInstance
 *  org.weasis.dicom.mf.WadoParameters
 *  org.weasis.dicom.web.MultipartConstants$DicomContentType
 */
package org.weasis.dicom.explorer.wado;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.SeriesImporter;
import org.weasis.core.api.media.data.SeriesThumbnail;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Thumbnail;
import org.weasis.core.api.model.PerformanceModel;
import org.weasis.core.api.net.ClosableURLConnection;
import org.weasis.core.api.net.NetworkUtil;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.HiddenSeriesManager;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.seg.SegVisibilityPolicy;
import org.weasis.dicom.codec.utils.SeriesInstanceList;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.LoadLocalDicom;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.PluginOpeningStrategy;
import org.weasis.dicom.explorer.exp.ExplorerTask;
import org.weasis.dicom.explorer.wado.DownloadManager;
import org.weasis.dicom.explorer.wado.DownloadPriority;
import org.weasis.dicom.explorer.wado.SeriesDownloadManager;
import org.weasis.dicom.explorer.wado.ThumbnailManager;
import org.weasis.dicom.mf.HttpTag;
import org.weasis.dicom.mf.SopInstance;
import org.weasis.dicom.mf.WadoParameters;
import org.weasis.dicom.web.MultipartConstants;

public class LoadSeries
extends ExplorerTask<Boolean, String>
implements SeriesImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadSeries.class);
    public static final String CONCURRENT_DOWNLOADS_IN_SERIES = "download.concurrent.series.images";
    public static final Path DICOM_TMP_DIR = AppProperties.buildAccessibleTempDirectory((String[])new String[]{"downloading"});
    public static final TagW DOWNLOAD_START_TIME = new TagW("DownloadStartTime", TagW.TagType.TIME);
    public static final TagW DOWNLOAD_TIME = new TagW("DownloadTime", TagW.TagType.TIME);
    public static final TagW DOWNLOAD_ERRORS = new TagW("DownloadErrors", TagW.TagType.INTEGER);
    public static final TagW SERIES_BULK_RETRIEVE = new TagW("SeriesBulkRetrieve", TagW.TagType.BOOLEAN);
    public static final String LOAD_TYPE_DICOMDIR = "DICOMDIR";
    public static final String LOAD_TYPE_URL = "URL";
    public static final String LOAD_TYPE_LOCAL = "local";
    public static final String LOAD_TYPE_WADO = "WADO";
    private PluginOpeningStrategy openingStrategy;
    public final int concurrentDownloads;
    private final DicomModel dicomModel;
    private final DicomSeries dicomSeries;
    private final SeriesInstanceList seriesInstanceList;
    private final JProgressBar progressBar;
    private final URLParameters urlParams;
    private DownloadPriority priority = null;
    private final boolean writeInCache;
    private final boolean startDownloading;
    private final AuthMethod authMethod;
    private final AtomicInteger errors;
    private volatile boolean hasError = false;
    private final AtomicBoolean seriesInitialized = new AtomicBoolean(false);
    private final ThumbnailManager thumbnailManager;
    private final SeriesDownloadManager downloadManager;

    static String dicomWebBaseUrl(WadoParameters wado, MediaSeriesGroup dicomSeries) {
        int index;
        String baseUrl = wado.getBaseURL();
        if (StringUtil.hasText((CharSequence)baseUrl)) {
            return baseUrl;
        }
        String retrieveUrl = (String)TagD.getTagValue((TagReadable)dicomSeries, (int)528784, String.class);
        if (retrieveUrl != null && (index = retrieveUrl.indexOf("/studies/")) > 0) {
            return retrieveUrl.substring(0, index);
        }
        return baseUrl;
    }

    public void setPOpeningStrategy(PluginOpeningStrategy openingStrategy) {
        this.openingStrategy = openingStrategy;
    }

    public PluginOpeningStrategy getOpeningStrategy() {
        return this.openingStrategy;
    }

    public LoadSeries(DicomSeries dicomSeries, DicomModel dicomModel, int concurrentDownloads, boolean writeInCache) {
        this(dicomSeries, dicomModel, null, concurrentDownloads, writeInCache, true);
    }

    public LoadSeries(DicomSeries dicomSeries, DicomModel dicomModel, AuthMethod authMethod, int concurrentDownloads, boolean writeInCache, boolean startDownloading) {
        this(dicomSeries, dicomModel, authMethod, null, concurrentDownloads, writeInCache, startDownloading, false);
    }

    public LoadSeries(DicomSeries dicomSeries, DicomModel dicomModel, AuthMethod authMethod, JProgressBar progressBar, int concurrentDownloads, boolean writeInCache, boolean startDownloading) {
        this(dicomSeries, dicomModel, authMethod, progressBar, concurrentDownloads, writeInCache, startDownloading, true);
    }

    public LoadSeries(DicomSeries dicomSeries, DicomModel dicomModel, AuthMethod authMethod, JProgressBar progressBar, int concurrentDownloads, boolean writeInCache, boolean startDownloading, boolean externalProgress) {
        super(Messages.getString("DicomExplorer.loading"), writeInCache, true);
        if (dicomModel == null || dicomSeries == null || progressBar == null && externalProgress) {
            throw new IllegalArgumentException("null parameters");
        }
        this.dicomModel = dicomModel;
        this.dicomSeries = dicomSeries;
        this.authMethod = authMethod;
        this.seriesInstanceList = Optional.ofNullable((SeriesInstanceList)dicomSeries.getTagValue(TagW.WadoInstanceReferenceList)).orElseGet(SeriesInstanceList::new);
        this.writeInCache = writeInCache;
        JProgressBar jProgressBar = this.progressBar = externalProgress ? progressBar : this.getBar();
        if (!externalProgress && !writeInCache) {
            this.progressBar.setVisible(false);
        }
        this.dicomSeries.setSeriesLoader((SeriesImporter)this);
        this.concurrentDownloads = concurrentDownloads;
        this.urlParams = new URLParameters(LoadSeries.getHttpTags((WadoParameters)dicomSeries.getTagValue(TagW.WadoParameters)));
        this.startDownloading = startDownloading;
        Integer downloadErrors = (Integer)dicomSeries.getTagValue(DOWNLOAD_ERRORS);
        if (downloadErrors == null) {
            downloadErrors = 0;
        }
        this.errors = new AtomicInteger(downloadErrors);
        this.announceSegmentations();
        this.thumbnailManager = new ThumbnailManager(dicomSeries, dicomModel, this.urlParams);
        this.downloadManager = new SeriesDownloadManager(this, dicomModel, dicomSeries, this.progressBar, this.urlParams, authMethod, writeInCache, concurrentDownloads, this.errors, this.seriesInitialized);
    }

    private void announceSegmentations() {
        if (!"SEG".equals(TagD.getTagValue((TagReadable)this.dicomSeries, (int)524384, String.class))) {
            return;
        }
        MediaSeriesGroup patient = this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.patient);
        if (patient == null) {
            return;
        }
        Integer related = (Integer)TagD.getTagValue((TagReadable)this.dicomSeries, (int)2101769, Integer.class);
        SegVisibilityPolicy.announce((String)((String)patient.getTagValue(TagW.PatientPseudoUID)), (String)((String)TagD.getTagValue((TagReadable)this.dicomSeries, (int)0x20000E, String.class)), (String)((String)TagD.getTagValue((TagReadable)this.dicomSeries, (int)528446, String.class)), (int)Math.max(this.seriesInstanceList.uniqueSopInstanceCount(), related == null ? 0 : related));
    }

    @Override
    protected Boolean doInBackground() {
        return this.downloadManager.startDownload();
    }

    public JProgressBar getProgressBar() {
        return this.progressBar;
    }

    public boolean isStopped() {
        return this.isCancelled();
    }

    public boolean stop() {
        if (!this.isDone()) {
            boolean val = this.cancel();
            this.dicomSeries.setSeriesLoader((SeriesImporter)this);
            this.dicomSeries.setTag(DOWNLOAD_TIME, (Object)this.getDownloadTime());
            LoadSeries.notifyDownloadCompletion(this.dicomModel);
            return val;
        }
        return true;
    }

    public static void notifyDownloadCompletion(DicomModel model) {
        if (!DownloadManager.hasRunningTasks()) {
            model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_GLOBAL_MSG, (Object)model, null, (Object)Messages.getString("stopped")));
        }
    }

    public void resume() {
        if (this.isStopped()) {
            this.getPriority().setPriority(DownloadPriority.COUNTER.getAndDecrement());
            this.cancelAndReplace(this, true);
        }
    }

    @Override
    protected void done() {
        if (!this.isStopped()) {
            Integer splitNb;
            Set list;
            String statisticServicePath;
            this.progressBar.setIndeterminate(false);
            this.dicomSeries.setSeriesLoader(null);
            DownloadManager.removeLoadSeries(this, this.dicomModel);
            LoadSeries.notifyDownloadCompletion(this.dicomModel);
            LoadLocalDicom.seriesPostProcessing(this.dicomSeries, this.dicomModel);
            String loadType = this.getLoadType();
            String seriesUID = (String)this.dicomSeries.getTagValue(this.dicomSeries.getTagID());
            String modality = (String)TagD.getTagValue((TagReadable)this.dicomSeries, (int)524384, String.class);
            int imageNumber = this.getImageNumber();
            long fileSize = this.dicomSeries.getFileSize();
            long time = this.getDownloadTime();
            String rate = this.getDownloadRate(time);
            Integer downloadErrors = (Integer)this.dicomSeries.getTagValue(DOWNLOAD_ERRORS);
            if (downloadErrors == null) {
                downloadErrors = 0;
            }
            LOGGER.info("{} Downloading, type:{} seriesUID:{} modality:{} nbImages:{} size:{} time:{} rate:{} errors:{}", new Object[]{"*PERF*", loadType, seriesUID, modality, imageNumber, fileSize, time, rate, downloadErrors});
            if (LOAD_TYPE_WADO.equals(loadType) && StringUtil.hasText((CharSequence)(statisticServicePath = GuiUtils.getUICore().getStatisticServiceUrl()))) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Content-Type", "application/json");
                URLParameters urlParameters = new URLParameters(map, true);
                PerformanceModel model = new PerformanceModel(loadType, seriesUID, modality, imageNumber, fileSize, time, rate, downloadErrors.intValue());
                try {
                    ClosableURLConnection http = NetworkUtil.getUrlConnection((String)statisticServicePath, (URLParameters)urlParameters);
                    try (OutputStream out = http.getOutputStream();){
                        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                        writer.write(new ObjectMapper().writeValueAsString((Object)model));
                    }
                    URLConnection uRLConnection = http.urlConnection();
                    if (uRLConnection instanceof HttpURLConnection) {
                        HttpURLConnection httpURLConnection = (HttpURLConnection)uRLConnection;
                        NetworkUtil.readResponse((HttpURLConnection)httpURLConnection, (Map)urlParameters.headers());
                    }
                }
                catch (Exception e) {
                    LOGGER.error("Cannot send log to the statisticService service", (Throwable)e);
                }
            }
            this.dicomSeries.removeTag(DOWNLOAD_START_TIME);
            this.dicomSeries.removeTag(DOWNLOAD_TIME);
            this.dicomSeries.removeTag(DOWNLOAD_ERRORS);
            SeriesThumbnail thumbnail = (SeriesThumbnail)this.dicomSeries.getTagValue(TagW.Thumbnail);
            if (thumbnail != null) {
                thumbnail.setProgressBar(null);
                if (thumbnail.getThumbnailPath() == null || this.dicomSeries.getTagValue(TagW.DirectDownloadThumbnail) != null) {
                    thumbnail.reBuildThumbnail(MediaSeries.MEDIA_POSITION.MIDDLE);
                } else {
                    thumbnail.repaint();
                }
            }
            if (DicomModel.isHiddenModality(this.dicomSeries) && (list = (Set)HiddenSeriesManager.getInstance().series2Elements.get(seriesUID)) != null) {
                list.stream().filter(Objects::nonNull).forEach(d -> this.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.UPDATE, (Object)this.dicomModel, null, d)));
            }
            if ((splitNb = (Integer)this.dicomSeries.getTagValue(TagW.SplitSeriesNumber)) != null) {
                this.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.UPDATE, (Object)this.dicomModel, null, (Object)this.dicomSeries));
            } else if (this.dicomSeries.size(null) == 0 && this.dicomSeries.getTagValue(TagW.DicomSpecialElementList) == null && !this.hasDownloadFailed()) {
                this.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.REMOVE, (Object)this.dicomModel, null, (Object)this.dicomSeries));
            }
        }
    }

    public boolean hasDownloadFailed() {
        return this.hasError;
    }

    public boolean isStartDownloading() {
        return this.startDownloading;
    }

    private String getLoadType() {
        WadoParameters wado = (WadoParameters)this.dicomSeries.getTagValue(TagW.WadoParameters);
        if (wado == null || !StringUtil.hasText((CharSequence)wado.getBaseURL())) {
            if (wado != null) {
                return wado.isRequireOnlySOPInstanceUID() ? LOAD_TYPE_DICOMDIR : LOAD_TYPE_URL;
            }
            return LOAD_TYPE_LOCAL;
        }
        return LOAD_TYPE_WADO;
    }

    private int getImageNumber() {
        String uid;
        MediaSeriesGroup study;
        int val = this.dicomSeries.size(null);
        Integer splitNb = (Integer)this.dicomSeries.getTagValue(TagW.SplitSeriesNumber);
        if (splitNb != null && (study = this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.study)) != null && (uid = (String)TagD.getTagValue((TagReadable)this.dicomSeries, (int)0x20000E, String.class)) != null) {
            Collection<MediaSeriesGroup> list = this.dicomModel.getChildren(study);
            list.remove(this.dicomSeries);
            for (MediaSeriesGroup s : list) {
                if (!(s instanceof Series) || !uid.equals(TagD.getTagValue((TagReadable)s, (int)0x20000E))) continue;
                val += ((Series)s).size(null);
            }
        }
        return val;
    }

    private long getDownloadTime() {
        Long val = (Long)this.dicomSeries.getTagValue(DOWNLOAD_START_TIME);
        if (val == null) {
            val = 0L;
        } else {
            val = System.currentTimeMillis() - val;
            this.dicomSeries.setTag(DOWNLOAD_START_TIME, null);
        }
        Long time = (Long)this.dicomSeries.getTagValue(DOWNLOAD_TIME);
        if (time == null) {
            time = 0L;
        }
        return val + time;
    }

    private String getDownloadRate(long time) {
        DecimalFormat format = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        return time <= 0L ? "0" : format.format((double)this.dicomSeries.getFileSize() / (double)time);
    }

    private static Map<String, String> getHttpTags(WadoParameters wadoParams) {
        boolean needsWadoRsAccept;
        boolean hasWadoTags = wadoParams != null && wadoParams.getHttpTaglist() != null;
        boolean hasWadoLogin = wadoParams != null && wadoParams.getWebLogin() != null;
        boolean bl = needsWadoRsAccept = wadoParams != null && wadoParams.isWadoRS();
        if (hasWadoTags || hasWadoLogin || needsWadoRsAccept) {
            HashMap<String, String> map = new HashMap<String, String>();
            if (hasWadoTags) {
                for (HttpTag tag : wadoParams.getHttpTaglist()) {
                    map.put(tag.getKey(), tag.getValue());
                }
            }
            if (hasWadoLogin) {
                map.put("Authorization", "Basic " + wadoParams.getWebLogin());
            }
            if (needsWadoRsAccept && !LoadSeries.containsKeyIgnoreCase(map, "Accept")) {
                map.put("Accept", "multipart/related;type=\"" + String.valueOf(MultipartConstants.DicomContentType.DICOM) + "\";transfer-syntax=*");
            }
            return map;
        }
        return null;
    }

    private static boolean containsKeyIgnoreCase(Map<String, String> map, String key) {
        for (String k : map.keySet()) {
            if (k == null || !k.equalsIgnoreCase(key)) continue;
            return true;
        }
        return false;
    }

    public void startDownloadImageReference(WadoParameters wadoParameters) {
        if (!this.seriesInstanceList.isEmpty()) {
            List sopList = this.seriesInstanceList.getSortedList();
            SopInstance instance = (SopInstance)sopList.get(sopList.size() / 2);
            this.thumbnailManager.createSeriesThumbnail(this, this.progressBar);
            this.thumbnailManager.loadThumbnail(instance, wadoParameters, this.authMethod);
        } else if (Boolean.TRUE.equals(this.dicomSeries.getTagValue(SERIES_BULK_RETRIEVE))) {
            this.thumbnailManager.createSeriesThumbnail(this, this.progressBar);
            this.thumbnailManager.loadSeriesThumbnail(wadoParameters, this.authMethod);
        }
    }

    public void createSeriesThumbnail() {
        this.thumbnailManager.createSeriesThumbnail(this, this.progressBar);
    }

    public DicomSeries getDicomSeries() {
        return this.dicomSeries;
    }

    public SeriesInstanceList getSeriesInstanceList() {
        return this.seriesInstanceList;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public synchronized DownloadPriority getPriority() {
        return this.priority;
    }

    public synchronized void setPriority(DownloadPriority priority) {
        this.priority = priority;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setPriority() {
        boolean change;
        DownloadPriority p = this.getPriority();
        if (p != null && SwingWorker.StateValue.PENDING.equals((Object)this.getState()) && (change = DownloadManager.removeSeriesInQueue(this))) {
            p.setPriority(DownloadPriority.COUNTER.getAndDecrement());
            DownloadManager.offerSeriesInQueue(this);
            List<LoadSeries> list = DownloadManager.getTasks();
            synchronized (list) {
                for (LoadSeries s : DownloadManager.getTasks()) {
                    if (s == this || !SwingWorker.StateValue.STARTED.equals((Object)s.getState())) continue;
                    this.cancelAndReplace(s, true);
                    break;
                }
            }
        }
    }

    protected LoadSeries createResumeTask() {
        return new LoadSeries(this.dicomSeries, this.dicomModel, this.authMethod, this.progressBar, this.concurrentDownloads, this.writeInCache, this.startDownloading);
    }

    public LoadSeries cancelAndReplace(LoadSeries s, boolean restartAllDownload) {
        LoadSeries taskResume = s.createResumeTask();
        s.cancel();
        taskResume.setPriority(s.getPriority());
        taskResume.setPOpeningStrategy(s.getOpeningStrategy());
        Thumbnail thumbnail = (Thumbnail)s.getDicomSeries().getTagValue(TagW.Thumbnail);
        if (thumbnail != null) {
            ThumbnailManager.removeThumbnailMouseAndKeyAdapter(thumbnail);
            this.thumbnailManager.addListenerToThumbnail(thumbnail, taskResume);
        }
        if (restartAllDownload) {
            DownloadManager.addLoadSeries(taskResume, this.dicomModel, true);
        } else {
            taskResume.execute();
        }
        DownloadManager.removeLoadSeries(s, this.dicomModel);
        return taskResume;
    }

    public int getConcurrentDownloads() {
        return this.concurrentDownloads;
    }

    public DicomModel getDicomModel() {
        return this.dicomModel;
    }

    public static boolean isSOPInstanceUIDExist(DicomModel dicomModel, MediaSeriesGroup study, Series<?> series, String sopUID) {
        TagW sopTag = TagD.getUID((TagD.Level)TagD.Level.INSTANCE);
        if (series.hasMediaContains(sopTag, (Object)sopUID)) {
            return true;
        }
        String seriesUID = (String)TagD.getTagValue(series, (int)0x20000E, String.class);
        if (study != null && seriesUID != null) {
            return dicomModel.getChildren(study).stream().filter(group -> series != group && group instanceof Series).filter(group -> seriesUID.equals(TagD.getTagValue((TagReadable)group, (int)0x20000E))).anyMatch(group -> ((Series)group).hasMediaContains(sopTag, (Object)sopUID));
        }
        return false;
    }

    public static enum Status {
        DOWNLOADING,
        COMPLETE,
        ERROR;

    }
}

