/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.dicom.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.codec.Messages;

public enum TransferSyntax {
    NONE(null, Messages.getString("keep.original.tsuid")),
    IMPLICIT_VR("1.2.840.10008.1.2", "Implicit VR Endian (default)"),
    DEFLATE_EXPLICIT_VR_LE("1.2.840.10008.1.2.1.99", "Deflate Explicit VR Little Endian"),
    EXPLICIT_VR_LE("1.2.840.10008.1.2.1", "Explicit VR Little Endian"),
    EXPLICIT_VR_BE("1.2.840.10008.1.2.2", "Explicit VR Big Endian"),
    RLE("1.2.840.10008.1.2.5", "RLE Lossless"),
    RFC("1.2.840.10008.1.2.6.1", "RFC 2557 MIME Encapsulation"),
    JPEG_LOSSY_8("1.2.840.10008.1.2.4.50", "JPEG Lossy (8 bits)"),
    JPEG_LOSSY_12("1.2.840.10008.1.2.4.51", "JPEG Lossy (12 bits)"),
    JPEG_LOSSLESS_57("1.2.840.10008.1.2.4.57", "JPEG Lossless"),
    JPEG_LOSSLESS_70("1.2.840.10008.1.2.4.70", "JPEG Lossless"),
    JPEGLS_LOSSLESS("1.2.840.10008.1.2.4.80", "JPEG-LS Lossless"),
    JPEGLS_NEAR_LOSSLESS("1.2.840.10008.1.2.4.81", "JPEG-LS Lossy (Near-Lossless)"),
    JPEG2000_LOSSLESS("1.2.840.10008.1.2.4.90", "JPEG 2000 (Lossless Only)"),
    JPEG2000("1.2.840.10008.1.2.4.91", "JPEG 2000"),
    JPEG2000_LOSSLESS_2("1.2.840.10008.1.2.4.92", "JPEG 2000 Part 2 (Lossless Only)"),
    JPEG2000_2("1.2.840.10008.1.2.4.93", "JPEG 2000 Part 2"),
    JPEGXL_LOSSLESS("1.2.840.10008.1.2.4.110", "JPEG XL Lossless"),
    JPEGXL_RECOMPRESSION("1.2.840.10008.1.2.4.111", "JPEG XL Recompression"),
    JPEGXL("1.2.840.10008.1.2.4.112", "JPEG XL"),
    JPIP("1.2.840.10008.1.2.4.94", "JPIP"),
    JPIP_DEFLATE("1.2.840.10008.1.2.4.95", " JPIP Deflate"),
    MPEG2("1.2.840.10008.1.2.4.100", "MPEG2 Main Level"),
    MPEG2_HIGH("1.2.840.10008.1.2.4.101", "JPEG 2000 High Level"),
    MPEG_4("1.2.840.10008.1.2.4.102", "MPEG-4 AVC/H.264"),
    MPEG_4_BD("1.2.840.10008.1.2.4.103", "MPEG-4 AVC/H.264 BD-compatible");

    private static final Logger LOGGER;
    private final String label;
    private final String transferSyntaxUID;

    private TransferSyntax(String transferSyntaxUID, String label) {
        this.label = label;
        this.transferSyntaxUID = transferSyntaxUID;
    }

    public String getLabel() {
        return this.label;
    }

    public String getTransferSyntaxUID() {
        return this.transferSyntaxUID;
    }

    public String toString() {
        if (this.transferSyntaxUID == null) {
            return this.label;
        }
        return this.label + " [" + this.transferSyntaxUID + "]";
    }

    public static TransferSyntax getTransferSyntax(String name) {
        try {
            return TransferSyntax.valueOf(name);
        }
        catch (Exception e) {
            LOGGER.error("Cannot get TransferSyntax from {}", (Object)name, (Object)e);
            return NONE;
        }
    }

    static {
        LOGGER = LoggerFactory.getLogger(TransferSyntax.class);
    }
}

