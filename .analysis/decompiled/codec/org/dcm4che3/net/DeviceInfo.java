/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.Serializable;

public class DeviceInfo
implements Serializable {
    private static final long serialVersionUID = 5456917095404357064L;
    private String deviceName;
    private String description;
    private String manufacturer;
    private String manufacturerModelName;
    private String stationName;
    private String[] softwareVersions = new String[0];
    private String[] primaryDeviceTypes = new String[0];
    private String[] institutionNames = new String[0];
    private String[] institutionalDepartmentNames = new String[0];
    private Boolean installed;
    private Boolean arcDevExt;

    public final String getDeviceName() {
        return this.deviceName;
    }

    public final void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public final String getDescription() {
        return this.description;
    }

    public final void setDescription(String description) {
        this.description = description;
    }

    public final String getManufacturer() {
        return this.manufacturer;
    }

    public final void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public final String getManufacturerModelName() {
        return this.manufacturerModelName;
    }

    public final void setManufacturerModelName(String manufacturerModelName) {
        this.manufacturerModelName = manufacturerModelName;
    }

    public final String getStationName() {
        return this.stationName;
    }

    public final void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public final String[] getSoftwareVersions() {
        return this.softwareVersions;
    }

    public final void setSoftwareVersions(String[] softwareVersions) {
        this.softwareVersions = softwareVersions;
    }

    public final String[] getPrimaryDeviceTypes() {
        return this.primaryDeviceTypes;
    }

    public final void setPrimaryDeviceTypes(String[] primaryDeviceTypes) {
        this.primaryDeviceTypes = primaryDeviceTypes;
    }

    public final String[] getInstitutionNames() {
        return this.institutionNames;
    }

    public final void setInstitutionNames(String[] institutionNames) {
        this.institutionNames = institutionNames;
    }

    public final String[] getInstitutionalDepartmentNames() {
        return this.institutionalDepartmentNames;
    }

    public final void setInstitutionalDepartmentNames(String[] institutionalDepartmentNames) {
        this.institutionalDepartmentNames = institutionalDepartmentNames;
    }

    public final Boolean getInstalled() {
        return this.installed;
    }

    public final void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    public Boolean getArcDevExt() {
        return this.arcDevExt;
    }

    public void setArcDevExt(Boolean arcDevExt) {
        this.arcDevExt = arcDevExt;
    }

    public String toString() {
        return "DeviceInfo[name=" + this.deviceName + ", installed=" + this.installed + "]";
    }
}

