/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.StringUtil
 *  org.weasis.opencv.data.PlanarImage
 */
package org.dcm4che3.img.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.imageio.codec.jpeg.JPEGParser;
import org.dcm4che3.img.DicomImageReadParam;
import org.dcm4che3.img.DicomImageReader;
import org.dcm4che3.img.DicomJpegWriteParam;
import org.dcm4che3.img.DicomOutputData;
import org.dcm4che3.img.Transcoder;
import org.dcm4che3.img.stream.BytesWithImageDescriptor;
import org.dcm4che3.img.stream.ImageDescriptor;
import org.dcm4che3.img.stream.SeekableInMemoryByteChannel;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.img.util.Editable;
import org.dcm4che3.img.util.SupplierEx;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.param.AttributeEditorContext;
import org.weasis.opencv.data.PlanarImage;

public final class ImageAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageAdapter.class);
    private static final DicomImageReadParam DICOM_IMAGE_READ_PARAM = ImageAdapter.createReadParam();
    private static final byte[] EMPTY_BYTES = new byte[0];

    private static DicomImageReadParam createReadParam() {
        DicomImageReadParam param = new DicomImageReadParam();
        param.setReleaseImageAfterProcessing(true);
        return param;
    }

    private ImageAdapter() {
    }

    public static boolean writeDicomFile(Attributes data, AdaptTransferSyntax syntax, Editable<PlanarImage> editable, BytesWithImageDescriptor desc, Path outputPath) {
        if (outputPath == null) {
            throw new IllegalArgumentException("Output path is required");
        }
        try {
            FileUtil.prepareToWriteFile((Path)outputPath);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return desc == null ? ImageAdapter.writeMetadataOnlyFile(data, syntax, outputPath) : ImageAdapter.writeImageFile(data, syntax, editable, desc, outputPath);
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static boolean writeMetadataOnlyFile(Attributes data, AdaptTransferSyntax syntax, Path outputPath) {
        ImageAdapter.adjustSyntaxForMetadataOnly(syntax);
        try (OutputStream outputStream = Files.newOutputStream(outputPath, new OpenOption[0]);){
            boolean bl;
            try (DicomOutputStream writer = new DicomOutputStream(outputStream, "1.2.840.10008.1.2.1");){
                writer.writeDataset(data.createFileMetaInformation(syntax.suitable), data);
                writer.finish();
                bl = true;
            }
            return bl;
        }
        catch (Exception e) {
            LOGGER.error("Writing DICOM metadata file to {}", (Object)outputPath, (Object)e);
            FileUtil.delete((Path)outputPath);
            return false;
        }
    }

    private static void adjustSyntaxForMetadataOnly(AdaptTransferSyntax syntax) {
        if ("1.2.840.10008.1.2".equals(syntax.suitable) || "1.2.840.10008.1.2.2".equals(syntax.suitable)) {
            syntax.suitable = "1.2.840.10008.1.2";
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean writeImageFile(Attributes data, AdaptTransferSyntax syntax, Editable<PlanarImage> editable, BytesWithImageDescriptor desc, Path outputPath) {
        DicomImageReader reader = new DicomImageReader(Transcoder.dicomImageReaderSpi);
        try {
            DicomOutputData imgData = ImageAdapter.getDicomOutputData(reader, syntax.requested, desc, editable);
            ImageAdapter.checkSyntax(syntax, imgData);
            Attributes dataSet = new Attributes(data);
            dataSet.remove(2145386512);
            String dstTsuid = syntax.suitable;
            try (OutputStream outputStream = Files.newOutputStream(outputPath, new OpenOption[0]);
                 DicomOutputStream dos = new DicomOutputStream(outputStream, dstTsuid);){
                dos.writeFileMetaInformation(dataSet.createFileMetaInformation(dstTsuid));
                ImageAdapter.writeImage(syntax, desc, imgData, dataSet, dstTsuid, dos);
            }
            boolean bl = true;
            return bl;
        }
        catch (IOException e) {
            LOGGER.error("Error getting DICOM output data for {}", (Object)outputPath, (Object)e);
            boolean bl = false;
            return bl;
        }
        catch (Exception e) {
            LOGGER.error("Error transcoding image data for {}", (Object)outputPath, (Object)e);
            FileUtil.delete((Path)outputPath);
            boolean bl = false;
            return bl;
        }
        finally {
            reader.dispose();
        }
    }

    private static void writeImage(AdaptTransferSyntax syntax, BytesWithImageDescriptor desc, DicomOutputData imgData, Attributes dataSet, String dstTsuid, DicomOutputStream dos) throws IOException {
        if (DicomOutputData.isNativeSyntax(dstTsuid)) {
            imgData.writeRawImageData(dos, dataSet);
        } else {
            ImageAdapter.writeCompressedImage(syntax, desc, imgData, dataSet, dstTsuid, dos);
        }
    }

    private static void writeCompressedImage(AdaptTransferSyntax syntax, BytesWithImageDescriptor desc, DicomOutputData imgData, Attributes dataSet, String dstTsuid, DicomOutputStream dos) throws IOException {
        DicomJpegWriteParam writeParam = DicomJpegWriteParam.buildDicomImageWriteParam(dstTsuid);
        ImageAdapter.configureCompressionParameters(syntax, writeParam);
        int[] jpegWriteParams = imgData.adaptTagsToCompressedImage(dataSet, imgData.getFirstImage().get(), desc.getImageDescriptor(), writeParam);
        imgData.writeCompressedImageData(dos, dataSet, jpegWriteParams);
    }

    private static void configureCompressionParameters(AdaptTransferSyntax syntax, DicomJpegWriteParam params) {
        if (params.getCompressionQuality() > 0 && !"1.2.840.10008.1.2.4.110".equals(syntax.suitable)) {
            int quality = syntax.getJpegQuality() <= 0 ? 85 : syntax.getJpegQuality();
            params.setCompressionQuality(quality);
        }
        if (params.getCompressionRatioFactor() > 0 && syntax.getCompressionRatioFactor() > 0) {
            params.setCompressionRatioFactor(syntax.getCompressionRatioFactor());
        }
    }

    public static void checkSyntax(AdaptTransferSyntax syntax, DicomOutputData imgData) {
        if (!syntax.requested.equals(imgData.getTsuid())) {
            syntax.suitable = imgData.getTsuid();
            LOGGER.warn("Transcoding into {} not possible, using {} instead", (Object)syntax.requested, (Object)syntax.suitable);
        }
    }

    public static DataWriter buildDataWriter(Attributes data, AdaptTransferSyntax syntax, Editable<PlanarImage> editable, BytesWithImageDescriptor desc) throws IOException {
        return desc == null ? ImageAdapter.createMetadataDataWriter(data, syntax) : ImageAdapter.createImageDataWriter(data, syntax, editable, desc);
    }

    private static DataWriter createMetadataDataWriter(Attributes data, AdaptTransferSyntax syntax) {
        syntax.suitable = syntax.original;
        return (out, tsuid) -> {
            try (DicomOutputStream writer = new DicomOutputStream(out, tsuid);){
                writer.writeDataset(null, data);
                writer.finish();
            }
        };
    }

    private static DataWriter createImageDataWriter(Attributes data, AdaptTransferSyntax syntax, Editable<PlanarImage> editable, BytesWithImageDescriptor desc) throws IOException {
        DicomImageReader reader = new DicomImageReader(Transcoder.dicomImageReaderSpi);
        DicomOutputData imgData = ImageAdapter.getDicomOutputData(reader, syntax.requested, desc, editable);
        ImageAdapter.checkSyntax(syntax, imgData);
        return (out, tsuid) -> {
            Attributes dataSet = new Attributes(data);
            dataSet.remove(2145386512);
            try (DicomOutputStream dos = new DicomOutputStream(out, tsuid);){
                ImageAdapter.writeImage(syntax, desc, imgData, dataSet, tsuid, dos);
            }
            catch (Exception e) {
                LOGGER.error("Error transcoding image data", (Throwable)e);
            }
            finally {
                reader.dispose();
            }
        };
    }

    private static boolean isTranscodable(String origUid, String desUid) {
        return !desUid.equals(origUid) && (!DicomUtils.isNative(origUid) || !DicomUtils.isNative(desUid));
    }

    public static BytesWithImageDescriptor imageTranscode(Attributes data, AdaptTransferSyntax syntax, AttributeEditorContext context) {
        VR.Holder pixelDataVR = new VR.Holder();
        Object dataValue = data.getValue(2145386512, pixelDataVR);
        if (!ImageAdapter.isTranscodingApplicable(dataValue, syntax, context)) {
            return null;
        }
        ImageDescriptor imageDescriptor = new ImageDescriptor(data);
        return new ImageBytesDescriptor(dataValue, pixelDataVR, imageDescriptor, syntax.original);
    }

    private static boolean isTranscodingApplicable(Object pixData, AdaptTransferSyntax syntax, AttributeEditorContext context) {
        return pixData != null && DicomImageReader.isSupportedSyntax(syntax.original) && DicomOutputData.isSupportedSyntax(syntax.requested) && (context.hasPixelProcessing() || ImageAdapter.isTranscodable(syntax.original, syntax.requested));
    }

    private static DicomOutputData getDicomOutputData(DicomImageReader reader, String outputTsuid, BytesWithImageDescriptor desc, Editable<PlanarImage> editable) throws IOException {
        reader.setInput(desc);
        List<SupplierEx<PlanarImage, IOException>> images = reader.getLazyPlanarImages(DICOM_IMAGE_READ_PARAM, editable);
        return new DicomOutputData(images, desc.getImageDescriptor(), outputTsuid);
    }

    public static final class AdaptTransferSyntax {
        private final String original;
        private final String requested;
        private String suitable;
        private int jpegQuality = 85;
        private int compressionRatioFactor;

        public AdaptTransferSyntax(String original, String requested) {
            if (!StringUtil.hasText((CharSequence)original) || !StringUtil.hasText((CharSequence)requested)) {
                throw new IllegalArgumentException("Non-empty values required for transfer syntax UIDs");
            }
            this.original = original;
            this.requested = requested;
            this.suitable = requested;
        }

        public String getOriginal() {
            return this.original;
        }

        public String getRequested() {
            return this.requested;
        }

        public String getSuitable() {
            return this.suitable;
        }

        public int getJpegQuality() {
            return this.jpegQuality;
        }

        public int getCompressionRatioFactor() {
            return this.compressionRatioFactor;
        }

        public void setJpegQuality(int jpegQuality) {
            this.jpegQuality = jpegQuality;
        }

        public void setCompressionRatioFactor(int compressionRatioFactor) {
            this.compressionRatioFactor = compressionRatioFactor;
        }

        public void setSuitable(String suitable) {
            if (TransferSyntaxType.forUID(suitable) != TransferSyntaxType.UNKNOWN) {
                this.suitable = suitable;
            }
        }
    }

    private static final class ImageBytesDescriptor
    implements BytesWithImageDescriptor {
        private final Object pixdata;
        private final VR.Holder pixeldataVR;
        private final ImageDescriptor imageDescriptor;
        private final String transferSyntax;
        private final ByteBuffer[] multiFrameBuffer = new ByteBuffer[1];
        private final List<Integer> fragmentsPositions = new ArrayList<Integer>();

        ImageBytesDescriptor(Object pixdata, VR.Holder pixeldataVR, ImageDescriptor imageDescriptor, String transferSyntax) {
            this.pixdata = pixdata;
            this.pixeldataVR = pixeldataVR;
            this.imageDescriptor = imageDescriptor;
            this.transferSyntax = transferSyntax;
        }

        @Override
        public ImageDescriptor getImageDescriptor() {
            return this.imageDescriptor;
        }

        @Override
        public boolean isBigEndian() {
            Object object = this.pixdata;
            if (object instanceof BulkData) {
                BulkData bulkData = (BulkData)object;
                return bulkData.bigEndian();
            }
            object = this.pixdata;
            if (object instanceof Fragments) {
                Fragments fragments = (Fragments)object;
                return fragments.bigEndian();
            }
            return false;
        }

        @Override
        public VR getPixelDataVR() {
            return this.pixeldataVR.vr;
        }

        @Override
        public String getTransferSyntax() {
            return this.transferSyntax;
        }

        @Override
        public ByteBuffer getBytes(int frame) throws IOException {
            if (this.imageDescriptor.getBitsStored() < 1) {
                return ByteBuffer.wrap(EMPTY_BYTES);
            }
            Object object = this.pixdata;
            if (object instanceof BulkData) {
                BulkData bulkData = (BulkData)object;
                return this.getBytesFromBulkData(bulkData, frame);
            }
            object = this.pixdata;
            if (object instanceof Fragments) {
                Fragments fragments = (Fragments)object;
                return this.getBytesFromFragments(fragments, frame);
            }
            throw new IOException("Neither fragments nor BulkData!");
        }

        private ByteBuffer getBytesFromBulkData(BulkData bulkData, int frame) throws IOException {
            int frameLength = this.calculateFrameLength();
            if (this.multiFrameBuffer[0] == null) {
                this.multiFrameBuffer[0] = ByteBuffer.wrap(bulkData.toBytes(this.pixeldataVR.vr, this.isBigEndian()));
            }
            this.validateFrameAccess(frame, frameLength);
            byte[] frameData = new byte[frameLength];
            this.multiFrameBuffer[0].position(frame * frameLength);
            this.multiFrameBuffer[0].get(frameData, 0, frameLength);
            return ByteBuffer.wrap(frameData);
        }

        private int calculateFrameLength() {
            return this.imageDescriptor.getPhotometricInterpretation().frameLength(this.imageDescriptor.getColumns(), this.imageDescriptor.getRows(), this.imageDescriptor.getSamples(), this.imageDescriptor.getBitsAllocated());
        }

        private void validateFrameAccess(int frame, int frameLength) throws IOException {
            if (this.multiFrameBuffer[0].limit() < (frame + 1) * frameLength) {
                throw new IOException("Frame " + frame + " exceeds stream bounds");
            }
        }

        private ByteBuffer getBytesFromFragments(Fragments fragments, int frame) throws IOException {
            int numberOfFrames = this.imageDescriptor.getFrames();
            return numberOfFrames == 1 ? this.getSingleFrameFromFragments(fragments) : this.getMultiFrameFromFragments(fragments, frame, numberOfFrames);
        }

        private ByteBuffer getSingleFrameFromFragments(Fragments fragments) throws IOException {
            int nbFragments = fragments.size();
            int totalLength = this.calculateTotalFragmentLength(fragments, nbFragments);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream(totalLength);){
                this.writeFragmentsToStream(fragments, 1, nbFragments, out);
                ByteBuffer byteBuffer = ByteBuffer.wrap(out.toByteArray());
                return byteBuffer;
            }
        }

        private int calculateTotalFragmentLength(Fragments fragments, int nbFragments) {
            return fragments.stream().skip(1L).limit((long)nbFragments - 1L).mapToInt(obj -> ((BulkData)obj).length()).sum();
        }

        private void writeFragmentsToStream(Fragments fragments, int start, int end, ByteArrayOutputStream out) throws IOException {
            for (int i = start; i < end; ++i) {
                BulkData bulkData = (BulkData)fragments.get(i);
                byte[] bytes = bulkData.toBytes(this.pixeldataVR.vr, this.isBigEndian());
                out.write(bytes);
            }
        }

        private ByteBuffer getMultiFrameFromFragments(Fragments fragments, int frame, int numberOfFrames) throws IOException {
            if (this.fragmentsPositions.isEmpty()) {
                this.initializeFragmentPositions(fragments);
            }
            if (this.fragmentsPositions.size() != numberOfFrames) {
                throw new IOException("Fragment count mismatch: expected " + numberOfFrames + ", found " + this.fragmentsPositions.size());
            }
            return this.extractFrameFromFragments(fragments, frame);
        }

        private void initializeFragmentPositions(Fragments fragments) {
            int nbFragments = fragments.size();
            if ("1.2.840.10008.1.2.5".equals(this.transferSyntax)) {
                this.initializeRLEFragmentPositions(nbFragments);
            } else {
                this.initializeJPEGFragmentPositions(fragments, nbFragments);
            }
        }

        private void initializeRLEFragmentPositions(int nbFragments) {
            for (int i = 1; i < nbFragments; ++i) {
                this.fragmentsPositions.add(i);
            }
        }

        private void initializeJPEGFragmentPositions(Fragments fragments, int nbFragments) {
            for (int i = 1; i < nbFragments; ++i) {
                BulkData bulkData = (BulkData)fragments.get(i);
                if (!this.isValidJPEGFragment(bulkData)) continue;
                this.fragmentsPositions.add(i);
            }
        }

        private boolean isValidJPEGFragment(BulkData bulkData) {
            byte[] bytes = bulkData.toBytes(this.pixeldataVR.vr, this.isBigEndian());
            SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(bytes);
            try {
                new JPEGParser(channel);
                boolean bl = true;
                channel.close();
                return bl;
            }
            catch (Throwable throwable) {
                try {
                    try {
                        channel.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
                catch (Exception e) {
                    return false;
                }
            }
        }

        private ByteBuffer extractFrameFromFragments(Fragments fragments, int frame) throws IOException {
            int start = this.fragmentsPositions.get(frame);
            int end = frame + 1 >= this.fragmentsPositions.size() ? fragments.size() : this.fragmentsPositions.get(frame + 1).intValue();
            int totalLength = this.calculateFragmentRangeLength(fragments, start, end);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream(totalLength);){
                this.writeFragmentsToStream(fragments, start, end, out);
                ByteBuffer byteBuffer = ByteBuffer.wrap(out.toByteArray());
                return byteBuffer;
            }
        }

        private int calculateFragmentRangeLength(Fragments fragments, int start, int end) {
            int length = 0;
            for (int i = start; i < end; ++i) {
                length += ((BulkData)fragments.get(i)).length();
            }
            return length;
        }
    }
}

