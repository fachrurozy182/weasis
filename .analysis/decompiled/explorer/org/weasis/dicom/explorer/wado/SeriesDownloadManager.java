/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dcm4che3.data.Attributes
 *  org.dcm4che3.data.ElementDictionary
 *  org.dcm4che3.img.util.DicomUtils
 *  org.dcm4che3.io.DicomInputStream
 *  org.dcm4che3.io.DicomInputStream$IncludeBulkData
 *  org.dcm4che3.io.DicomOutputStream
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.gui.task.SeriesProgressMonitor
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Thumbnail
 *  org.weasis.core.api.net.HttpStream
 *  org.weasis.core.api.net.HttpUtils
 *  org.weasis.core.api.net.URIUtils
 *  org.weasis.core.api.net.URLParameters
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.api.util.ThreadUtil
 *  org.weasis.core.ui.model.GraphicModel
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.StreamIOException
 *  org.weasis.core.util.StreamUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.DicomImageElement
 *  org.weasis.dicom.codec.DicomMediaIO
 *  org.weasis.dicom.codec.DicomMediaIO$Reading
 *  org.weasis.dicom.codec.DicomMediaIO$ResultContainer
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.DicomSpecialElement
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.codec.utils.DicomMediaUtils
 *  org.weasis.dicom.codec.utils.SeriesInstanceList
 *  org.weasis.dicom.mf.ArcParameters
 *  org.weasis.dicom.mf.SopInstance
 *  org.weasis.dicom.mf.WadoParameters
 *  org.weasis.dicom.param.CancelListener
 *  org.weasis.dicom.web.BoundaryExtractor
 *  org.weasis.dicom.web.MultipartReader
 *  org.weasis.dicom.web.MultipartReader$PartInputStream
 *  org.weasis.dicom.web.MultipartStreamException
 */
package org.weasis.dicom.explorer.wado;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.swing.JProgressBar;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.task.SeriesProgressMonitor;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Thumbnail;
import org.weasis.core.api.net.HttpStream;
import org.weasis.core.api.net.HttpUtils;
import org.weasis.core.api.net.URIUtils;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.api.util.ThreadUtil;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.StreamIOException;
import org.weasis.core.util.StreamUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.DicomSpecialElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.utils.DicomMediaUtils;
import org.weasis.dicom.codec.utils.SeriesInstanceList;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.PluginOpeningStrategy;
import org.weasis.dicom.explorer.rs.RsQueryResult;
import org.weasis.dicom.explorer.wado.DicomSeriesProgressMonitor;
import org.weasis.dicom.explorer.wado.LoadSeries;
import org.weasis.dicom.mf.ArcParameters;
import org.weasis.dicom.mf.SopInstance;
import org.weasis.dicom.mf.WadoParameters;
import org.weasis.dicom.param.CancelListener;
import org.weasis.dicom.web.BoundaryExtractor;
import org.weasis.dicom.web.MultipartReader;
import org.weasis.dicom.web.MultipartStreamException;

public class SeriesDownloadManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeriesDownloadManager.class);
    private static final int INSTANCE_PAGE_SIZE = 1000;
    private final LoadSeries loadSeries;
    private final DicomModel dicomModel;
    private final DicomSeries dicomSeries;
    private final JProgressBar progressBar;
    private final URLParameters urlParams;
    private final AuthMethod authMethod;
    private final boolean writeInCache;
    private final AtomicInteger errors;
    private final AtomicBoolean seriesInitialized;
    private final Semaphore downloadSemaphore;

    public SeriesDownloadManager(LoadSeries loadSeries, DicomModel dicomModel, DicomSeries dicomSeries, JProgressBar progressBar, URLParameters urlParams, AuthMethod authMethod, boolean writeInCache, int concurrentDownloads, AtomicInteger errors, AtomicBoolean seriesInitialized) {
        this.loadSeries = loadSeries;
        this.dicomModel = dicomModel;
        this.dicomSeries = Objects.requireNonNull(dicomSeries);
        this.progressBar = progressBar;
        this.urlParams = urlParams;
        this.authMethod = authMethod;
        this.writeInCache = writeInCache;
        this.errors = errors;
        this.seriesInitialized = seriesInitialized;
        this.downloadSemaphore = new Semaphore(concurrentDownloads);
    }

    public Boolean startDownload() {
        MediaSeriesGroup patient = this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.patient);
        LOGGER.info("Downloading series of {} [{}]", (Object)patient, (Object)this.dicomSeries);
        WadoParameters wado = (WadoParameters)this.dicomSeries.getTagValue(TagW.WadoParameters);
        if (wado == null) {
            return false;
        }
        if (Boolean.TRUE.equals(this.dicomSeries.getTagValue(LoadSeries.SERIES_BULK_RETRIEVE))) {
            if (this.dicomSeries.size(null) == 0 && this.startBulkSeriesDownload(wado).booleanValue()) {
                return true;
            }
            if (!this.completeMissingInstances(wado)) {
                return this.startBulkSeriesDownload(wado);
            }
        }
        SeriesInstanceList seriesInstanceList = this.loadSeries.getSeriesInstanceList();
        List sopList = seriesInstanceList.getSortedList();
        try (ExecutorService imageDownloader = ThreadUtil.newVirtualThreadPerTaskExecutor((String)"Image Downloader");){
            List<Callable<Boolean>> tasks = this.createDownloadTasks(sopList, seriesInstanceList, wado);
            this.dicomSeries.setTag(LoadSeries.DOWNLOAD_START_TIME, (Object)System.currentTimeMillis());
            imageDownloader.invokeAll(tasks);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }

    private List<Callable<Boolean>> createDownloadTasks(List<SopInstance> sopList, SeriesInstanceList seriesInstanceList, WadoParameters wado) {
        MediaSeriesGroup study = this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.study);
        ArrayList<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>(sopList.size());
        int[] downloadOrder = this.generateDownloadOrder(sopList.size());
        this.initializeProgressBar(sopList.size());
        for (int k = 0; k < sopList.size() && !this.loadSeries.isCancelled(); ++k) {
            SopInstance instance = sopList.get(downloadOrder[k]);
            if (this.shouldSkipInstance(seriesInstanceList, instance, study)) continue;
            String url = this.buildDownloadUrl(wado, instance, study);
            LOGGER.debug("Download DICOM instance {} index {}.", (Object)url, (Object)k);
            tasks.add(new Download(this, url));
        }
        return tasks;
    }

    private boolean shouldSkipInstance(SeriesInstanceList seriesInstanceList, SopInstance instance, MediaSeriesGroup study) {
        if (seriesInstanceList.isContainsMultiframes() && seriesInstanceList.getSopInstance(instance.getSopInstanceUID()) != instance) {
            return true;
        }
        if (this.isSOPInstanceUIDExist(study, (Series<?>)this.dicomSeries, instance.getSopInstanceUID())) {
            this.incrementProgressBarValue();
            LOGGER.debug("DICOM instance {} already exists, skip.", (Object)instance.getSopInstanceUID());
            return true;
        }
        return false;
    }

    private String buildDownloadUrl(WadoParameters wado, SopInstance instance, MediaSeriesGroup study) {
        StringBuilder url = new StringBuilder(wado.getBaseURL());
        if (instance.getDirectDownloadFile() != null) {
            url.append(instance.getDirectDownloadFile());
        } else {
            this.appendWadoQueryParameters(url, wado, instance, study);
        }
        url.append(wado.getAdditionalParameters());
        return url.toString();
    }

    private void appendWadoQueryParameters(StringBuilder url, WadoParameters wado, SopInstance instance, MediaSeriesGroup study) {
        url.append("?requestType=WADO");
        if (!wado.isRequireOnlySOPInstanceUID()) {
            String studyUID = (String)TagD.getTagValue((TagReadable)study, (int)0x20000D, String.class);
            String seriesUID = (String)TagD.getTagValue((TagReadable)this.dicomSeries, (int)0x20000E, String.class);
            url.append("&studyUID=").append(studyUID);
            url.append("&seriesUID=").append(seriesUID);
        }
        url.append("&objectUID=").append(instance.getSopInstanceUID());
        url.append("&contentType=application%2Fdicom");
        this.appendTransferSyntaxParameters(url);
    }

    private void appendTransferSyntaxParameters(StringBuilder url) {
        String wadoTsuid = (String)this.dicomSeries.getTagValue(TagW.WadoTransferSyntaxUID);
        if (StringUtil.hasText((CharSequence)wadoTsuid)) {
            url.append("&transferSyntax=").append(wadoTsuid);
            Integer rate = (Integer)this.dicomSeries.getTagValue(TagW.WadoCompressionRate);
            if (rate != null && rate > 0) {
                url.append("&imageQuality=").append(rate);
            }
        }
    }

    private int[] generateDownloadOrder(int size) {
        int[] order = new int[size];
        if (size < 4) {
            for (int i = 0; i < size; ++i) {
                order[i] = i;
            }
            return order;
        }
        boolean[] visited = new boolean[size];
        int pos = 0;
        order[pos++] = 0;
        visited[0] = true;
        order[pos++] = size - 1;
        visited[size - 1] = true;
        int middle = (size - 1) / 2;
        order[pos++] = middle;
        visited[middle] = true;
        for (int step = middle; step > 0; step /= 2) {
            for (int i = 1; i < visited.length; ++i) {
                int start;
                if (!visited[i] || visited[i - 1]) continue;
                for (start = i - 1; start > 0 && !visited[start]; --start) {
                }
                int mid = start + (i - start) / 2;
                if (visited[mid]) continue;
                visited[mid] = true;
                order[pos++] = mid;
            }
        }
        return order;
    }

    private boolean isSOPInstanceUIDExist(MediaSeriesGroup study, Series<?> series, String sopUID) {
        return LoadSeries.isSOPInstanceUIDExist(this.dicomModel, study, series, sopUID);
    }

    private void initializeProgressBar(int max) {
        GuiExecutor.execute(() -> {
            this.progressBar.setMaximum(max);
            this.progressBar.setValue(0);
        });
    }

    private void incrementProgressBarValue() {
        GuiExecutor.execute(() -> this.progressBar.setValue(this.progressBar.getValue() + 1));
    }

    private Path ensureDicomTmpDir() {
        if (!Files.exists(LoadSeries.DICOM_TMP_DIR, new LinkOption[0])) {
            LOGGER.info("DICOM tmp dir not found. Re-creating it!");
            AppProperties.buildAccessibleTempDirectory((String[])new String[]{"downloading"});
        }
        return LoadSeries.DICOM_TMP_DIR;
    }

    private Path createTempFile() throws IOException {
        return Files.createTempFile(this.ensureDicomTmpDir(), "image_", ".dcm", new FileAttribute[0]);
    }

    private boolean completeMissingInstances(WadoParameters wado) {
        List<Attributes> instances;
        SeriesInstanceList seriesInstanceList = this.loadSeries.getSeriesInstanceList();
        if (!seriesInstanceList.getSortedList().isEmpty()) {
            return true;
        }
        MediaSeriesGroup study = this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.study);
        String studyUID = (String)TagD.getTagValue((TagReadable)study, (int)0x20000D, String.class);
        String seriesUID = (String)TagD.getTagValue((TagReadable)this.dicomSeries, (int)0x20000E, String.class);
        if (!StringUtil.hasText((CharSequence)studyUID) || !StringUtil.hasText((CharSequence)seriesUID)) {
            return false;
        }
        String baseUrl = LoadSeries.dicomWebBaseUrl(wado, (MediaSeriesGroup)this.dicomSeries);
        String retrievePrefix = StringUtil.hasText((CharSequence)wado.getBaseURL()) ? "" : baseUrl;
        URLParameters queryParams = RsQueryResult.jsonQueryParameters(this.urlParams.headers());
        String baseQuery = baseUrl + "/studies/" + studyUID + "/series/" + seriesUID + "/instances?includefield=00080016,00080018,00200013";
        int offset = 0;
        do {
            String url = baseQuery + "&offset=" + offset + "&limit=1000";
            try {
                instances = RsQueryResult.parseJSON(url, this.authMethod, queryParams);
            }
            catch (Exception e) {
                LOGGER.error("QIDO-RS enumeration failed for resumed bulk series {}", (Object)seriesUID, (Object)e);
                break;
            }
            if (instances.isEmpty()) break;
            for (Attributes instance : instances) {
                SeriesDownloadManager.addSopInstance(instance, seriesInstanceList, studyUID, seriesUID, retrievePrefix);
            }
            offset += instances.size();
        } while (instances.size() >= 1000);
        return !seriesInstanceList.getSortedList().isEmpty();
    }

    private static void addSopInstance(Attributes instance, SeriesInstanceList seriesInstanceList, String studyUID, String seriesUID, String retrievePrefix) {
        String sopUID = instance.getString(524312);
        if (!StringUtil.hasText((CharSequence)sopUID)) {
            return;
        }
        Integer frame = DicomUtils.getIntegerFromDicomElement((Attributes)instance, (int)2097171, null);
        if (seriesInstanceList.getSopInstance(sopUID, frame) == null) {
            SopInstance sop = new SopInstance(sopUID, instance.getString(524310), frame);
            sop.setDirectDownloadFile(retrievePrefix + "/studies/" + studyUID + "/series/" + seriesUID + "/instances/" + sopUID);
            seriesInstanceList.addSopInstance(sop);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    private Boolean startBulkSeriesDownload(WadoParameters wado) {
        boolean complete;
        int parts;
        HttpStream httpStream;
        Integer count;
        String seriesUID;
        block15: {
            MediaSeriesGroup study = this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.study);
            String studyUID = (String)TagD.getTagValue((TagReadable)study, (int)0x20000D, String.class);
            seriesUID = (String)TagD.getTagValue((TagReadable)this.dicomSeries, (int)0x20000E, String.class);
            if (!StringUtil.hasText((CharSequence)studyUID) || !StringUtil.hasText((CharSequence)seriesUID)) {
                return false;
            }
            String url = LoadSeries.dicomWebBaseUrl(wado, (MediaSeriesGroup)this.dicomSeries) + "/studies/" + studyUID + "/series/" + seriesUID + wado.getAdditionalParameters();
            LOGGER.info("Bulk downloading series {}", (Object)url);
            count = this.fetchSeriesInstanceCount(wado, studyUID, seriesUID);
            if (count != null && count > 0) {
                this.initializeProgressBar(count);
                GuiExecutor.execute(() -> this.progressBar.setIndeterminate(false));
            } else {
                GuiExecutor.execute(() -> this.progressBar.setIndeterminate(true));
            }
            this.dicomSeries.setTag(LoadSeries.DOWNLOAD_START_TIME, (Object)System.currentTimeMillis());
            httpStream = null;
            CancelListener onCancel = null;
            parts = 0;
            try {
                HttpStream toClose = httpStream = HttpUtils.getHttpResponse((String)url, (URLParameters)this.urlParams, (AuthMethod)this.authMethod);
                onCancel = () -> SeriesDownloadManager.closeQuietly((AutoCloseable)toClose);
                this.loadSeries.addCancelListener(onCancel);
                int code = httpStream.getResponseCode();
                if (code >= 400) {
                    if (this.authMethod != null && code == 401) {
                        this.authMethod.resetToken();
                        this.authMethod.getToken();
                    }
                    throw new IOException("Server response code: " + code);
                }
                parts = this.downloadSeriesMultipart(httpStream);
                if (onCancel == null) break block15;
            }
            catch (StreamIOException e) {
                block16: {
                    if (!this.loadSeries.isCancelled()) {
                        this.loadSeries.setHasError(true);
                        this.dicomSeries.setTag(LoadSeries.DOWNLOAD_ERRORS, (Object)this.errors.incrementAndGet());
                        LOGGER.error("Bulk series download failed: {}", (Object)url, (Object)e);
                    }
                    if (onCancel == null) break block16;
                    this.loadSeries.removeCancelListener(onCancel);
                }
                SeriesDownloadManager.closeQuietly((AutoCloseable)httpStream);
                GuiExecutor.execute(() -> this.progressBar.setIndeterminate(false));
            }
            catch (IOException e2) {
                block17: {
                    if (!this.loadSeries.isCancelled()) {
                        this.dicomSeries.setTag(LoadSeries.DOWNLOAD_ERRORS, (Object)this.errors.incrementAndGet());
                        LOGGER.error("Bulk series download failed: {}", (Object)url, (Object)e2);
                    }
                    if (onCancel == null) break block17;
                    {
                        catch (Throwable throwable) {
                            if (onCancel != null) {
                                this.loadSeries.removeCancelListener(onCancel);
                            }
                            SeriesDownloadManager.closeQuietly(httpStream);
                            GuiExecutor.execute(() -> this.progressBar.setIndeterminate(false));
                            throw throwable;
                        }
                    }
                    this.loadSeries.removeCancelListener(onCancel);
                }
                SeriesDownloadManager.closeQuietly((AutoCloseable)httpStream);
                GuiExecutor.execute(() -> this.progressBar.setIndeterminate(false));
            }
            this.loadSeries.removeCancelListener(onCancel);
        }
        SeriesDownloadManager.closeQuietly((AutoCloseable)httpStream);
        GuiExecutor.execute(() -> this.progressBar.setIndeterminate(false));
        boolean bl = complete = this.loadSeries.isCancelled() || count == null || parts >= count;
        if (!complete) {
            LOGGER.info("Bulk retrieve returned {}/{} instances for series {}; completing per-instance", new Object[]{parts, count, seriesUID});
        }
        return complete;
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (Exception e) {
                LOGGER.debug("Error closing HTTP stream", (Throwable)e);
            }
        }
    }

    private Integer fetchSeriesInstanceCount(WadoParameters wado, String studyUID, String seriesUID) {
        Integer queried = (Integer)TagD.getTagValue((TagReadable)this.dicomSeries, (int)2101769, Integer.class);
        if (queried != null && queried > 0) {
            return queried;
        }
        int n = RsQueryResult.seriesInstanceCount(LoadSeries.dicomWebBaseUrl(wado, (MediaSeriesGroup)this.dicomSeries), studyUID, seriesUID, this.urlParams, this.authMethod);
        return n > 0 ? Integer.valueOf(n) : null;
    }

    private int downloadSeriesMultipart(HttpStream httpStream) throws IOException {
        String contentType = httpStream.getHeaderField("Content-Type");
        byte[] boundary = BoundaryExtractor.extractBoundary((String)contentType, (String)"multipart/related");
        if (boundary == null) {
            throw new IOException("No boundary in Content-Type: " + contentType);
        }
        int[] overrideList = Optional.ofNullable((WadoParameters)this.dicomSeries.getTagValue(TagW.WadoParameters)).map(ArcParameters::getOverrideDicomTagIDList).orElse(null);
        int parts = 0;
        try (MultipartReader reader = new MultipartReader(httpStream.getInputStream(), boundary);){
            reader.skipFirstBoundary();
            do {
                boolean written;
                if (this.loadSeries.isCancelled()) {
                    break;
                }
                reader.readHeaders();
                boolean isFirstImage = this.dicomSeries.size(null) == 0 && this.seriesInitialized.compareAndSet(false, true);
                Path tempFile = this.createTempFile();
                try (MultipartReader.PartInputStream partStream = reader.newPartInputStream();){
                    SeriesProgressMonitor monitored = new SeriesProgressMonitor((Series)this.dicomSeries, (InputStream)partStream);
                    written = overrideList == null ? FileUtil.writeStream((InputStream)monitored, (Path)tempFile, (boolean)false) == -1 : this.writeFileWithOverrides((InputStream)monitored, tempFile, overrideList) == -1;
                }
                if (!written) {
                    FileUtil.delete((Path)tempFile);
                    continue;
                }
                ++parts;
                try {
                    this.ingest(tempFile, isFirstImage, true);
                }
                catch (IOException e) {
                    this.dicomSeries.setTag(LoadSeries.DOWNLOAD_ERRORS, (Object)this.errors.incrementAndGet());
                    LOGGER.error("Failed to ingest a series part", (Throwable)e);
                }
            } while (reader.readBoundary());
        }
        catch (MultipartStreamException e) {
            throw new IOException("Failed to parse multipart content", e);
        }
        return parts;
    }

    private int writeFileWithOverrides(InputStream in, Path targetFile, int[] overrideList) throws StreamIOException {
        int n;
        DicomInputStream dis = new DicomInputStream(in);
        try {
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            Attributes dataset = dis.readDataset();
            String tsuid = dis.getTransferSyntax();
            this.applyOverrides(dataset, overrideList);
            try (DicomOutputStream dos = new DicomOutputStream(targetFile.toFile());){
                dos.writeDataset(dataset.createFileMetaInformation(tsuid), dataset);
                dos.finish();
                dos.flush();
            }
            this.cleanupBulkDataFiles(dis);
            n = -1;
        }
        catch (Throwable throwable) {
            try {
                try {
                    dis.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (InterruptedIOException e) {
                FileUtil.delete((Path)targetFile);
                LOGGER.error("Interrupted while writing: {}", (Object)e.getMessage());
                return e.bytesTransferred;
            }
            catch (IOException e) {
                FileUtil.delete((Path)targetFile);
                throw new StreamIOException((Throwable)e);
            }
            catch (Exception e) {
                FileUtil.delete((Path)targetFile);
                LOGGER.error("Error writing DICOM file", (Throwable)e);
                return 0;
            }
        }
        dis.close();
        return n;
    }

    private void applyOverrides(Attributes dataset, int[] overrideList) {
        SeriesDownloadManager.applyOverrides(dataset, overrideList, this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.patient), this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.study));
    }

    public static void applyOverrides(Attributes dataset, int[] overrideList, MediaSeriesGroup patient, MediaSeriesGroup study) {
        if (dataset == null || overrideList == null) {
            return;
        }
        List groups = Stream.of(patient, study).filter(Objects::nonNull).toList();
        ElementDictionary dic = ElementDictionary.getStandardElementDictionary();
        block0: for (int tag : overrideList) {
            if (tag == 0x20000D) {
                LOGGER.debug("Study Instance UID cannot be overridden, ignoring it in the override list");
                continue;
            }
            for (MediaSeriesGroup group : groups) {
                TagW tagElement = group.getTagElement(tag);
                if (tagElement == null) continue;
                DicomMediaUtils.fillAttributes((Attributes)dataset, (TagW)tagElement, (Object)group.getTagValue(tagElement), (ElementDictionary)dic);
                continue block0;
            }
        }
    }

    private void cleanupBulkDataFiles(DicomInputStream dis) {
        List bulkFiles = dis.getBulkDataFiles();
        if (bulkFiles != null) {
            bulkFiles.forEach(file -> FileUtil.delete((Path)file.toPath()));
        }
    }

    private void ingest(Path file, boolean isFirstImage, boolean skipIfExists) throws IOException {
        DicomMediaIO dicomReader = new DicomMediaIO(file.toFile());
        if (skipIfExists && dicomReader.isReadableDicom() && this.isAlreadyStored(dicomReader)) {
            dicomReader.close();
            FileUtil.delete((Path)file);
            this.incrementProgressBarValue();
            return;
        }
        if (dicomReader.isReadableDicom() && isFirstImage) {
            this.updateSeriesMetadata(dicomReader);
        }
        this.handleReadableFile(dicomReader, file, isFirstImage);
        this.incrementProgressBarValue();
    }

    private boolean isAlreadyStored(DicomMediaIO reader) {
        String sopUID = (String)TagD.getTagValue((TagReadable)reader, (int)524312, String.class);
        if (sopUID == null) {
            return false;
        }
        MediaSeriesGroup study = this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.study);
        return this.isSOPInstanceUIDExist(study, (Series<?>)this.dicomSeries, sopUID);
    }

    private void updateSeriesMetadata(DicomMediaIO reader) {
        MediaSeriesGroup patient = this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.patient);
        MediaSeriesGroup study = this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.study);
        reader.writeMetaData(patient);
        reader.writeMetaData(study);
        reader.writeMetaData((MediaSeriesGroup)this.dicomSeries);
        GuiExecutor.invokeAndWait(() -> {
            Thumbnail thumb = (Thumbnail)this.dicomSeries.getTagValue(TagW.Thumbnail);
            if (thumb != null) {
                thumb.repaint();
            }
            this.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.UPDATE_PARENT, (Object)this.dicomModel, null, (Object)this.dicomSeries));
        });
    }

    private void handleReadableFile(DicomMediaIO reader, Path file, boolean isFirstImage) {
        DicomMediaIO.Reading reading = reader.getReadingStatus();
        if (reading == DicomMediaIO.Reading.READABLE) {
            if (file.startsWith(AppProperties.APP_TEMP_DIR)) {
                reader.getFileCache().setOriginalTempFile(file);
            }
            this.updateUI(reader, isFirstImage);
        } else if (reading == DicomMediaIO.Reading.ERROR) {
            this.errors.incrementAndGet();
        } else if (reading == DicomMediaIO.Reading.UNSUPPORTED) {
            LOGGER.info("Skipping unsupported DICOM SOP Class for file: {}", (Object)file);
        }
    }

    private void updateUI(DicomMediaIO reader, boolean firstImageToDisplay) {
        DicomSpecialElement specialElement;
        DicomMediaIO.ResultContainer result = reader.getMediaElement(factory -> factory.buildDicomSpecialElement(reader));
        DicomImageElement[] medias = result.getImage();
        if (medias != null) {
            if (firstImageToDisplay) {
                this.reconcilePatientAndStudyUIDs(reader);
            }
            for (DicomImageElement media : medias) {
                this.applyPresentationModel(media);
                this.dicomModel.applySplittingRules(this.dicomSeries, media);
            }
        }
        if ((specialElement = result.getSpecialElement()) != null) {
            this.dicomModel.applySplittingRules(this.dicomSeries, specialElement);
        }
        this.openViewerIfNeeded();
        this.refreshThumbnail();
    }

    private void reconcilePatientAndStudyUIDs(DicomMediaIO reader) {
        String newStudyUID;
        String oldStudyUID;
        MediaSeriesGroup study;
        String newPatientUID;
        String oldPatientUID;
        MediaSeriesGroup patient = this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.patient);
        if (patient != null && !Objects.equals(oldPatientUID = (String)patient.getTagValue(TagW.PatientPseudoUID), newPatientUID = (String)reader.getTagValue(TagW.PatientPseudoUID))) {
            this.dicomModel.mergePatientUID(oldPatientUID, newPatientUID, this.loadSeries.getOpeningStrategy());
        }
        if ((study = this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.study)) != null && !Objects.equals(oldStudyUID = (String)study.getTagValue(TagD.get((int)0x20000D)), newStudyUID = (String)TagD.getTagValue((TagReadable)reader, (int)0x20000D, String.class))) {
            this.dicomModel.mergeStudyUID(oldStudyUID, newStudyUID);
        }
    }

    private void openViewerIfNeeded() {
        PluginOpeningStrategy openingStrategy;
        MediaSeriesGroup patient = this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.patient);
        if (patient != null && (openingStrategy = this.loadSeries.getOpeningStrategy()) != null) {
            openingStrategy.openViewerPlugin(patient, this.dicomModel, (Series<?>)this.dicomSeries);
        }
    }

    private void refreshThumbnail() {
        GuiExecutor.execute(() -> {
            Thumbnail thumb = (Thumbnail)this.dicomSeries.getTagValue(TagW.Thumbnail);
            if (thumb != null) {
                thumb.repaint();
            }
        });
    }

    private void applyPresentationModel(DicomImageElement media) {
        GraphicModel model;
        Object object;
        SopInstance sop;
        String sopUID = (String)TagD.getTagValue((TagReadable)media, (int)524312, String.class);
        SeriesInstanceList seriesInstanceList = this.loadSeries.getSeriesInstanceList();
        SopInstance sopInstance = sop = seriesInstanceList.isContainsMultiframes() ? seriesInstanceList.getSopInstance(sopUID, (Integer)TagD.getTagValue((TagReadable)media, (int)2097171, Integer.class)) : seriesInstanceList.getSopInstance(sopUID);
        if (sop != null && (object = sop.getGraphicModel()) instanceof GraphicModel && this.shouldApplyModel(media, model = (GraphicModel)object, sopUID)) {
            media.setTag(TagW.PresentationModel, (Object)model);
        }
    }

    private boolean shouldApplyModel(DicomImageElement media, GraphicModel model, String sopUID) {
        Object object;
        int frames = media.getMediaReader().getMediaElementNumber();
        if (frames <= 1 || !((object = media.getKey()) instanceof Integer)) {
            return true;
        }
        Integer frameKey = (Integer)object;
        String seriesUID = (String)TagD.getTagValue((TagReadable)media, (int)0x20000E, String.class);
        return model.getReferencedSeries().stream().filter(s -> s.getUuid().equals(seriesUID)).flatMap(s -> s.getImages().stream()).filter(img -> img.getUuid().equals(sopUID)).anyMatch(img -> img.getFrames() == null || img.getFrames().contains(frameKey));
    }

    class Download
    implements Callable<Boolean> {
        private final String url;
        final /* synthetic */ SeriesDownloadManager this$0;

        public Download(SeriesDownloadManager this$0, String url) {
            SeriesDownloadManager seriesDownloadManager = this$0;
            Objects.requireNonNull(seriesDownloadManager);
            this.this$0 = seriesDownloadManager;
            this.url = url;
        }

        @Override
        public Boolean call() {
            try {
                this.this$0.downloadSemaphore.acquire();
                try {
                    this.process();
                }
                finally {
                    this.this$0.downloadSemaphore.release();
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Boolean.FALSE;
            }
            catch (StreamIOException e) {
                this.this$0.loadSeries.setHasError(true);
                this.markAsError();
                LOGGER.error("Download failed: {}", (Object)this.url, (Object)e);
            }
            catch (IOException | URISyntaxException e) {
                this.markAsError();
                LOGGER.error("Download failed: {}", (Object)this.url, (Object)e);
            }
            return Boolean.TRUE;
        }

        private void markAsError() {
            this.this$0.dicomSeries.setTag(LoadSeries.DOWNLOAD_ERRORS, (Object)this.this$0.errors.incrementAndGet());
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Unable to fully structure code
         */
        private void process() throws IOException, URISyntaxException {
            block11: {
                block12: {
                    isFirstImage = this.this$0.dicomSeries.size(null) == 0 && this.this$0.seriesInitialized.compareAndSet(false, true) != false;
                    uri = URIUtils.getURI((String)this.url);
                    if (URIUtils.isFileURI((URI)uri)) {
                        this.ingestLocalFile(uri, isFirstImage);
                        return;
                    }
                    httpStream = HttpUtils.getHttpResponse((String)this.url, (URLParameters)this.this$0.urlParams, (AuthMethod)this.this$0.authMethod);
                    this.handleAuthenticationIfNeeded(httpStream);
                    stream = httpStream.getInputStream();
                    v0 = tempFile = this.this$0.writeInCache != false ? this.this$0.createTempFile() : URIUtils.toPath((URI)uri);
                    if (!this.this$0.writeInCache) ** GOTO lbl25
                    SeriesDownloadManager.LOGGER.debug("Downloading DICOM instance {} to {}", (Object)this.url, (Object)tempFile.getFileName());
                    bytesTransferred = this.downloadToCache(httpStream, tempFile);
                    if (bytesTransferred < 0) break block11;
                    if (stream == null) break block12;
                    stream.close();
                }
                this.this$0.progressBar.setIndeterminate(this.this$0.progressBar.getMaximum() < 3);
                return;
            }
            try {
                block13: {
                    tempFile = this.moveToExportDir(tempFile);
lbl25:
                    // 2 sources

                    StreamUtil.safeClose((AutoCloseable)stream);
                    this.this$0.ingest(tempFile, isFirstImage, false);
                    break block13;
                    {
                        catch (Throwable var5_6) {
                            throw var5_6;
                        }
                    }
                    finally {
                        if (stream != null) {
                            stream.close();
                        }
                    }
                }
                this.this$0.progressBar.setIndeterminate(this.this$0.progressBar.getMaximum() < 3);
            }
            catch (Throwable var7_9) {
                this.this$0.progressBar.setIndeterminate(this.this$0.progressBar.getMaximum() < 3);
                throw var7_9;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        private void ingestLocalFile(URI uri, boolean isFirstImage) throws IOException {
            Path file;
            block9: {
                InputStream in;
                Path tempFile;
                block10: {
                    Path localFile = URIUtils.getAbsolutePath((URI)uri);
                    if (localFile == null) throw new IOException("Local file not found: " + this.url);
                    if (!Files.exists(localFile, new LinkOption[0])) {
                        throw new IOException("Local file not found: " + this.url);
                    }
                    try {
                        block11: {
                            file = localFile;
                            if (!this.this$0.writeInCache) break block9;
                            tempFile = this.this$0.createTempFile();
                            LOGGER.debug("Copying DICOM instance {} to {}", (Object)localFile, (Object)tempFile.getFileName());
                            in = Files.newInputStream(localFile, new OpenOption[0]);
                            try {
                                if (FileUtil.writeStream((InputStream)((Object)new DicomSeriesProgressMonitor((Series)this.this$0.dicomSeries, in, false)), (Path)tempFile, (boolean)false) < 0) break block10;
                                FileUtil.delete((Path)tempFile);
                                if (in == null) break block11;
                            }
                            catch (Throwable throwable) {
                                if (in == null) throw throwable;
                                try {
                                    in.close();
                                    throw throwable;
                                }
                                catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                                throw throwable;
                            }
                            in.close();
                        }
                        this.this$0.progressBar.setIndeterminate(this.this$0.progressBar.getMaximum() < 3);
                        return;
                    }
                    catch (Throwable throwable) {
                        this.this$0.progressBar.setIndeterminate(this.this$0.progressBar.getMaximum() < 3);
                        throw throwable;
                    }
                }
                if (in != null) {
                    in.close();
                }
                file = this.moveToExportDir(tempFile);
            }
            this.this$0.ingest(file, isFirstImage, false);
            this.this$0.progressBar.setIndeterminate(this.this$0.progressBar.getMaximum() < 3);
        }

        private void handleAuthenticationIfNeeded(HttpStream httpStream) throws IOException {
            int code = httpStream.getResponseCode();
            if (code >= 400) {
                if (this.this$0.authMethod != null && code == 401) {
                    this.this$0.authMethod.resetToken();
                    this.this$0.authMethod.getToken();
                }
                throw new IOException("Server response code: " + code);
            }
        }

        private Path moveToExportDir(Path tempFile) throws IOException {
            Path targetPath = DicomMediaIO.DICOM_EXPORT_DIR.resolve(tempFile.getFileName());
            return Files.move(tempFile, targetPath, new CopyOption[0]);
        }

        private int downloadToCache(HttpStream response, Path targetFile) throws IOException {
            int bytesTransferred;
            WadoParameters wadoParams = (WadoParameters)this.this$0.dicomSeries.getTagValue(TagW.WadoParameters);
            int[] overrideList = Optional.ofNullable(wadoParams).map(ArcParameters::getOverrideDicomTagIDList).orElse(null);
            int n = bytesTransferred = overrideList == null ? this.downloadWithoutOverride(response, targetFile, wadoParams) : this.downloadWithOverride(response, targetFile, overrideList);
            if (bytesTransferred == Integer.MIN_VALUE) {
                LOGGER.warn("Unsupported TSUID, retrying with default");
                bytesTransferred = this.retryWithDefaultTransferSyntax(targetFile, overrideList);
            }
            return bytesTransferred;
        }

        private int downloadWithoutOverride(HttpStream response, Path targetFile, WadoParameters wadoParams) throws IOException {
            if (wadoParams != null && wadoParams.isWadoRS()) {
                return this.downloadWadoRS(response, targetFile);
            }
            return FileUtil.writeStream((InputStream)((Object)new DicomSeriesProgressMonitor((Series)this.this$0.dicomSeries, response.getInputStream(), false)), (Path)targetFile);
        }

        private int downloadWadoRS(HttpStream response, Path targetFile) throws IOException {
            int n;
            String contentType = response.getHeaderField("Content-Type");
            byte[] boundary = BoundaryExtractor.extractBoundary((String)contentType, (String)"multipart/related");
            if (boundary == null) {
                throw new IOException("No boundary in Content-Type: " + contentType);
            }
            MultipartReader reader = new MultipartReader(response.getInputStream(), boundary);
            try {
                int totalBytes;
                reader.skipFirstBoundary();
                do {
                    reader.readHeaders();
                    try (MultipartReader.PartInputStream partStream = reader.newPartInputStream();){
                        totalBytes = FileUtil.writeStream((InputStream)new SeriesProgressMonitor((Series)this.this$0.dicomSeries, (InputStream)partStream), (Path)targetFile, (boolean)false);
                    }
                } while (reader.readBoundary());
                n = totalBytes;
            }
            catch (Throwable throwable) {
                try {
                    try {
                        reader.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
                catch (MultipartStreamException e) {
                    throw new IOException("Failed to parse multipart content", e);
                }
            }
            reader.close();
            return n;
        }

        private int downloadWithOverride(HttpStream response, Path targetFile, int[] overrideList) throws IOException {
            return this.this$0.writeFileWithOverrides((InputStream)((Object)new DicomSeriesProgressMonitor((Series)this.this$0.dicomSeries, response.getInputStream(), false)), targetFile, overrideList);
        }

        private int retryWithDefaultTransferSyntax(Path targetFile, int[] overrideList) throws IOException {
            try (InputStream stream = this.replaceToDefaultTSUID().getInputStream();){
                int n = overrideList == null ? FileUtil.writeStream((InputStream)((Object)new DicomSeriesProgressMonitor((Series)this.this$0.dicomSeries, stream, false)), (Path)targetFile) : this.this$0.writeFileWithOverrides((InputStream)((Object)new DicomSeriesProgressMonitor((Series)this.this$0.dicomSeries, stream, false)), targetFile, overrideList);
                return n;
            }
        }

        private HttpStream replaceToDefaultTSUID() throws IOException {
            String modifiedUrl = this.url.contains("&transferSyntax=") ? this.url.replaceFirst("&transferSyntax=[^&]*", "&transferSyntax=1.2.840.10008.1.2.1") : this.url + "&transferSyntax=1.2.840.10008.1.2.1";
            return HttpUtils.getHttpResponse((String)modifiedUrl, (URLParameters)this.this$0.urlParams, (AuthMethod)this.this$0.authMethod);
        }
    }
}

