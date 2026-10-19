/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.annotations.Generated
 */
package org.weasis.dicom.web;

import java.io.IOException;
import org.weasis.core.util.annotations.Generated;

@Generated
public class MultipartStreamException
extends IOException {
    private static final long serialVersionUID = 1L;

    public MultipartStreamException(String message) {
        super(message);
    }

    public MultipartStreamException(String message, Throwable cause) {
        super(message, cause);
    }

    public static MultipartStreamException boundaryError(String boundary) {
        return new MultipartStreamException("Invalid or missing boundary: " + boundary);
    }

    public static MultipartStreamException parsingError(long position) {
        return new MultipartStreamException("Parsing error at stream position: " + position);
    }

    public static MultipartStreamException headerSizeExceeded(int actualSize, int maxSize) {
        return new MultipartStreamException("Header size %d exceeds maximum allowed size %d".formatted(new Object[]{actualSize, maxSize}));
    }

    public static MultipartStreamException unexpectedEndOfStream() {
        return new MultipartStreamException("Unexpected end of multipart stream");
    }
}

