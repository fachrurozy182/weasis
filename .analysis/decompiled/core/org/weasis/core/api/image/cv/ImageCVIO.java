/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Core
 *  org.opencv.core.Core$MinMaxLocResult
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.MathUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.opencv.data.FileRawImage
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageAnalyzer
 *  org.weasis.opencv.op.ImageConversion
 *  org.weasis.opencv.op.ImageIOHandler
 */
package org.weasis.core.api.image.cv;

import java.awt.image.BandedSampleModel;
import java.awt.image.IndexColorModel;
import java.awt.image.PackedColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.AbstractFileModel;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.FileCache;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.SeriesEvent;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.internal.cv.NativeOpenCVCodec;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.MathUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.opencv.data.FileRawImage;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageAnalyzer;
import org.weasis.opencv.op.ImageConversion;
import org.weasis.opencv.op.ImageIOHandler;

public class ImageCVIO
implements MediaReader<ImageElement> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageCVIO.class);
    public static final int TILE_SIZE = 512;
    public static final Path CACHE_UNCOMPRESSED_DIR = AppProperties.buildAccessibleTempDirectory("cache", "uncompressed");
    private static final String WCV_EXTENSION = ".wcv";
    private static final String JPEG_EXTENSION = ".jpg";
    private static final String DICOM_MIME_TYPE = "dicom";
    private static final double MIN_MAX_EQUAL_OFFSET = 1.0;
    private final URI uri;
    private final String mimeType;
    private final FileCache fileCache;
    private final Codec codec;
    private ImageElement image;

    public ImageCVIO(URI media, String mimeType, Codec codec) {
        this.uri = Objects.requireNonNull(media);
        this.fileCache = new FileCache(this);
        this.mimeType = Objects.requireNonNullElse(mimeType, "application/x-unknown-mime-type");
        this.codec = codec;
    }

    @Override
    public PlanarImage getImageFragment(MediaElement media) throws Exception {
        Objects.requireNonNull(media);
        Path imagePath = this.resolveImagePath(media);
        if (imagePath == null) {
            return null;
        }
        Path cachePath = this.getCachePathIfNeeded(media);
        PlanarImage img = this.readImage(imagePath);
        if (cachePath != null && img != null) {
            return this.processCachedImage(media, img, cachePath);
        }
        return img;
    }

    @Override
    public URI getUri() {
        return this.uri;
    }

    @Override
    public MediaElement getPreview() {
        return this.getSingleImage();
    }

    @Override
    public boolean delegate(DataExplorerModel explorerModel) {
        return false;
    }

    public ImageElement[] getMediaElement() {
        return new ImageElement[]{this.getSingleImage()};
    }

    @Override
    public MediaSeries<ImageElement> getMediaSeries() {
        String seriesUID = this.getSeriesUID();
        MediaSeries<ImageElement> series = this.createMediaSeries(seriesUID);
        ImageElement img = this.getSingleImage();
        series.add(img);
        series.setTag(TagW.FileName, img.getName());
        return series;
    }

    @Override
    public int getMediaElementNumber() {
        return 1;
    }

    @Override
    public String getMediaFragmentMimeType() {
        return this.mimeType;
    }

    @Override
    public Map<TagW, Object> getMediaFragmentTags(Object key) {
        return new HashMap<TagW, Object>();
    }

    @Override
    public void close() {
    }

    @Override
    public Codec getCodec() {
        return this.codec;
    }

    @Override
    public String[] getReaderDescription() {
        return new String[]{"Image Codec: " + this.codec.getCodecName(), "Version: " + Core.VERSION};
    }

    @Override
    public Object getTagValue(TagW tag) {
        return tag != null ? this.getSingleImage().getTagValue(tag) : null;
    }

    @Override
    public void replaceURI(URI uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTag(TagW tag, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containTagKey(TagW tag) {
        return false;
    }

    @Override
    public void setTagNoNull(TagW tag, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Map.Entry<TagW, Object>> getTagEntrySetIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileCache getFileCache() {
        return this.fileCache;
    }

    @Override
    public boolean buildFile(File output) {
        return false;
    }

    private Path resolveImagePath(MediaElement media) throws NoSuchAlgorithmException {
        FileCache cache = media.getFileCache();
        if (!cache.isRequireTransformation()) {
            return cache.getOriginalFile().orElse(null);
        }
        Path transformed = cache.getTransformedFile();
        if (transformed != null) {
            return transformed;
        }
        Path cachePath = this.buildCachePath(media);
        if (Files.isReadable(cachePath)) {
            cache.setTransformedFile(cachePath);
            return cachePath;
        }
        return cache.getOriginalFile().orElse(null);
    }

    private Path getCachePathIfNeeded(MediaElement media) throws NoSuchAlgorithmException {
        FileCache cache = media.getFileCache();
        if (!cache.isRequireTransformation() || cache.getTransformedFile() != null) {
            return null;
        }
        Path cachePath = this.buildCachePath(media);
        return Files.isReadable(cachePath) ? null : cachePath;
    }

    private Path buildCachePath(MediaElement media) throws NoSuchAlgorithmException {
        String filename = StringUtil.bytesToMD5((byte[])media.getMediaURI().toString().getBytes(StandardCharsets.UTF_8));
        return CACHE_UNCOMPRESSED_DIR.resolve(filename + WCV_EXTENSION);
    }

    private PlanarImage processCachedImage(MediaElement media, PlanarImage img, Path cachePath) throws Exception {
        Path rawFile = this.uncompressIfNeeded(img, cachePath, media);
        if (rawFile != null) {
            media.getFileCache().setTransformedFile(rawFile);
            return this.readImage(rawFile);
        }
        return img;
    }

    private PlanarImage readImage(Path path) throws Exception {
        if (path.getFileName().toString().endsWith(WCV_EXTENSION)) {
            return new FileRawImage(path).read();
        }
        if (this.codec instanceof NativeOpenCVCodec) {
            return this.readWithOpenCV(path);
        }
        return this.readWithImageIO(path);
    }

    private PlanarImage readWithOpenCV(Path path) throws Exception {
        ArrayList<String> exifTags = new ArrayList<String>();
        ImageCV img = ImageIOHandler.readImageWithCvException((Path)path, exifTags);
        if (img != null) {
            ImageCVIO.applyExifTags(this.getSingleImage(), exifTags);
            this.updateImageDimensions((PlanarImage)img);
            return img;
        }
        return this.readWithImageIO(path);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private PlanarImage readWithImageIO(Path path) throws IOException {
        ImageReader reader = ImageIO.getImageReadersByMIMEType(this.mimeType).next();
        if (reader == null) {
            LOGGER.info("Cannot find a reader for the mime type: {}", (Object)this.mimeType);
            return null;
        }
        try (FileImageInputStream stream = new FileImageInputStream(new RandomAccessFile(path.toFile(), "r"));){
            RenderedImage bi;
            reader.setInput(stream, true, true);
            ImageReadParam param = reader.getDefaultReadParam();
            try {
                bi = reader.read(0, param);
            }
            finally {
                reader.dispose();
            }
            bi = ImageCVIO.convertToReadableImage(bi);
            ImageCV result = ImageConversion.toMat((RenderedImage)bi);
            this.updateImageDimensions((PlanarImage)result);
            ImageCV imageCV = result;
            return imageCV;
        }
    }

    private void updateImageDimensions(PlanarImage img) {
        if (img != null && this.image != null) {
            this.image.setTag(TagW.ImageWidth, img.width());
            this.image.setTag(TagW.ImageHeight, img.height());
        }
    }

    private ImageElement getSingleImage() {
        if (this.image == null) {
            this.image = new ImageElement(this, (Object)0);
        }
        return this.image;
    }

    private String getSeriesUID() {
        ImageElement element = this.getSingleImage();
        String sUID = (String)element.getTagValue(TagW.get("SeriesInstanceUID"));
        return sUID != null ? sUID : this.uri.toString();
    }

    private MediaSeries<ImageElement> createMediaSeries(String seriesUID) {
        return new Series<ImageElement>(this, TagW.SubseriesInstanceUID, (Object)seriesUID, AbstractFileModel.series.tagView()){
            {
                Objects.requireNonNull(this$0);
                super(tagID, identifier, displayTag);
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public String getMimeType() {
                1 var1_1 = this;
                synchronized (var1_1) {
                    return this.medias.isEmpty() ? null : ((ImageElement)this.medias.getFirst()).getMimeType();
                }
            }

            @Override
            public void addMedia(ImageElement media) {
                if (media != null) {
                    this.add(media);
                    this.notifyDataExplorerModel(media);
                }
            }

            @Override
            public MediaElement getFirstSpecialElement() {
                return null;
            }

            private void notifyDataExplorerModel(ImageElement media) {
                DataExplorerModel model = (DataExplorerModel)this.getTagValue(TagW.ExplorerModel);
                if (model != null) {
                    model.firePropertyChange(new ObservableEvent(ObservableEvent.BasicAction.ADD, model, null, (Object)new SeriesEvent(SeriesEvent.Action.ADD_IMAGE, this, media)));
                }
            }
        };
    }

    private Path uncompressIfNeeded(PlanarImage img, Path cachePath, MediaElement media) {
        if (!this.shouldUncompress(img)) {
            return null;
        }
        try {
            new FileRawImage(cachePath).write(img);
            this.writeThumbnail(cachePath, img, media);
            return cachePath;
        }
        catch (Exception e) {
            FileUtil.delete((Path)cachePath);
            LOGGER.error("Failed to uncompress temporary image", (Throwable)e);
            return null;
        }
    }

    private boolean shouldUncompress(PlanarImage img) {
        return img != null && (img.width() > 512 || img.height() > 512) && !this.mimeType.contains(DICOM_MIME_TYPE);
    }

    private void writeThumbnail(Path outFile, PlanarImage img, MediaElement media) {
        PlanarImage thumbnailImg = this.prepareThumbnailImage(img, media);
        Path thumbnailPath = Path.of(ImageCVIO.changeExtension(outFile.toString(), JPEG_EXTENSION), new String[0]);
        ImageIOHandler.writeThumbnail((Mat)thumbnailImg.toMat(), (Path)thumbnailPath, (int)256);
    }

    private PlanarImage prepareThumbnailImage(PlanarImage img, MediaElement media) {
        if (CvType.depth((int)img.type()) <= 1 || !(media instanceof ImageElement)) {
            return img;
        }
        ImageElement imgElement = (ImageElement)media;
        Map<String, Object> params = imgElement.isImageAvailable() ? null : this.createRenderingParams(img);
        return imgElement.getRenderedImage(img, params);
    }

    private Map<String, Object> createRenderingParams(PlanarImage img) {
        Core.MinMaxLocResult val = ImageAnalyzer.findMinMaxValues((Mat)img.toMat());
        double min = val.minVal;
        double max = MathUtil.isEqual((double)min, (double)val.maxVal) ? val.maxVal + 1.0 : val.maxVal;
        return Map.of(ActionW.WINDOW.cmd(), max - min, ActionW.LEVEL.cmd(), min + (max - min) / 2.0);
    }

    private static void applyExifTags(ImageElement img, List<String> exifTags) {
        if (exifTags.size() < 9) {
            return;
        }
        ImageCVIO.setTagIfPresent(img, TagW.ExifImageDescription, exifTags.get(0));
        ImageCVIO.setTagIfPresent(img, TagW.ExifMake, exifTags.get(1));
        ImageCVIO.setTagIfPresent(img, TagW.ExifModel, exifTags.get(2));
        ImageCVIO.setTagIfPresent(img, TagW.ExifOrientation, exifTags.get(3));
        ImageCVIO.setTagIfPresent(img, TagW.ExifXResolution, exifTags.get(4));
        ImageCVIO.setTagIfPresent(img, TagW.ExifYResolution, exifTags.get(5));
        ImageCVIO.setTagIfPresent(img, TagW.ExifResolutionUnit, exifTags.get(6));
        ImageCVIO.setTagIfPresent(img, TagW.ExifSoftware, exifTags.get(7));
        ImageCVIO.setTagIfPresent(img, TagW.ExifDateTime, exifTags.get(8));
        ImageCVIO.setTagIfPresent(img, TagW.ExifCopyright, exifTags.get(9));
    }

    private static void setTagIfPresent(ImageElement img, TagW tag, String value) {
        if (StringUtil.hasText((CharSequence)value)) {
            img.setTag(tag, value);
        }
    }

    public static RenderedImage convertToReadableImage(RenderedImage source) {
        if (source == null || source.getSampleModel() == null) {
            return source;
        }
        if (ImageConversion.isBinary((SampleModel)source.getSampleModel())) {
            return ImageConversion.convertTo((RenderedImage)source, (int)10);
        }
        int numBands = source.getSampleModel().getNumBands();
        if (ImageCVIO.requiresConversion(source, numBands)) {
            int imageType = numBands >= 3 ? 5 : 10;
            return ImageConversion.convertTo((RenderedImage)source, (int)imageType);
        }
        return source;
    }

    private static boolean requiresConversion(RenderedImage source, int numBands) {
        return source.getColorModel() instanceof PackedColorModel || source.getColorModel() instanceof IndexColorModel || numBands == 2 || numBands > 3 || source.getSampleModel() instanceof BandedSampleModel && numBands > 1;
    }

    public static String changeExtension(String filename, String ext) {
        if (filename == null) {
            return "";
        }
        int pointPos = filename.lastIndexOf(46);
        return filename.substring(0, pointPos == -1 ? filename.length() : pointPos) + ext;
    }
}

