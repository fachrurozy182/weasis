/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.opencv.core.MatOfInt
 *  org.opencv.imgcodecs.Imgcodecs
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 *  org.weasis.opencv.data.PlanarImage
 */
package org.dcm4che3.img;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.img.DicomImageReader;
import org.dcm4che3.img.DicomJpegWriteParam;
import org.dcm4che3.img.stream.ImageDescriptor;
import org.dcm4che3.img.util.DicomUtils;
import org.dcm4che3.img.util.PixelDataUtils;
import org.dcm4che3.img.util.SupplierEx;
import org.dcm4che3.io.DicomOutputStream;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StringUtil;
import org.weasis.opencv.data.PlanarImage;

public class DicomOutputData {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomOutputData.class);
    private final List<SupplierEx<PlanarImage, IOException>> images;
    private final ImageDescriptor desc;
    private final String tsuid;

    public DicomOutputData(List<SupplierEx<PlanarImage, IOException>> images, ImageDescriptor desc, String tsuid) throws IOException {
        this.validateInputs(images, desc, tsuid);
        this.images = new ArrayList<SupplierEx<PlanarImage, IOException>>(images);
        this.desc = desc;
        int type = CvType.depth((int)this.getFirstImage().get().type());
        this.tsuid = DicomOutputData.adaptSuitableSyntax(desc.getBitsStored(), type, tsuid);
        this.validateTransferSyntax();
    }

    public DicomOutputData(SupplierEx<PlanarImage, IOException> image, ImageDescriptor desc, String tsuid) throws IOException {
        this(Collections.singletonList(image), desc, tsuid);
    }

    public DicomOutputData(PlanarImage image, ImageDescriptor desc, String tsuid) throws IOException {
        this(Collections.singletonList(() -> image), desc, tsuid);
    }

    private void validateInputs(List<SupplierEx<PlanarImage, IOException>> images, ImageDescriptor desc, String tsuid) {
        if (Objects.requireNonNull(images, "Images list cannot be null").isEmpty()) {
            throw new IllegalStateException("No image found!");
        }
        Objects.requireNonNull(desc, "Image descriptor cannot be null");
        Objects.requireNonNull(tsuid, "Transfer syntax UID cannot be null");
    }

    private void validateTransferSyntax() {
        if (!DicomOutputData.isSupportedSyntax(this.tsuid)) {
            throw new IllegalStateException(this.tsuid + " is not supported as encoding syntax!");
        }
    }

    public SupplierEx<PlanarImage, IOException> getFirstImage() {
        return this.images.get(0);
    }

    public List<SupplierEx<PlanarImage, IOException>> getImages() {
        return this.images;
    }

    public String getTsuid() {
        return this.tsuid;
    }

    public void writeCompressedImageData(DicomOutputStream dos, Attributes dataSet, int[] params) throws IOException {
        Mat buf = null;
        MatOfInt dicomParams = null;
        try {
            dicomParams = new MatOfInt(params);
            double uncompressedFrameSize = DicomOutputData.frameSizeInBytes(this.images.get(0).get());
            for (int i = 0; i < this.images.size(); ++i) {
                buf = this.encodeImageFrame(i, dicomParams);
                if (i == 0) {
                    this.writeDatasetHeader(dos, dataSet, params, buf, uncompressedFrameSize);
                }
                this.writeCompressedFrame(dos, buf);
            }
            dos.writeHeader(-73507, null, 0);
        }
        catch (Throwable t) {
            try {
                throw new IOException("Native encoding error", t);
            }
            catch (Throwable throwable) {
                DicomImageReader.closeMat(dicomParams);
                DicomImageReader.closeMat(buf);
                throw throwable;
            }
        }
        DicomImageReader.closeMat((Mat)dicomParams);
        DicomImageReader.closeMat(buf);
    }

    private Mat encodeImageFrame(int frameIndex, MatOfInt dicomParams) throws IOException {
        Mat buf;
        PlanarImage writeImg;
        PlanarImage image = this.images.get(frameIndex).get();
        boolean releaseSrc = image.isReleasedAfterProcessing();
        PlanarImage planarImage = writeImg = DicomUtils.isJpeg2000(this.tsuid) || DicomUtils.isJpegXL(this.tsuid) ? image : PixelDataUtils.bgr2rgb(image);
        if (releaseSrc && !writeImg.equals(image)) {
            image.release();
        }
        if ((buf = Imgcodecs.dicomJpgWrite((Mat)writeImg.toMat(), (MatOfInt)dicomParams, (String)"")).empty()) {
            writeImg.release();
            throw new IOException("Native encoding error: null image");
        }
        if (releaseSrc) {
            writeImg.release();
        }
        return buf;
    }

    private static double frameSizeInBytes(PlanarImage image) {
        return (double)image.width() * (double)image.height() * (double)image.elemSize();
    }

    private void writeDatasetHeader(DicomOutputStream dos, Attributes dataSet, int[] params, Mat buffer, double uncompressedSize) throws IOException {
        this.adaptCompressionRatio(dataSet, params, this.calculateCompressionRatio(uncompressedSize, buffer));
        dos.writeDataset(null, dataSet);
        dos.writeHeader(2145386512, VR.OB, -1);
        dos.writeHeader(-73728, null, 0);
    }

    private double calculateCompressionRatio(double uncompressedSize, Mat buffer) {
        return uncompressedSize / (double)(buffer.width() * buffer.height() * (int)buffer.elemSize());
    }

    private void writeCompressedFrame(DicomOutputStream dos, Mat buffer) throws IOException {
        int frameSize = buffer.width() * buffer.height() * (int)buffer.elemSize();
        byte[] frameData = new byte[frameSize];
        buffer.get(0, 0, frameData);
        dos.writeHeader(-73728, null, frameData.length);
        dos.write(frameData);
    }

    private void adaptCompressionRatio(Attributes dataSet, int[] params, double ratio) {
        if (TransferSyntaxType.isLossyCompression(this.tsuid)) {
            this.setLossyCompressionTags(dataSet, params[4], ratio);
        }
    }

    private void setLossyCompressionTags(Attributes dataSet, int compressType, double ratio) {
        dataSet.setString(2629904, VR.CS, "01");
        String method = this.getCompressionMethod(compressType);
        dataSet.setString(2629908, VR.CS, this.updateCompressionMethods(dataSet, method));
        if (ratio > 0.0 && Double.isFinite(ratio)) {
            dataSet.setDouble(0x282112, VR.DS, this.updateCompressionRatios(dataSet, ratio));
        } else {
            LOGGER.warn("Cannot compute the lossy compression ratio of {}", (Object)method);
        }
    }

    private String getCompressionMethod(int compressType) {
        return switch (compressType) {
            case 3 -> "ISO_15444_1";
            case 2 -> "ISO_14495_1";
            case 4 -> "ISO_18181_1";
            default -> "ISO_10918_1";
        };
    }

    private double[] updateCompressionRatios(Attributes dataSet, double ratio) {
        double[] dArray;
        double[] existing = dataSet.getDoubles(0x282112);
        if (existing == null) {
            double[] dArray2 = new double[1];
            dArray = dArray2;
            dArray2[0] = ratio;
        } else {
            dArray = this.appendToArray(existing, ratio);
        }
        return dArray;
    }

    private double[] appendToArray(double[] array, double value) {
        double[] result = Arrays.copyOf(array, array.length + 1);
        result[result.length - 1] = value;
        return result;
    }

    private String[] updateCompressionMethods(Attributes dataSet, String method) {
        String[] existing = DicomUtils.getStringArrayFromDicomElement(dataSet, 2629908, new String[0]);
        String[] updated = Arrays.copyOf(existing, existing.length + 1);
        updated[updated.length - 1] = method;
        return this.sanitizeCompressionMethods(updated);
    }

    private String[] sanitizeCompressionMethods(String[] methods) {
        for (int i = 0; i < methods.length; ++i) {
            if (StringUtil.hasText((CharSequence)methods[i])) continue;
            methods[i] = "unknown";
        }
        return methods;
    }

    public void writeRawImageData(DicomOutputStream dos, Attributes data) {
        try {
            PlanarImage firstImage = this.getFirstImage().get();
            DicomOutputData.adaptTagsToRawImage(data, firstImage, this.desc);
            dos.writeDataset(null, data);
            PixelDataInfo pixelDataInfo = this.calculatePixelDataInfo(firstImage);
            dos.writeHeader(2145386512, VR.OB, pixelDataInfo.totalLength());
            this.writePixelDataByType(dos, pixelDataInfo);
        }
        catch (Exception e) {
            LOGGER.error("Error writing raw pixel data", (Throwable)e);
        }
    }

    private PixelDataInfo calculatePixelDataInfo(PlanarImage firstImage) {
        int type = CvType.depth((int)firstImage.type());
        int channels = CvType.channels((int)firstImage.type());
        int imageSize = firstImage.width() * firstImage.height();
        int totalLength = this.images.size() * imageSize * (int)firstImage.elemSize();
        return new PixelDataInfo(type, channels, imageSize, totalLength);
    }

    private void writePixelDataByType(DicomOutputStream dos, PixelDataInfo info) throws IOException {
        switch (info.type()) {
            case 0: 
            case 1: {
                new ByteDataWriter().write(dos, info);
                break;
            }
            case 2: 
            case 3: {
                new ShortDataWriter().write(dos, info);
                break;
            }
            case 4: {
                new IntDataWriter().write(dos, info);
                break;
            }
            case 5: {
                new FloatDataWriter().write(dos, info);
                break;
            }
            case 6: {
                new DoubleDataWriter().write(dos, info);
                break;
            }
            default: {
                throw new IllegalStateException("Cannot write unknown image type: " + info.type());
            }
        }
    }

    public static void adaptTagsToRawImage(Attributes data, PlanarImage img, ImageDescriptor desc) {
        ImageAttributes imageAttrs = DicomOutputData.extractImageAttributes(img, desc);
        DicomOutputData.updateRawImageAttributes(data, img, imageAttrs, desc);
    }

    private static ImageAttributes extractImageAttributes(PlanarImage img, ImageDescriptor desc) {
        int cvType = img.type();
        int channels = CvType.channels((int)cvType);
        boolean signed = CvType.depth((int)cvType) == 3 || desc.isSigned();
        return new ImageAttributes(channels, signed);
    }

    private static void updateRawImageAttributes(Attributes data, PlanarImage img, ImageAttributes attrs, ImageDescriptor desc) {
        data.setInt(2621457, VR.US, img.width());
        data.setInt(2621456, VR.US, img.height());
        data.setInt(0x280002, VR.US, attrs.channels());
        data.setInt(2621696, VR.US, desc.getBitsAllocated());
        data.setInt(2621697, VR.US, desc.getBitsStored());
        data.setInt(2621698, VR.US, desc.getBitsStored() - 1);
        data.setInt(2621699, VR.US, attrs.signed() ? 1 : 0);
        DicomOutputData.setPhotometricInterpretation(data, img, desc);
    }

    private static void setPhotometricInterpretation(Attributes data, PlanarImage img, ImageDescriptor desc) {
        String pmi = desc.getPhotometricInterpretation().toString();
        if (img.channels() > 1) {
            pmi = PhotometricInterpretation.RGB.toString();
            data.setInt(2621446, VR.US, 0);
        }
        data.setString(2621444, VR.CS, pmi);
    }

    public int[] adaptTagsToCompressedImage(Attributes data, PlanarImage img, ImageDescriptor desc, DicomJpegWriteParam param) {
        ImageCharacteristics characteristics = this.analyzeImageCharacteristics(img, desc);
        CompressionSettings settings = this.determineCompressionSettings(param, characteristics, (int)img.elemSize1());
        int[] encodingParams = this.buildEncodingParameters(img, settings, characteristics, param);
        this.updateDicomAttributes(data, img, characteristics);
        return encodingParams;
    }

    private ImageCharacteristics analyzeImageCharacteristics(PlanarImage img, ImageDescriptor desc) {
        int cvType = img.type();
        int elemSize = (int)img.elemSize1();
        int channels = CvType.channels((int)cvType);
        int depth = CvType.depth((int)cvType);
        boolean signed = depth != 0 && (depth != 2 || desc.isSigned());
        int bitAllocated = elemSize * 8;
        int bitCompressed = Math.min(desc.getBitsCompressed(), bitAllocated);
        return new ImageCharacteristics(channels, signed, bitAllocated, bitCompressed, depth);
    }

    private CompressionSettings determineCompressionSettings(DicomJpegWriteParam param, ImageCharacteristics characteristics, int elemSize) {
        TransferSyntaxType transferSyntax = param.getType();
        int jpeglsNearLosslessError = param.getNearLosslessError();
        int bitDepthForEncoder = characteristics.bitCompressed();
        int compressionType = this.determineCompressionType(transferSyntax);
        if (!(transferSyntax != TransferSyntaxType.JPEG_XL || "1.2.840.10008.1.2.4.112".equals(param.getTransferSyntaxUid()) && elemSize != 2)) {
            param.setCompressionQuality(100);
        }
        if (transferSyntax == TransferSyntaxType.JPEG_LS && characteristics.signed()) {
            LOGGER.warn("Force compression to JPEG-LS lossless as lossy is not adapted to signed data.");
            jpeglsNearLosslessError = 0;
            bitDepthForEncoder = 16;
        } else if (transferSyntax != TransferSyntaxType.JPEG_2000) {
            bitDepthForEncoder = this.adjustBitDepthForEncoder(characteristics, param, transferSyntax);
        }
        return new CompressionSettings(compressionType, bitDepthForEncoder, jpeglsNearLosslessError);
    }

    private int determineCompressionType(TransferSyntaxType transferSyntax) {
        return switch (transferSyntax) {
            case TransferSyntaxType.JPEG_2000 -> 3;
            case TransferSyntaxType.JPEG_LS -> 2;
            case TransferSyntaxType.JPEG_XL -> 4;
            default -> 1;
        };
    }

    private int adjustBitDepthForEncoder(ImageCharacteristics characteristics, DicomJpegWriteParam param, TransferSyntaxType transferSyntax) {
        int bitDepth = characteristics.bitCompressed();
        if (transferSyntax != TransferSyntaxType.JPEG_2000 && bitDepth == 8 && characteristics.bitAllocated() == 16) {
            return 12;
        }
        return this.adjustJpegBitDepth(characteristics, param);
    }

    private int adjustJpegBitDepth(ImageCharacteristics characteristics, DicomJpegWriteParam param) {
        int bitCompressed = characteristics.bitCompressed();
        if (bitCompressed <= 8) {
            return 8;
        }
        if (bitCompressed <= 12) {
            if (characteristics.signed() && param.getPrediction() > 1) {
                LOGGER.warn("Force JPEGLosslessNonHierarchical14 compression to 16-bit with signed data.");
                return 16;
            }
            return 12;
        }
        return 16;
    }

    private int[] buildEncodingParameters(PlanarImage img, CompressionSettings settings, ImageCharacteristics characteristics, DicomJpegWriteParam param) {
        int dcmFlags = characteristics.signed ? 1 : 0;
        int epi = characteristics.channels == 1 ? 3 : 5;
        int[] params = new int[18];
        params[0] = -1;
        params[1] = dcmFlags;
        params[2] = img.width();
        params[3] = img.height();
        params[4] = settings.compressType;
        params[5] = characteristics.channels;
        params[6] = settings.bitCompressedForEncoder;
        params[7] = 2;
        params[9] = epi;
        params[10] = param.getJpegMode();
        params[11] = settings.jpeglsNLE;
        params[12] = param.getCompressionRatioFactor();
        params[13] = param.getCompressionQuality();
        params[14] = param.getPrediction();
        params[15] = param.getPointTransform();
        params[16] = param.getJxlEffort();
        params[17] = param.getJxlDecodingSpeed();
        return params;
    }

    private void updateDicomAttributes(Attributes data, PlanarImage img, ImageCharacteristics characteristics) {
        data.setInt(2621457, VR.US, img.width());
        data.setInt(2621456, VR.US, img.height());
        data.setInt(0x280002, VR.US, characteristics.channels());
        data.setInt(2621696, VR.US, characteristics.bitAllocated());
        data.setInt(2621697, VR.US, characteristics.bitCompressed());
        data.setInt(2621698, VR.US, characteristics.bitCompressed() - 1);
        data.setInt(2621699, VR.US, characteristics.signed() ? 1 : 0);
        DicomOutputData.setPhotometricInterpretation(data, img, this.desc);
    }

    public static String adaptSuitableSyntax(int bitStored, int type, String dstTsuid) {
        return switch (dstTsuid) {
            case "1.2.840.10008.1.2", "1.2.840.10008.1.2.1" -> "1.2.840.10008.1.2.1";
            case "1.2.840.10008.1.2.4.50" -> DicomOutputData.adaptJpegBaseline(type);
            case "1.2.840.10008.1.2.4.51", "1.2.840.10008.1.2.4.53", "1.2.840.10008.1.2.4.55" -> DicomOutputData.adaptJpegExtended(type, bitStored, dstTsuid);
            case "1.2.840.10008.1.2.4.57", "1.2.840.10008.1.2.4.70", "1.2.840.10008.1.2.4.80", "1.2.840.10008.1.2.4.81", "1.2.840.10008.1.2.4.90", "1.2.840.10008.1.2.4.91", "1.2.840.10008.1.2.4.201", "1.2.840.10008.1.2.4.202", "1.2.840.10008.1.2.4.203" -> {
                if (type <= 3) {
                    yield dstTsuid;
                }
                yield "1.2.840.10008.1.2.1";
            }
            case "1.2.840.10008.1.2.4.110" -> {
                if (type <= 5) {
                    yield dstTsuid;
                }
                yield "1.2.840.10008.1.2.1";
            }
            case "1.2.840.10008.1.2.4.111", "1.2.840.10008.1.2.4.112" -> {
                if (type <= 1) {
                    yield dstTsuid;
                }
                if (type <= 5) {
                    yield "1.2.840.10008.1.2.4.110";
                }
                yield "1.2.840.10008.1.2.1";
            }
            default -> dstTsuid;
        };
    }

    private static String adaptJpegBaseline(int type) {
        if (type <= 1) {
            return "1.2.840.10008.1.2.4.50";
        }
        if (type <= 3) {
            return "1.2.840.10008.1.2.4.70";
        }
        return "1.2.840.10008.1.2.1";
    }

    private static String adaptJpegExtended(int type, int bitStored, String dstTsuid) {
        if (type <= 2 && bitStored <= 12) {
            return dstTsuid;
        }
        if (type <= 3) {
            return "1.2.840.10008.1.2.4.70";
        }
        return "1.2.840.10008.1.2.1";
    }

    public static boolean isAdaptableSyntax(String uid) {
        return switch (uid) {
            case "1.2.840.10008.1.2.4.50", "1.2.840.10008.1.2.4.51", "1.2.840.10008.1.2.4.53", "1.2.840.10008.1.2.4.55" -> true;
            default -> false;
        };
    }

    public static boolean isNativeSyntax(String uid) {
        return switch (uid) {
            case "1.2.840.10008.1.2", "1.2.840.10008.1.2.1" -> true;
            default -> false;
        };
    }

    public static boolean isSupportedSyntax(String uid) {
        return switch (uid) {
            case "1.2.840.10008.1.2", "1.2.840.10008.1.2.1", "1.2.840.10008.1.2.4.50", "1.2.840.10008.1.2.4.51", "1.2.840.10008.1.2.4.53", "1.2.840.10008.1.2.4.55", "1.2.840.10008.1.2.4.57", "1.2.840.10008.1.2.4.70", "1.2.840.10008.1.2.4.80", "1.2.840.10008.1.2.4.81", "1.2.840.10008.1.2.4.90", "1.2.840.10008.1.2.4.91", "1.2.840.10008.1.2.4.112", "1.2.840.10008.1.2.4.110", "1.2.840.10008.1.2.4.111" -> true;
            default -> false;
        };
    }

    private record PixelDataInfo(int type, int channels, int imageSize, int totalLength) {
    }

    private class ByteDataWriter
    extends PixelDataWriter {
        private ByteDataWriter() {
        }

        @Override
        void write(DicomOutputStream dos, PixelDataInfo info) throws IOException {
            byte[] data = new byte[info.imageSize() * info.channels()];
            for (SupplierEx<PlanarImage, IOException> imageSupplier : DicomOutputData.this.images) {
                PlanarImage img = this.convertImage(imageSupplier);
                img.get(0, 0, data);
                dos.write(data);
            }
        }
    }

    private class ShortDataWriter
    extends PixelDataWriter {
        private ShortDataWriter() {
        }

        @Override
        void write(DicomOutputStream dos, PixelDataInfo info) throws IOException {
            short[] data = new short[info.imageSize() * info.channels()];
            ByteBuffer buffer = ByteBuffer.allocate(data.length * 2).order(ByteOrder.LITTLE_ENDIAN);
            for (SupplierEx<PlanarImage, IOException> imageSupplier : DicomOutputData.this.images) {
                PlanarImage img = this.convertImage(imageSupplier);
                img.get(0, 0, data);
                buffer.clear().asShortBuffer().put(data);
                dos.write(buffer.array());
            }
        }
    }

    private class IntDataWriter
    extends PixelDataWriter {
        private IntDataWriter() {
        }

        @Override
        void write(DicomOutputStream dos, PixelDataInfo info) throws IOException {
            int[] data = new int[info.imageSize() * info.channels()];
            ByteBuffer buffer = ByteBuffer.allocate(data.length * 4).order(ByteOrder.LITTLE_ENDIAN);
            for (SupplierEx<PlanarImage, IOException> imageSupplier : DicomOutputData.this.images) {
                PlanarImage img = this.convertImage(imageSupplier);
                img.get(0, 0, data);
                buffer.clear().asIntBuffer().put(data);
                dos.write(buffer.array());
            }
        }
    }

    private class FloatDataWriter
    extends PixelDataWriter {
        private FloatDataWriter() {
        }

        @Override
        void write(DicomOutputStream dos, PixelDataInfo info) throws IOException {
            float[] data = new float[info.imageSize() * info.channels()];
            ByteBuffer buffer = ByteBuffer.allocate(data.length * 4).order(ByteOrder.LITTLE_ENDIAN);
            for (SupplierEx<PlanarImage, IOException> imageSupplier : DicomOutputData.this.images) {
                PlanarImage img = this.convertImage(imageSupplier);
                img.get(0, 0, data);
                buffer.clear().asFloatBuffer().put(data);
                dos.write(buffer.array());
            }
        }
    }

    private class DoubleDataWriter
    extends PixelDataWriter {
        private DoubleDataWriter() {
        }

        @Override
        void write(DicomOutputStream dos, PixelDataInfo info) throws IOException {
            double[] data = new double[info.imageSize() * info.channels()];
            ByteBuffer buffer = ByteBuffer.allocate(data.length * 8).order(ByteOrder.LITTLE_ENDIAN);
            for (SupplierEx<PlanarImage, IOException> imageSupplier : DicomOutputData.this.images) {
                PlanarImage img = this.convertImage(imageSupplier);
                img.get(0, 0, data);
                buffer.clear().asDoubleBuffer().put(data);
                dos.write(buffer.array());
            }
        }
    }

    private record ImageAttributes(int channels, boolean signed) {
    }

    private record ImageCharacteristics(int channels, boolean signed, int bitAllocated, int bitCompressed, int depth) {
    }

    private record CompressionSettings(int compressType, int bitCompressedForEncoder, int jpeglsNLE) {
    }

    private static abstract class PixelDataWriter {
        private PixelDataWriter() {
        }

        abstract void write(DicomOutputStream var1, PixelDataInfo var2) throws IOException;

        protected PlanarImage convertImage(SupplierEx<PlanarImage, IOException> imageSupplier) throws IOException {
            return PixelDataUtils.bgr2rgb(imageSupplier.get());
        }
    }
}

