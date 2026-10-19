/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Core$MinMaxLocResult
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.opencv.core.MatOfDouble
 *  org.opencv.core.MatOfInt
 *  org.opencv.imgcodecs.Imgcodecs
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.Pair
 *  org.weasis.core.util.StreamUtil
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.LookupTableCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.natives.NativeLibrary
 *  org.weasis.opencv.op.ImageConversion
 *  org.weasis.opencv.op.ImageTransformer
 */
package org.dcm4che3.img;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.VR;
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.imageio.codec.XPEGParserException;
import org.dcm4che3.imageio.codec.jpeg.JPEGParser;
import org.dcm4che3.img.DicomImageAdapter;
import org.dcm4che3.img.DicomImageReadParam;
import org.dcm4che3.img.DicomMetaData;
import org.dcm4che3.img.lut.ModalityLutModule;
import org.dcm4che3.img.stream.BytesWithImageDescriptor;
import org.dcm4che3.img.stream.DicomFileInputStream;
import org.dcm4che3.img.stream.ExtendSegmentedInputImageStream;
import org.dcm4che3.img.stream.ImageDescriptor;
import org.dcm4che3.img.stream.SeekableInMemoryByteChannel;
import org.dcm4che3.img.util.Editable;
import org.dcm4che3.img.util.PaletteColorUtils;
import org.dcm4che3.img.util.PixelDataUtils;
import org.dcm4che3.img.util.SupplierEx;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.TagUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.Pair;
import org.weasis.core.util.StreamUtil;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.LookupTableCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.natives.NativeLibrary;
import org.weasis.opencv.op.ImageConversion;
import org.weasis.opencv.op.ImageTransformer;

public class DicomImageReader
extends ImageReader {
    private static final Logger LOG = LoggerFactory.getLogger(DicomImageReader.class);
    public static final Set<Integer> BULK_TAGS = Set.of(2654176, 1342185484, 0x50003000, 1442840608, 2626049, 2626050, 2626051, 2626052, 0x281211, 0x281212, 2626067, 0x281221, 0x281222, 2626083, 2626084, 0x60003000, 4325393, 2145386504, 2145386505, 2145386512);
    public static final BulkDataDescriptor BULK_DATA_DESCRIPTOR = DicomImageReader::shouldBeBulkData;
    private final List<Integer> fragmentsPositions = new ArrayList<Integer>();
    private BytesWithImageDescriptor bdis;
    private DicomFileInputStream dis;

    public DicomImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        this.resetInternalState();
        if (input instanceof DicomFileInputStream) {
            DicomFileInputStream dicomStream = (DicomFileInputStream)input;
            this.configureFileInputStream(dicomStream, seekForwardOnly, ignoreMetadata);
        } else if (input instanceof BytesWithImageDescriptor) {
            BytesWithImageDescriptor bytesDescriptor;
            this.bdis = bytesDescriptor = (BytesWithImageDescriptor)input;
        } else {
            throw new IllegalArgumentException("Unsupported input type: " + input.getClass().getName());
        }
    }

    private void configureFileInputStream(DicomFileInputStream dicomStream, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(dicomStream, seekForwardOnly, ignoreMetadata);
        this.dis = dicomStream;
        this.dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
        this.dis.setBulkDataDescriptor(BULK_DATA_DESCRIPTOR);
        this.dis.setURI(this.dis.getPath().toUri().toString());
    }

    public ImageDescriptor getImageDescriptor() {
        if (this.bdis != null) {
            return this.bdis.getImageDescriptor();
        }
        if (this.dis == null) {
            throw new IllegalStateException("DicomInputStream is not set");
        }
        return this.dis.getImageDescriptor();
    }

    @Override
    public int getNumImages(boolean allowSearch) {
        return this.getImageDescriptor().getFrames();
    }

    @Override
    public int getWidth(int frameIndex) {
        this.checkFrameIndex(frameIndex);
        return this.getImageDescriptor().getColumns();
    }

    @Override
    public int getHeight(int frameIndex) {
        this.checkFrameIndex(frameIndex);
        return this.getImageDescriptor().getRows();
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int frameIndex) {
        throw new UnsupportedOperationException("getImageTypes not implemented");
    }

    @Override
    public ImageReadParam getDefaultReadParam() {
        return new DicomImageReadParam();
    }

    @Override
    public DicomMetaData getStreamMetadata() throws IOException {
        return this.dis == null ? null : this.dis.getMetadata();
    }

    @Override
    public IIOMetadata getImageMetadata(int frameIndex) {
        return null;
    }

    @Override
    public boolean canReadRaster() {
        return true;
    }

    @Override
    public Raster readRaster(int frameIndex, ImageReadParam param) {
        try {
            PlanarImage img = this.getPlanarImage(frameIndex, this.ensureDicomParam(param));
            return ImageConversion.toBufferedImage((PlanarImage)img).getRaster();
        }
        catch (Exception e) {
            LOG.error("Error reading raster for frame {}", (Object)frameIndex, (Object)e);
            return null;
        }
    }

    @Override
    public BufferedImage read(int frameIndex, ImageReadParam param) {
        try {
            PlanarImage img = this.getPlanarImage(frameIndex, this.ensureDicomParam(param));
            return ImageConversion.toBufferedImage((PlanarImage)img);
        }
        catch (Exception e) {
            LOG.error("Error reading image for frame {}", (Object)frameIndex, (Object)e);
            return null;
        }
    }

    public List<SupplierEx<PlanarImage, IOException>> getLazyPlanarImages(DicomImageReadParam param, Editable<PlanarImage> editor) {
        int frameCount = this.getImageDescriptor().getFrames();
        return IntStream.range(0, frameCount).mapToObj(i -> this.createLazyImageSupplier(i, param, editor)).toList();
    }

    private SupplierEx<PlanarImage, IOException> createLazyImageSupplier(int frameIndex, DicomImageReadParam param, Editable<PlanarImage> editor) {
        SupplierEx baseSupplier = () -> {
            PlanarImage img = this.getPlanarImage(frameIndex, param);
            if (editor != null) {
                PlanarImage processedImg = editor.process(img);
                if (!img.equals(processedImg)) {
                    img.release();
                }
                return processedImg;
            }
            return img;
        };
        return baseSupplier.memoized();
    }

    public List<PlanarImage> getPlanarImages() throws IOException {
        return this.getPlanarImages(null);
    }

    public List<PlanarImage> getPlanarImages(DicomImageReadParam param) throws IOException {
        int frameCount = this.getImageDescriptor().getFrames();
        ArrayList<PlanarImage> images = new ArrayList<PlanarImage>(frameCount);
        for (int i = 0; i < frameCount; ++i) {
            images.add(this.getPlanarImage(i, param));
        }
        return images;
    }

    public PlanarImage getPlanarImage() throws IOException {
        return this.getPlanarImage(0, null);
    }

    public PlanarImage getPlanarImage(int frameIndex, DicomImageReadParam param) throws IOException {
        if (frameIndex < 0 || frameIndex >= this.getImageDescriptor().getFrames()) {
            return null;
        }
        PlanarImage rawImage = this.getRawImage(frameIndex, param);
        return this.applyImageTransformations(rawImage, frameIndex, param);
    }

    private PlanarImage applyImageTransformations(PlanarImage rawImage, int frameIndex, DicomImageReadParam param) {
        PlanarImage result = rawImage;
        result = this.applyPaletteColorLUT(result);
        result = this.applyRegionTransformations(result, param);
        if (param != null && param.isAllowFloatImageConversion()) {
            result = this.applyFloatConversion(result, this.getImageDescriptor(), frameIndex);
        }
        if (!rawImage.equals(result)) {
            rawImage.release();
        }
        return result;
    }

    private PlanarImage applyPaletteColorLUT(PlanarImage image) {
        ImageDescriptor desc = this.getImageDescriptor();
        if (desc.hasPaletteColorLookupTable()) {
            LookupTableCV paletteColorLUT = desc.getPaletteColorLookupTable();
            return PaletteColorUtils.getRGBImageFromPaletteColorModel(image, paletteColorLUT);
        }
        return image;
    }

    private PlanarImage applyRegionTransformations(PlanarImage image, DicomImageReadParam param) {
        if (param == null) {
            return image;
        }
        PlanarImage result = image;
        if (param.getSourceRegion() != null && !(result = ImageTransformer.crop((Mat)result.toMat(), (Rectangle)param.getSourceRegion())).equals(image)) {
            image.release();
        }
        if (param.getSourceRenderSize() != null) {
            ImageCV scaledImage = ImageTransformer.scale((Mat)result.toMat(), (Dimension)param.getSourceRenderSize(), (Integer)4);
            if (!scaledImage.equals(result)) {
                result.release();
            }
            result = scaledImage;
        }
        return result;
    }

    private PlanarImage applyFloatConversion(PlanarImage image, ImageDescriptor desc, int frameIndex) {
        ModalityLutModule mLut = desc.getModalityLutForFrame(frameIndex);
        boolean forceFloat = mLut.isReset();
        if (!forceFloat && mLut.getRescaleSlope().isEmpty()) {
            return image;
        }
        double slope = mLut.getRescaleSlope().orElse(1.0);
        double intercept = mLut.getRescaleIntercept().orElse(0.0);
        Core.MinMaxLocResult minMax = DicomImageAdapter.getMinMaxValues(image, desc, frameIndex);
        Pair<Double, Double> rescale = DicomImageReader.getRescaleSlopeAndIntercept(slope, intercept, minMax);
        if (forceFloat || slope < 0.5 || DicomImageReader.rangeOutsideLut(rescale, desc)) {
            if (!forceFloat) {
                desc.resetModalityLutForFrame(frameIndex);
            }
            return DicomImageReader.convertToFloatImage(image, slope, intercept, rescale, desc);
        }
        return image;
    }

    private static PlanarImage convertToFloatImage(PlanarImage input, double slope, double intercept, Pair<Double, Double> rescale, ImageDescriptor desc) {
        ImageCV dstImg = new ImageCV();
        boolean invertLUT = desc.getPhotometricInterpretation() == PhotometricInterpretation.MONOCHROME1;
        double alpha = slope;
        double beta = intercept;
        if (invertLUT) {
            alpha = -slope;
            beta = (Double)rescale.second() + (Double)rescale.first() - intercept;
        }
        input.toImageCV().convertTo((Mat)dstImg, 5, alpha, beta);
        return dstImg;
    }

    private static boolean rangeOutsideLut(Pair<Double, Double> rescale, ImageDescriptor desc) {
        boolean outputSigned = (Double)rescale.first() < 0.0 || desc.isSigned();
        Pair<Double, Double> minMax = PixelDataUtils.getMinMax(desc.getBitsAllocated(), outputSigned);
        return (Double)rescale.first() + 1.0 < (Double)minMax.first() || (Double)rescale.second() - 1.0 > (Double)minMax.second();
    }

    private static Pair<Double, Double> getRescaleSlopeAndIntercept(double slope, double intercept, Core.MinMaxLocResult minMax) {
        double min = minMax.minVal * slope + intercept;
        double max = minMax.maxVal * slope + intercept;
        return new Pair((Object)Math.min(min, max), (Object)Math.max(min, max));
    }

    public PlanarImage getRawImage(int frameIndex, DicomImageReadParam param) throws IOException {
        return this.dis != null ? this.getRawImageFromFile(frameIndex, param) : this.getRawImageFromBytes(frameIndex, param);
    }

    protected PlanarImage getRawImageFromFile(int frameIndex, DicomImageReadParam param) throws IOException {
        if (this.dis == null) {
            throw new IOException("No DicomInputStream available");
        }
        Attributes dcm = this.dis.getMetadata().getDicomObject();
        PixelDataInfo pixelInfo = this.extractPixelDataInfo(dcm);
        this.validatePixelData(pixelInfo);
        ExtendSegmentedInputImageStream segmentedStream = this.buildSegmentedImageInputStream(frameIndex, pixelInfo.fragments, pixelInfo.bulkData);
        return this.readImageFromSegments(segmentedStream, frameIndex, pixelInfo, param);
    }

    protected PlanarImage getRawImageFromBytes(int frameIndex, DicomImageReadParam param) throws IOException {
        if (this.bdis == null) {
            throw new IOException("No BytesWithImageDescriptor available");
        }
        ImageDescriptor desc = this.getImageDescriptor();
        String tsuid = this.bdis.getTransferSyntax();
        int dcmFlags = this.buildBytesDecodingFlags(tsuid, desc, frameIndex, param);
        Mat buffer = DicomImageReader.createMatFromBytes(this.bdis.getBytes(frameIndex));
        TransferSyntaxType type = TransferSyntaxType.forUID(tsuid);
        boolean isRawData = type == TransferSyntaxType.NATIVE || type == TransferSyntaxType.RLE;
        ImageCV imageCV = isRawData ? this.readRawBytesData(buffer, dcmFlags, desc) : this.readCompressedBytesData(buffer, dcmFlags);
        return DicomImageReader.applyReleaseImageAfterProcessing(imageCV, param);
    }

    private PixelDataInfo extractPixelDataInfo(Attributes dcm) {
        VR.Holder pixelDataVR = new VR.Holder();
        Object pixelData = null;
        boolean floatPixelData = false;
        List<Integer> tags = List.of(Integer.valueOf(2145386512), Integer.valueOf(2145386504), Integer.valueOf(2145386505));
        for (int tag : tags) {
            pixelData = dcm.getValue(tag, pixelDataVR);
            if (pixelData == null) continue;
            floatPixelData = tag != 2145386512;
            break;
        }
        Fragments fragments = null;
        BulkData bulkData = null;
        boolean bigEndian = false;
        if (pixelData instanceof BulkData) {
            BulkData b;
            bulkData = b = (BulkData)pixelData;
            bigEndian = bulkData.bigEndian();
        } else if (pixelData instanceof Fragments) {
            Fragments f;
            fragments = f = (Fragments)pixelData;
            bigEndian = fragments.bigEndian();
        }
        return new PixelDataInfo(pixelData, fragments, bulkData, bigEndian, floatPixelData, pixelDataVR);
    }

    private void validatePixelData(PixelDataInfo pixelInfo) {
        if (pixelInfo.pixelData == null || this.getImageDescriptor().getBitsStored() < 1) {
            throw new IllegalStateException("No valid pixel data in DICOM object");
        }
    }

    private PlanarImage readImageFromSegments(ExtendSegmentedInputImageStream segmentedStream, int frameIndex, PixelDataInfo pixelInfo, DicomImageReadParam param) throws IOException {
        if (segmentedStream.getSegmentCount() <= 0) {
            return null;
        }
        frameIndex = Math.min(frameIndex, segmentedStream.getSegmentCount() - 1);
        String tsuid = this.dis.getMetadata().getTransferSyntaxUID();
        int dcmFlags = this.buildDecodingFlags(tsuid, this.getImageDescriptor(), pixelInfo, segmentedStream, frameIndex, param);
        MatOfDouble positions = DicomImageReader.createMatOfDouble(segmentedStream.segmentPositions());
        MatOfDouble lengths = DicomImageReader.createMatOfDouble(segmentedStream.segmentLengths());
        TransferSyntaxType type = TransferSyntaxType.forUID(tsuid);
        boolean isRawData = pixelInfo.fragments == null || type == TransferSyntaxType.NATIVE || type == TransferSyntaxType.RLE;
        ImageCV imageCV = isRawData ? this.readRawImageData(segmentedStream, dcmFlags, this.getImageDescriptor(), pixelInfo, positions, lengths, tsuid) : this.readCompressedImageData(segmentedStream, dcmFlags, positions, lengths);
        return DicomImageReader.applyReleaseImageAfterProcessing(imageCV, param);
    }

    private ImageCV readRawImageData(ExtendSegmentedInputImageStream segmentedStream, int dcmFlags, ImageDescriptor desc, PixelDataInfo pixelInfo, MatOfDouble positions, MatOfDouble lengths, String tsuid) {
        int bitsStored = desc.getBitsStored();
        int bits = bitsStored <= 8 && desc.getBitsAllocated() > 8 ? 9 : bitsStored;
        int streamVR = pixelInfo.pixelDataVR.vr.numEndianBytes();
        MatOfInt dicomParams = new MatOfInt(new int[]{-1, dcmFlags, desc.getColumns(), desc.getRows(), 0, desc.getSamples(), bits, desc.isBanded() ? 0 : 2, streamVR});
        if ("1.2.840.10008.1.2.1.99".equals(tsuid) && pixelInfo.bulkData != null) {
            return ImageCV.fromMat((Mat)Imgcodecs.dicomRawMatRead((Mat)DicomImageReader.geBufferData(pixelInfo.bulkData), (MatOfInt)dicomParams, (String)desc.getPhotometricInterpretation().name()));
        }
        return ImageCV.fromMat((Mat)Imgcodecs.dicomRawFileRead((String)segmentedStream.path().toString(), (MatOfDouble)positions, (MatOfDouble)lengths, (MatOfInt)dicomParams, (String)desc.getPhotometricInterpretation().name()));
    }

    private static Mat geBufferData(BulkData bulkData) {
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
                LOG.error("Reading bulk data", (Throwable)e);
                return new Mat();
            }
        }
        input.close();
        return mat;
    }

    private ImageCV readCompressedImageData(ExtendSegmentedInputImageStream segmentedStream, int dcmFlags, MatOfDouble positions, MatOfDouble lengths) {
        return ImageCV.fromMat((Mat)Imgcodecs.dicomJpgFileRead((String)segmentedStream.path().toString(), (MatOfDouble)positions, (MatOfDouble)lengths, (int)dcmFlags, (int)-1));
    }

    private int buildDecodingFlags(String tsuid, ImageDescriptor desc, PixelDataInfo pixelInfo, ExtendSegmentedInputImageStream segmentedStream, int frameIndex, DicomImageReadParam param) {
        boolean rawData;
        TransferSyntaxType type = TransferSyntaxType.forUID(tsuid);
        PhotometricInterpretation pmi = desc.getPhotometricInterpretation();
        int dcmFlags = type.canEncodeSigned() && desc.isSigned() ? 1 : 0;
        boolean bl = rawData = pixelInfo.fragments == null || type == TransferSyntaxType.NATIVE || type == TransferSyntaxType.RLE;
        if (!rawData && this.shouldConvertYbr2Rgb(pmi, tsuid, segmentedStream, frameIndex, param)) {
            dcmFlags |= 2;
            if (type == TransferSyntaxType.JPEG_LS) {
                dcmFlags |= 0x40;
            }
        }
        if (pixelInfo.bigEndian) {
            dcmFlags |= 4;
        }
        if (pixelInfo.floatPixelData) {
            dcmFlags |= 0x10;
        }
        if ("1.2.840.10008.1.2.5".equals(tsuid)) {
            dcmFlags |= 0x20;
        }
        return dcmFlags;
    }

    private ImageCV readRawBytesData(Mat buffer, int dcmFlags, ImageDescriptor desc) {
        int bitsStored = desc.getBitsStored();
        int bits = bitsStored <= 8 && desc.getBitsAllocated() > 8 ? 9 : bitsStored;
        int streamVR = this.bdis.getPixelDataVR().numEndianBytes();
        MatOfInt dicomParams = new MatOfInt(new int[]{-1, dcmFlags, desc.getColumns(), desc.getRows(), 0, desc.getSamples(), bits, desc.isBanded() ? 0 : 2, streamVR});
        return ImageCV.fromMat((Mat)Imgcodecs.dicomRawMatRead((Mat)buffer, (MatOfInt)dicomParams, (String)desc.getPhotometricInterpretation().name()));
    }

    private ImageCV readCompressedBytesData(Mat buffer, int dcmFlags) {
        return ImageCV.fromMat((Mat)Imgcodecs.dicomJpgMatRead((Mat)buffer, (int)dcmFlags, (int)-1));
    }

    private int buildBytesDecodingFlags(String tsuid, ImageDescriptor desc, int frameIndex, DicomImageReadParam param) {
        boolean rawData;
        TransferSyntaxType type = TransferSyntaxType.forUID(tsuid);
        PhotometricInterpretation pmi = desc.getPhotometricInterpretation();
        int dcmFlags = type.canEncodeSigned() && desc.isSigned() ? 1 : 0;
        boolean bl = rawData = type == TransferSyntaxType.NATIVE || type == TransferSyntaxType.RLE;
        if (!rawData && this.shouldConvertYbr2Rgb(pmi, tsuid, frameIndex, param)) {
            dcmFlags |= 2;
            if (type == TransferSyntaxType.JPEG_LS) {
                dcmFlags |= 0x40;
            }
        }
        if (this.bdis.isBigEndian()) {
            dcmFlags |= 4;
        }
        if (this.bdis.isFloatPixelData()) {
            dcmFlags |= 0x10;
        }
        if ("1.2.840.10008.1.2.5".equals(tsuid)) {
            dcmFlags |= 0x20;
        }
        return dcmFlags;
    }

    private boolean shouldConvertYbr2Rgb(PhotometricInterpretation pmi, String tsuid, ExtendSegmentedInputImageStream segmentedStream, int frameIndex, DicomImageReadParam param) {
        BooleanSupplier isYbrModel = () -> {
            boolean bl;
            block8: {
                SeekableByteChannel channel = Files.newByteChannel(this.dis.getPath(), StandardOpenOption.READ);
                try {
                    channel.position(segmentedStream.segmentPositions()[frameIndex]);
                    bl = DicomImageReader.isYbrModel(channel, pmi, param);
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
                    catch (IOException e) {
                        LOG.error("Cannot read JPEG header", (Throwable)e);
                        return false;
                    }
                }
                channel.close();
            }
            return bl;
        };
        return DicomImageReader.shouldConvertYbr2Rgb(pmi, tsuid, isYbrModel);
    }

    private boolean shouldConvertYbr2Rgb(PhotometricInterpretation pmi, String tsuid, int frameIndex, DicomImageReadParam param) {
        BooleanSupplier isYbrModel = () -> {
            SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(this.bdis.getBytes(frameIndex).array());
            try {
                boolean bl = DicomImageReader.isYbrModel(channel, pmi, param);
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
                    LOG.error("Cannot read JPEG header", (Throwable)e);
                    return false;
                }
            }
        };
        return DicomImageReader.shouldConvertYbr2Rgb(pmi, tsuid, isYbrModel);
    }

    private static boolean isYbrModel(SeekableByteChannel channel, PhotometricInterpretation pmi, DicomImageReadParam param) throws IOException {
        boolean keepRgbForLossyJpeg;
        JPEGParser parser = new JPEGParser(channel);
        String tsuid = null;
        try {
            tsuid = parser.getTransferSyntaxUID();
        }
        catch (XPEGParserException e) {
            LOG.warn("Cannot parse JPEG type", (Throwable)e);
        }
        if (tsuid != null && !TransferSyntaxType.isLossyCompression(tsuid)) {
            return false;
        }
        boolean bl = keepRgbForLossyJpeg = param != null && param.getKeepRgbForLossyJpeg().orElse(Boolean.FALSE) != false;
        if (pmi == PhotometricInterpretation.RGB && !keepRgbForLossyJpeg) {
            return !"RGB".equals(parser.getParams().colorPhotometricInterpretation());
        }
        return false;
    }

    private static boolean shouldConvertYbr2Rgb(PhotometricInterpretation pmi, String tsuid, BooleanSupplier isYbrModel) {
        return switch (pmi) {
            case PhotometricInterpretation.MONOCHROME1, PhotometricInterpretation.MONOCHROME2, PhotometricInterpretation.PALETTE_COLOR, PhotometricInterpretation.YBR_ICT, PhotometricInterpretation.YBR_RCT -> false;
            default -> {
                switch (tsuid) {
                    case "1.2.840.10008.1.2.4.50": 
                    case "1.2.840.10008.1.2.4.51": 
                    case "1.2.840.10008.1.2.4.53": 
                    case "1.2.840.10008.1.2.4.55": {
                        if (pmi != PhotometricInterpretation.RGB || isYbrModel.getAsBoolean()) {
                            yield true;
                        }
                        yield false;
                    }
                }
                yield pmi.name().startsWith("YBR");
            }
        };
    }

    private ExtendSegmentedInputImageStream buildSegmentedImageInputStream(int frameIndex, Fragments fragments, BulkData bulkData) throws IOException {
        ImageDescriptor desc = this.getImageDescriptor();
        if (bulkData != null && fragments == null) {
            return this.buildBulkDataStream(frameIndex, bulkData, desc);
        }
        if (fragments != null) {
            return this.buildFragmentedStream(frameIndex, fragments, desc);
        }
        throw new IOException("Neither fragments nor BulkData available");
    }

    private ExtendSegmentedInputImageStream buildBulkDataStream(int frameIndex, BulkData bulkData, ImageDescriptor desc) {
        int frameLength = desc.getPhotometricInterpretation().frameLength(desc.getColumns(), desc.getRows(), desc.getSamples(), desc.getBitsAllocated());
        long[] offsets = new long[]{bulkData.offset() + (long)frameIndex * (long)frameLength};
        int[] lengths = new int[]{frameLength};
        return new ExtendSegmentedInputImageStream(this.dis.getPath(), offsets, lengths, desc);
    }

    private ExtendSegmentedInputImageStream buildFragmentedStream(int frameIndex, Fragments fragments, ImageDescriptor desc) throws IOException {
        int fragmentCount = fragments.size();
        int frameCount = desc.getFrames();
        return frameCount >= fragmentCount - 1 ? this.buildSingleFragmentStream(frameIndex, fragments, fragmentCount) : this.buildMultiFragmentStream(frameIndex, fragments, frameCount, fragmentCount);
    }

    private ExtendSegmentedInputImageStream buildSingleFragmentStream(int frameIndex, Fragments fragments, int fragmentCount) {
        int index = Math.min(frameIndex + 1, fragmentCount - 1);
        BulkData bulkData = (BulkData)fragments.get(index);
        long[] offsets = new long[]{bulkData.offset()};
        int[] lengths = new int[]{bulkData.length()};
        return new ExtendSegmentedInputImageStream(this.dis.getPath(), offsets, lengths, this.getImageDescriptor());
    }

    private ExtendSegmentedInputImageStream buildMultiFragmentStream(int frameIndex, Fragments fragments, int frameCount, int fragmentCount) throws IOException {
        return frameCount == 1 ? this.buildAllFragmentsStream(frameIndex, fragments, fragmentCount) : this.buildPerFrameFragmentStream(frameIndex, fragments, frameCount, fragmentCount);
    }

    private ExtendSegmentedInputImageStream buildAllFragmentsStream(int frameIndex, Fragments fragments, int fragmentCount) {
        long[] offsets = new long[fragmentCount - 1];
        int[] lengths = new int[offsets.length];
        for (int i = 0; i < lengths.length; ++i) {
            BulkData bulkData = (BulkData)fragments.get(i + frameIndex + 1);
            offsets[i] = bulkData.offset();
            lengths[i] = bulkData.length();
        }
        return new ExtendSegmentedInputImageStream(this.dis.getPath(), offsets, lengths, this.getImageDescriptor());
    }

    private ExtendSegmentedInputImageStream buildPerFrameFragmentStream(int frameIndex, Fragments fragments, int frameCount, int fragmentCount) throws IOException {
        if (this.fragmentsPositions.isEmpty()) {
            this.identifyFrameFragments(fragments, fragmentCount);
        }
        if (this.fragmentsPositions.size() != frameCount) {
            throw new IOException("Cannot match fragments to frames: " + this.fragmentsPositions.size() + " vs " + frameCount);
        }
        int startFragment = this.fragmentsPositions.get(frameIndex);
        int endFragment = frameIndex + 1 >= this.fragmentsPositions.size() ? fragmentCount : this.fragmentsPositions.get(frameIndex + 1);
        long[] offsets = new long[endFragment - startFragment];
        int[] lengths = new int[offsets.length];
        for (int i = 0; i < offsets.length; ++i) {
            BulkData bulkData = (BulkData)fragments.get(startFragment + i);
            offsets[i] = bulkData.offset();
            lengths[i] = bulkData.length();
        }
        return new ExtendSegmentedInputImageStream(this.dis.getPath(), offsets, lengths, this.getImageDescriptor());
    }

    private void identifyFrameFragments(Fragments fragments, int fragmentCount) throws IOException {
        Path path = this.dis.getPath();
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ);){
            for (int i = 1; i < fragmentCount; ++i) {
                BulkData bulkData = (BulkData)fragments.get(i);
                channel.position(bulkData.offset());
                try {
                    new JPEGParser(channel);
                    this.fragmentsPositions.add(i);
                    continue;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }

    protected DicomImageReadParam ensureDicomParam(ImageReadParam param) {
        if (param instanceof DicomImageReadParam) {
            DicomImageReadParam dicomParam = (DicomImageReadParam)param;
            return dicomParam;
        }
        return param == null ? new DicomImageReadParam() : new DicomImageReadParam(param);
    }

    private void resetInternalState() {
        StreamUtil.safeClose((AutoCloseable)this.dis);
        this.dis = null;
        this.bdis = null;
        this.fragmentsPositions.clear();
    }

    private void checkFrameIndex(int frameIndex) {
        int maxFrames = this.getImageDescriptor().getFrames();
        if (frameIndex < 0 || frameIndex >= maxFrames) {
            throw new IndexOutOfBoundsException("Frame index %d exceeds available frames (%d)".formatted(new Object[]{frameIndex, maxFrames}));
        }
    }

    @Override
    public void dispose() {
        this.resetInternalState();
    }

    public static ImageCV applyReleaseImageAfterProcessing(ImageCV imageCV, DicomImageReadParam param) {
        if (DicomImageReader.isReleaseImageAfterProcessing(param)) {
            imageCV.setReleasedAfterProcessing(true);
        }
        return imageCV;
    }

    public static boolean isReleaseImageAfterProcessing(DicomImageReadParam param) {
        return param != null && param.getReleaseImageAfterProcessing().orElse(Boolean.FALSE) != false;
    }

    public static void closeMat(Mat mat) {
        if (mat != null) {
            mat.release();
        }
    }

    public static boolean isSupportedSyntax(String uid) {
        return switch (uid) {
            case "1.2.840.10008.1.2", "1.2.840.10008.1.2.1", "1.2.840.10008.1.2.2", "1.2.840.10008.1.2.5", "1.2.840.10008.1.2.4.50", "1.2.840.10008.1.2.4.51", "1.2.840.10008.1.2.4.53", "1.2.840.10008.1.2.4.55", "1.2.840.10008.1.2.4.57", "1.2.840.10008.1.2.4.70", "1.2.840.10008.1.2.4.80", "1.2.840.10008.1.2.4.81", "1.2.840.10008.1.2.4.90", "1.2.840.10008.1.2.4.91", "1.2.840.10008.1.2.4.92", "1.2.840.10008.1.2.4.93", "1.2.840.10008.1.2.4.112", "1.2.840.10008.1.2.4.110", "1.2.840.10008.1.2.4.111" -> true;
            default -> false;
        };
    }

    private static boolean shouldBeBulkData(List<ItemPointer> itemPointer, String privateCreator, int tag, VR vr, long length) {
        int tagNormalized = TagUtils.normalizeRepeatingGroup(tag);
        if (tagNormalized == 1409290256) {
            return itemPointer.size() == 1 && itemPointer.get((int)0).sequenceTag == 1409286400;
        }
        if (BULK_TAGS.contains(tagNormalized)) {
            return itemPointer.isEmpty();
        }
        if (TagUtils.isPrivateTag(tag)) {
            return length > 1000L;
        }
        return switch (vr) {
            case VR.OB, VR.OD, VR.OF, VR.OL, VR.OW, VR.UN -> {
                if (length > 64L) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    private static Mat createMatFromBytes(ByteBuffer byteBuffer) {
        Mat buffer = new Mat(1, byteBuffer.limit(), CvType.CV_8UC1);
        buffer.put(0, 0, byteBuffer.array());
        return buffer;
    }

    private static MatOfDouble createMatOfDouble(long[] values) {
        return new MatOfDouble(Arrays.stream(values).asDoubleStream().toArray());
    }

    private static MatOfDouble createMatOfDouble(int[] values) {
        return new MatOfDouble(Arrays.stream(values).asDoubleStream().toArray());
    }

    static {
        NativeLibrary.loadLibraryFromLibraryName();
    }

    private record PixelDataInfo(Object pixelData, Fragments fragments, BulkData bulkData, boolean bigEndian, boolean floatPixelData, VR.Holder pixelDataVR) {
    }
}

