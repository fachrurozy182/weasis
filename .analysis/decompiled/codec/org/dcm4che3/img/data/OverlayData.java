/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.CvType
 *  org.opencv.core.Mat
 *  org.weasis.core.util.LangUtil
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageTransformer
 */
package org.dcm4che3.img.data;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.img.DicomImageReadParam;
import org.dcm4che3.img.data.EmbeddedOverlay;
import org.dcm4che3.img.data.PrDicomObject;
import org.dcm4che3.img.stream.ImageDescriptor;
import org.dcm4che3.img.util.DicomAttributeUtils;
import org.dcm4che3.img.util.DicomUtils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.weasis.core.util.LangUtil;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageTransformer;

public record OverlayData(int groupOffset, int rows, int columns, int imageFrameOrigin, int framesInOverlay, int[] origin, byte[] data) {
    static final int MAX_OVERLAY_GROUPS = 16;
    static final int GROUP_OFFSET_SHIFT = 17;
    private static final byte OVERLAY_PIXEL_VALUE = -1;

    public static List<OverlayData> getOverlayData(Attributes dcm, int activationMask) {
        return OverlayData.getOverlayData(dcm, activationMask, false);
    }

    public static List<OverlayData> getPrOverlayData(Attributes dcm, int activationMask) {
        return OverlayData.getOverlayData(dcm, activationMask, true);
    }

    private static List<OverlayData> getOverlayData(Attributes dcm, int activationMask, boolean pr) {
        if (dcm == null) {
            return Collections.emptyList();
        }
        return IntStream.range(0, 16).filter(groupIndex -> OverlayData.isGroupActivated(groupIndex, activationMask)).mapToObj(groupIndex -> groupIndex << 17).filter(groupOffset -> OverlayData.isLayerActivated(dcm, groupOffset, pr)).map(groupOffset -> OverlayData.createOverlayData(dcm, groupOffset)).filter(Optional::isPresent).map(Optional::get).toList();
    }

    private static boolean isGroupActivated(int groupIndex, int activationMask) {
        return (activationMask & 1 << groupIndex) != 0;
    }

    private static boolean isLayerActivated(Attributes dcm, int groupOffset, boolean pr) {
        return !pr || dcm.getString(0x60001001 | groupOffset) != null;
    }

    private static Optional<OverlayData> createOverlayData(Attributes dcm, int groupOffset) {
        Optional<byte[]> overlayDataBytes = DicomAttributeUtils.getByteData(dcm, 0x60003000 | groupOffset);
        if (overlayDataBytes.isEmpty()) {
            return Optional.empty();
        }
        OverlayAttributes attributes = OverlayData.extractOverlayAttributes(dcm, groupOffset);
        return Optional.of(new OverlayData(groupOffset, attributes.rows(), attributes.columns(), attributes.imageFrameOrigin(), attributes.framesInOverlay(), attributes.origin(), overlayDataBytes.get()));
    }

    private static OverlayAttributes extractOverlayAttributes(Attributes dcm, int groupOffset) {
        int rows = dcm.getInt(0x60000010 | groupOffset, 0);
        int columns = dcm.getInt(0x60000011 | groupOffset, 0);
        int imageFrameOrigin = dcm.getInt(0x60000051 | groupOffset, 1);
        int framesInOverlay = dcm.getInt(0x60000015 | groupOffset, 1);
        int[] origin = DicomUtils.getIntArrayFromDicomElement(dcm, 0x60000050 | groupOffset, new int[]{1, 1});
        return new OverlayAttributes(rows, columns, imageFrameOrigin, framesInOverlay, origin);
    }

    public static PlanarImage getOverlayImage(PlanarImage imageSource, PlanarImage currentImage, ImageDescriptor desc, DicomImageReadParam params, int frameIndex) {
        if (imageSource == null || currentImage == null || desc == null || params == null) {
            return currentImage;
        }
        OverlayContext context = OverlayData.buildOverlayContext(desc, params);
        if (!context.hasOverlays() || !OverlayData.hasSameDimensions(imageSource, currentImage)) {
            return currentImage;
        }
        return OverlayData.applyOverlaysToImage(imageSource, currentImage, context, frameIndex);
    }

    private static OverlayContext buildOverlayContext(ImageDescriptor desc, DicomImageReadParam params) {
        ArrayList<OverlayData> allOverlays = new ArrayList<OverlayData>(params.getPresentationState().map(PrDicomObject::getOverlays).orElse(List.of()));
        allOverlays.addAll(desc.getOverlayData());
        return new OverlayContext(desc.getEmbeddedOverlay(), allOverlays, params);
    }

    private static boolean hasSameDimensions(PlanarImage image1, PlanarImage image2) {
        return image1.width() == image2.width() && image1.height() == image2.height();
    }

    private static ImageCV applyOverlaysToImage(PlanarImage imageSource, PlanarImage currentImage, OverlayContext context, int frameIndex) {
        ImageDimensions dimensions = new ImageDimensions(currentImage.width(), currentImage.height());
        byte[] overlayPixelData = OverlayData.createOverlayPixelData(imageSource, context, frameIndex, dimensions);
        ImageCV overlayImage = OverlayData.createOverlayImage(overlayPixelData, dimensions);
        Color overlayColor = context.params().getOverlayColor().orElse(Color.WHITE);
        return ImageTransformer.overlay((Mat)currentImage.toMat(), (Mat)overlayImage, (Color)overlayColor);
    }

    private static byte[] createOverlayPixelData(PlanarImage imageSource, OverlayContext context, int frameIndex, ImageDimensions dimensions) {
        byte[] pixelData = new byte[dimensions.totalPixels()];
        context.embeddedOverlays().forEach(overlay -> {
            int mask = 1 << overlay.bitPosition();
            OverlayData.applyEmbeddedOverlayMask(imageSource, mask, pixelData, dimensions);
        });
        OverlayData.applyRegularOverlays(context.overlays(), pixelData, frameIndex, dimensions.width());
        return pixelData;
    }

    private static void applyEmbeddedOverlayMask(PlanarImage imageSource, int mask, byte[] pixelData, ImageDimensions dimensions) {
        for (int row = 0; row < dimensions.height(); ++row) {
            for (int col = 0; col < dimensions.width(); ++col) {
                double[] pixel = imageSource.get(row, col);
                if (((int)pixel[0] & mask) == 0) continue;
                pixelData[row * dimensions.width() + col] = -1;
            }
        }
    }

    private static void applyRegularOverlays(List<OverlayData> overlays, byte[] pixelData, int frameIndex, int imageWidth) {
        LangUtil.emptyIfNull(overlays).stream().filter(overlay -> OverlayData.isOverlayApplicableToFrame(overlay, frameIndex)).forEach(overlay -> OverlayData.applyOverlayToPixelData(overlay, pixelData, frameIndex, imageWidth));
    }

    private static boolean isOverlayApplicableToFrame(OverlayData overlay, int frameIndex) {
        int overlayFrameIndex = frameIndex - overlay.imageFrameOrigin() + 1;
        return overlayFrameIndex >= 0 && overlayFrameIndex < overlay.framesInOverlay();
    }

    private static void applyOverlayToPixelData(OverlayData overlay, byte[] pixelData, int frameIndex, int imageWidth) {
        OverlayFrameInfo frameInfo = OverlayData.calculateOverlayFrameInfo(overlay, frameIndex);
        OverlayData.applyOverlayBits(overlay, frameInfo, pixelData, imageWidth);
    }

    private static OverlayFrameInfo calculateOverlayFrameInfo(OverlayData overlay, int frameIndex) {
        int overlayFrameIndex = frameIndex - overlay.imageFrameOrigin() + 1;
        int overlayOffset = overlay.rows() * overlay.columns() * overlayFrameIndex;
        int originX = overlay.origin()[1] - 1;
        int originY = overlay.origin()[0] - 1;
        return new OverlayFrameInfo(overlayOffset, originX, originY, overlay.rows(), overlay.columns());
    }

    private static void applyOverlayBits(OverlayData overlay, OverlayFrameInfo frameInfo, byte[] pixelData, int imageWidth) {
        byte[] overlayData = overlay.data();
        int endRow = Math.min(frameInfo.originY() + frameInfo.height(), pixelData.length / imageWidth);
        int endCol = Math.min(frameInfo.originX() + frameInfo.width(), imageWidth);
        for (int row = Math.max(0, frameInfo.originY()); row < endRow; ++row) {
            for (int col = Math.max(0, frameInfo.originX()); col < endCol; ++col) {
                if (!OverlayData.isOverlayPixelSet(overlayData, frameInfo, row, col)) continue;
                pixelData[row * imageWidth + col] = -1;
            }
        }
    }

    private static boolean isOverlayPixelSet(byte[] overlayData, OverlayFrameInfo frameInfo, int row, int col) {
        int pixelIndex = frameInfo.overlayOffset() + (row - frameInfo.originY()) * frameInfo.width() + (col - frameInfo.originX());
        int byteIndex = pixelIndex / 8;
        int bitIndex = pixelIndex % 8;
        return byteIndex < overlayData.length && (overlayData[byteIndex] & 0xFF & 1 << bitIndex) != 0;
    }

    private static ImageCV createOverlayImage(byte[] pixelData, ImageDimensions dimensions) {
        ImageCV overlay = new ImageCV(dimensions.height(), dimensions.width(), CvType.CV_8UC1);
        overlay.put(0, 0, pixelData);
        return overlay;
    }

    public static PlanarImage getOverlayImage(PlanarImage imageSource, List<OverlayData> overlays, int frameIndex) {
        if (imageSource == null || overlays == null || overlays.isEmpty()) {
            return imageSource;
        }
        ImageDimensions dimensions = new ImageDimensions(imageSource.width(), imageSource.height());
        byte[] pixelData = new byte[dimensions.totalPixels()];
        OverlayData.applyRegularOverlays(overlays, pixelData, frameIndex, dimensions.width());
        return OverlayData.createOverlayImage(pixelData, dimensions);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OverlayData)) return false;
        OverlayData that = (OverlayData)((Object)o);
        if (this.groupOffset != that.groupOffset) return false;
        if (this.rows != that.rows) return false;
        if (this.columns != that.columns) return false;
        if (this.imageFrameOrigin != that.imageFrameOrigin) return false;
        if (this.framesInOverlay != that.framesInOverlay) return false;
        if (!Arrays.equals(this.origin, that.origin)) return false;
        if (!Arrays.equals(this.data, that.data)) return false;
        return true;
    }

    public int hashCode() {
        int result = Objects.hash(this.groupOffset, this.rows, this.columns, this.imageFrameOrigin, this.framesInOverlay);
        result = 31 * result + Arrays.hashCode(this.origin);
        result = 31 * result + Arrays.hashCode(this.data);
        return result;
    }

    public String toString() {
        return "OverlayData{groupOffset=%d, rows=%d, columns=%d}".formatted(new Object[]{this.groupOffset, this.rows, this.columns});
    }

    private record OverlayAttributes(int rows, int columns, int imageFrameOrigin, int framesInOverlay, int[] origin) {
    }

    private record OverlayContext(List<EmbeddedOverlay> embeddedOverlays, List<OverlayData> overlays, DicomImageReadParam params) {
        boolean hasOverlays() {
            return !this.embeddedOverlays.isEmpty() || !this.overlays.isEmpty();
        }
    }

    private record ImageDimensions(int width, int height) {
        int totalPixels() {
            return this.width * this.height;
        }
    }

    private record OverlayFrameInfo(int overlayOffset, int originX, int originY, int height, int width) {
    }
}

