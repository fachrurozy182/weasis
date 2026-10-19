/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.imageio.codec;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;

public enum TransferSyntaxType {
    NATIVE(false, false, true, 16, 0),
    JPEG_BASELINE(true, true, false, 8, 0),
    JPEG_EXTENDED(true, true, false, 12, 0),
    JPEG_SPECTRAL(true, true, false, 12, 0),
    JPEG_PROGRESSIVE(true, true, false, 12, 0),
    JPEG_LOSSLESS(true, true, true, 16, 0),
    JPEG_LS(true, true, true, 16, 0),
    JPEG_2000(true, true, true, 16, 0),
    JPEG_XL(true, true, true, 24, 0),
    RLE(true, false, true, 16, 1),
    JPIP(false, false, true, 16, 0),
    MPEG(true, false, false, 8, 0),
    DEFLATED(false, false, true, 16, 0),
    UNKNOWN(false, false, true, 16, 0);

    private final boolean pixeldataEncapsulated;
    private final boolean frameSpanMultipleFragments;
    private final boolean encodeSigned;
    private final int maxBitsStored;
    private final int planarConfiguration;

    private TransferSyntaxType(boolean pixeldataEncapsulated, boolean frameSpanMultipleFragments, boolean encodeSigned, int maxBitsStored, int planarConfiguration) {
        this.pixeldataEncapsulated = pixeldataEncapsulated;
        this.frameSpanMultipleFragments = frameSpanMultipleFragments;
        this.encodeSigned = encodeSigned;
        this.maxBitsStored = maxBitsStored;
        this.planarConfiguration = planarConfiguration;
    }

    public boolean isPixeldataEncapsulated() {
        return this.pixeldataEncapsulated;
    }

    public boolean canEncodeSigned() {
        return this.encodeSigned;
    }

    public boolean mayFrameSpanMultipleFragments() {
        return this.frameSpanMultipleFragments;
    }

    public int getPlanarConfiguration() {
        return this.planarConfiguration;
    }

    public int getMaxBitsStored() {
        return this.maxBitsStored;
    }

    public boolean adjustBitsStoredTo12(Attributes attrs) {
        int bitsStored;
        if (this.maxBitsStored == 12 && (bitsStored = attrs.getInt(2621697, 8)) > 8 && bitsStored < 12) {
            attrs.setInt(2621697, VR.US, 12);
            attrs.setInt(2621698, VR.US, 11);
            return true;
        }
        return false;
    }

    public static TransferSyntaxType forUID(String uid) {
        return switch (uid) {
            case "1.2.840.10008.1.2", "1.2.840.10008.1.2.1", "1.2.840.10008.1.2.2" -> NATIVE;
            case "1.2.840.10008.1.2.1.99" -> DEFLATED;
            case "1.2.840.10008.1.2.4.50" -> JPEG_BASELINE;
            case "1.2.840.10008.1.2.4.51" -> JPEG_EXTENDED;
            case "1.2.840.10008.1.2.4.53" -> JPEG_SPECTRAL;
            case "1.2.840.10008.1.2.4.55" -> JPEG_PROGRESSIVE;
            case "1.2.840.10008.1.2.4.57", "1.2.840.10008.1.2.4.70" -> JPEG_LOSSLESS;
            case "1.2.840.10008.1.2.4.80", "1.2.840.10008.1.2.4.81" -> JPEG_LS;
            case "1.2.840.10008.1.2.4.90", "1.2.840.10008.1.2.4.91", "1.2.840.10008.1.2.4.92", "1.2.840.10008.1.2.4.93", "1.2.840.10008.1.2.4.201", "1.2.840.10008.1.2.4.202", "1.2.840.10008.1.2.4.203" -> JPEG_2000;
            case "1.2.840.10008.1.2.4.110", "1.2.840.10008.1.2.4.111", "1.2.840.10008.1.2.4.112" -> JPEG_XL;
            case "1.2.840.10008.1.2.4.94", "1.2.840.10008.1.2.4.95", "1.2.840.10008.1.2.4.204", "1.2.840.10008.1.2.4.205" -> JPIP;
            case "1.2.840.10008.1.2.4.100", "1.2.840.10008.1.2.4.100.1", "1.2.840.10008.1.2.4.101", "1.2.840.10008.1.2.4.101.1", "1.2.840.10008.1.2.4.102", "1.2.840.10008.1.2.4.102.1", "1.2.840.10008.1.2.4.103", "1.2.840.10008.1.2.4.103.1", "1.2.840.10008.1.2.4.104", "1.2.840.10008.1.2.4.104.1", "1.2.840.10008.1.2.4.105", "1.2.840.10008.1.2.4.105.1", "1.2.840.10008.1.2.4.106", "1.2.840.10008.1.2.4.106.1", "1.2.840.10008.1.2.4.107", "1.2.840.10008.1.2.4.108" -> MPEG;
            case "1.2.840.10008.1.2.5" -> RLE;
            default -> UNKNOWN;
        };
    }

    public static boolean isLossyCompression(String uid) {
        return switch (uid) {
            case "1.2.840.10008.1.2.4.50", "1.2.840.10008.1.2.4.51", "1.2.840.10008.1.2.4.53", "1.2.840.10008.1.2.4.55", "1.2.840.10008.1.2.4.81", "1.2.840.10008.1.2.4.91", "1.2.840.10008.1.2.4.93", "1.2.840.10008.1.2.4.203", "1.2.840.10008.1.2.4.111", "1.2.840.10008.1.2.4.112", "1.2.840.10008.1.2.4.100", "1.2.840.10008.1.2.4.100.1", "1.2.840.10008.1.2.4.101", "1.2.840.10008.1.2.4.101.1", "1.2.840.10008.1.2.4.102", "1.2.840.10008.1.2.4.102.1", "1.2.840.10008.1.2.4.103", "1.2.840.10008.1.2.4.103.1", "1.2.840.10008.1.2.4.104", "1.2.840.10008.1.2.4.104.1", "1.2.840.10008.1.2.4.105", "1.2.840.10008.1.2.4.105.1", "1.2.840.10008.1.2.4.106", "1.2.840.10008.1.2.4.106.1", "1.2.840.10008.1.2.4.107", "1.2.840.10008.1.2.4.108" -> true;
            default -> false;
        };
    }

    public static boolean isYBRCompression(String uid) {
        return switch (uid) {
            case "1.2.840.10008.1.2.4.50", "1.2.840.10008.1.2.4.51", "1.2.840.10008.1.2.4.53", "1.2.840.10008.1.2.4.55", "1.2.840.10008.1.2.4.90", "1.2.840.10008.1.2.4.91", "1.2.840.10008.1.2.4.201", "1.2.840.10008.1.2.4.202", "1.2.840.10008.1.2.4.203", "1.2.840.10008.1.2.4.111" -> true;
            default -> false;
        };
    }
}

