/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Core
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.opencv.core.MatOfInt
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.FileUtil
 *  org.weasis.core.util.MathUtil
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.op.ImageIOHandler
 */
package org.weasis.dicom.tool;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.XPEGParser;
import org.dcm4che3.imageio.codec.jpeg.JPEGHeader;
import org.dcm4che3.imageio.codec.jpeg.JPEGParser;
import org.dcm4che3.imageio.codec.mp4.MP4Parser;
import org.dcm4che3.imageio.codec.mpeg.MPEG2Parser;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.UIDUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.FileUtil;
import org.weasis.core.util.MathUtil;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.op.ImageIOHandler;

public final class Dicomizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Dicomizer.class);
    private static final long FRAGMENT_LENGTH = 0xFFFFFFFEL;
    private static final int INIT_BUFFER_SIZE = 8192;
    private static final int JPEG_HEADER_BUFFER_SIZE = 65536;
    private static final int DEFAULT_JPEG_QUALITY = 95;
    private static final String UNKNOWN = "UNKNOWN";
    public static final long MAX_FILE_SIZE = 0x7FFFFFFEL;

    private Dicomizer() {
    }

    private static void encapsulateDocument(Attributes attrs, Path inputFile, Path dcmFile, String sopClassUID, String mimeType) throws IOException {
        if (!Dicomizer.isValidInputFile(inputFile) || !Dicomizer.isValidOutputFile(dcmFile)) {
            return;
        }
        Dicomizer.validateFileSize(inputFile);
        long fileLength = Files.size(inputFile);
        attrs.setLong(4325397, VR.UL, fileLength);
        Dicomizer.setupDocumentAttributes(attrs, sopClassUID, inputFile.getFileName().toString());
        Dicomizer.setupBulkData(attrs, inputFile, fileLength, mimeType);
        Dicomizer.writeDicomFile(attrs, dcmFile);
    }

    private static void validateFileSize(Path inputFile) throws IOException {
        long fileLength = Files.size(inputFile);
        if (fileLength > 0x7FFFFFFEL) {
            throw new IOException("Encapsulated file too large %s: %s".formatted(new Object[]{inputFile, FileUtil.humanReadableByte((long)fileLength, (boolean)false)}));
        }
    }

    private static void setupDocumentAttributes(Attributes attrs, String sopClassUID, String fileName) {
        Dicomizer.ensureString(attrs, 524310, VR.UI, sopClassUID);
        Dicomizer.commonRequiredAttributes(attrs);
        Dicomizer.ensureString(attrs, 2097171, VR.IS, "1");
        Dicomizer.ensureString(attrs, 0x200011, VR.IS, "999");
        Dicomizer.ensureString(attrs, 2622209, VR.CS, "YES");
        if (!attrs.containsValue(4325392) && attrs.containsValue(0x204000)) {
            attrs.setString(4325392, VR.LO, attrs.getString(0x204000));
        }
        Dicomizer.ensureString(attrs, 4325392, VR.CS, fileName);
    }

    private static void setupBulkData(Attributes attrs, Path inputFile, long fileLength, String mimeType) {
        BulkData bulk = new BulkData(inputFile.toUri().toString(), 0L, fileLength, false);
        attrs.setValue(4325393, VR.OB, bulk);
        attrs.setString(4325394, VR.LO, mimeType);
    }

    private static void writeDicomFile(Attributes attrs, Path dcmFile) throws IOException {
        Attributes fmi = attrs.createFileMetaInformation("1.2.840.10008.1.2.1");
        try (DicomOutputStream dos = new DicomOutputStream(dcmFile.toFile());){
            dos.writeDataset(fmi, attrs);
        }
    }

    private static void encapsulateM3d(Attributes attrs) {
        Dicomizer.setupM3dAttributes(attrs);
        Dicomizer.setupMeasurementUnitsSequence(attrs);
    }

    private static void setupM3dAttributes(Attributes attrs) {
        Dicomizer.ensureString(attrs, 524384, VR.CS, "M3D");
        Dicomizer.ensureString(attrs, 524400, VR.LO, UNKNOWN);
        Dicomizer.ensureString(attrs, 528528, VR.LO, UNKNOWN);
        Dicomizer.ensureString(attrs, 0x181000, VR.LO, UNKNOWN);
        Dicomizer.ensureString(attrs, 1576992, VR.LO, UNKNOWN);
    }

    private static void setupMeasurementUnitsSequence(Attributes attrs) {
        Attributes unitAttributes = new Attributes();
        unitAttributes.setString(524544, VR.SH, "mm");
        unitAttributes.setString(524546, VR.SH, "UCUM");
        unitAttributes.setString(524548, VR.LO, "mm");
        Sequence sequence = attrs.ensureSequence(4196586, 1);
        sequence.add(unitAttributes);
    }

    public static void pdf(Attributes attrs, Path pdfFile, Path dcmFile) throws IOException {
        Dicomizer.ensureString(attrs, 524384, VR.CS, "DOC");
        Dicomizer.ensureString(attrs, 524388, VR.CS, "SD");
        Dicomizer.encapsulateDocument(attrs, pdfFile, dcmFile, "1.2.840.10008.5.1.4.1.1.104.1", "application/pdf");
    }

    public static void stl(Attributes attrs, Path stlFile, Path dcmFile) throws IOException {
        Dicomizer.encapsulateM3d(attrs);
        Dicomizer.ensureString(attrs, 0x200052, VR.UI, UIDUtils.createUID());
        Dicomizer.encapsulateDocument(attrs, stlFile, dcmFile, "1.2.840.10008.5.1.4.1.1.104.3", "model/stl");
    }

    public static void mtl(Attributes attrs, Path mtlFile, Path dcmFile) throws IOException {
        Dicomizer.encapsulateM3d(attrs);
        Dicomizer.encapsulateDocument(attrs, mtlFile, dcmFile, "1.2.840.10008.5.1.4.1.1.104.5", "model/mtl");
    }

    public static void obj(Attributes attrs, Path objFile, Path dcmFile) throws IOException {
        Dicomizer.encapsulateM3d(attrs);
        Dicomizer.ensureString(attrs, 0x200052, VR.UI, UIDUtils.createUID());
        Dicomizer.encapsulateDocument(attrs, objFile, dcmFile, "1.2.840.10008.5.1.4.1.1.104.4", "model/obj");
    }

    public static void cda(Attributes attrs, Path cdaFile, Path dcmFile) throws IOException {
        Dicomizer.ensureString(attrs, 524384, VR.CS, "DOC");
        Dicomizer.ensureString(attrs, 524388, VR.CS, "WSD");
        Dicomizer.encapsulateDocument(attrs, cdaFile, dcmFile, "1.2.840.10008.5.1.4.1.1.104.2", "text/XML");
    }

    public static void jpeg(Attributes attrs, Path jpgFile, Path dcmFile, boolean noAPPn) throws IOException {
        if (!Dicomizer.isValidInputFile(jpgFile) || !Dicomizer.isValidOutputFile(dcmFile)) {
            return;
        }
        try (SeekableByteChannel channel = Files.newByteChannel(jpgFile, new OpenOption[0]);){
            JPEGParser parser = new JPEGParser(channel);
            Dicomizer.buildDicomUsingParser(parser, attrs, jpgFile, dcmFile, "1.2.840.10008.5.1.4.1.1.77.1.4", noAPPn);
        }
    }

    public static void mpeg2(Attributes attrs, Path mpegFile, Path dcmFile) throws IOException {
        if (!Dicomizer.isValidInputFile(mpegFile) || !Dicomizer.isValidOutputFile(dcmFile)) {
            return;
        }
        try (SeekableByteChannel channel = Files.newByteChannel(mpegFile, new OpenOption[0]);){
            MPEG2Parser parser = new MPEG2Parser(channel);
            Dicomizer.buildDicomUsingParser(parser, attrs, mpegFile, dcmFile, "1.2.840.10008.5.1.4.1.1.77.1.4.1", false);
        }
    }

    public static void mpeg4(Attributes attrs, Path mpegFile, Path dcmFile) throws IOException {
        if (!Dicomizer.isValidInputFile(mpegFile) || !Dicomizer.isValidOutputFile(dcmFile)) {
            return;
        }
        try (SeekableByteChannel channel = Files.newByteChannel(mpegFile, new OpenOption[0]);){
            MP4Parser parser = new MP4Parser(channel);
            Dicomizer.buildDicomUsingParser(parser, attrs, mpegFile, dcmFile, "1.2.840.10008.5.1.4.1.1.77.1.4.1", false);
        }
    }

    private static void buildDicomUsingParser(XPEGParser parser, Attributes attrs, Path inputFile, Path dcmFile, String sopClassUID, boolean noAPPn) throws IOException {
        try {
            Dicomizer.setupParserAttributes(attrs, sopClassUID, parser);
            Dicomizer.writePixelData(parser, attrs, inputFile, dcmFile, noAPPn);
        }
        catch (IOException e) {
            Files.deleteIfExists(dcmFile);
            throw e;
        }
        catch (Exception e) {
            Files.deleteIfExists(dcmFile);
            throw new IOException(e);
        }
    }

    private static void setupParserAttributes(Attributes attrs, String sopClassUID, XPEGParser parser) {
        Dicomizer.ensureString(attrs, 524310, VR.UI, sopClassUID);
        Dicomizer.ensureString(attrs, 524296, VR.CS, "ORIGINAL\\PRIMARY");
        Dicomizer.commonRequiredAttributes(attrs);
        parser.getAttributes(attrs);
    }

    private static void writePixelData(XPEGParser parser, Attributes attrs, Path inputFile, Path dcmFile, boolean noAPPn) throws IOException {
        try (SeekableByteChannel channel = Files.newByteChannel(inputFile, new OpenOption[0]);
             DicomOutputStream dos = new DicomOutputStream(dcmFile.toFile());){
            PixelDataContext context = Dicomizer.preparePixelDataContext(parser, channel, noAPPn);
            Attributes fmi = attrs.createFileMetaInformation(parser.getTransferSyntaxUID(context.codeStreamSize > 0xFFFFFFFEL));
            dos.writeDataset(fmi, attrs);
            Dicomizer.writePixelDataFragments(dos, channel, context);
        }
    }

    private static PixelDataContext preparePixelDataContext(XPEGParser parser, SeekableByteChannel channel, boolean noAPPn) throws IOException {
        byte[] prefix = ByteUtils.EMPTY_BYTES;
        if (noAPPn && parser.getPositionAfterAPPSegments() > 0L) {
            channel.position(parser.getPositionAfterAPPSegments());
            prefix = new byte[]{-1, -40};
        } else {
            channel.position(parser.getCodeStreamPosition());
        }
        long codeStreamSize = channel.size() - channel.position() + (long)prefix.length;
        return new PixelDataContext(prefix, codeStreamSize);
    }

    private static void writePixelDataFragments(DicomOutputStream dos, SeekableByteChannel channel, PixelDataContext context) throws IOException {
        long fragmentSize;
        dos.writeHeader(2145386512, VR.OB, -1);
        dos.writeHeader(-73728, null, 0);
        byte[] prefix = context.prefix;
        long remainingSize = context.codeStreamSize;
        do {
            fragmentSize = Math.min(remainingSize, 0xFFFFFFFEL);
            dos.writeHeader(-73728, null, (int)(fragmentSize + 1L & 0xFFFFFFFFFFFFFFFEL));
            dos.write(prefix);
            Dicomizer.copy(channel, fragmentSize - (long)prefix.length, dos);
            if ((fragmentSize & 1L) != 0L) {
                dos.write(0);
            }
            prefix = ByteUtils.EMPTY_BYTES;
        } while ((remainingSize -= fragmentSize) > 0L);
        dos.writeHeader(-73507, null, 0);
    }

    private static void copy(ByteChannel in, long len, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        while (len > 0L) {
            byteBuffer.clear();
            byteBuffer.limit((int)Math.min(len, (long)buffer.length));
            int bytesRead = in.read(byteBuffer);
            out.write(buffer, 0, bytesRead);
            len -= (long)bytesRead;
        }
    }

    private static void commonRequiredAttributes(Attributes attrs) {
        Dicomizer.ensureString(attrs, 524293, VR.CS, "ISO_IR 192");
        Dicomizer.ensureUID(attrs, 0x20000D);
        Dicomizer.ensureUID(attrs, 0x20000E);
        Dicomizer.ensureUID(attrs, 524312);
        Dicomizer.setCreationDate(attrs);
    }

    private static void setCreationDate(Attributes attrs) {
        Date now = new Date();
        attrs.setDate(524306, VR.DA, now);
        attrs.setDate(524307, VR.TM, now);
    }

    private static void ensureString(Attributes attrs, int tag, VR vr, String value) {
        if (!attrs.containsValue(tag)) {
            attrs.setString(tag, vr, value);
        }
    }

    private static void ensureUID(Attributes attrs, int tag) {
        if (!attrs.containsValue(tag)) {
            attrs.setString(tag, VR.UI, UIDUtils.createUID());
        }
    }

    public static boolean convertToJpegAndWrite(Path inputFile, Path outputFile, Integer quality) {
        try {
            if (!Dicomizer.isValidInputFile(inputFile)) {
                return false;
            }
            if (!Dicomizer.isValidOutputFile(outputFile)) {
                return false;
            }
            String inputExtension = FileUtil.getExtension((String)inputFile.toString()).toLowerCase(Locale.ROOT);
            if (Dicomizer.handleJpegFile(inputFile, outputFile, inputExtension)) {
                return true;
            }
            return Dicomizer.convertAndWriteImage(inputFile, outputFile, quality);
        }
        catch (Exception e) {
            LOGGER.error("Error converting image to JPEG: {}", (Object)e.getMessage(), (Object)e);
            return false;
        }
    }

    private static boolean isValidInputFile(Path inputFile) {
        if (inputFile == null || !Files.isReadable(inputFile)) {
            LOGGER.error("Input file does not exist: {}", (Object)inputFile);
            return false;
        }
        return true;
    }

    private static boolean isValidOutputFile(Path outputFile) {
        if (outputFile == null || Files.exists(outputFile, new LinkOption[0]) && !Files.isWritable(outputFile)) {
            LOGGER.error("Output file is not writable: {}", (Object)outputFile);
            return false;
        }
        return true;
    }

    private static boolean handleJpegFile(Path inputFile, Path outputFile, String inputExtension) throws IOException {
        if (!Dicomizer.isJpegExtension(inputExtension)) {
            return false;
        }
        if (Dicomizer.isValidJpegFile(inputFile)) {
            LOGGER.info("Input file is already a JPEG image: {}", (Object)inputFile);
            if (outputFile != null && !inputFile.equals(outputFile)) {
                Files.copy(inputFile, outputFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean convertAndWriteImage(Path inputFile, Path outputFile, Integer quality) {
        ImageCV imageCV = ImageIOHandler.readImage((Path)inputFile, null);
        if (imageCV == null || imageCV.empty()) {
            LOGGER.warn("Failed to read image from file: {}", (Object)inputFile);
            return false;
        }
        ImageCV jpegImage = Dicomizer.toEightBit(imageCV);
        try {
            Path finalOutputFile = Dicomizer.determineOutputFile(inputFile, outputFile);
            MatOfInt encodingParams = Dicomizer.createJpegEncodingParams(quality);
            boolean success = ImageIOHandler.writeImage((Mat)jpegImage, (Path)finalOutputFile, (MatOfInt)encodingParams);
            Dicomizer.logConversionResult(success, finalOutputFile);
            boolean bl = success;
            return bl;
        }
        finally {
            if (jpegImage != imageCV) {
                jpegImage.release();
            }
            imageCV.release();
        }
    }

    private static ImageCV toEightBit(ImageCV image) {
        int depth = CvType.depth((int)image.type());
        if (depth == 0) {
            return image;
        }
        Mat dst = new Mat();
        if (depth == 2 || depth == 3) {
            image.convertTo(dst, 0, 0.0038910505836575876);
        } else {
            Core.normalize((Mat)image, (Mat)dst, (double)0.0, (double)255.0, (int)32, (int)0);
        }
        return ImageCV.fromMat((Mat)dst);
    }

    private static Path determineOutputFile(Path inputFile, Path outputFile) {
        if (outputFile != null) {
            return Dicomizer.ensureJpegExtension(outputFile);
        }
        String defaultOutputPath = inputFile.toString().replaceAll("\\.[^.]+$", ".jpg");
        return Path.of(defaultOutputPath, new String[0]);
    }

    private static Path ensureJpegExtension(Path outputFile) {
        String outputExtension = FileUtil.getExtension((String)outputFile.toString()).toLowerCase(Locale.ROOT);
        if (!Dicomizer.isJpegExtension(outputExtension)) {
            return Path.of(String.valueOf(outputFile) + ".jpg", new String[0]);
        }
        return outputFile;
    }

    private static MatOfInt createJpegEncodingParams(Integer quality) {
        int jpegQuality = quality != null ? MathUtil.clamp((int)quality, (int)0, (int)100) : 95;
        return new MatOfInt(new int[]{1, jpegQuality});
    }

    private static void logConversionResult(boolean success, Path outputFile) {
        if (success) {
            LOGGER.info("Successfully converted and wrote JPEG image to: {}", (Object)outputFile);
        } else {
            LOGGER.warn("Failed to write JPEG image to: {}", (Object)outputFile);
        }
    }

    private static boolean isJpegExtension(String extension) {
        return extension.endsWith(".jpg") || extension.endsWith(".jpeg");
    }

    private static boolean isValidJpegFile(Path inputFile) {
        boolean bl;
        block8: {
            SeekableByteChannel channel = Files.newByteChannel(inputFile, new OpenOption[0]);
            try {
                int headerBufferSize = Math.min(65536, (int)Files.size(inputFile));
                ByteBuffer buffer = ByteBuffer.allocate(headerBufferSize);
                channel.read(buffer);
                byte[] headerData = new byte[buffer.position()];
                buffer.flip();
                buffer.get(headerData);
                JPEGHeader header = new JPEGHeader(headerData, 192);
                bl = Objects.equals(header.getTransferSyntaxUID(), "1.2.840.10008.1.2.4.50");
                if (channel == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (channel != null) {
                        try {
                            channel.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception e) {
                    LOGGER.debug("Could not parse JPEG header, proceeding with conversion: {}", (Object)e.getMessage());
                    return false;
                }
            }
            channel.close();
        }
        return bl;
    }

    private record PixelDataContext(byte[] prefix, long codeStreamSize) {
    }
}

