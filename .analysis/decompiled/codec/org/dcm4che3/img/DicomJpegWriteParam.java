/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.img;

import java.awt.Rectangle;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.img.DicomOutputData;

public class DicomJpegWriteParam {
    public static final int DEFAULT_COMPRESSION_QUALITY = 85;
    public static final int DEFAULT_LOSSY_COMPRESSION_RATIO = 10;
    public static final int DEFAULT_NEAR_LOSSLESS_ERROR = 0;
    private static final int DEFAULT_PREDICTION = 1;
    private static final int DEFAULT_POINT_TRANSFORM = 0;
    private static final int DEFAULT_COMPRESSION_RATIO = 0;
    private static final int LOSSLESS_PREDICTION_MODE = 6;
    private static final int DEFAULT_LOSSY_NEAR_LOSSLESS = 2;
    private final TransferSyntaxType type;
    private final String transferSyntaxUid;
    private int prediction = 1;
    private int pointTransform = 0;
    private int nearLosslessError = 0;
    private int compressionQuality = 85;
    private int compressionRatioFactor = 0;
    private boolean losslessCompression = true;
    private Rectangle sourceRegion;
    private int jxlEffort = 7;
    private int jxlDecodingSpeed = 0;

    private DicomJpegWriteParam(TransferSyntaxType type, String transferSyntaxUid) {
        this.type = type;
        this.transferSyntaxUid = transferSyntaxUid;
    }

    public String getTransferSyntaxUid() {
        return this.transferSyntaxUid;
    }

    public int getPrediction() {
        return this.prediction;
    }

    public void setPrediction(int prediction) {
        this.prediction = prediction;
    }

    public int getPointTransform() {
        return this.pointTransform;
    }

    public void setPointTransform(int pointTransform) {
        this.pointTransform = pointTransform;
    }

    public int getNearLosslessError() {
        return this.nearLosslessError;
    }

    public void setNearLosslessError(int nearLosslessError) {
        if (nearLosslessError < 0) {
            throw new IllegalArgumentException("nearLossless invalid value: " + nearLosslessError);
        }
        this.nearLosslessError = nearLosslessError;
    }

    public int getJxlEffort() {
        return this.jxlEffort;
    }

    public int getJxlDecodingSpeed() {
        return this.jxlDecodingSpeed;
    }

    public void setJxlEffort(int jxlEffort) {
        if (jxlEffort < 1 || jxlEffort > 9) {
            throw new IllegalArgumentException("jxlEffort invalid value: " + jxlEffort);
        }
        this.jxlEffort = jxlEffort;
    }

    public void setJxlDecodingSpeed(int jxlDecodingSpeed) {
        if (jxlDecodingSpeed < 0 || jxlDecodingSpeed > 7) {
            throw new IllegalArgumentException("jxlDecodingSpeed invalid value: " + jxlDecodingSpeed);
        }
        this.jxlDecodingSpeed = jxlDecodingSpeed;
    }

    public int getCompressionQuality() {
        return this.compressionQuality;
    }

    public void setCompressionQuality(int compressionQuality) {
        this.compressionQuality = compressionQuality;
    }

    public int getCompressionRatioFactor() {
        return this.compressionRatioFactor;
    }

    public void setCompressionRatioFactor(int compressionRatioFactor) {
        this.compressionRatioFactor = compressionRatioFactor;
    }

    public TransferSyntaxType getType() {
        return this.type;
    }

    public boolean isCompressionLossless() {
        return this.losslessCompression;
    }

    public int getJpegMode() {
        return switch (this.type) {
            case TransferSyntaxType.JPEG_BASELINE -> 0;
            case TransferSyntaxType.JPEG_EXTENDED -> 1;
            case TransferSyntaxType.JPEG_SPECTRAL -> 2;
            case TransferSyntaxType.JPEG_PROGRESSIVE -> 3;
            case TransferSyntaxType.JPEG_LOSSLESS -> 4;
            default -> 0;
        };
    }

    public Rectangle getSourceRegion() {
        return this.sourceRegion;
    }

    public void setSourceRegion(Rectangle sourceRegion) {
        if (sourceRegion != null) {
            this.validateSourceRegion(sourceRegion);
        }
        this.sourceRegion = sourceRegion;
    }

    private void validateSourceRegion(Rectangle region) {
        if (region.x < 0 || region.y < 0 || region.width <= 0 || region.height <= 0) {
            throw new IllegalArgumentException("sourceRegion has illegal values!");
        }
    }

    public static DicomJpegWriteParam buildDicomImageWriteParam(String tsuid) {
        TransferSyntaxType type = TransferSyntaxType.forUID(tsuid);
        DicomJpegWriteParam.validateSupportedSyntax(type, tsuid);
        DicomJpegWriteParam param = new DicomJpegWriteParam(type, tsuid);
        DicomJpegWriteParam.configureCompressionMode(param, tsuid);
        DicomJpegWriteParam.configureLosslessParameters(param, type, tsuid);
        return param;
    }

    private static void validateSupportedSyntax(TransferSyntaxType type, String tsuid) {
        if (DicomJpegWriteParam.isUnsupportedSyntax(type) || !DicomOutputData.isSupportedSyntax(tsuid)) {
            throw new IllegalStateException(tsuid + " is not supported for compression!");
        }
    }

    private static boolean isUnsupportedSyntax(TransferSyntaxType type) {
        return switch (type) {
            case TransferSyntaxType.NATIVE, TransferSyntaxType.RLE, TransferSyntaxType.JPIP, TransferSyntaxType.MPEG -> true;
            default -> false;
        };
    }

    private static void configureCompressionMode(DicomJpegWriteParam param, String tsuid) {
        boolean isLossless;
        param.losslessCompression = isLossless = !TransferSyntaxType.isLossyCompression(tsuid);
        if (isLossless) {
            param.setNearLosslessError(0);
            param.setCompressionRatioFactor(0);
            param.setCompressionQuality(0);
        } else {
            param.setNearLosslessError(2);
            param.setCompressionRatioFactor(10);
        }
    }

    private static void configureLosslessParameters(DicomJpegWriteParam param, TransferSyntaxType type, String tsuid) {
        if (type == TransferSyntaxType.JPEG_LOSSLESS) {
            param.setPointTransform(0);
            param.setPrediction("1.2.840.10008.1.2.4.57".equals(tsuid) ? 6 : 1);
        }
    }
}

