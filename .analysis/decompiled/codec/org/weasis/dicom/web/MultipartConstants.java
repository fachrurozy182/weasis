/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.web;

public final class MultipartConstants {
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String MULTIPART_RELATED = "multipart/related";
    public static final byte CR = 13;
    public static final byte LF = 10;
    public static final byte DASH = 45;

    private MultipartConstants() {
    }

    public static enum DicomContentType {
        DICOM("application/dicom"),
        XML("application/dicom+xml"),
        JSON("application/dicom+json");

        private final String mimeType;

        private DicomContentType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getMimeType() {
            return this.mimeType;
        }

        public String toString() {
            return this.mimeType;
        }
    }

    public static enum Separator {
        HEADER(new byte[]{13, 10, 13, 10}),
        FIELD(new byte[]{13, 10}),
        BOUNDARY(new byte[]{13, 10, 45, 45}),
        STREAM(new byte[]{45, 45});

        private final byte[] bytes;

        private Separator(byte[] bytes) {
            this.bytes = (byte[])bytes.clone();
        }

        public byte[] getBytes() {
            return (byte[])this.bytes.clone();
        }

        public String toString() {
            return new String(this.bytes);
        }
    }
}

