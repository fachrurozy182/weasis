/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.dcm4che3.net.Connection;

public class ApplicationEntityInfo
implements Serializable {
    private String deviceName;
    private String description;
    private String aet;
    private String[] applicationClusters = new String[0];
    private Boolean associationInitiator;
    private Boolean associationAcceptor;
    private Boolean installed;
    private String[] otherAETitle;
    private String hl7ApplicationName;
    private final List<Connection> conns = new ArrayList<Connection>(1);

    public Boolean getInstalled() {
        return this.installed;
    }

    public void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    public String[] getOtherAETitle() {
        return this.otherAETitle;
    }

    public void setOtherAETitle(String[] otherAETitle) {
        this.otherAETitle = otherAETitle;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAETitle() {
        return this.aet;
    }

    public void setAETitle(String aet) {
        this.aet = aet;
    }

    public String[] getApplicationClusters() {
        return this.applicationClusters;
    }

    public void setApplicationClusters(String[] applicationClusters) {
        this.applicationClusters = applicationClusters;
    }

    public Boolean getAssociationInitiator() {
        return this.associationInitiator;
    }

    public void setAssociationInitiator(Boolean associationInitiator) {
        this.associationInitiator = associationInitiator;
    }

    public Boolean getAssociationAcceptor() {
        return this.associationAcceptor;
    }

    public void setAssociationAcceptor(Boolean associationAcceptor) {
        this.associationAcceptor = associationAcceptor;
    }

    public List<Connection> getConnections() {
        return this.conns;
    }

    public void setHl7ApplicationName(String hl7ApplicationName) {
        this.hl7ApplicationName = hl7ApplicationName;
    }

    public String getHl7ApplicationName() {
        return this.hl7ApplicationName;
    }

    public String toString() {
        return "ApplicationEntityInfo[dicomAETitle=" + this.aet + "]";
    }
}

