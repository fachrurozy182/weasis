/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Mat
 *  org.opencv.core.MatOfInt
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.FileUtil
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageIOHandler
 */
package org.dcm4che3.img;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.DicomImageReadParam;
import org.dcm4che3.img.DicomImageReader;
import org.dcm4che3.img.DicomImageReaderSpi;
import org.dcm4che3.img.DicomJpegWriteParam;
import org.dcm4che3.img.DicomMetaData;
import org.dcm4che3.img.DicomOutputData;
import org.dcm4che3.img.DicomTranscodeParam;
import org.dcm4che3.img.ImageRendering;
import org.dcm4che3.img.ImageTranscodeParam;
import org.dcm4che3.img.op.MaskArea;
import org.dcm4che3.img.stream.DicomFileInputStream;
import org.dcm4che3.img.stream.ImageDescriptor;
import org.dcm4che3.img.util.Editable;
import org.dcm4che3.img.util.SupplierEx;
import org.dcm4che3.io.DicomOutputStream;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.FileUtil;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageIOHandler;

public final class Transcoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(Transcoder.class);
    private static final int DEFAULT_JPEG_QUALITY = 80;
    public static final DicomImageReaderSpi dicomImageReaderSpi = new DicomImageReaderSpi();
    private static final DicomImageReadParam dicomImageReadParam = new DicomImageReadParam();

    private Transcoder() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static List<Path> dcm2image(Path srcPath, Path dstPath, ImageTranscodeParam params) throws Exception {
        ArrayList<Path> outFiles = new ArrayList<Path>();
        Format format = params.getFormat();
        DicomImageReader reader = Transcoder.createAndConfigureReader(srcPath);
        try {
            MatOfInt compressionParams = Transcoder.createCompressionParams(format, params);
            int frameCount = reader.getImageDescriptor().getFrames();
            int indexSize = Transcoder.calculateIndexSize(frameCount);
            for (int frameIndex = 0; frameIndex < frameCount; ++frameIndex) {
                PlanarImage processedImage = Transcoder.processImageFrame(reader, params, format, frameIndex);
                Path outputPath = Transcoder.createOutputPath(srcPath, dstPath, format, frameIndex + 1, indexSize);
                Transcoder.writeImageToFile(processedImage, outputPath, format, compressionParams);
                outFiles.add(outputPath);
            }
        }
        finally {
            reader.dispose();
        }
        return outFiles;
    }

    public static Path dcm2dcm(Path srcPath, Path dstPath, DicomTranscodeParam params) throws IOException {
        Path outPath = Transcoder.adaptFileExtension(FileUtil.getOutputPath((Path)srcPath, (Path)dstPath), ".dcm");
        try (OutputStream outputStream = Files.newOutputStream(outPath, new OpenOption[0]);){
            Transcoder.dcm2dcm(srcPath, outputStream, params);
        }
        catch (Exception e) {
            FileUtil.delete((Path)outPath);
            if (e instanceof IOException) {
                IOException ioException = (IOException)e;
                throw ioException;
            }
            throw new IOException("Transcoding failed", e);
        }
        return outPath;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void dcm2dcm(Path srcPath, OutputStream outputStream, DicomTranscodeParam params) throws IOException {
        DicomImageReader reader = new DicomImageReader(dicomImageReaderSpi);
        try {
            reader.setInput(new DicomFileInputStream(srcPath), false, false);
            DicomTranscodeContext context = Transcoder.createTranscodeContext(reader, params);
            try (DicomOutputStream dos = new DicomOutputStream(outputStream, context.actualTsuid);){
                Transcoder.writeTranscodedDicom(dos, context);
            }
            catch (Exception e) {
                throw new IOException("Transcoding failed", e);
            }
        }
        finally {
            reader.dispose();
        }
    }

    public static Editable<PlanarImage> getMaskedImage(MaskArea maskArea) {
        if (maskArea == null) {
            return null;
        }
        return image -> {
            ImageCV maskedImage = MaskArea.drawShape(image.toMat(), maskArea);
            if (image.isReleasedAfterProcessing()) {
                image.release();
                maskedImage.setReleasedAfterProcessing(true);
            }
            return maskedImage;
        };
    }

    private static DicomImageReader createAndConfigureReader(Path srcPath) throws Exception {
        DicomImageReader reader = new DicomImageReader(dicomImageReaderSpi);
        reader.setInput(new DicomFileInputStream(srcPath), false, false);
        return reader;
    }

    private static MatOfInt createCompressionParams(Format format, ImageTranscodeParam params) {
        return format == Format.JPEG ? new MatOfInt(new int[]{1, Transcoder.getCompressionQuality(params)}) : null;
    }

    private static int calculateIndexSize(int frameCount) {
        return frameCount > 1 ? (int)Math.log10(frameCount) + 1 : 0;
    }

    private static PlanarImage processImageFrame(DicomImageReader reader, ImageTranscodeParam params, Format format, int frameIndex) throws IOException {
        PlanarImage image = reader.getPlanarImage(frameIndex, params.getReadParam());
        boolean preserveRaw = Transcoder.shouldPreserveRawImage(params, format, image.type());
        return preserveRaw ? ImageRendering.getRawRenderedImage(image, reader.getImageDescriptor(), params.getReadParam(), frameIndex) : ImageRendering.getDefaultRenderedImage(image, reader.getImageDescriptor(), params.getReadParam(), frameIndex);
    }

    private static Path createOutputPath(Path srcPath, Path dstPath, Format format, int frameNumber, int indexSize) {
        Path basePath = FileUtil.getOutputPath((Path)srcPath, (Path)dstPath);
        Path pathWithExtension = Transcoder.adaptFileExtension(basePath, format.extension);
        return FileUtil.addFileIndex((Path)pathWithExtension, (int)frameNumber, (int)indexSize);
    }

    private static void writeImageToFile(PlanarImage image, Path outputPath, Format format, MatOfInt compressionParams) {
        boolean success;
        boolean bl = success = compressionParams == null ? ImageIOHandler.writeImage((Mat)image.toMat(), (Path)outputPath) : ImageIOHandler.writeImage((Mat)image.toMat(), (Path)outputPath, (MatOfInt)compressionParams);
        if (!success) {
            LOGGER.error("Failed to write {} image: {}", (Object)format, (Object)outputPath);
            FileUtil.delete((Path)outputPath);
        }
    }

    private static DicomTranscodeContext createTranscodeContext(DicomImageReader reader, DicomTranscodeParam params) throws IOException {
        DicomMetaData metaData = reader.getStreamMetadata();
        if (metaData == null) {
            throw new IOException("No DICOM metadata available from the image reader");
        }
        Attributes dataSet = new Attributes(metaData.getDicomObject());
        dataSet.remove(2145386512);
        Editable<PlanarImage> mask = Transcoder.createMask(dataSet, params);
        DicomImageReadParam readParams = Transcoder.getEffectiveReadParams(params);
        List<SupplierEx<PlanarImage, IOException>> images = reader.getLazyPlanarImages(readParams, mask);
        String targetTsuid = params.getOutputTsuid();
        DicomJpegWriteParam writeParams = params.getWriteJpegParam();
        ImageDescriptor descriptor = metaData.getImageDescriptor();
        DicomOutputData outputData = new DicomOutputData(images, descriptor, targetTsuid);
        DicomJpegWriteParam adaptedWriteParams = Transcoder.adaptTransferSyntax(outputData, targetTsuid, writeParams);
        String actualTsuid = outputData.getTsuid();
        return new DicomTranscodeContext(dataSet, outputData, actualTsuid, adaptedWriteParams, descriptor);
    }

    private static DicomJpegWriteParam adaptTransferSyntax(DicomOutputData outputData, String targetTsuid, DicomJpegWriteParam writeParams) {
        String actualTsuid = outputData.getTsuid();
        if (!targetTsuid.equals(actualTsuid)) {
            if (!DicomOutputData.isNativeSyntax(actualTsuid)) {
                writeParams = DicomJpegWriteParam.buildDicomImageWriteParam(actualTsuid);
            }
            LOGGER.warn("Cannot transcode to {}, using {} instead", (Object)targetTsuid, (Object)actualTsuid);
        }
        return writeParams;
    }

    private static void writeTranscodedDicom(DicomOutputStream dos, DicomTranscodeContext context) throws IOException {
        dos.writeFileMetaInformation(context.dataSet.createFileMetaInformation(context.actualTsuid));
        if (DicomOutputData.isNativeSyntax(context.actualTsuid)) {
            context.outputData.writeRawImageData(dos, context.dataSet);
        } else {
            Transcoder.writeCompressedDicomData(dos, context);
        }
    }

    private static void writeCompressedDicomData(DicomOutputStream dos, DicomTranscodeContext context) throws IOException {
        int[] jpegParams = context.outputData.adaptTagsToCompressedImage(context.dataSet, context.outputData.getFirstImage().get(), context.descriptor, context.writeParams);
        context.outputData.writeCompressedImageData(dos, context.dataSet, jpegParams);
    }

    private static Editable<PlanarImage> createMask(Attributes dataSet, DicomTranscodeParam params) {
        String stationName = dataSet.getString(528400, "*");
        return Transcoder.getMaskedImage(params.getMask(stationName));
    }

    private static DicomImageReadParam getEffectiveReadParams(DicomTranscodeParam params) {
        DicomImageReadParam readParams = params.getReadParam();
        if (readParams == null) {
            return dicomImageReadParam;
        }
        readParams.setReleaseImageAfterProcessing(true);
        return readParams;
    }

    private static int getCompressionQuality(ImageTranscodeParam params) {
        return params != null ? params.getJpegCompressionQuality().orElse(80) : 80;
    }

    private static boolean shouldPreserveRawImage(ImageTranscodeParam params, Format format, int cvType) {
        if (params == null) {
            return false;
        }
        if (!params.isPreserveRawImage().orElse(false).booleanValue()) {
            return false;
        }
        if (format == Format.HDR || cvType == 0) {
            return true;
        }
        return format.isSupported(cvType);
    }

    private static Path adaptFileExtension(Path path, String outputExt) {
        String filename = path.getFileName().toString();
        String currentExt = FileUtil.getExtension((String)filename);
        if (currentExt.equals(outputExt)) {
            return path;
        }
        if (currentExt.endsWith(".dcm")) {
            String baseName = filename.substring(0, filename.length() - ".dcm".length());
            return path.resolveSibling(baseName + outputExt);
        }
        return path.resolveSibling(filename + outputExt);
    }

    static {
        dicomImageReadParam.setReleaseImageAfterProcessing(true);
    }

    public static enum Format {
        JPEG(".jpg", false, false, false, false),
        PNG(".png", true, false, false, false),
        TIF(".tif", true, false, true, true),
        JP2(".jp2", true, false, false, false),
        PNM(".pnm", true, false, false, false),
        BMP(".bmp", false, false, false, false),
        HDR(".hdr", false, false, false, true);

        private final String extension;
        private final boolean support16U;
        private final boolean support16S;
        private final boolean support32F;
        private final boolean support64F;

        private Format(String ext, boolean support16U, boolean support16S, boolean support32F, boolean support64F) {
            this.extension = ext;
            this.support16U = support16U;
            this.support16S = support16S;
            this.support32F = support32F;
            this.support64F = support64F;
        }

        public String getExtension() {
            return this.extension;
        }

        public boolean isSupport16U() {
            return this.support16U;
        }

        public boolean isSupport16S() {
            return this.support16S;
        }

        public boolean isSupport32F() {
            return this.support32F;
        }

        public boolean isSupport64F() {
            return this.support64F;
        }

        public boolean isSupported(int cvType) {
            return switch (cvType) {
                case 0 -> true;
                case 2 -> this.support16U;
                case 3 -> this.support16S;
                case 5 -> this.support32F;
                case 6 -> this.support64F;
                default -> false;
            };
        }
    }

    private record DicomTranscodeContext(Attributes dataSet, DicomOutputData outputData, String actualTsuid, DicomJpegWriteParam writeParams, ImageDescriptor descriptor) {
    }
}

