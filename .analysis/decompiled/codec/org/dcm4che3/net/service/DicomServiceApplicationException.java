/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.service;

import org.dcm4che3.net.service.DicomServiceException;

public class DicomServiceApplicationException
extends DicomServiceException {
    private static final long serialVersionUID = 1418366693655498008L;

    public DicomServiceApplicationException(int status) {
        super(status);
    }

    public DicomServiceApplicationException(int status, String message) {
        super(status, message);
    }

    public DicomServiceApplicationException(int status, String message, boolean errorComment) {
        super(status, message, errorComment);
    }

    public DicomServiceApplicationException(int status, Throwable cause) {
        super(status, cause);
    }

    public DicomServiceApplicationException(int status, Throwable cause, boolean errorComment) {
        super(status, cause, errorComment);
    }
}

