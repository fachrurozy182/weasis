/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.send;

public class HttpServerErrorException
extends RuntimeException {
    public HttpServerErrorException(String message) {
        super(message);
    }

    public HttpServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}

