/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.KeycloakClient;
import org.dcm4che3.net.WebApplication;

public class WebApplicationInfo {
    private String deviceName;
    private String applicationName;
    private String description;
    private String servicePath;
    private String aeTitle;
    private String[] applicationClusters = new String[0];
    private String keycloakClientID;
    private KeycloakClient keycloakClient;
    private Boolean installed;
    private final EnumSet<WebApplication.ServiceClass> serviceClasses = EnumSet.noneOf(WebApplication.ServiceClass.class);
    private final Map<String, String> properties = new HashMap<String, String>();
    private final List<Connection> conns = new ArrayList<Connection>(1);

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public void setApplicationName(String name) {
        this.applicationName = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getServicePath() {
        return this.servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getAETitle() {
        return this.aeTitle;
    }

    public void setAETitle(String aeTitle) {
        this.aeTitle = aeTitle;
    }

    public String[] getApplicationClusters() {
        return this.applicationClusters;
    }

    public void setApplicationClusters(String[] applicationClusters) {
        this.applicationClusters = applicationClusters;
    }

    public String getKeycloakClientID() {
        return this.keycloakClientID;
    }

    public void setKeycloakClientID(String keycloakClientID) {
        this.keycloakClientID = keycloakClientID;
    }

    public KeycloakClient getKeycloakClient() {
        return this.keycloakClient;
    }

    public void setKeycloakClient(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    public Boolean getInstalled() {
        return this.installed;
    }

    public void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    public List<Connection> getConnections() {
        return this.conns;
    }

    public WebApplication.ServiceClass[] getServiceClasses() {
        return this.serviceClasses.toArray(new WebApplication.ServiceClass[0]);
    }

    public void setServiceClasses(WebApplication.ServiceClass ... serviceClasses) {
        this.setServiceClasses(Arrays.asList(serviceClasses));
    }

    public void setServiceClasses(List<WebApplication.ServiceClass> serviceClasses) {
        this.serviceClasses.clear();
        this.serviceClasses.addAll(serviceClasses);
    }

    public void setProperty(String name, String value) {
        this.properties.put(name, value);
    }

    public String getProperty(String name, String defValue) {
        String value = this.properties.get(name);
        return value != null ? value : defValue;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public void setProperties(String[] ss) {
        this.properties.clear();
        for (String s : ss) {
            int index = s.indexOf(61);
            if (index < 0) {
                throw new IllegalArgumentException("Property in incorrect format : " + s);
            }
            this.setProperty(s.substring(0, index), s.substring(index + 1));
        }
    }

    public String toString() {
        return "WebApplicationInfo[name=" + this.applicationName + ",classes=" + this.serviceClasses + ",path=" + this.servicePath + ",aet=" + this.aeTitle + ",applicationClusters=" + Arrays.toString(this.applicationClusters) + ",keycloakClientID=" + this.keycloakClientID + ",serviceClasses=" + this.serviceClasses + ",properties=" + this.properties + ",installed=" + this.installed + ']';
    }
}

