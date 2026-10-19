/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Core
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.explorer.model.DataExplorerModel
 *  org.weasis.core.api.gui.util.AppProperties
 *  org.weasis.core.api.media.data.Codec
 *  org.weasis.core.api.media.data.FileCache
 *  org.weasis.core.api.media.data.MediaElement
 *  org.weasis.core.api.media.data.MediaReader
 *  org.weasis.core.api.media.data.MediaSeriesGroup
 *  org.weasis.core.api.media.data.SimpleTaggable
 *  org.weasis.core.api.media.data.TagReadable
 *  org.weasis.core.api.media.data.TagView
 *  org.weasis.core.api.media.data.TagW
 *  org.weasis.core.api.media.data.Taggable
 *  org.weasis.core.api.service.BundleTools
 *  org.weasis.core.util.SoftHashMap
 *  org.weasis.core.util.StringUtil
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.dicom.codec;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Implementation;
import org.dcm4che3.data.VR;
import org.dcm4che3.img.DicomImageReadParam;
import org.dcm4che3.img.DicomImageReader;
import org.dcm4che3.img.DicomMetaData;
import org.dcm4che3.img.ImageRendering;
import org.dcm4che3.img.Transcoder;
import org.dcm4che3.img.data.PrDicomObject;
import org.dcm4che3.img.stream.DicomFileInputStream;
import org.dcm4che3.img.stream.ImageDescriptor;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.io.DicomOutputStream;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.FileCache;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.SimpleTaggable;
import org.weasis.core.api.media.data.TagReadable;
import org.weasis.core.api.media.data.TagView;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Taggable;
import org.weasis.core.api.service.BundleTools;
import org.weasis.core.util.SoftHashMap;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DcmMediaReader;
import org.weasis.dicom.codec.DicomEncapDocElement;
import org.weasis.dicom.codec.DicomEncapDocSeries;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.codec.DicomSpecialElement;
import org.weasis.dicom.codec.DicomSpecialElementFactory;
import org.weasis.dicom.codec.DicomVideoElement;
import org.weasis.dicom.codec.DicomVideoSeries;
import org.weasis.dicom.codec.ExtendSegmentedInputImageStream;
import org.weasis.dicom.codec.KOSpecialElement;
import org.weasis.dicom.codec.PRSpecialElement;
import org.weasis.dicom.codec.RejectedKOSpecialElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.TagManager;
import org.weasis.dicom.codec.display.CornerDisplay;
import org.weasis.dicom.codec.display.Modality;
import org.weasis.dicom.codec.display.ModalityInfoData;
import org.weasis.dicom.codec.display.ModalityView;
import org.weasis.dicom.codec.geometry.ImageOrientation;
import org.weasis.dicom.codec.seg.SegSpecialElement;
import org.weasis.dicom.codec.utils.DicomMediaUtils;
import org.weasis.dicom.codec.utils.PatientComparator;
import org.weasis.opencv.data.PlanarImage;

public class DicomMediaIO
implements DcmMediaReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomMediaIO.class);
    public static final Path DICOM_EXPORT_DIR = AppProperties.buildAccessibleTempDirectory((String[])new String[]{"dicom"});
    public static final Path CACHE_UNCOMPRESSED_DIR = AppProperties.buildAccessibleTempDirectory((String[])new String[]{"cache", "dcm-rawcv"});
    public static final String DICOM_MIMETYPE = "application/dicom";
    public static final String IMAGE_MIMETYPE = "image/dicom";
    public static final String SERIES_VIDEO_MIMETYPE = "video/dicom";
    public static final String SERIES_MIMETYPE = "series/dicom";
    public static final String SERIES_PR_MIMETYPE = "pr/dicom";
    public static final String SERIES_KO_MIMETYPE = "ko/dicom";
    public static final String SERIES_SEG_MIMETYPE = "seg/dicom";
    public static final String SERIES_ENCAP_DOC_MIMETYPE = "encap/dicom";
    public static final String UNREADABLE = "unreadable/dicom";
    public static final String SERIES_XDSI = "xds-i/dicom";
    private static final AtomicInteger instanceID = new AtomicInteger(1);
    public static final TagManager tagManager = new TagManager();
    public static final Map<String, DicomSpecialElementFactory> DCM_ELEMENT_FACTORIES;
    private static final SoftHashMap<DicomMediaIO, DicomMetaData> HEADER_CACHE;
    private DicomMetaData dcmMetadata = null;
    private URI uri;
    private int numberOfFrame;
    private final Map<TagW, Object> tags;
    private DicomImageElement[] image = null;
    private String mimeType;
    private boolean hasPixel = false;
    private VR pixelDataVR = null;
    private final FileCache fileCache;

    public DicomMediaIO(URI uri) {
        this.uri = Objects.requireNonNull(uri);
        this.numberOfFrame = 0;
        this.tags = new HashMap<TagW, Object>();
        this.mimeType = DICOM_MIMETYPE;
        this.fileCache = new FileCache((MediaReader)this);
    }

    public DicomMediaIO(File source) {
        this(Objects.requireNonNull(source).toURI());
    }

    public DicomMediaIO(Path path) {
        this(Objects.requireNonNull(path).toUri());
    }

    public DicomMediaIO(Attributes dcmItems) throws URISyntaxException {
        this(new URI("data:" + Objects.requireNonNull(dcmItems).getString(524312)));
        this.dcmMetadata = new DicomMetaData(dcmItems, "1.2.840.10008.1.2.1");
    }

    private static void readTagsInModalityView(TagView[] views) {
        for (TagView tagView : views) {
            if (tagView == null) continue;
            for (TagW tag : tagView.getTag()) {
                if (tag == null || tagManager.contains(tag, TagD.Level.PATIENT) || tagManager.contains(tag, TagD.Level.STUDY) || tagManager.contains(tag, TagD.Level.SERIES)) continue;
                tagManager.addTag(tag, TagD.Level.INSTANCE);
            }
        }
    }

    public synchronized void replaceURI(URI uri) {
        if (!Objects.equals(this.uri, Objects.requireNonNull(uri))) {
            this.uri = uri;
        }
    }

    @Override
    public boolean isEditableDicom() {
        return this.dcmMetadata != null && "data".equals(this.uri.getScheme());
    }

    public synchronized boolean isReadableDicom() {
        return this.getReadingStatus() == Reading.READABLE;
    }

    public synchronized Reading getReadingStatus() {
        if (UNREADABLE.equals(this.mimeType)) {
            return Reading.EXCLUDED;
        }
        if ("data".equals(this.uri.getScheme()) && this.dcmMetadata == null) {
            return Reading.EXCLUDED;
        }
        if (this.tags.isEmpty()) {
            return this.setMimeType();
        }
        return Reading.READABLE;
    }

    private Reading setMimeType() {
        try {
            DicomMetaData md = this.readMetaData();
            Attributes header = md.getDicomObject();
            if (md.isMediaStorageDirectory()) {
                this.mimeType = UNREADABLE;
                this.close();
                return Reading.EXCLUDED;
            }
            if (this.hasPixel) {
                this.mimeType = md.isVideoTransferSyntaxUID() ? SERIES_VIDEO_MIMETYPE : (md.isSegmentationStorage() ? SERIES_SEG_MIMETYPE : IMAGE_MIMETYPE);
            } else {
                boolean special = this.setDicomSpecialType(header);
                if (!special) {
                    String sopClassUID = header.getString(524310);
                    LOGGER.info("Unsupported DICOM SOP Class (no pixel data, no registered handler in Weasis): {}", (Object)sopClassUID);
                    this.mimeType = UNREADABLE;
                    this.close();
                    return Reading.UNSUPPORTED;
                }
            }
            this.writeInstanceTags(md);
            return Reading.READABLE;
        }
        catch (Exception | OutOfMemoryError e) {
            this.mimeType = UNREADABLE;
            LOGGER.error("Cannot read DICOM:", e);
            this.close();
            return Reading.ERROR;
        }
    }

    private boolean setDicomSpecialType(Attributes header) {
        String modality = header.getString(524384);
        if (modality != null) {
            String encap = header.getString(4325394);
            DicomSpecialElementFactory factory = DCM_ELEMENT_FACTORIES.get(modality);
            if (factory != null) {
                this.mimeType = factory.getSeriesMimeType();
                if (encap == null) {
                    return true;
                }
            }
            if (encap != null) {
                this.mimeType = SERIES_ENCAP_DOC_MIMETYPE;
                return true;
            }
        }
        return false;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public Object getTagValue(TagW tag) {
        return tag == null ? null : this.tags.get(tag);
    }

    public void setTag(TagW tag, Object value) {
        DicomMediaUtils.setTag(this.tags, tag, value);
    }

    public void setTagNoNull(TagW tag, Object value) {
        if (value != null) {
            this.setTag(tag, value);
        }
    }

    public boolean containTagKey(TagW tag) {
        return this.tags.containsKey(tag);
    }

    public Iterator<Map.Entry<TagW, Object>> getTagEntrySetIterator() {
        return this.tags.entrySet().iterator();
    }

    private void writeInstanceTags(DicomMetaData md) {
        String concatenationUID;
        if (!this.tags.isEmpty() || md == null || md.getDicomObject() == null) {
            return;
        }
        Attributes fmi = md.getFileMetaInformation();
        Attributes header = md.getDicomObject();
        tagManager.readTags(TagD.Level.INSTANCE, header, (Taggable)this);
        PatientComparator patientComparator = new PatientComparator((TagReadable)this);
        this.setTag(TagW.PatientPseudoUID, patientComparator.buildPatientPseudoUID());
        Integer instNb = DicomUtils.getIntegerFromDicomElement(header, 2097171, instanceID.incrementAndGet());
        this.setTag(TagD.get(2097171), instNb);
        this.setTag(TagD.get(524312), header.getString(524312, String.valueOf(instNb)));
        if (fmi != null) {
            this.setTagNoNull(TagD.get(131088), fmi.getString(131088));
        }
        if (StringUtil.hasText((CharSequence)(concatenationUID = header.getString(2134369)))) {
            this.setTag(TagD.get(2134369), concatenationUID);
            this.setTag(TagD.get(2134568), header.getInt(2134568, 0));
        }
        this.writeImageValues(md);
        this.writeSharedFunctionalGroupsSequence(header, md);
        DicomMediaUtils.writeFrameGeometry((Taggable)this, md, 0);
        boolean pr = SERIES_PR_MIMETYPE.equals(this.mimeType);
        boolean ko = SERIES_KO_MIMETYPE.equals(this.mimeType);
        if (pr) {
            PrDicomObject prDcm = new PrDicomObject(header, md.getImageDescriptor());
            this.setTag(TagW.PrDicomObject, prDcm);
        }
        if (pr || ko) {
            TagW[] tagIDs;
            for (TagW tag : tagIDs = TagD.getTagFromIDs(528446, 524321, 524337, 0x200011)) {
                tag.readValue((Object)header, (Taggable)this);
            }
        }
        DicomMediaUtils.computeSlicePosition((Taggable)this);
        DicomMediaUtils.setShutter((Taggable)this, header);
        DicomMediaUtils.computeSUVFactor(header, (Taggable)this, 0);
    }

    private void writeSharedFunctionalGroupsSequence(Attributes header, DicomMetaData md) {
        if (header != null) {
            DicomMediaUtils.writeFunctionalGroupsSequence((Taggable)this, md, header.getNestedDataset(1375769129), -1);
        }
    }

    private void writeImageValues(DicomMetaData md) {
        if (md != null && md.getDicomObject() != null && this.hasPixel) {
            Attributes header = md.getDicomObject();
            ImageDescriptor desc = md.getImageDescriptor();
            TagD.get(0x200032).readValue((Object)header, (Taggable)this);
            TagD.get(2097207).readValue((Object)header, (Taggable)this);
            this.setTagNoNull(TagW.ImageOrientationPlane, (Object)ImageOrientation.getPlan((TagReadable)this));
            int bitsAllocated = desc.getBitsAllocated();
            int bitsStored = desc.getBitsStored();
            int pixelRepresentation = desc.getPixelRepresentation();
            this.setTagNoNull(TagD.get(2621696), bitsAllocated);
            if (this.pixelDataVR == VR.OB || this.pixelDataVR == VR.OW) {
                this.setTagNoNull(TagD.get(2621697), bitsStored);
                this.setTagNoNull(TagD.get(2621699), pixelRepresentation);
            }
            TagD.get(2621488).readValue((Object)header, (Taggable)this);
            TagD.get(2621492).readValue((Object)header, (Taggable)this);
            TagD.get(2624004).readValue((Object)header, (Taggable)this);
            TagD.get(1577316).readValue((Object)header, (Taggable)this);
            TagD.get(0x181114).readValue((Object)header, (Taggable)this);
            TagD.get(0x181110).readValue((Object)header, (Taggable)this);
            TagD.get(0x181111).readValue((Object)header, (Taggable)this);
            TagD.get(1581072).readValue((Object)header, (Taggable)this);
            this.setTag(TagW.AnatomicRegion, desc.getAnatomicRegion());
            TagD.get(2625600).readValue((Object)header, (Taggable)this);
            TagD.get(5509121).readValue((Object)header, (Taggable)this);
            TagD.get(0x280008).readValue((Object)header, (Taggable)this);
            this.setTagNoNull(TagD.get(1577061), DicomMediaUtils.getFrameTime(header));
            TagD.get(1577540).readValue((Object)header, (Taggable)this);
            int samplesPerPixel = DicomUtils.getIntegerFromDicomElement(header, 0x280002, 1);
            this.setTag(TagD.get(0x280002), samplesPerPixel);
            String photometricInterpretation = header.getString(2621444, "MONOCHROME2");
            TagD.get(0x20500020).readValue((Object)header, (Taggable)this);
            this.setTag(TagD.get(2621444), photometricInterpretation);
            this.setTag(TagW.MonoChrome, samplesPerPixel == 1 && !"PALETTE COLOR".equalsIgnoreCase(photometricInterpretation));
            this.setTag(TagD.get(2621456), desc.getRows());
            this.setTag(TagD.get(2621457), desc.getColumns());
            this.setTagNoNull(TagD.get(2621728), DicomMediaUtils.getIntPixelValue(header, 2621728, pixelRepresentation != 0, bitsStored));
            this.setTagNoNull(TagD.get(2621729), DicomMediaUtils.getIntPixelValue(header, 2621729, pixelRepresentation != 0, bitsStored));
            this.setTagNoNull(TagD.get(2629904), header.getString(2629904, header.getString(532752)));
            TagD.get(0x282112).readValue((Object)header, (Taggable)this);
            TagD.get(2629908).readValue((Object)header, (Taggable)this);
            TagD.get(532753).readValue((Object)header, (Taggable)this);
            this.setTagNoNull(TagW.ImageDescriptor, desc.getEmbeddedOverlay());
        }
    }

    public boolean containTag(int id) {
        for (TagW tagW : this.tags.keySet()) {
            if (tagW.getId() != id) continue;
            return true;
        }
        return false;
    }

    public URI getUri() {
        return this.uri;
    }

    public FileCache getFileCache() {
        return this.fileCache;
    }

    public boolean buildFile(File output) {
        Attributes dcm;
        if (this.isEditableDicom() && (dcm = this.getDicomObject()) != null) {
            boolean bl;
            DicomOutputStream out = new DicomOutputStream(output);
            try {
                out.writeDataset(dcm.createFileMetaInformation("1.2.840.10008.1.2"), dcm);
                bl = true;
            }
            catch (Throwable throwable) {
                try {
                    try {
                        out.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    LOGGER.error("Cannot write dicom file", (Throwable)e);
                }
            }
            out.close();
            return bl;
        }
        return false;
    }

    public PlanarImage getImageFragment(MediaElement media) throws Exception {
        if (Objects.requireNonNull(media).getKey() instanceof Integer) {
            return this.getImageFragment(media, (Integer)media.getKey(), true);
        }
        return null;
    }

    public PlanarImage getImageFragment(MediaElement media, int frame, boolean noEmbeddedOverlay) throws Exception {
        FileCache cache;
        Optional original;
        if (this.isReadableDicom() && frame >= 0 && frame < this.numberOfFrame && this.hasPixel && (original = (cache = media.getFileCache()).getOriginalFile()).isPresent()) {
            LOGGER.debug("Start reading dicom image frame: {} sopUID: {}", (Object)frame, TagD.getTagValue((TagReadable)this, 524312));
            DicomImageReader reader = new DicomImageReader(Transcoder.dicomImageReaderSpi);
            DicomMetaData metaData = (DicomMetaData)HEADER_CACHE.get((Object)this);
            try {
                PlanarImage planarImage;
                try (DicomFileInputStream inputStream = new DicomFileInputStream((Path)original.get(), metaData);){
                    reader.setInput(inputStream);
                    if (metaData == null) {
                        metaData = reader.getStreamMetadata();
                        HEADER_CACHE.put((Object)this, (Object)metaData);
                    }
                    ImageDescriptor desc = metaData.getImageDescriptor();
                    DicomImageReadParam param = new DicomImageReadParam();
                    param.setAllowFloatImageConversion(true);
                    PlanarImage img = reader.getPlanarImage(frame, param);
                    if (img.width() != desc.getColumns() || img.height() != desc.getRows()) {
                        LOGGER.error("The native image size ({}x{}) does not match with the DICOM attributes({}x{})", new Object[]{img.width(), img.height(), desc.getColumns(), desc.getRows()});
                    }
                    planarImage = noEmbeddedOverlay ? ImageRendering.getImageWithoutEmbeddedOverlay(img, desc, frame) : img;
                }
                return planarImage;
            }
            finally {
                reader.dispose();
            }
        }
        return null;
    }

    private static Mat getMatBuffer(ExtendSegmentedInputImageStream extParams) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(extParams.getFile(), "r");){
            long cols = Arrays.stream(extParams.getSegmentLengths()).sum();
            Mat buf = new Mat(1, (int)cols, CvType.CV_8UC1);
            long[] pos = extParams.getSegmentPositions();
            int offset = 0;
            for (int i = 0; i < pos.length; ++i) {
                int len = (int)extParams.getSegmentLengths()[i];
                byte[] b = new byte[len];
                raf.seek(pos[i]);
                raf.read(b);
                buf.put(0, offset, b);
                offset += len;
            }
            Mat mat = buf;
            return mat;
        }
    }

    private static Mat getRawData(BulkData bulkData) {
        Mat mat;
        BufferedInputStream input = new BufferedInputStream(bulkData.openStream());
        try {
            Mat buf = new Mat(1, bulkData.length(), CvType.CV_8UC1);
            byte[] b = new byte[bulkData.length()];
            input.read(b, 0, b.length);
            buf.put(0, 0, b);
            mat = buf;
        }
        catch (Throwable throwable) {
            try {
                try {
                    input.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Exception e) {
                LOGGER.error("Reading Waveform data");
                return new Mat();
            }
        }
        input.close();
        return mat;
    }

    private DicomImageElement getSingleImage() {
        return this.getSingleImage(0);
    }

    private DicomImageElement getSingleImage(int frame) {
        DicomImageElement[] elements = this.getMediaElement();
        if (elements != null && elements.length > frame) {
            return elements[frame];
        }
        return null;
    }

    public MediaElement getPreview() {
        return this.getSingleImage();
    }

    public boolean delegate(DataExplorerModel explorerModel) {
        return false;
    }

    public synchronized DicomImageElement[] getMediaElement() {
        ResultContainer result = this.getMediaElement(null);
        return result.getImage();
    }

    public synchronized ResultContainer getMediaElement(Function<DicomSpecialElementFactory, DicomSpecialElement> buildSpecialElement) {
        DicomSpecialElement specialElement = null;
        if (this.image == null && this.isReadableDicom()) {
            if (SERIES_VIDEO_MIMETYPE.equals(this.mimeType)) {
                this.image = new DicomImageElement[]{new DicomVideoElement(this, null)};
            } else if (SERIES_ENCAP_DOC_MIMETYPE.equals(this.mimeType)) {
                this.image = new DicomImageElement[]{new DicomEncapDocElement(this, null)};
            } else {
                specialElement = this.buildImageElement(buildSpecialElement);
            }
        }
        return new ResultContainer(this.image, specialElement);
    }

    private DicomSpecialElement buildImageElement(Function<DicomSpecialElementFactory, DicomSpecialElement> buildSpecialElement) {
        DicomSpecialElementFactory factory = this.getDicomSpecialElementFactory();
        if (this.numberOfFrame > 0) {
            this.image = new DicomImageElement[this.numberOfFrame];
            for (int i = 0; i < this.image.length; ++i) {
                this.image[i] = new DicomImageElement(this, i);
            }
            if (this.numberOfFrame > 1) {
                this.buildMultiframe();
            }
        } else if (factory == null) {
            this.image = new DicomImageElement[0];
        }
        return factory == null || buildSpecialElement == null ? null : buildSpecialElement.apply(factory);
    }

    private void buildMultiframe() {
        Integer offset = (Integer)this.tags.get(TagD.get(2134568));
        double[] frameTimes = (double[])this.tags.get(TagD.get(1577061));
        for (int i = 0; i < this.image.length; ++i) {
            double frameTime;
            int nb = offset == null ? i + 1 : offset + i + 1;
            this.image[i].setTag(TagD.get(2097171), nb);
            if (frameTimes == null || !((frameTime = i < frameTimes.length ? frameTimes[i] / 1000.0 : frameTimes[frameTimes.length - 1] / 1000.0) > 0.0)) continue;
            this.image[i].setTag(TagD.get(1572928), 1.0 / frameTime);
        }
    }

    private DicomSpecialElementFactory getDicomSpecialElementFactory() {
        String modality = TagD.getTagValue((TagReadable)this, 524384, String.class);
        if (modality != null) {
            return DCM_ELEMENT_FACTORIES.get(modality);
        }
        return null;
    }

    @Override
    public DicomSeries getMediaSeries() {
        DicomSeries series = null;
        if (this.isReadableDicom()) {
            String seriesUID = TagD.getTagValue((TagReadable)this, 0x20000E, String.class);
            series = this.buildSeries(seriesUID);
            this.writeMetaData((MediaSeriesGroup)series);
            DicomImageElement[] elements = this.getMediaElement();
            if (elements != null) {
                for (DicomImageElement media : elements) {
                    series.addMedia(media);
                }
            }
        }
        return series;
    }

    public int getMediaElementNumber() {
        return this.numberOfFrame;
    }

    public String getMediaFragmentMimeType() {
        return this.getMimeType();
    }

    public Map<TagW, Object> getMediaFragmentTags(Object key) {
        Integer val;
        if (key instanceof Integer && (val = (Integer)key) > 0) {
            HashMap<TagW, Object> tagList = new HashMap<TagW, Object>(this.tags);
            SimpleTaggable taggable = new SimpleTaggable(tagList);
            if (DicomMediaUtils.writeFrameGeometry((Taggable)taggable, this.getDicomMetaData(), val)) {
                DicomMediaUtils.computeSlicePosition((Taggable)taggable);
            }
            return tagList;
        }
        return this.tags;
    }

    public void close() {
        HEADER_CACHE.remove((Object)this);
    }

    public Codec getCodec() {
        return BundleTools.getCodec((String)DICOM_MIMETYPE, (String)"dcm4che");
    }

    public String[] getReaderDescription() {
        return new String[]{"DICOM Codec: dcm4che", "Version: " + Implementation.getVersionName(), "Image decompression: OpenCV imgcodecs", "Version: " + Core.VERSION};
    }

    public DicomSeries buildSeries(String seriesUID) {
        DicomSeries series = IMAGE_MIMETYPE.equals(this.mimeType) ? new DicomSeries(seriesUID) : (SERIES_VIDEO_MIMETYPE.equals(this.mimeType) ? new DicomVideoSeries(seriesUID) : (SERIES_ENCAP_DOC_MIMETYPE.equals(this.mimeType) ? new DicomEncapDocSeries(seriesUID) : new DicomSeries(seriesUID)));
        return series;
    }

    @Override
    public Attributes getDicomObject() {
        try {
            DicomMetaData md = this.readMetaData();
            return md.getDicomObject();
        }
        catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("Cannot read DICOM:", (Throwable)e);
            } else {
                LOGGER.error(e.getMessage());
            }
            return null;
        }
    }

    @Override
    public DicomMetaData getDicomMetaData() {
        try {
            return this.readMetaData();
        }
        catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("Cannot read DICOM:", (Throwable)e);
            } else {
                LOGGER.error(e.getMessage());
            }
            return null;
        }
    }

    private synchronized DicomMetaData readMetaData() throws IOException {
        DicomMetaData header = (DicomMetaData)HEADER_CACHE.get((Object)this);
        if (header != null) {
            return header;
        }
        if (this.dcmMetadata != null) {
            return this.dcmMetadata;
        }
        Optional file = this.fileCache.getOriginalFile();
        if (file.isEmpty()) {
            throw new IllegalArgumentException("No file found!");
        }
        Path path = (Path)file.get();
        DicomImageReader reader = new DicomImageReader(Transcoder.dicomImageReaderSpi);
        try {
            DicomMetaData dicomMetaData;
            try (DicomFileInputStream inputStream = new DicomFileInputStream(path);){
                reader.setInput(inputStream);
                DicomMetaData dicomMetaData2 = reader.getStreamMetadata();
                Attributes dcm = dicomMetaData2.getDicomObject();
                this.numberOfFrame = dcm.getInt(0x280008, 0);
                VR.Holder pixelatedVR = new VR.Holder();
                Object pixelData = dcm.getValue(2145386512, pixelatedVR);
                if (pixelData == null) {
                    pixelData = dcm.getValue(2145386504, pixelatedVR);
                }
                if (pixelData == null) {
                    pixelData = dcm.getValue(2145386505, pixelatedVR);
                }
                if (pixelData != null) {
                    this.pixelDataVR = pixelatedVR.vr;
                    this.hasPixel = true;
                }
                if (this.numberOfFrame <= 0 && this.hasPixel) {
                    this.numberOfFrame = 1;
                }
                HEADER_CACHE.put((Object)this, (Object)dicomMetaData2);
                dicomMetaData = dicomMetaData2;
            }
            return dicomMetaData;
        }
        finally {
            reader.dispose();
        }
    }

    public static boolean isHiddenModality(String modality) {
        DicomSpecialElementFactory factory;
        if (modality != null && (factory = DCM_ELEMENT_FACTORIES.get(modality)) != null) {
            return factory.isHidden();
        }
        return false;
    }

    static {
        tagManager.addTag(0x100020, TagD.Level.PATIENT);
        tagManager.addTag(0x100010, TagD.Level.PATIENT);
        tagManager.addTag(0x100030, TagD.Level.PATIENT);
        tagManager.addTag(1048626, TagD.Level.PATIENT);
        tagManager.addTag(0x101010, TagD.Level.SERIES);
        tagManager.addTag(0x100040, TagD.Level.PATIENT);
        tagManager.addTag(0x100021, TagD.Level.PATIENT);
        tagManager.addTag(0x101030, TagD.Level.PATIENT);
        tagManager.addTag(0x104000, TagD.Level.PATIENT);
        tagManager.addTag(0x200010, TagD.Level.STUDY);
        tagManager.addTag(524320, TagD.Level.STUDY);
        tagManager.addTag(524336, TagD.Level.STUDY);
        tagManager.addTag(528432, TagD.Level.STUDY);
        tagManager.addTag(3293184, TagD.Level.STUDY);
        tagManager.addTag(524368, TagD.Level.STUDY);
        tagManager.addTag(524385, TagD.Level.STUDY);
        tagManager.addTag(2101768, TagD.Level.STUDY);
        tagManager.addTag(2101766, TagD.Level.STUDY);
        tagManager.addTag(0x20000E, TagD.Level.SERIES);
        tagManager.addTag(524384, TagD.Level.SERIES);
        tagManager.addTag(528446, TagD.Level.SERIES);
        tagManager.addTag(524310, TagD.Level.SERIES);
        tagManager.addTag(524372, TagD.Level.SERIES);
        tagManager.addTag(524432, TagD.Level.SERIES);
        tagManager.addTag(524416, TagD.Level.SERIES);
        tagManager.addTag(528448, TagD.Level.SERIES);
        tagManager.addTag(528400, TagD.Level.SERIES);
        tagManager.addTag(524400, TagD.Level.SERIES);
        tagManager.addTag(0x102210, TagD.Level.SERIES);
        tagManager.addTag(528528, TagD.Level.SERIES);
        tagManager.addTag(0x200011, TagD.Level.SERIES);
        tagManager.addTag(0x280008, TagD.Level.SERIES);
        tagManager.addTag(524321, TagD.Level.SERIES);
        tagManager.addTag(524337, TagD.Level.SERIES);
        tagManager.addTag(0x400244, TagD.Level.SERIES);
        tagManager.addTag(4194885, TagD.Level.SERIES);
        tagManager.addTag(1577540, TagD.Level.SERIES);
        tagManager.addTag(1572960, TagD.Level.SERIES);
        tagManager.addTag(1572885, TagD.Level.SERIES);
        tagManager.addTag(0x200052, TagD.Level.SERIES);
        tagManager.addTag(2101769, TagD.Level.SERIES);
        tagManager.addTag(0x200060, TagD.Level.SERIES);
        tagManager.addTag(0x100020, TagD.Level.INSTANCE);
        tagManager.addTag(0x100010, TagD.Level.INSTANCE);
        tagManager.addTag(0x100030, TagD.Level.INSTANCE);
        tagManager.addTag(0x100021, TagD.Level.INSTANCE);
        tagManager.addTag(0x20000D, TagD.Level.INSTANCE);
        tagManager.addTag(0x20000E, TagD.Level.INSTANCE);
        tagManager.addTag(524384, TagD.Level.INSTANCE);
        tagManager.addTag(1577248, TagD.Level.INSTANCE);
        tagManager.addTag(0x200020, TagD.Level.INSTANCE);
        tagManager.addTag(2101313, TagD.Level.INSTANCE);
        tagManager.addTag(1572944, TagD.Level.INSTANCE);
        tagManager.addTag(524322, TagD.Level.INSTANCE);
        tagManager.addTag(524338, TagD.Level.INSTANCE);
        tagManager.addTag(524323, TagD.Level.INSTANCE);
        tagManager.addTag(524339, TagD.Level.INSTANCE);
        tagManager.addTag(1609863, TagD.Level.INSTANCE);
        tagManager.addTag(4325394, TagD.Level.INSTANCE);
        tagManager.addTag(2654176, TagD.Level.INSTANCE);
        tagManager.addTag(533016, TagD.Level.INSTANCE);
        for (Map.Entry<Modality, ModalityInfoData> entry : ModalityView.getModalityViewEntries()) {
            DicomMediaIO.readTagsInModalityView(entry.getValue().getCornerInfo(CornerDisplay.TOP_LEFT).getInfos());
            DicomMediaIO.readTagsInModalityView(entry.getValue().getCornerInfo(CornerDisplay.TOP_RIGHT).getInfos());
            DicomMediaIO.readTagsInModalityView(entry.getValue().getCornerInfo(CornerDisplay.BOTTOM_RIGHT).getInfos());
        }
        DicomMediaUtils.enableAnonymizationProfile(true);
        DCM_ELEMENT_FACTORIES = new ConcurrentHashMap<String, DicomSpecialElementFactory>();
        DCM_ELEMENT_FACTORIES.put("PR", new DicomSpecialElementFactory(){

            @Override
            public String getSeriesMimeType() {
                return DicomMediaIO.SERIES_PR_MIMETYPE;
            }

            @Override
            public String[] getModalities() {
                return new String[]{"PR"};
            }

            @Override
            public boolean isHidden() {
                return true;
            }

            @Override
            public DicomSpecialElement buildDicomSpecialElement(DicomMediaIO mediaIO) {
                return new PRSpecialElement(mediaIO);
            }
        });
        DCM_ELEMENT_FACTORIES.put("KO", new DicomSpecialElementFactory(){

            @Override
            public String getSeriesMimeType() {
                return DicomMediaIO.SERIES_KO_MIMETYPE;
            }

            @Override
            public String[] getModalities() {
                return new String[]{"KO"};
            }

            @Override
            public boolean isHidden() {
                return true;
            }

            @Override
            public DicomSpecialElement buildDicomSpecialElement(DicomMediaIO mediaIO) {
                if (RejectedKOSpecialElement.isRejectionKOS(mediaIO)) {
                    return new RejectedKOSpecialElement(mediaIO);
                }
                return new KOSpecialElement(mediaIO);
            }
        });
        DCM_ELEMENT_FACTORIES.put("SEG", new DicomSpecialElementFactory(){

            @Override
            public String getSeriesMimeType() {
                return DicomMediaIO.SERIES_SEG_MIMETYPE;
            }

            @Override
            public String[] getModalities() {
                return new String[]{"SEG"};
            }

            @Override
            public boolean isHidden() {
                return true;
            }

            @Override
            public DicomSpecialElement buildDicomSpecialElement(DicomMediaIO mediaIO) {
                return new SegSpecialElement(mediaIO);
            }
        });
        HEADER_CACHE = new SoftHashMap();
    }

    public static enum Reading {
        ERROR,
        EXCLUDED,
        UNSUPPORTED,
        READABLE;

    }

    public static class ResultContainer {
        private final DicomImageElement[] image;
        private final DicomSpecialElement specialElement;

        public ResultContainer(DicomImageElement[] image, DicomSpecialElement specialElement) {
            this.image = image;
            this.specialElement = specialElement;
        }

        public DicomImageElement[] getImage() {
            return this.image;
        }

        public DicomSpecialElement getSpecialElement() {
            return this.specialElement;
        }
    }
}

