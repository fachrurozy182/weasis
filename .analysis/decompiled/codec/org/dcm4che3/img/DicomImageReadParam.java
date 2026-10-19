/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.LangUtil
 *  org.weasis.core.util.annotations.Generated
 *  org.weasis.opencv.op.lut.LutShape
 */
package org.dcm4che3.img;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.OptionalDouble;
import javax.imageio.IIOParamController;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import org.dcm4che3.img.data.PrDicomObject;
import org.weasis.core.util.LangUtil;
import org.weasis.core.util.annotations.Generated;
import org.weasis.opencv.op.lut.LutShape;

@Generated
public class DicomImageReadParam
extends ImageReadParam {
    private static final String NOT_COMPATIBLE = "Not compatible with the native DICOM Decoder";
    private Double windowCenter;
    private Double windowWidth;
    private Double levelMin;
    private Double levelMax;
    private LutShape voiLutShape;
    private Boolean applyPixelPadding;
    private Boolean inverseLut;
    private Boolean fillOutsideLutRange;
    private Boolean applyWindowLevelToColorImage;
    private Boolean keepRgbForLossyJpeg;
    private Boolean releaseImageAfterProcessing;
    private PrDicomObject presentationState;
    private int windowIndex;
    private int voiLUTIndex;
    private int overlayActivationMask = 15;
    private int overlayGrayscaleValue = 65535;
    private Color overlayColor;
    private boolean allowFloatImageConversion = false;

    public DicomImageReadParam() {
        this.canSetSourceRenderSize = true;
    }

    public DicomImageReadParam(ImageReadParam param) {
        this();
        this.copySourceParameters(param);
    }

    private void copySourceParameters(ImageReadParam param) {
        this.sourceRegion = param.getSourceRegion();
        this.sourceRenderSize = param.getSourceRenderSize();
    }

    @Override
    public void setDestinationOffset(Point destinationOffset) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    @Override
    public void setController(IIOParamController controller) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    @Override
    public void setSourceBands(int[] sourceBands) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    @Override
    public void setSourceSubsampling(int sourceXSubsampling, int sourceYSubsampling, int subsamplingXOffset, int subsamplingYOffset) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    @Override
    public void setDestination(BufferedImage destination) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    @Override
    public void setDestinationBands(int[] destinationBands) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    @Override
    public void setDestinationType(ImageTypeSpecifier destinationType) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    @Override
    public void setSourceProgressivePasses(int minPass, int numPasses) {
        throw new UnsupportedOperationException(NOT_COMPATIBLE);
    }

    public OptionalDouble getWindowCenter() {
        return LangUtil.toOptional((Double)this.windowCenter);
    }

    public void setWindowCenter(Double windowCenter) {
        this.windowCenter = windowCenter;
    }

    public OptionalDouble getWindowWidth() {
        return LangUtil.toOptional((Double)this.windowWidth);
    }

    public void setWindowWidth(Double windowWidth) {
        this.windowWidth = windowWidth;
    }

    public OptionalDouble getLevelMin() {
        return LangUtil.toOptional((Double)this.levelMin);
    }

    public void setLevelMin(Double levelMin) {
        this.levelMin = levelMin;
    }

    public OptionalDouble getLevelMax() {
        return LangUtil.toOptional((Double)this.levelMax);
    }

    public void setLevelMax(Double levelMax) {
        this.levelMax = levelMax;
    }

    public Optional<LutShape> getVoiLutShape() {
        return Optional.ofNullable(this.voiLutShape);
    }

    public void setVoiLutShape(LutShape voiLutShape) {
        this.voiLutShape = voiLutShape;
    }

    public Optional<Boolean> getApplyPixelPadding() {
        return Optional.ofNullable(this.applyPixelPadding);
    }

    public void setApplyPixelPadding(Boolean applyPixelPadding) {
        this.applyPixelPadding = applyPixelPadding;
    }

    public Optional<Boolean> getInverseLut() {
        return Optional.ofNullable(this.inverseLut);
    }

    public void setInverseLut(Boolean inverseLut) {
        this.inverseLut = inverseLut;
    }

    public Optional<Boolean> getReleaseImageAfterProcessing() {
        return Optional.ofNullable(this.releaseImageAfterProcessing);
    }

    public void setReleaseImageAfterProcessing(Boolean releaseImageAfterProcessing) {
        this.releaseImageAfterProcessing = releaseImageAfterProcessing;
    }

    public Optional<Boolean> getFillOutsideLutRange() {
        return Optional.ofNullable(this.fillOutsideLutRange);
    }

    public void setFillOutsideLutRange(Boolean fillOutsideLutRange) {
        this.fillOutsideLutRange = fillOutsideLutRange;
    }

    public Optional<Boolean> getApplyWindowLevelToColorImage() {
        return Optional.ofNullable(this.applyWindowLevelToColorImage);
    }

    public void setApplyWindowLevelToColorImage(Boolean applyWindowLevelToColorImage) {
        this.applyWindowLevelToColorImage = applyWindowLevelToColorImage;
    }

    public Optional<Boolean> getKeepRgbForLossyJpeg() {
        return Optional.ofNullable(this.keepRgbForLossyJpeg);
    }

    public void setKeepRgbForLossyJpeg(Boolean keepRgbForLossyJpeg) {
        this.keepRgbForLossyJpeg = keepRgbForLossyJpeg;
    }

    public int getVoiLUTIndex() {
        return this.voiLUTIndex;
    }

    public void setVoiLUTIndex(int voiLUTIndex) {
        this.voiLUTIndex = Math.max(voiLUTIndex, 0);
    }

    public int getWindowIndex() {
        return this.windowIndex;
    }

    public void setWindowIndex(int windowIndex) {
        this.windowIndex = Math.max(windowIndex, 0);
    }

    public Optional<PrDicomObject> getPresentationState() {
        return Optional.ofNullable(this.presentationState);
    }

    public void setPresentationState(PrDicomObject presentationState) {
        this.presentationState = presentationState;
    }

    public int getOverlayActivationMask() {
        return this.overlayActivationMask;
    }

    public void setOverlayActivationMask(int overlayActivationMask) {
        this.overlayActivationMask = overlayActivationMask;
    }

    public int getOverlayGrayscaleValue() {
        return this.overlayGrayscaleValue;
    }

    public void setOverlayGrayscaleValue(int overlayGrayscaleValue) {
        this.overlayGrayscaleValue = overlayGrayscaleValue;
    }

    public Optional<Color> getOverlayColor() {
        return Optional.ofNullable(this.overlayColor);
    }

    public void setOverlayColor(Color overlayColor) {
        this.overlayColor = overlayColor;
    }

    public boolean isAllowFloatImageConversion() {
        return this.allowFloatImageConversion;
    }

    public void setAllowFloatImageConversion(boolean allowFloatImageConversion) {
        this.allowFloatImageConversion = allowFloatImageConversion;
    }
}

