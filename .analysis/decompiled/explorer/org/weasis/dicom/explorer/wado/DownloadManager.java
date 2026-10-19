/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.gui.util.WinUtil
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Thumbnail
 *  org.weasis.core.api.net.ClosableURLConnection
 *  org.weasis.core.api.net.NetworkUtil
 *  org.weasis.core.api.net.URLParameters
 *  org.weasis.core.api.util.GzipManager
 *  org.weasis.core.api.util.ThreadUtil
 *  org.weasis.core.ui.util.ColorLayerUI
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.StreamIOException
 *  org.weasis.core.util.StringUtil
 *  org.weasis.core.util.StringUtil$Suffix
 *  org.weasis.dicom.codec.DicomSeries
 */
package org.weasis.dicom.explorer.wado;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Thumbnail;
import org.weasis.core.api.net.ClosableURLConnection;
import org.weasis.core.api.net.NetworkUtil;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.api.util.GzipManager;
import org.weasis.core.api.util.ThreadUtil;
import org.weasis.core.ui.util.ColorLayerUI;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.StreamIOException;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.DicomSorter;
import org.weasis.dicom.explorer.HangingProtocols;
import org.weasis.dicom.explorer.Messages;
import org.weasis.dicom.explorer.wado.DownloadException;
import org.weasis.dicom.explorer.wado.DownloadPriority;
import org.weasis.dicom.explorer.wado.JsonManifestParser;
import org.weasis.dicom.explorer.wado.LoadSeries;
import org.weasis.dicom.explorer.wado.ReaderParams;
import org.weasis.dicom.explorer.wado.XmlManifestParser;

public class DownloadManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadManager.class);
    public static final String CONCURRENT_SERIES = "download.concurrent.series";
    private static final List<LoadSeries> TASKS = new ArrayList<LoadSeries>();
    private static final BlockingQueue<Runnable> UNIQUE_QUEUE = new PriorityBlockingQueue<Runnable>(10, new PriorityTaskComparator());
    public static final ThreadPoolExecutor UNIQUE_EXECUTOR = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, UNIQUE_QUEUE);
    private static final BlockingQueue<Runnable> PRIORITY_QUEUE = new PriorityBlockingQueue<Runnable>(10, new PriorityTaskComparator());
    public static final ThreadPoolExecutor CONCURRENT_EXECUTOR = new ThreadPoolExecutor(GuiUtils.getUICore().getSystemPreferences().getIntProperty("download.concurrent.series", 3), GuiUtils.getUICore().getSystemPreferences().getIntProperty("download.concurrent.series", 3), 0L, TimeUnit.MILLISECONDS, PRIORITY_QUEUE, ThreadUtil.namedThreadFactory((String)"SeriesDownloader"));

    private DownloadManager() {
    }

    public static List<LoadSeries> getTasks() {
        return TASKS;
    }

    public static HangingProtocols.OpeningViewer getOpeningViewer() {
        return HangingProtocols.OpeningViewer.ALL_PATIENTS;
    }

    public static boolean removeSeriesInQueue(LoadSeries series) {
        return series.getPriority().hasConcurrentDownload() ? PRIORITY_QUEUE.remove(series) : UNIQUE_QUEUE.remove(series);
    }

    public static void offerSeriesInQueue(LoadSeries series) {
        if (series.getPriority().hasConcurrentDownload()) {
            if (!PRIORITY_QUEUE.offer(series)) {
                LOGGER.warn("Cannot add series {} to download queue", (Object)series.getDicomSeries());
            }
        } else if (!UNIQUE_QUEUE.offer(series)) {
            LOGGER.warn("Cannot add series {} to download queue", (Object)series.getDicomSeries());
        }
    }

    public static synchronized void addLoadSeries(LoadSeries series, DicomModel dicomModel, boolean startLoading) {
        if (series != null) {
            boolean isHiddenSeries = DicomModel.isHiddenModality(series.getDicomSeries());
            if (startLoading || isHiddenSeries) {
                DownloadManager.offerSeriesInQueue(series);
            } else {
                GuiExecutor.execute(() -> {
                    series.getProgressBar().setValue(0);
                    series.stop();
                });
            }
            if (dicomModel != null) {
                dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_START, (Object)dicomModel, null, (Object)series));
            }
            if (!isHiddenSeries && !TASKS.contains(series)) {
                TASKS.add(series);
            }
        }
    }

    public static synchronized void removeLoadSeries(LoadSeries series, DicomModel dicomModel) {
        if (series != null) {
            TASKS.remove(series);
            if (dicomModel != null) {
                if (series.isCancelled()) {
                    dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_CANCEL, (Object)dicomModel, null, (Object)series));
                } else {
                    dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.LOADING_STOP, (Object)dicomModel, null, (Object)series));
                }
            }
            if (TASKS.isEmpty()) {
                CONCURRENT_EXECUTOR.setCorePoolSize(GuiUtils.getUICore().getSystemPreferences().getIntProperty(CONCURRENT_SERIES, 3));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean hasRunningTasks() {
        List<LoadSeries> list = DownloadManager.getTasks();
        synchronized (list) {
            return DownloadManager.getTasks().stream().anyMatch(loadSeries -> SwingWorker.StateValue.STARTED.equals((Object)loadSeries.getState()));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void stopDownloading(DicomSeries series, DicomModel dicomModel) {
        if (series != null) {
            List<LoadSeries> list = DownloadManager.getTasks();
            synchronized (list) {
                for (LoadSeries loading : DownloadManager.getTasks()) {
                    if (loading.getDicomSeries() != series) continue;
                    DownloadManager.removeLoadSeries(loading, dicomModel);
                    DownloadManager.removeSeriesInQueue(loading);
                    if (SwingWorker.StateValue.STARTED.equals((Object)loading.getState())) {
                        loading.cancel();
                    }
                    series.setSeriesLoader(null);
                    break;
                }
            }
        }
    }

    public static void resume() {
        DownloadManager.handleAllSeries(LoadSeries::resume);
    }

    public static void stop() {
        DownloadManager.handleAllSeries(LoadSeries::stop);
    }

    private static void handleAllSeries(LoadSeriesHandler handler) {
        for (LoadSeries loadSeries : new ArrayList<LoadSeries>(DownloadManager.getTasks())) {
            handler.handle(loadSeries);
            Thumbnail thumbnail = (Thumbnail)loadSeries.getDicomSeries().getTagValue(TagW.Thumbnail);
            if (thumbnail == null) continue;
            thumbnail.repaint();
        }
    }

    public static Collection<LoadSeries> buildDicomSeries(URI uri, DicomModel model) throws DownloadException {
        HashMap<String, LoadSeries> seriesMap = new HashMap<String, LoadSeries>();
        try {
            Path manifestFile = DownloadManager.downloadManifest(uri);
            ReaderParams params = new ReaderParams(model, seriesMap);
            if (DownloadManager.isJsonManifest(manifestFile)) {
                JsonManifestParser.parse(manifestFile, params);
            } else {
                XmlManifestParser.parse(manifestFile, params);
            }
        }
        catch (StreamIOException e) {
            throw new DownloadException(DownloadManager.getErrorMessage(uri), e);
        }
        catch (Exception e) {
            String message = DownloadManager.getErrorMessage(uri);
            LOGGER.error("{}", (Object)message, (Object)e);
            GuiExecutor.execute(() -> {
                ColorLayerUI layer = ColorLayerUI.createTransparentLayerUI((Component)GuiUtils.getUICore().getBaseArea());
                JOptionPane.showOptionDialog(WinUtil.getValidComponent((Component)ColorLayerUI.getContentPane((ColorLayerUI)layer)), StringUtil.getTruncatedString((String)message, (int)130, (StringUtil.Suffix)StringUtil.Suffix.THREE_PTS), null, -1, 0, null, null, null);
                if (layer != null) {
                    layer.hideUI();
                }
            });
        }
        return seriesMap.values();
    }

    private static Path downloadManifest(URI uri) throws IOException {
        String path = uri.getPath();
        URLParameters urlParameters = new URLParameters(DownloadManager.manifestAcceptHeaders(), StringUtil.getInt((String)System.getProperty("UrlConnectionTimeout"), (int)7000), StringUtil.getInt((String)System.getProperty("UrlReadTimeout"), (int)15000) * 2);
        ClosableURLConnection urlConnection = NetworkUtil.getUrlConnection((URL)uri.toURL(), (URLParameters)urlParameters);
        LOGGER.info("Downloading manifest: {}", (Object)path);
        try (InputStream urlInputStream = urlConnection.getInputStream();){
            FilterInputStream stream;
            PushbackInputStream pb = new PushbackInputStream(new BufferedInputStream(urlInputStream), 4);
            boolean gzip = DownloadManager.startsWithGzipMagic(pb);
            FilterInputStream filterInputStream = stream = gzip ? new GZIPInputStream(pb) : pb;
            if (!gzip && uri.toString().startsWith("file:") && (path.endsWith(".xml") || path.endsWith(".json"))) {
                Path path2 = Path.of(path, new String[0]);
                return path2;
            }
            Path tempFile = Files.createTempFile(AppProperties.APP_TEMP_DIR, "wado_", ".manifest", new FileAttribute[0]);
            FileUtil.writeStreamWithIOException((InputStream)stream, (Path)tempFile);
            Path path3 = tempFile;
            return path3;
        }
    }

    private static boolean startsWithGzipMagic(PushbackInputStream in) throws IOException {
        byte[] header = new byte[4];
        int n = in.read(header, 0, 4);
        if (n > 0) {
            in.unread(header, 0, n);
        }
        return n == 4 && GzipManager.isGzip((byte[])header);
    }

    private static boolean isJsonManifest(Path file) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file, new OpenOption[0]));){
            int c = ((InputStream)in).read();
            if (c == 239) {
                ((InputStream)in).read();
                ((InputStream)in).read();
                c = ((InputStream)in).read();
            }
            while (c != -1 && Character.isWhitespace(c)) {
                c = ((InputStream)in).read();
            }
            boolean bl = c == 123;
            return bl;
        }
    }

    private static Map<String, String> manifestAcceptHeaders() {
        String accept = "json".equalsIgnoreCase(System.getProperty("weasis.manifest.accept")) ? "application/json" : "application/xml";
        return Map.of("Accept", accept, "Accept-Encoding", "gzip");
    }

    private static String getErrorMessage(URI uri) {
        return Messages.getString("DownloadManager.error_load_xml") + ": " + uri.toString();
    }

    @FunctionalInterface
    private static interface LoadSeriesHandler {
        public void handle(LoadSeries var1);
    }

    public static class PriorityTaskComparator
    implements Comparator<Runnable> {
        @Override
        public int compare(Runnable r1, Runnable r2) {
            LoadSeries o1 = (LoadSeries)r1;
            LoadSeries o2 = (LoadSeries)r2;
            DownloadPriority val1 = o1.getPriority();
            DownloadPriority val2 = o2.getPriority();
            int rep = val1.getPriority().compareTo(val2.getPriority());
            if (rep != 0) {
                return rep;
            }
            if (val1.getPatient() != val2.getPatient()) {
                rep = DicomSorter.PATIENT_COMPARATOR.compare(val1.getPatient(), val2.getPatient());
            }
            if (rep != 0) {
                return rep;
            }
            if (val1.getStudy() != val2.getStudy()) {
                rep = DicomSorter.STUDY_COMPARATOR.compare(val1.getStudy(), val2.getStudy());
            }
            if (rep != 0) {
                return rep;
            }
            return DicomSorter.SERIES_COMPARATOR.compare(val1.getSeries(), val2.getSeries());
        }
    }

    public static enum Status {
        DOWNLOADING,
        COMPLETE,
        ERROR;

    }
}

