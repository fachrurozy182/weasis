/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opencv.core.Mat
 *  org.weasis.core.util.MathUtil
 *  org.weasis.opencv.data.ImageCV
 *  org.weasis.opencv.data.LookupTableCV
 *  org.weasis.opencv.data.PlanarImage
 *  org.weasis.opencv.op.ImageTransformer
 *  org.weasis.opencv.op.lut.WlPresentation
 */
package org.dcm4che3.img;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.img.DicomImageAdapter;
import org.dcm4che3.img.DicomImageReadParam;
import org.dcm4che3.img.data.EmbeddedOverlay;
import org.dcm4che3.img.data.OverlayData;
import org.dcm4che3.img.data.PrDicomObject;
import org.dcm4che3.img.lut.ModalityLutModule;
import org.dcm4che3.img.lut.WindLevelParameters;
import org.dcm4che3.img.stream.ImageDescriptor;
import org.opencv.core.Mat;
import org.weasis.core.util.MathUtil;
import org.weasis.opencv.data.ImageCV;
import org.weasis.opencv.data.LookupTableCV;
import org.weasis.opencv.data.PlanarImage;
import org.weasis.opencv.op.ImageTransformer;
import org.weasis.opencv.op.lut.WlPresentation;

public final class ImageRendering {
    private ImageRendering() {
    }

    public static PlanarImage getRawRenderedImage(PlanarImage imageSource, ImageDescriptor desc, DicomImageReadParam params, int frameIndex) {
        PlanarImage imageWithoutOverlay = ImageRendering.getImageWithoutEmbeddedOverlay(imageSource, desc, frameIndex);
        DicomImageAdapter adapter = new DicomImageAdapter(imageWithoutOverlay, desc, frameIndex);
        return ImageRendering.getModalityLutImage(imageSource, adapter, params);
    }

    public static PlanarImage getModalityLutImage(PlanarImage img, DicomImageAdapter adapter, DicomImageReadParam params) {
        WindLevelParameters windLevelParams = new WindLevelParameters(adapter, params);
        int dataType = Objects.requireNonNull(img).type();
        return ImageRendering.isIntegerDataType(dataType) ? ImageRendering.applyModalityLutToIntegerData(img, adapter, windLevelParams) : img;
    }

    public static PlanarImage getDefaultRenderedImage(PlanarImage imageSource, ImageDescriptor desc, DicomImageReadParam params, int frameIndex) {
        PlanarImage imageWithoutOverlay = ImageRendering.getImageWithoutEmbeddedOverlay(imageSource, desc, frameIndex);
        PlanarImage voiProcessedImage = ImageRendering.getVoiLutImage(imageWithoutOverlay, desc, params, frameIndex);
        return OverlayData.getOverlayImage(imageSource, voiProcessedImage, desc, params, frameIndex);
    }

    public static PlanarImage getVoiLutImage(PlanarImage imageSource, ImageDescriptor desc, DicomImageReadParam params, int frameIndex) {
        DicomImageAdapter adapter = new DicomImageAdapter(imageSource, desc, frameIndex);
        return ImageRendering.getVoiLutImage(imageSource, adapter, params);
    }

    public static PlanarImage getVoiLutImage(PlanarImage imageSource, DicomImageAdapter adapter, DicomImageReadParam params) {
        WindLevelParameters windLevelParams = new WindLevelParameters(adapter, params);
        int dataType = Objects.requireNonNull(imageSource).type();
        return switch (ImageRendering.getDataTypeCategory(dataType)) {
            default -> throw new IncompatibleClassChangeError();
            case DataTypeCategory.INTEGER -> ImageRendering.processIntegerDataForVoi(imageSource, adapter, windLevelParams);
            case DataTypeCategory.FLOATING_POINT -> ImageRendering.processFloatingPointDataForVoi(imageSource, windLevelParams, dataType);
            case DataTypeCategory.UNSUPPORTED -> null;
        };
    }

    public static PlanarImage getImageWithoutEmbeddedOverlay(PlanarImage img, ImageDescriptor desc, int frameIndex) {
        int bitsAllocated;
        Objects.requireNonNull(img);
        List<EmbeddedOverlay> embeddedOverlays = Objects.requireNonNull(desc).getEmbeddedOverlay();
        if (embeddedOverlays.isEmpty()) {
            return img;
        }
        int bitsStored = desc.getBitsStored();
        if (ImageRendering.shouldApplyOverlayMask(bitsStored, bitsAllocated = desc.getBitsAllocated())) {
            int overlayMask = ImageRendering.calculateOverlayMask(desc, frameIndex, bitsStored);
            return ImageTransformer.bitwiseAnd((Mat)img.toMat(), (int)overlayMask);
        }
        return img;
    }

    private static DataTypeCategory getDataTypeCategory(int dataType) {
        if (dataType >= 0 && dataType < 4) {
            return DataTypeCategory.INTEGER;
        }
        if (dataType >= 4) {
            return DataTypeCategory.FLOATING_POINT;
        }
        return DataTypeCategory.UNSUPPORTED;
    }

    private static boolean isIntegerDataType(int dataType) {
        return ImageRendering.getDataTypeCategory(dataType) == DataTypeCategory.INTEGER;
    }

    private static ImageCV applyModalityLutToIntegerData(PlanarImage img, DicomImageAdapter adapter, WindLevelParameters params) {
        LookupTableCV modalityLookup = adapter.getModalityLookup((WlPresentation)params, params.isInverseLut());
        return modalityLookup == null ? img.toImageCV() : modalityLookup.lookup(img.toMat());
    }

    private static ImageCV processIntegerDataForVoi(PlanarImage imageSource, DicomImageAdapter adapter, WindLevelParameters params) {
        LookupTableCV modalityLookup = adapter.getModalityLookup((WlPresentation)params, params.isInverseLut());
        ImageCV imageModalityTransformed = ImageRendering.applyModalityTransformation(imageSource, modalityLookup);
        if (ImageRendering.shouldSkipVoiLutForColorImage(params, adapter.getImageDescriptor())) {
            return imageModalityTransformed;
        }
        return ImageRendering.applyVoiAndPresentationLuts(imageModalityTransformed, adapter, params);
    }

    private static ImageCV applyModalityTransformation(PlanarImage imageSource, LookupTableCV modalityLookup) {
        return modalityLookup == null ? imageSource.toImageCV() : modalityLookup.lookup(imageSource.toMat());
    }

    private static boolean shouldSkipVoiLutForColorImage(WindLevelParameters params, ImageDescriptor desc) {
        PhotometricInterpretation photometricInterpretation = desc.getPhotometricInterpretation();
        if (photometricInterpretation.isMonochrome()) {
            return false;
        }
        boolean isDefaultWindow = MathUtil.isEqual((double)params.getWindow(), (double)255.0) && MathUtil.isEqual((double)params.getLevel(), (double)127.5);
        return !params.isAllowWinLevelOnColorImage() || isDefaultWindow;
    }

    private static ImageCV applyVoiAndPresentationLuts(ImageCV imageModalityTransformed, DicomImageAdapter adapter, WindLevelParameters params) {
        PrDicomObject presentationState = params.getPresentationState();
        Optional<Object> presentationLut = presentationState == null ? Optional.empty() : presentationState.getPrLut();
        LookupTableCV voiLookup = null;
        if (presentationLut.isEmpty() || params.getLutShape().getLookup() != null) {
            voiLookup = adapter.getVOILookup(params);
        }
        if (presentationLut.isEmpty()) {
            return voiLookup.lookup((Mat)imageModalityTransformed);
        }
        ImageCV imageVoiTransformed = voiLookup == null ? imageModalityTransformed : voiLookup.lookup((Mat)imageModalityTransformed);
        return ((LookupTableCV)presentationLut.get()).lookup((Mat)imageVoiTransformed);
    }

    private static ImageCV processFloatingPointDataForVoi(PlanarImage imageSource, WindLevelParameters params, int dataType) {
        double window = params.getWindow();
        double level = params.getLevel();
        double low = level - window / 2.0;
        double high = level + window / 2.0;
        double range = ImageRendering.calculateRange(high, low, dataType);
        double slope = 255.0 / range;
        double yIntercept = 255.0 - slope * high;
        return ImageTransformer.rescaleToByte((Mat)ImageCV.toMat((PlanarImage)imageSource), (double)slope, (double)yIntercept);
    }

    private static double calculateRange(double high, double low, int dataType) {
        double range = high - low;
        return range < 1.0 && dataType == 3 ? 1.0 : range;
    }

    private static boolean shouldApplyOverlayMask(int bitsStored, int bitsAllocated) {
        return bitsStored < bitsAllocated && bitsAllocated >= 8 && bitsAllocated <= 16;
    }

    private static int calculateOverlayMask(ImageDescriptor desc, int frameIndex, int bitsStored) {
        int highBit = desc.getHighBit();
        int high = highBit + 1;
        int mask = (1 << high) - 1;
        if (high > bitsStored) {
            mask -= (1 << high - bitsStored) - 1;
            ImageRendering.adaptModalityLutForOverlay(desc, frameIndex, high - bitsStored);
        }
        return mask;
    }

    private static void adaptModalityLutForOverlay(ImageDescriptor desc, int frameIndex, int overlayBits) {
        ModalityLutModule modalityLut = desc.getModalityLutForFrame(frameIndex);
        if (modalityLut != null && !modalityLut.isOverlayBitMaskApplied()) {
            ModalityLutModule updatedLut = modalityLut.withOverlayBitMask(overlayBits);
            desc.setModalityLutForFrame(frameIndex, updatedLut);
        }
    }

    private static enum DataTypeCategory {
        INTEGER,
        FLOATING_POINT,
        UNSUPPORTED;

    }
}

