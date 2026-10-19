/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dcm4che3.data.Attributes
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.ObservableEvent
 *  org.weasis.core.api.explorer.ObservableEvent$BasicAction
 *  org.weasis.core.api.gui.util.GuiExecutor
 *  org.weasis.core.api.media.data.MediaSeries
 *  org.weasis.core.api.media.data.MediaSeries$MEDIA_POSITION
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.SeriesThumbnail
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Thumbnail
 *  org.weasis.core.api.net.HttpStream
 *  org.weasis.core.api.net.HttpUtils
 *  org.weasis.core.api.net.URLParameters
 *  org.weasis.core.api.net.auth.AuthMethod
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.DicomSeries
 *  org.weasis.dicom.codec.HiddenSeriesManager
 *  org.weasis.dicom.codec.TagD
 *  org.weasis.dicom.mf.SopInstance
 *  org.weasis.dicom.mf.WadoParameters
 */
package org.weasis.dicom.explorer.wado;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.JProgressBar;
import org.dcm4che3.data.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.SeriesThumbnail;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Thumbnail;
import org.weasis.core.api.net.HttpStream;
import org.weasis.core.api.net.HttpUtils;
import org.weasis.core.api.net.URLParameters;
import org.weasis.core.api.net.auth.AuthMethod;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.HiddenSeriesManager;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.explorer.DicomModel;
import org.weasis.dicom.explorer.main.ThumbnailMouseAndKeyAdapter;
import org.weasis.dicom.explorer.rs.RsQueryResult;
import org.weasis.dicom.explorer.wado.LoadSeries;
import org.weasis.dicom.mf.SopInstance;
import org.weasis.dicom.mf.WadoParameters;

public record ThumbnailManager(DicomSeries dicomSeries, DicomModel dicomModel, URLParameters urlParams) {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThumbnailManager.class);
    private static final String JPEG_EXTENSION = ".jpg";
    private static final String IMAGE_JPEG_MIME = "image/jpeg";
    private static final String DICOM_JSON_MIME = "application/dicom+json";
    private static final int THUMBNAIL_QUALITY = 75;
    private static final Map<String, ThumbnailMode> SERVER_THUMBNAIL_MODE = new ConcurrentHashMap<String, ThumbnailMode>();

    public void loadThumbnail(SopInstance instance, WadoParameters wadoParameters, AuthMethod authMethod) {
        Path thumbnailPath = wadoParameters.isWadoRS() && instance.getDirectDownloadFile() != null && this.dicomSeries.getTagValue(TagW.DirectDownloadThumbnail) == null ? this.fetchInstanceThumbnail(wadoParameters, instance.getSopInstanceUID(), authMethod) : (instance.getDirectDownloadFile() == null ? this.downloadThumbnailFromWado(instance, wadoParameters, authMethod) : this.downloadDirectThumbnail(wadoParameters, authMethod));
        if (thumbnailPath != null) {
            this.updateSeriesThumbnail(thumbnailPath);
        }
    }

    public void loadSeriesThumbnail(WadoParameters wadoParameters, AuthMethod authMethod) {
        if (!"series/dicom".equals(this.dicomSeries.getMimeType())) {
            return;
        }
        MediaSeriesGroup study = this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.study);
        String studyUID = (String)TagD.getTagValue((TagReadable)study, (int)0x20000D, String.class);
        String seriesUID = (String)TagD.getTagValue((TagReadable)this.dicomSeries, (int)0x20000E, String.class);
        if (!StringUtil.hasText((CharSequence)studyUID) || !StringUtil.hasText((CharSequence)seriesUID)) {
            return;
        }
        Path thumbnailPath = this.fetchRsThumbnail(wadoParameters, studyUID, seriesUID, null, authMethod);
        if (thumbnailPath != null) {
            this.updateSeriesThumbnail(thumbnailPath);
        }
    }

    private Path fetchInstanceThumbnail(WadoParameters wadoParameters, String sopInstanceUID, AuthMethod authMethod) {
        MediaSeriesGroup study = this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.study);
        String studyUID = (String)TagD.getTagValue((TagReadable)study, (int)0x20000D, String.class);
        String seriesUID = (String)TagD.getTagValue((TagReadable)this.dicomSeries, (int)0x20000E, String.class);
        if (!StringUtil.hasText((CharSequence)studyUID) || !StringUtil.hasText((CharSequence)seriesUID)) {
            return null;
        }
        return this.fetchRsThumbnail(wadoParameters, studyUID, seriesUID, sopInstanceUID, authMethod);
    }

    private Path fetchRsThumbnail(WadoParameters wadoParameters, String studyUID, String seriesUID, String sopInstanceUID, AuthMethod authMethod) {
        String baseUrl = LoadSeries.dicomWebBaseUrl(wadoParameters, (MediaSeriesGroup)this.dicomSeries);
        if (SERVER_THUMBNAIL_MODE.getOrDefault(baseUrl, ThumbnailMode.RS_THUMBNAIL) == ThumbnailMode.RS_THUMBNAIL) {
            String url = sopInstanceUID == null ? "%s/studies/%s/series/%s/thumbnail?viewport=%d%%2C%d".formatted(new Object[]{baseUrl, studyUID, seriesUID, 256, 256}) : "%s/studies/%s/series/%s/instances/%s/thumbnail?viewport=%d%%2C%d".formatted(new Object[]{baseUrl, studyUID, seriesUID, sopInstanceUID, 256, 256});
            FetchResult result = this.download(url, JPEG_EXTENSION, this.createWadoRsParams(), authMethod);
            if (result.file() != null) {
                return result.file();
            }
            if (ThumbnailManager.isThumbnailServiceUnsupported(result.code())) {
                LOGGER.info("WADO-RS thumbnail service unavailable on {} (HTTP {}); switching to rendered service", (Object)baseUrl, (Object)result.code());
                SERVER_THUMBNAIL_MODE.put(baseUrl, ThumbnailMode.RENDERED);
            }
        }
        return this.fetchRenderedThumbnail(baseUrl, studyUID, seriesUID, sopInstanceUID, authMethod);
    }

    private Path fetchRenderedThumbnail(String baseUrl, String studyUID, String seriesUID, String sopInstanceUID, AuthMethod authMethod) {
        String instanceUID;
        String string = instanceUID = sopInstanceUID != null ? sopInstanceUID : this.fetchRepresentativeInstanceUID(baseUrl, studyUID, seriesUID, authMethod);
        if (!StringUtil.hasText((CharSequence)instanceUID)) {
            return null;
        }
        String url = "%s/studies/%s/series/%s/instances/%s/rendered?viewport=%d%%2C%d".formatted(new Object[]{baseUrl, studyUID, seriesUID, instanceUID, 256, 256});
        return this.download(url, JPEG_EXTENSION, this.createWadoRsParams(), authMethod).file();
    }

    private static boolean isThumbnailServiceUnsupported(int code) {
        return code == 404 || code == 405 || code == 501;
    }

    private String fetchRepresentativeInstanceUID(String baseUrl, String studyUID, String seriesUID, AuthMethod authMethod) {
        int offset = RsQueryResult.seriesInstanceCount(baseUrl, studyUID, seriesUID, this.urlParams, authMethod) / 2;
        String url = "%s/studies/%s/series/%s/instances?includefield=00080018&limit=1&offset=%d".formatted(new Object[]{baseUrl, studyUID, seriesUID, offset});
        HashMap<String, String> headers = new HashMap<String, String>(this.urlParams.headers());
        headers.put("Accept", DICOM_JSON_MIME);
        try {
            for (Attributes instance : RsQueryResult.parseJSON(url, authMethod, new URLParameters(headers))) {
                String sopUID = instance.getString(524312);
                if (!StringUtil.hasText((CharSequence)sopUID)) continue;
                return sopUID;
            }
        }
        catch (Exception e) {
            LOGGER.debug("Cannot fetch a representative instance for series {}", (Object)seriesUID, (Object)e);
        }
        return null;
    }

    private Path downloadThumbnailFromWado(SopInstance instance, WadoParameters wadoParameters, AuthMethod authMethod) {
        if (!"series/dicom".equals(this.dicomSeries.getMimeType())) {
            return null;
        }
        String url = this.buildWadoUrl(wadoParameters.getBaseURL(), this.getStudyUID(wadoParameters), this.getSeriesUID(wadoParameters), instance.getSopInstanceUID(), wadoParameters.getAdditionalParameters());
        return this.download(url, JPEG_EXTENSION, this.urlParams, authMethod).file();
    }

    private Path downloadDirectThumbnail(WadoParameters wadoParameters, AuthMethod authMethod) {
        Object thumbURL = this.getWadoRsThumbnailUrl(wadoParameters);
        if (thumbURL == null && StringUtil.hasLength((CharSequence)(thumbURL = (String)this.dicomSeries.getTagValue(TagW.DirectDownloadThumbnail)))) {
            if (((String)thumbURL).startsWith(Thumbnail.THUMBNAIL_CACHE_DIR.toString())) {
                return Path.of((String)thumbURL, new String[0]);
            }
            thumbURL = wadoParameters.getBaseURL() + (String)thumbURL;
        }
        URLParameters params = wadoParameters.isWadoRS() ? this.createWadoRsParams() : this.urlParams;
        String extension = wadoParameters.isWadoRS() ? JPEG_EXTENSION : FileUtil.getExtension((String)thumbURL);
        return this.downloadFromUrl((String)thumbURL, extension, params, authMethod);
    }

    private String getStudyUID(WadoParameters wadoParameters) {
        if (wadoParameters.isRequireOnlySOPInstanceUID()) {
            return "";
        }
        MediaSeriesGroup study = this.dicomModel.getParent((MediaSeriesGroup)this.dicomSeries, DicomModel.study);
        return (String)TagD.getTagValue((TagReadable)study, (int)0x20000D, String.class);
    }

    private String getSeriesUID(WadoParameters wadoParameters) {
        return wadoParameters.isRequireOnlySOPInstanceUID() ? "" : (String)TagD.getTagValue((TagReadable)this.dicomSeries, (int)0x20000E, String.class);
    }

    private String getWadoRsThumbnailUrl(WadoParameters wadoParameters) {
        if (wadoParameters.isWadoRS()) {
            String baseUrl = (String)TagD.getTagValue((TagReadable)this.dicomSeries, (int)528784, String.class);
            return baseUrl != null ? "%s/thumbnail?viewport=%d%%2C%d".formatted(new Object[]{baseUrl, 256, 256}) : null;
        }
        return null;
    }

    private URLParameters createWadoRsParams() {
        HashMap<String, String> headers = new HashMap<String, String>(this.urlParams.headers());
        headers.put("Accept", IMAGE_JPEG_MIME);
        return new URLParameters(headers);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private FetchResult download(String url, String extension, URLParameters params, AuthMethod authMethod) {
        try (HttpStream httpCon = HttpUtils.getHttpResponse((String)url, (URLParameters)params, (AuthMethod)authMethod);){
            int code = httpCon.getResponseCode();
            if (code >= 200 && code < 400) {
                FetchResult fetchResult2 = new FetchResult(this.saveThumbnailFile(httpCon, extension), code);
                return fetchResult2;
            }
            if (code == 401 && authMethod != null) {
                authMethod.resetToken();
                authMethod.getToken();
            }
            FetchResult fetchResult = new FetchResult(null, code);
            return fetchResult;
        }
        catch (Exception e) {
            LOGGER.error("Error downloading thumbnail from {}", (Object)url, (Object)e);
            return new FetchResult(null, -1);
        }
    }

    private Path downloadFromUrl(String url, String extension, URLParameters params, AuthMethod authMethod) {
        return this.download(url, extension, params, authMethod).file();
    }

    private Path saveThumbnailFile(HttpStream httpCon, String extension) throws IOException {
        Path outFile = Files.createTempFile(Thumbnail.THUMBNAIL_CACHE_DIR, "thumb_", extension, new FileAttribute[0]);
        FileUtil.writeStreamWithIOException((InputStream)httpCon.getInputStream(), (Path)outFile);
        if (Files.size(outFile) == 0L) {
            Files.deleteIfExists(outFile);
            throw new IllegalStateException("Thumbnail file is empty");
        }
        return outFile;
    }

    private String buildWadoUrl(String baseUrl, String studyUID, String seriesUID, String sopInstanceUID, String additionalParameters) {
        String addParams = this.filterAdditionalParameters(additionalParameters);
        return "%s?requestType=WADO&studyUID=%s&seriesUID=%s&objectUID=%s&contentType=%s&imageQuality=%d&rows=%d&columns=%d%s".formatted(new Object[]{baseUrl, studyUID, seriesUID, sopInstanceUID, IMAGE_JPEG_MIME, 75, 256, 256, addParams});
    }

    private String filterAdditionalParameters(String params) {
        if (!StringUtil.hasText((CharSequence)params)) {
            return "";
        }
        return Arrays.stream(params.split("&")).filter(p -> !p.startsWith("transferSyntax") && !p.startsWith("anonymize")).collect(Collectors.joining("&", "&", ""));
    }

    private void updateSeriesThumbnail(Path thumbnailPath) {
        GuiExecutor.execute(() -> {
            SeriesThumbnail thumbnail = (SeriesThumbnail)this.dicomSeries.getTagValue(TagW.Thumbnail);
            if (thumbnail != null) {
                thumbnail.reBuildThumbnail(thumbnailPath.toFile(), MediaSeries.MEDIA_POSITION.MIDDLE);
            }
        });
    }

    public void createSeriesThumbnail(LoadSeries loadSeries, JProgressBar progressBar) {
        GuiExecutor.execute(() -> {
            SeriesThumbnail thumbnail = this.getOrCreateThumbnail();
            thumbnail.setProgressBar(loadSeries.isDone() ? null : progressBar);
            thumbnail.registerListeners();
            this.addListenerToThumbnail((Thumbnail)thumbnail, loadSeries);
            this.dicomSeries.setTag(TagW.Thumbnail, (Object)thumbnail);
            this.dicomModel.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.ADD, (Object)this.dicomModel, null, (Object)this.dicomSeries));
        });
    }

    private SeriesThumbnail getOrCreateThumbnail() {
        SeriesThumbnail thumbnail = (SeriesThumbnail)this.dicomSeries.getTagValue(TagW.Thumbnail);
        if (thumbnail == null) {
            int thumbnailSize = SeriesThumbnail.getThumbnailSizeFromPreferences();
            Function<String, Set> drawIcons = HiddenSeriesManager::getRelatedIcons;
            thumbnail = new SeriesThumbnail((MediaSeries)this.dicomSeries, thumbnailSize, drawIcons);
        }
        return thumbnail;
    }

    public static void removeThumbnailMouseAndKeyAdapter(Thumbnail thumbnail) {
        ThumbnailManager.removeListeners(thumbnail.getMouseListeners(), arg_0 -> thumbnail.removeMouseListener(arg_0));
        ThumbnailManager.removeListeners(thumbnail.getMouseMotionListeners(), arg_0 -> thumbnail.removeMouseMotionListener(arg_0));
        ThumbnailManager.removeListeners(thumbnail.getMouseWheelListeners(), arg_0 -> thumbnail.removeMouseWheelListener(arg_0));
        ThumbnailManager.removeListeners(thumbnail.getKeyListeners(), arg_0 -> thumbnail.removeKeyListener(arg_0));
    }

    private static <T> void removeListeners(T[] listeners, Consumer<T> remover) {
        Arrays.stream(listeners).filter(ThumbnailMouseAndKeyAdapter.class::isInstance).forEach(remover);
    }

    void addListenerToThumbnail(Thumbnail thumbnail, LoadSeries loadSeries) {
        ThumbnailMouseAndKeyAdapter thumbAdapter = new ThumbnailMouseAndKeyAdapter(loadSeries.getDicomSeries(), this.dicomModel, loadSeries);
        thumbnail.addMouseListener((MouseListener)thumbAdapter);
        thumbnail.addKeyListener((KeyListener)thumbAdapter);
        if (thumbnail instanceof SeriesThumbnail) {
            SeriesThumbnail seriesThumbnail = (SeriesThumbnail)thumbnail;
            seriesThumbnail.setProgressBar(loadSeries.getProgressBar());
        }
    }

    private static enum ThumbnailMode {
        RS_THUMBNAIL,
        RENDERED;

    }

    private record FetchResult(Path file, int code) {
    }
}

