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
import org.dcm4che3.net.Device;
import org.dcm4che3.net.KeycloakClient;

public class WebApplication {
    private Device device;
    private String applicationName;
    private String description;
    private String servicePath;
    private String aeTitle;
    private String[] applicationClusters = new String[0];
    private String keycloakClientID;
    private Boolean installed;
    private final EnumSet<ServiceClass> serviceClasses = EnumSet.noneOf(ServiceClass.class);
    private final Map<String, String> properties = new HashMap<String, String>();
    private final List<Connection> conns = new ArrayList<Connection>(1);

    public WebApplication() {
    }

    public WebApplication(String applicationName) {
        this.applicationName = applicationName;
    }

    public Device getDevice() {
        return this.device;
    }

    void setDevice(Device device) {
        if (device != null) {
            if (this.device != null) {
                throw new IllegalStateException("already owned by " + this.device.getDeviceName());
            }
            for (Connection conn : this.conns) {
                if (conn.getDevice() == device) continue;
                throw new IllegalStateException(conn + " not owned by " + device.getDeviceName());
            }
        }
        this.device = device;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public void setApplicationName(String name) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        Device device = this.device;
        if (device != null) {
            device.removeWebApplication(this.applicationName);
        }
        this.applicationName = name;
        if (device != null) {
            device.addWebApplication(this);
        }
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
        this.servicePath = servicePath.startsWith("/") ? servicePath : '/' + servicePath;
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

    public void setApplicationClusters(String ... applicationClusters) {
        this.applicationClusters = applicationClusters;
    }

    public String getKeycloakClientID() {
        return this.keycloakClientID;
    }

    public void setKeycloakClientID(String keycloakClientID) {
        this.keycloakClientID = keycloakClientID;
    }

    public boolean isInstalled() {
        return this.device != null && this.device.isInstalled() && (this.installed == null || this.installed != false);
    }

    public final Boolean getInstalled() {
        return this.installed;
    }

    public void setInstalled(Boolean installed) {
        if (installed != null && installed.booleanValue() && this.device != null && !this.device.isInstalled()) {
            throw new IllegalStateException("owning device not installed");
        }
        this.installed = installed;
    }

    public KeycloakClient getKeycloakClient() {
        return this.keycloakClientID != null ? this.device.getKeycloakClient(this.keycloakClientID) : null;
    }

    public void addConnection(Connection conn) {
        if (conn.getProtocol() != Connection.Protocol.HTTP) {
            throw new IllegalArgumentException("Web Application does not support protocol " + (Object)((Object)conn.getProtocol()));
        }
        if (this.device != null && this.device != conn.getDevice()) {
            throw new IllegalStateException(conn + " not contained by " + this.device.getDeviceName());
        }
        this.conns.add(conn);
    }

    public StringBuilder getServiceURL() {
        return this.getServiceURL(this.firstInstalledConnection());
    }

    public Connection firstInstalledConnection() {
        for (Connection conn : this.conns) {
            if (!conn.isInstalled()) continue;
            return conn;
        }
        throw new IllegalStateException("No installed Network Connection");
    }

    public StringBuilder getServiceURL(Connection conn) {
        return new StringBuilder(64).append(conn.isTls() ? "https://" : "http://").append(conn.getHostname()).append(':').append(conn.getPort()).append(this.servicePath);
    }

    public boolean removeConnection(Connection conn) {
        return this.conns.remove(conn);
    }

    public List<Connection> getConnections() {
        return this.conns;
    }

    public ServiceClass[] getServiceClasses() {
        return this.serviceClasses.toArray(new ServiceClass[0]);
    }

    public void setServiceClasses(ServiceClass ... serviceClasses) {
        this.serviceClasses.clear();
        this.serviceClasses.addAll(Arrays.asList(serviceClasses));
    }

    public boolean containsServiceClass(ServiceClass serviceClass) {
        return this.serviceClasses.contains((Object)serviceClass);
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

    void reconfigure(WebApplication src) {
        this.description = src.description;
        this.servicePath = src.servicePath;
        this.aeTitle = src.aeTitle;
        this.applicationClusters = src.applicationClusters;
        this.keycloakClientID = src.keycloakClientID;
        this.installed = src.installed;
        this.serviceClasses.clear();
        this.serviceClasses.addAll(src.serviceClasses);
        this.properties.clear();
        this.properties.putAll(src.properties);
        this.device.reconfigureConnections(this.conns, src.conns);
    }

    public String toString() {
        return "WebApplication[name=" + this.applicationName + ",serviceClasses=" + this.serviceClasses + ",path=" + this.servicePath + ",aet=" + this.aeTitle + ",applicationClusters=" + Arrays.toString(this.applicationClusters) + ",keycloakClientID=" + this.keycloakClientID + ",properties=" + this.properties + ",installed=" + this.installed + ']';
    }

    public static enum ServiceClass {
        WADO_URI,
        WADO_RS,
        STOW_RS,
        QIDO_RS,
        UPS_RS,
        MWL_RS,
        MPPS_RS,
        QIDO_COUNT,
        DCM4CHEE_ARC,
        DCM4CHEE_ARC_AET,
        DCM4CHEE_ARC_AET_DIFF,
        PAM,
        REJECT,
        MOVE,
        MOVE_MATCHING,
        UPS_MATCHING,
        ELASTICSEARCH,
        PROMETHEUS,
        GRAFANA,
        DOCUMENTATION,
        XDS_RS,
        AGFA_BLOB,
        J4C_ROUTER,
        FHIR,
        AI_CHAT,
        WORKFLOW_MANAGER;

    }
}

