/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

import java.io.IOException;

public class DicomStreamException
extends IOException {
    private static final long serialVersionUID = -6502979077324809342L;

    public DicomStreamException() {
    }

    public DicomStreamException(String message) {
        super(message);
    }

    public DicomStreamException(Throwable cause) {
        super(cause);
    }

    public DicomStreamException(String message, Throwable cause) {
        super(message, cause);
    }
}

