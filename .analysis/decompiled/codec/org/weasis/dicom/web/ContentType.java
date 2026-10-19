/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.web;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public enum ContentType {
    APPLICATION_DICOM("application/dicom", -1);

    private final String type;
    private final int bulkdataTag;

    private ContentType(String type, int bulkdataTag) {
        this.type = Objects.requireNonNull(type, "Content type cannot be null");
        this.bulkdataTag = bulkdataTag;
    }

    public String getType() {
        return this.type;
    }

    public int getBulkdataTag() {
        return this.bulkdataTag;
    }

    static ContentType probe(Path path) {
        Objects.requireNonNull(path, "Path cannot be null");
        try {
            String detectedType = Files.probeContentType(path);
            if (detectedType == null) {
                throw new IOException("Failed to determine content type of file: '%s'".formatted(new Object[]{path}));
            }
            if (ContentType.APPLICATION_DICOM.type.equalsIgnoreCase(detectedType)) {
                return APPLICATION_DICOM;
            }
            throw new UnsupportedOperationException("Unsupported content type: '%s' of file: '%s'".formatted(new Object[]{detectedType, path}));
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toMultipartContentType(String boundary) {
        Objects.requireNonNull(boundary, "Boundary cannot be null");
        return "multipart/related" + "; type=\"%s\";boundary=%s".formatted(new Object[]{this.type, boundary});
    }

    public String toString() {
        return this.type;
    }
}

