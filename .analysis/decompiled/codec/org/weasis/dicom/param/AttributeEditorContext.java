/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.LangUtil
 *  org.weasis.opencv.data.PlanarImage
 */
package org.weasis.dicom.param;

import java.util.Objects;
import java.util.Properties;
import org.dcm4che3.img.Transcoder;
import org.dcm4che3.img.op.MaskArea;
import org.dcm4che3.img.util.Editable;
import org.weasis.core.util.LangUtil;
import org.weasis.dicom.param.DicomNode;
import org.weasis.opencv.data.PlanarImage;

public class AttributeEditorContext {
    private static final String DEFACING_PROPERTY = "defacing";
    private final String tsuid;
    private final DicomNode sourceNode;
    private final DicomNode destinationNode;
    private final Properties properties;
    private Abort abort;
    private String abortMessage;
    private MaskArea maskArea;

    public AttributeEditorContext(String tsuid, DicomNode sourceNode, DicomNode destinationNode) {
        this.tsuid = tsuid;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.abort = Abort.NONE;
        this.properties = new Properties();
    }

    public Abort getAbort() {
        return this.abort;
    }

    public void setAbort(Abort abort) {
        this.abort = Objects.requireNonNullElse(abort, Abort.NONE);
    }

    public String getAbortMessage() {
        return this.abortMessage;
    }

    public void setAbortMessage(String abortMessage) {
        this.abortMessage = abortMessage;
    }

    public String getTsuid() {
        return this.tsuid;
    }

    public DicomNode getSourceNode() {
        return this.sourceNode;
    }

    public DicomNode getDestinationNode() {
        return this.destinationNode;
    }

    public MaskArea getMaskArea() {
        return this.maskArea;
    }

    public void setMaskArea(MaskArea maskArea) {
        this.maskArea = maskArea;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public Editable<PlanarImage> getEditable() {
        return Transcoder.getMaskedImage(this.getMaskArea());
    }

    public boolean hasPixelProcessing() {
        return this.hasMaskArea() || this.isDefacingEnabled();
    }

    private boolean hasMaskArea() {
        return Objects.nonNull(this.getMaskArea());
    }

    private boolean isDefacingEnabled() {
        return LangUtil.emptyToFalse((String)this.getProperties().getProperty(DEFACING_PROPERTY));
    }

    public void setAbort(Abort abort, String message) {
        this.setAbort(abort);
        this.setAbortMessage(message);
    }

    public boolean shouldAbort() {
        return this.abort != Abort.NONE;
    }

    public String toString() {
        return "AttributeEditorContext{tsuid='" + this.tsuid + "', sourceNode=" + String.valueOf(this.sourceNode) + ", destinationNode=" + String.valueOf(this.destinationNode) + ", abort=" + String.valueOf((Object)this.abort) + ", abortMessage='" + this.abortMessage + "', maskArea=" + String.valueOf(this.maskArea) + ", propertiesCount=" + this.properties.size() + "}";
    }

    public static enum Abort {
        NONE,
        FILE_EXCEPTION,
        CONNECTION_EXCEPTION;

    }
}

