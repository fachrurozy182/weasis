/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.service;

import java.io.IOException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.ValidationResult;
import org.dcm4che3.util.StringUtils;

public class DicomServiceException
extends IOException {
    private static final long serialVersionUID = -8680017798403768406L;
    private final Attributes rsp = new Attributes();
    private Attributes data;

    public DicomServiceException(int status) {
        this.setStatus(status);
    }

    public DicomServiceException(int status, String message) {
        this(status, message, true);
    }

    public DicomServiceException(int status, String message, boolean errorComment) {
        super(message);
        this.setStatus(status);
        if (errorComment) {
            this.setErrorComment(this.getMessage());
        }
    }

    public DicomServiceException(int status, Throwable cause) {
        this(status, cause, true);
    }

    public DicomServiceException(int status, Throwable cause, boolean errorComment) {
        super(cause);
        this.setStatus(status);
        if (errorComment) {
            this.setErrorComment(this.getMessage());
        }
    }

    public static Throwable initialCauseOf(Throwable e) {
        Throwable cause;
        if (e == null) {
            return null;
        }
        while ((cause = e.getCause()) != null) {
            e = cause;
        }
        return e;
    }

    private void setStatus(int status) {
        this.rsp.setInt(2304, VR.US, status);
    }

    public int getStatus() {
        return this.rsp.getInt(2304, 0);
    }

    public DicomServiceException setUID(int tag, String value) {
        this.rsp.setString(tag, VR.UI, value);
        return this;
    }

    public DicomServiceException setErrorComment(String val) {
        if (val != null) {
            this.rsp.setString(2306, VR.LO, StringUtils.truncate(val, 64));
        }
        return this;
    }

    public DicomServiceException setErrorID(int val) {
        this.rsp.setInt(2307, VR.US, val);
        return this;
    }

    public DicomServiceException setEventTypeID(int val) {
        this.rsp.setInt(4098, VR.US, val);
        return this;
    }

    public DicomServiceException setActionTypeID(int val) {
        this.rsp.setInt(4104, VR.US, val);
        return this;
    }

    public DicomServiceException setNumberOfCompletedFailedWarningSuboperations(int completed, int failed, int warning) {
        this.rsp.setInt(4129, VR.US, completed);
        this.rsp.setInt(4130, VR.US, failed);
        this.rsp.setInt(4131, VR.US, warning);
        return this;
    }

    public DicomServiceException setOffendingElements(int ... tags) {
        this.rsp.setInt(2305, VR.AT, tags);
        return this;
    }

    public DicomServiceException setAttributeIdentifierList(int ... tags) {
        this.rsp.setInt(4101, VR.AT, tags);
        return this;
    }

    public Attributes mkRSP(int cmdField, int msgId) {
        this.rsp.setInt(256, VR.US, cmdField);
        this.rsp.setInt(288, VR.US, msgId);
        return this.rsp;
    }

    public final Attributes getDataset() {
        return this.data;
    }

    public final DicomServiceException setDataset(Attributes data) {
        this.data = data;
        return this;
    }

    public static DicomServiceException valueOf(ValidationResult result, Attributes attrs) {
        if (result.hasNotAllowedAttributes()) {
            return new DicomServiceException(261, result.getErrorComment(), false).setAttributeIdentifierList(result.tagsOfNotAllowedAttributes());
        }
        if (result.hasMissingAttributes()) {
            return new DicomServiceException(288, result.getErrorComment(), false).setAttributeIdentifierList(result.tagsOfMissingAttributes());
        }
        if (result.hasMissingAttributeValues()) {
            return new DicomServiceException(289, result.getErrorComment(), false).setDataset(new Attributes(attrs, result.tagsOfMissingAttributeValues()));
        }
        if (result.hasInvalidAttributeValues()) {
            return new DicomServiceException(262, result.getErrorComment(), false).setDataset(new Attributes(attrs, result.tagsOfInvalidAttributeValues()));
        }
        return null;
    }
}

