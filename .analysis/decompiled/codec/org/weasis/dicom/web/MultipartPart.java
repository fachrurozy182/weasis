/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.annotations.Generated
 */
package org.weasis.dicom.web;

import java.io.InputStream;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.annotations.Generated;
import org.weasis.dicom.web.Payload;

public record MultipartPart(String contentType, String location, Payload payload) {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipartPart.class);

    public MultipartPart {
        Objects.requireNonNull(contentType, "Content type cannot be null");
        Objects.requireNonNull(payload, "Payload cannot be null");
    }

    public InputStream newInputStream() {
        return this.payload.newInputStream();
    }

    public String generateHeader(String boundary) {
        Objects.requireNonNull(boundary, "Boundary cannot be null");
        StringBuilder header = new StringBuilder(256).append("\r\n--").append(boundary).append("\r\n").append("Content-Type").append(": ").append(this.contentType);
        long size = this.payload.size();
        if (size < 0L) {
            header.append("\r\nContent-Encoding: gzip, identity");
        } else {
            header.append("\r\nContent-Length: ").append(size);
        }
        if (this.location != null && !this.location.isEmpty()) {
            header.append("\r\nContent-Location: ").append(this.location);
        }
        return header.append("\r\n\r\n").toString();
    }

    @Generated
    public void logDebugInfo(String boundary) {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }
        LOGGER.debug("> --{}", (Object)boundary);
        LOGGER.debug("> Content-Type: {}", (Object)this.contentType);
        long size = this.payload.size();
        if (size < 0L) {
            LOGGER.debug("> Content-Encoding: gzip, identity");
        } else {
            LOGGER.debug("> Content-Length: {}", (Object)size);
        }
        if (this.location != null) {
            LOGGER.debug("> Content-Location: {}", (Object)this.location);
        }
        LOGGER.debug(">");
        LOGGER.debug("> [payload data]");
    }
}

