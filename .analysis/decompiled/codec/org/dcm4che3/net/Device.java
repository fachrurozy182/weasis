/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.AssociationHandler;
import org.dcm4che3.net.AssociationMonitor;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.ConnectionMonitor;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.DimseRQHandler;
import org.dcm4che3.net.KeycloakClient;
import org.dcm4che3.net.SSLManagerFactory;
import org.dcm4che3.net.WebApplication;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.util.StringUtils;

public class Device
implements Serializable {
    private static final long serialVersionUID = -5816872456184522866L;
    private String deviceName;
    private String deviceUID;
    private String description;
    private String manufacturer;
    private String manufacturerModelName;
    private String stationName;
    private String deviceSerialNumber;
    private String trustStoreURL;
    private String trustStoreType;
    private String trustStorePin;
    private String trustStorePinProperty;
    private String keyStoreURL;
    private String keyStoreType;
    private String keyStorePin;
    private String keyStorePinProperty;
    private String keyStoreKeyPin;
    private String keyStoreKeyPinProperty;
    private Issuer issuerOfPatientID;
    private Issuer issuerOfAccessionNumber;
    private Issuer orderPlacerIdentifier;
    private Issuer orderFillerIdentifier;
    private Issuer issuerOfAdmissionID;
    private Issuer issuerOfServiceEpisodeID;
    private Issuer issuerOfContainerIdentifier;
    private Issuer issuerOfSpecimenIdentifier;
    private String[] softwareVersions = new String[0];
    private String[] primaryDeviceTypes = new String[0];
    private String[] institutionNames = new String[0];
    private Code[] institutionCodes = new Code[0];
    private String[] institutionAddresses = new String[0];
    private String[] institutionalDepartmentNames = new String[0];
    private String[] relatedDeviceRefs = new String[0];
    private byte[][] vendorData = new byte[0][];
    private int limitOpenAssociations;
    private boolean installed = true;
    private boolean roleSelectionNegotiationLenient;
    private TimeZone timeZoneOfDevice;
    private final LinkedHashMap<String, Integer> limitAssociationsInitiatedBy = new LinkedHashMap();
    private final LinkedHashMap<String, X509Certificate[]> authorizedNodeCertificates = new LinkedHashMap();
    private final LinkedHashMap<String, X509Certificate[]> thisNodeCertificates = new LinkedHashMap();
    private final List<Connection> conns = new ArrayList<Connection>();
    private final LinkedHashMap<String, ApplicationEntity> aes = new LinkedHashMap();
    private final LinkedHashMap<String, WebApplication> webapps = new LinkedHashMap();
    private final LinkedHashMap<String, KeycloakClient> keycloakClients = new LinkedHashMap();
    private final Map<Class<? extends DeviceExtension>, DeviceExtension> extensions = new HashMap<Class<? extends DeviceExtension>, DeviceExtension>();
    private transient AssociationHandler associationHandler = new AssociationHandler();
    private transient DimseRQHandler dimseRQHandler;
    private transient ConnectionMonitor connectionMonitor;
    private transient AssociationMonitor associationMonitor;
    private final transient List<Association> associations = new ArrayList<Association>();
    private transient Executor executor;
    private transient ScheduledExecutorService scheduledExecutor;
    private volatile transient SSLContext sslContext;
    private volatile transient KeyManager km;
    private volatile transient TrustManager tm;

    public Device() {
    }

    public Device(String name) {
        this.setDeviceName(name);
    }

    private void checkNotEmpty(String name, String val) {
        if (val != null && val.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
    }

    public final String getDeviceName() {
        return this.deviceName;
    }

    public final void setDeviceName(String name) {
        this.checkNotEmpty("Device Name", name);
        this.deviceName = name;
    }

    public final String getDescription() {
        return this.description;
    }

    public final void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceUID() {
        return this.deviceUID;
    }

    public void setDeviceUID(String deviceUID) {
        this.deviceUID = deviceUID;
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

    public final String[] getSoftwareVersions() {
        return this.softwareVersions;
    }

    public final void setSoftwareVersions(String ... softwareVersions) {
        this.softwareVersions = softwareVersions;
    }

    public final String getStationName() {
        return this.stationName;
    }

    public final void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public final String getDeviceSerialNumber() {
        return this.deviceSerialNumber;
    }

    public final void setDeviceSerialNumber(String deviceSerialNumber) {
        this.deviceSerialNumber = deviceSerialNumber;
    }

    public final String[] getPrimaryDeviceTypes() {
        return this.primaryDeviceTypes;
    }

    public void setPrimaryDeviceTypes(String ... primaryDeviceTypes) {
        this.primaryDeviceTypes = primaryDeviceTypes;
    }

    public final String[] getInstitutionNames() {
        return this.institutionNames;
    }

    public void setInstitutionNames(String ... names) {
        this.institutionNames = names;
    }

    public final Code[] getInstitutionCodes() {
        return this.institutionCodes;
    }

    public void setInstitutionCodes(Code ... codes) {
        this.institutionCodes = codes;
    }

    public final String[] getInstitutionAddresses() {
        return this.institutionAddresses;
    }

    public void setInstitutionAddresses(String ... addresses) {
        this.institutionAddresses = addresses;
    }

    public final String[] getInstitutionalDepartmentNames() {
        return this.institutionalDepartmentNames;
    }

    public void setInstitutionalDepartmentNames(String ... names) {
        this.institutionalDepartmentNames = names;
    }

    public final Issuer getIssuerOfPatientID() {
        return this.issuerOfPatientID;
    }

    public final void setIssuerOfPatientID(Issuer issuerOfPatientID) {
        this.issuerOfPatientID = issuerOfPatientID;
    }

    public final Issuer getIssuerOfAccessionNumber() {
        return this.issuerOfAccessionNumber;
    }

    public final void setIssuerOfAccessionNumber(Issuer issuerOfAccessionNumber) {
        this.issuerOfAccessionNumber = issuerOfAccessionNumber;
    }

    public final Issuer getOrderPlacerIdentifier() {
        return this.orderPlacerIdentifier;
    }

    public final void setOrderPlacerIdentifier(Issuer orderPlacerIdentifier) {
        this.orderPlacerIdentifier = orderPlacerIdentifier;
    }

    public final Issuer getOrderFillerIdentifier() {
        return this.orderFillerIdentifier;
    }

    public final void setOrderFillerIdentifier(Issuer orderFillerIdentifier) {
        this.orderFillerIdentifier = orderFillerIdentifier;
    }

    public final Issuer getIssuerOfAdmissionID() {
        return this.issuerOfAdmissionID;
    }

    public final void setIssuerOfAdmissionID(Issuer issuerOfAdmissionID) {
        this.issuerOfAdmissionID = issuerOfAdmissionID;
    }

    public final Issuer getIssuerOfServiceEpisodeID() {
        return this.issuerOfServiceEpisodeID;
    }

    public final void setIssuerOfServiceEpisodeID(Issuer issuerOfServiceEpisodeID) {
        this.issuerOfServiceEpisodeID = issuerOfServiceEpisodeID;
    }

    public final Issuer getIssuerOfContainerIdentifier() {
        return this.issuerOfContainerIdentifier;
    }

    public final void setIssuerOfContainerIdentifier(Issuer issuerOfContainerIdentifier) {
        this.issuerOfContainerIdentifier = issuerOfContainerIdentifier;
    }

    public final Issuer getIssuerOfSpecimenIdentifier() {
        return this.issuerOfSpecimenIdentifier;
    }

    public final void setIssuerOfSpecimenIdentifier(Issuer issuerOfSpecimenIdentifier) {
        this.issuerOfSpecimenIdentifier = issuerOfSpecimenIdentifier;
    }

    public X509Certificate[] getAuthorizedNodeCertificates(String ref) {
        return this.authorizedNodeCertificates.get(ref);
    }

    public void setAuthorizedNodeCertificates(String ref, X509Certificate ... certs) {
        this.authorizedNodeCertificates.put(ref, certs);
        this.setTrustManager(null);
    }

    public X509Certificate[] removeAuthorizedNodeCertificates(String ref) {
        X509Certificate[] certs = (X509Certificate[])this.authorizedNodeCertificates.remove(ref);
        this.setTrustManager(null);
        return certs;
    }

    public void removeAllAuthorizedNodeCertificates() {
        this.authorizedNodeCertificates.clear();
        this.setTrustManager(null);
    }

    public X509Certificate[] getAllAuthorizedNodeCertificates() {
        return Device.toArray(this.authorizedNodeCertificates.values());
    }

    public String[] getAuthorizedNodeCertificateRefs() {
        return this.authorizedNodeCertificates.keySet().toArray(StringUtils.EMPTY_STRING);
    }

    public final String getTrustStoreURL() {
        return this.trustStoreURL;
    }

    public final void setTrustStoreURL(String trustStoreURL) {
        this.checkNotEmpty("trustStoreURL", trustStoreURL);
        if (trustStoreURL == null ? this.trustStoreURL == null : trustStoreURL.equals(this.trustStoreURL)) {
            return;
        }
        this.trustStoreURL = trustStoreURL;
        this.setTrustManager(null);
    }

    public final String getTrustStoreType() {
        return this.trustStoreType;
    }

    public final void setTrustStoreType(String trustStoreType) {
        this.checkNotEmpty("trustStoreType", trustStoreType);
        this.trustStoreType = trustStoreType;
    }

    public final String getTrustStorePin() {
        return this.trustStorePin;
    }

    public final void setTrustStorePin(String trustStorePin) {
        this.checkNotEmpty("trustStorePin", trustStorePin);
        this.trustStorePin = trustStorePin;
    }

    public final String getTrustStorePinProperty() {
        return this.trustStorePinProperty;
    }

    public final void setTrustStorePinProperty(String trustStorePinProperty) {
        this.checkNotEmpty("trustStorePinProperty", trustStorePinProperty);
        this.trustStorePinProperty = trustStorePinProperty;
    }

    public X509Certificate[] getThisNodeCertificates(String ref) {
        return this.thisNodeCertificates.get(ref);
    }

    public void setThisNodeCertificates(String ref, X509Certificate ... certs) {
        this.thisNodeCertificates.put(ref, certs);
    }

    public X509Certificate[] removeThisNodeCertificates(String ref) {
        return (X509Certificate[])this.thisNodeCertificates.remove(ref);
    }

    public final String getKeyStoreURL() {
        return this.keyStoreURL;
    }

    public final void setKeyStoreURL(String keyStoreURL) {
        this.checkNotEmpty("keyStoreURL", keyStoreURL);
        if (keyStoreURL == null ? this.keyStoreURL == null : keyStoreURL.equals(this.keyStoreURL)) {
            return;
        }
        this.keyStoreURL = keyStoreURL;
        this.setKeyManager(null);
    }

    public final String getKeyStoreType() {
        return this.keyStoreType;
    }

    public final void setKeyStoreType(String keyStoreType) {
        this.checkNotEmpty("keyStoreType", keyStoreType);
        this.keyStoreType = keyStoreType;
    }

    public final String getKeyStorePin() {
        return this.keyStorePin;
    }

    public final void setKeyStorePin(String keyStorePin) {
        this.checkNotEmpty("keyStorePin", keyStorePin);
        this.keyStorePin = keyStorePin;
    }

    public final String getKeyStorePinProperty() {
        return this.keyStorePinProperty;
    }

    public final void setKeyStorePinProperty(String keyStorePinProperty) {
        this.checkNotEmpty("keyStorePinProperty", keyStorePinProperty);
        this.keyStorePinProperty = keyStorePinProperty;
    }

    public final String getKeyStoreKeyPin() {
        return this.keyStoreKeyPin;
    }

    public final void setKeyStoreKeyPin(String keyStorePin) {
        this.checkNotEmpty("keyStoreKeyPin", keyStorePin);
        this.keyStoreKeyPin = keyStorePin;
    }

    public final String getKeyStoreKeyPinProperty() {
        return this.keyStoreKeyPinProperty;
    }

    public final void setKeyStoreKeyPinProperty(String keyStoreKeyPinProperty) {
        this.checkNotEmpty("keyStoreKeyPinProperty", keyStoreKeyPinProperty);
        this.keyStoreKeyPinProperty = keyStoreKeyPinProperty;
    }

    public void removeAllThisNodeCertificates() {
        this.thisNodeCertificates.clear();
    }

    public X509Certificate[] getAllThisNodeCertificates() {
        return Device.toArray(this.thisNodeCertificates.values());
    }

    public String[] getThisNodeCertificateRefs() {
        return this.thisNodeCertificates.keySet().toArray(StringUtils.EMPTY_STRING);
    }

    private static X509Certificate[] toArray(Collection<X509Certificate[]> c) {
        int size = 0;
        for (X509Certificate[] certs : c) {
            size += certs.length;
        }
        X509Certificate[] dest = new X509Certificate[size];
        int destPos = 0;
        for (X509Certificate[] certs : c) {
            System.arraycopy(certs, 0, dest, destPos, certs.length);
            destPos += certs.length;
        }
        return dest;
    }

    public final String[] getRelatedDeviceRefs() {
        return this.relatedDeviceRefs;
    }

    public void setRelatedDeviceRefs(String ... refs) {
        this.relatedDeviceRefs = refs;
    }

    public final byte[][] getVendorData() {
        return this.vendorData;
    }

    public void setVendorData(byte[] ... vendorData) {
        this.vendorData = vendorData;
    }

    public final boolean isInstalled() {
        return this.installed;
    }

    public final void setInstalled(boolean installed) {
        if (this.installed == installed) {
            return;
        }
        this.installed = installed;
        this.needRebindConnections();
    }

    public boolean isRoleSelectionNegotiationLenient() {
        return this.roleSelectionNegotiationLenient;
    }

    public void setRoleSelectionNegotiationLenient(boolean roleSelectionNegotiationLenient) {
        this.roleSelectionNegotiationLenient = roleSelectionNegotiationLenient;
    }

    public void setTimeZoneOfDevice(TimeZone timeZoneOfDevice) {
        this.timeZoneOfDevice = timeZoneOfDevice;
    }

    public TimeZone getTimeZoneOfDevice() {
        return this.timeZoneOfDevice;
    }

    public final void setDimseRQHandler(DimseRQHandler dimseRQHandler) {
        this.dimseRQHandler = dimseRQHandler;
    }

    public final DimseRQHandler getDimseRQHandler() {
        return this.dimseRQHandler;
    }

    public final AssociationHandler getAssociationHandler() {
        return this.associationHandler;
    }

    public void setAssociationHandler(AssociationHandler associationHandler) {
        if (associationHandler == null) {
            throw new NullPointerException();
        }
        this.associationHandler = associationHandler;
    }

    public ConnectionMonitor getConnectionMonitor() {
        return this.connectionMonitor;
    }

    public void setConnectionMonitor(ConnectionMonitor connectionMonitor) {
        this.connectionMonitor = connectionMonitor;
    }

    public AssociationMonitor getAssociationMonitor() {
        return this.associationMonitor;
    }

    public void setAssociationMonitor(AssociationMonitor associationMonitor) {
        this.associationMonitor = associationMonitor;
    }

    public void bindConnections() throws IOException, GeneralSecurityException {
        for (Connection con : this.conns) {
            con.bind();
        }
    }

    public void rebindConnections() throws IOException, GeneralSecurityException {
        for (Connection con : this.conns) {
            if (!con.isRebindNeeded()) continue;
            con.rebind();
        }
    }

    private void needRebindConnections() {
        for (Connection con : this.conns) {
            con.needRebind();
        }
    }

    private void needReconfigureTLS() {
        for (Connection con : this.conns) {
            if (!con.isTls()) continue;
            con.needRebind();
        }
        this.sslContext = null;
    }

    public void unbindConnections() {
        for (Connection con : this.conns) {
            con.unbind();
        }
    }

    public final Executor getExecutor() {
        return this.executor;
    }

    public final void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public final ScheduledExecutorService getScheduledExecutor() {
        return this.scheduledExecutor;
    }

    public final void setScheduledExecutor(ScheduledExecutorService executor) {
        this.scheduledExecutor = executor;
    }

    public void addConnection(Connection conn) {
        conn.setDevice(this);
        this.conns.add(conn);
        conn.needRebind();
    }

    public boolean removeConnection(Connection conn) {
        for (ApplicationEntity ae : this.aes.values()) {
            if (!ae.getConnections().contains(conn)) continue;
            throw new IllegalStateException(conn + " used by AE: " + ae.getAETitle());
        }
        for (DeviceExtension ext : this.extensions.values()) {
            ext.verifyNotUsed(conn);
        }
        if (!this.conns.remove(conn)) {
            return false;
        }
        conn.setDevice(null);
        conn.unbind();
        return true;
    }

    public List<Connection> listConnections() {
        return Collections.unmodifiableList(this.conns);
    }

    public Connection connectionWithEqualsRDN(Connection other) {
        for (Connection conn : this.conns) {
            if (!conn.equalsRDN(other)) continue;
            return conn;
        }
        return null;
    }

    public void addApplicationEntity(ApplicationEntity ae) {
        ae.setDevice(this);
        this.aes.put(ae.getAETitle(), ae);
    }

    public ApplicationEntity removeApplicationEntity(ApplicationEntity ae) {
        return this.removeApplicationEntity(ae.getAETitle());
    }

    public ApplicationEntity removeApplicationEntity(String aet) {
        ApplicationEntity ae = (ApplicationEntity)this.aes.remove(aet);
        if (ae != null) {
            ae.setDevice(null);
        }
        return ae;
    }

    public Collection<String> getWebApplicationNames() {
        return this.webapps.keySet();
    }

    public Collection<WebApplication> getWebApplications() {
        return this.webapps.values();
    }

    public Collection<WebApplication> getWebApplicationsWithServiceClass(WebApplication.ServiceClass serviceClass) {
        ArrayList<WebApplication> result = new ArrayList<WebApplication>(this.webapps.size());
        for (WebApplication webapp : this.webapps.values()) {
            if (!webapp.containsServiceClass(serviceClass)) continue;
            result.add(webapp);
        }
        return result;
    }

    public WebApplication getWebApplication(String name) {
        return this.webapps.get(name);
    }

    public void addWebApplication(WebApplication webapp) {
        webapp.setDevice(this);
        this.webapps.put(webapp.getApplicationName(), webapp);
    }

    public WebApplication removeWebApplication(WebApplication webapp) {
        return this.removeWebApplication(webapp.getApplicationName());
    }

    public WebApplication removeWebApplication(String name) {
        WebApplication webapp = (WebApplication)this.webapps.remove(name);
        if (webapp != null) {
            webapp.setDevice(null);
        }
        return webapp;
    }

    public Collection<String> getKeycloakClientIDs() {
        return this.keycloakClients.keySet();
    }

    public Collection<KeycloakClient> getKeycloakClients() {
        return this.keycloakClients.values();
    }

    public KeycloakClient getKeycloakClient(String clientID) {
        return this.keycloakClients.get(clientID);
    }

    public void addKeycloakClient(KeycloakClient client) {
        client.setDevice(this);
        this.keycloakClients.put(client.getKeycloakClientID(), client);
    }

    public KeycloakClient removeKeycloakClient(KeycloakClient client) {
        return this.removeKeycloakClient(client.getKeycloakClientID());
    }

    public KeycloakClient removeKeycloakClient(String name) {
        KeycloakClient client = (KeycloakClient)this.keycloakClients.remove(name);
        if (client != null) {
            client.setDevice(null);
        }
        return client;
    }

    public void addDeviceExtension(DeviceExtension ext) {
        Class<?> clazz = ext.getClass();
        if (this.extensions.containsKey(clazz)) {
            throw new IllegalStateException("already contains Device Extension:" + clazz);
        }
        ext.setDevice(this);
        this.extensions.put(clazz, ext);
    }

    public boolean removeDeviceExtension(DeviceExtension ext) {
        if (this.extensions.remove(ext.getClass()) == null) {
            return false;
        }
        ext.setDevice(null);
        return true;
    }

    public final int getLimitOpenAssociations() {
        return this.limitOpenAssociations;
    }

    public final void setLimitOpenAssociations(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit: " + limit);
        }
        this.limitOpenAssociations = limit;
    }

    public int getLimitAssociationsInitiatedBy(String callingAET) {
        Integer value = this.limitAssociationsInitiatedBy.get(Objects.requireNonNull(callingAET));
        return value != null ? value : 0;
    }

    public void setLimitAssociationsInitiatedBy(String callingAET, int limit) {
        Objects.requireNonNull(callingAET);
        if (limit < 0) {
            throw new IllegalArgumentException("limit: " + limit);
        }
        if (limit > 0) {
            this.limitAssociationsInitiatedBy.put(callingAET, limit);
        } else {
            this.limitAssociationsInitiatedBy.remove(callingAET);
        }
    }

    public String[] getLimitAssociationsInitiatedBy() {
        String[] ss = new String[this.limitAssociationsInitiatedBy.size()];
        int i = 0;
        for (Map.Entry<String, Integer> entry : this.limitAssociationsInitiatedBy.entrySet()) {
            ss[i++] = entry.getKey() + '=' + entry.getValue();
        }
        return ss;
    }

    public void setLimitAssociationsInitiatedBy(String[] values) {
        HashMap<String, Integer> tmp = new HashMap<String, Integer>();
        for (String value : values) {
            int endIndex = value.lastIndexOf(61);
            try {
                tmp.put(value.substring(0, endIndex), Integer.valueOf(value.substring(endIndex + 1)));
            }
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(value);
            }
        }
        this.setLimitAssociationsInitiatedBy(tmp);
    }

    private void setLimitAssociationsInitiatedBy(Map<String, Integer> tmp) {
        this.limitAssociationsInitiatedBy.clear();
        this.limitAssociationsInitiatedBy.putAll(tmp);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void addAssociation(Association as) {
        List<Association> list = this.associations;
        synchronized (list) {
            this.associations.add(as);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void removeAssociation(Association as) {
        List<Association> list = this.associations;
        synchronized (list) {
            this.associations.remove(as);
            this.associations.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Association[] listOpenAssociations() {
        List<Association> list = this.associations;
        synchronized (list) {
            return this.associations.toArray(new Association[this.associations.size()]);
        }
    }

    public int getNumberOfOpenAssociations() {
        return this.associations.size();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getNumberOfAssociationsInitiatedBy(String callingAET) {
        List<Association> list = this.associations;
        synchronized (list) {
            int count = 0;
            for (Association association : this.associations) {
                if (!callingAET.equals(association.getCallingAET())) continue;
                ++count;
            }
            return count;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getNumberOfAssociationsInitiatedTo(String calledAET) {
        List<Association> list = this.associations;
        synchronized (list) {
            int count = 0;
            for (Association association : this.associations) {
                if (!calledAET.equals(association.getCalledAET())) continue;
                ++count;
            }
            return count;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitForNoOpenConnections() throws InterruptedException {
        List<Association> list = this.associations;
        synchronized (list) {
            while (!this.associations.isEmpty()) {
                this.associations.wait();
            }
        }
    }

    public boolean isLimitOfAssociationsExceeded(AAssociateRQ rq) {
        Integer limit;
        return this.limitOpenAssociations > 0 && this.associations.size() > this.limitOpenAssociations || (limit = this.limitAssociationsInitiatedBy.get(rq.getCallingAET())) != null && this.getNumberOfAssociationsInitiatedBy(rq.getCallingAET()) > limit;
    }

    public ApplicationEntity getApplicationEntity(String aet) {
        return this.aes.get(aet);
    }

    public ApplicationEntity getApplicationEntity(String aet, boolean matchOtherAETs) {
        ApplicationEntity ae = this.aes.get(aet);
        if (ae == null) {
            ae = this.aes.get("*");
        }
        if (ae == null && matchOtherAETs) {
            for (ApplicationEntity ae1 : this.getApplicationEntities()) {
                if (!ae1.isOtherAETitle(aet)) continue;
                return ae1;
            }
        }
        return ae;
    }

    public Collection<String> getApplicationAETitles() {
        return this.aes.keySet();
    }

    public Collection<ApplicationEntity> getApplicationEntities() {
        return this.aes.values();
    }

    public final void setKeyManager(KeyManager km) {
        this.km = km;
        this.needReconfigureTLS();
    }

    public final KeyManager getKeyManager() {
        return this.km;
    }

    private KeyManager km() throws GeneralSecurityException, IOException {
        KeyManager ret = this.km;
        if (ret != null || this.keyStoreURL == null) {
            return ret;
        }
        String keyStorePin = this.keyStorePin();
        this.km = ret = SSLManagerFactory.createKeyManager(StringUtils.replaceSystemProperties(this.keyStoreType()), StringUtils.replaceSystemProperties(this.keyStoreURL), StringUtils.replaceSystemProperties(this.keyStorePin()), StringUtils.replaceSystemProperties(this.keyPin(keyStorePin)));
        return ret;
    }

    private String keyStoreType() {
        if (this.keyStoreType == null) {
            throw new IllegalStateException("keyStoreURL requires keyStoreType");
        }
        return this.keyStoreType;
    }

    private String keyStorePin() {
        if (this.keyStorePin != null) {
            return this.keyStorePin;
        }
        if (this.keyStorePinProperty == null) {
            throw new IllegalStateException("keyStoreURL requires keyStorePin or keyStorePinProperty");
        }
        String pin = System.getProperty(this.keyStorePinProperty);
        if (pin == null) {
            throw new IllegalStateException("No such keyStorePinProperty: " + this.keyStorePinProperty);
        }
        return pin;
    }

    private String keyPin(String keyStorePin) {
        if (this.keyStoreKeyPin != null) {
            return this.keyStoreKeyPin;
        }
        if (this.keyStoreKeyPinProperty == null) {
            return keyStorePin;
        }
        String pin = System.getProperty(this.keyStoreKeyPinProperty);
        if (pin == null) {
            throw new IllegalStateException("No such keyPinProperty: " + this.keyStoreKeyPinProperty);
        }
        return pin;
    }

    public final void setTrustManager(TrustManager tm) {
        this.tm = tm;
        this.needReconfigureTLS();
    }

    public final TrustManager getTrustManager() {
        return this.tm;
    }

    private TrustManager tm() throws GeneralSecurityException, IOException {
        TrustManager ret = this.tm;
        if (ret != null || this.trustStoreURL == null && this.authorizedNodeCertificates.isEmpty()) {
            return ret;
        }
        ret = this.trustStoreURL != null ? SSLManagerFactory.createTrustManager(StringUtils.replaceSystemProperties(this.trustStoreType()), StringUtils.replaceSystemProperties(this.trustStoreURL), StringUtils.replaceSystemProperties(this.trustStorePin())) : SSLManagerFactory.createTrustManager(this.getAllAuthorizedNodeCertificates());
        this.tm = ret;
        return ret;
    }

    private String trustStoreType() {
        if (this.trustStoreType == null) {
            throw new IllegalStateException("trustStoreURL requires trustStoreType");
        }
        return this.trustStoreType;
    }

    private String trustStorePin() {
        if (this.trustStorePin != null) {
            return this.trustStorePin;
        }
        if (this.trustStorePinProperty == null) {
            throw new IllegalStateException("trustStoreURL requires trustStorePin or trustStorePinProperty");
        }
        String pin = System.getProperty(this.trustStorePinProperty);
        if (pin == null) {
            throw new IllegalStateException("No such trustStorePinProperty: " + this.trustStorePinProperty);
        }
        return pin;
    }

    public SSLContext sslContext() throws GeneralSecurityException, IOException {
        SSLContext ctx = this.sslContext;
        if (ctx != null) {
            return ctx;
        }
        ctx = SSLContext.getInstance("TLS");
        ctx.init(this.keyManagers(), this.trustManagers(), null);
        this.sslContext = ctx;
        return ctx;
    }

    public KeyManager[] keyManagers() throws GeneralSecurityException, IOException {
        KeyManager[] keyManagerArray;
        KeyManager tmp = this.km();
        if (tmp != null) {
            KeyManager[] keyManagerArray2 = new KeyManager[1];
            keyManagerArray = keyManagerArray2;
            keyManagerArray2[0] = tmp;
        } else {
            keyManagerArray = null;
        }
        return keyManagerArray;
    }

    public TrustManager[] trustManagers() throws GeneralSecurityException, IOException {
        TrustManager[] trustManagerArray;
        TrustManager tmp = this.tm();
        if (tmp != null) {
            TrustManager[] trustManagerArray2 = new TrustManager[1];
            trustManagerArray = trustManagerArray2;
            trustManagerArray2[0] = tmp;
        } else {
            trustManagerArray = null;
        }
        return trustManagerArray;
    }

    public void execute(Runnable command) {
        if (this.executor == null) {
            throw new IllegalStateException("executor not initialized");
        }
        this.executor.execute(command);
    }

    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        if (this.scheduledExecutor == null) {
            throw new IllegalStateException("scheduled executor service not initialized");
        }
        return this.scheduledExecutor.schedule(command, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        if (this.scheduledExecutor == null) {
            throw new IllegalStateException("scheduled executor service not initialized");
        }
        return this.scheduledExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        if (this.scheduledExecutor == null) {
            throw new IllegalStateException("scheduled executor service not initialized");
        }
        return this.scheduledExecutor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    public String toString() {
        return this.promptTo(new StringBuilder(512), "").toString();
    }

    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + "  ";
        StringUtils.appendLine(sb, indent, "Device[name: ", this.deviceName);
        StringUtils.appendLine(sb, indent2, "desc: ", this.description);
        StringUtils.appendLine(sb, indent2, "installed: ", this.installed);
        for (Connection conn : this.conns) {
            conn.promptTo(sb, indent2).append(StringUtils.LINE_SEPARATOR);
        }
        for (ApplicationEntity ae : this.aes.values()) {
            ae.promptTo(sb, indent2).append(StringUtils.LINE_SEPARATOR);
        }
        return sb.append(indent).append(']');
    }

    public void reconfigure(Device from) throws IOException, GeneralSecurityException {
        this.setDeviceAttributes(from);
        this.reconfigureConnections(from);
        this.reconfigureApplicationEntities(from);
        this.reconfigureWebApplications(from);
        this.reconfigureKeycloakClients(from);
        this.reconfigureDeviceExtensions(from);
    }

    protected void setDeviceAttributes(Device from) {
        this.setDescription(from.description);
        this.setDeviceUID(from.deviceUID);
        this.setManufacturer(from.manufacturer);
        this.setManufacturerModelName(from.manufacturerModelName);
        this.setSoftwareVersions(from.softwareVersions);
        this.setStationName(from.stationName);
        this.setDeviceSerialNumber(from.deviceSerialNumber);
        this.setTrustStoreURL(from.trustStoreURL);
        this.setTrustStoreType(from.trustStoreType);
        this.setTrustStorePin(from.trustStorePin);
        this.setKeyStoreURL(from.keyStoreURL);
        this.setKeyStoreType(from.keyStoreType);
        this.setKeyStorePin(from.keyStorePin);
        this.setKeyStoreKeyPin(from.keyStoreKeyPin);
        this.setTimeZoneOfDevice(from.timeZoneOfDevice);
        this.setIssuerOfPatientID(from.issuerOfPatientID);
        this.setIssuerOfAccessionNumber(from.issuerOfAccessionNumber);
        this.setOrderPlacerIdentifier(from.orderPlacerIdentifier);
        this.setOrderFillerIdentifier(from.orderFillerIdentifier);
        this.setIssuerOfAdmissionID(from.issuerOfAdmissionID);
        this.setIssuerOfServiceEpisodeID(from.issuerOfServiceEpisodeID);
        this.setIssuerOfContainerIdentifier(from.issuerOfContainerIdentifier);
        this.setIssuerOfSpecimenIdentifier(from.issuerOfSpecimenIdentifier);
        this.setInstitutionNames(from.institutionNames);
        this.setInstitutionCodes(from.institutionCodes);
        this.setInstitutionAddresses(from.institutionAddresses);
        this.setInstitutionalDepartmentNames(from.institutionalDepartmentNames);
        this.setPrimaryDeviceTypes(from.primaryDeviceTypes);
        this.setRelatedDeviceRefs(from.relatedDeviceRefs);
        this.setAuthorizedNodeCertificates(from.authorizedNodeCertificates);
        this.setThisNodeCertificates(from.thisNodeCertificates);
        this.setVendorData(from.vendorData);
        this.setLimitOpenAssociations(from.limitOpenAssociations);
        this.setInstalled(from.installed);
        this.setLimitAssociationsInitiatedBy(from.limitAssociationsInitiatedBy);
        this.setRoleSelectionNegotiationLenient(from.roleSelectionNegotiationLenient);
    }

    private void setAuthorizedNodeCertificates(Map<String, X509Certificate[]> from) {
        if (this.update(this.authorizedNodeCertificates, from)) {
            this.setTrustManager(null);
        }
    }

    private void setThisNodeCertificates(Map<String, X509Certificate[]> from) {
        this.update(this.thisNodeCertificates, from);
    }

    private boolean update(Map<String, X509Certificate[]> target, Map<String, X509Certificate[]> from) {
        boolean updated = target.keySet().retainAll(from.keySet());
        for (Map.Entry<String, X509Certificate[]> e : from.entrySet()) {
            String key = e.getKey();
            Object[] value = e.getValue();
            Object[] certs = target.get(key);
            if (certs != null && Arrays.equals(value, certs)) continue;
            target.put(key, (X509Certificate[])value);
            updated = true;
        }
        return updated;
    }

    private void reconfigureConnections(Device from) {
        Iterator<Connection> connIter = this.conns.iterator();
        while (connIter.hasNext()) {
            Connection conn = connIter.next();
            if (from.connectionWithEqualsRDN(conn) != null) continue;
            connIter.remove();
            conn.setDevice(null);
            conn.unbind();
        }
        for (Connection src : from.conns) {
            Connection conn = this.connectionWithEqualsRDN(src);
            if (conn == null) {
                conn = new Connection();
                this.addConnection(conn);
            }
            conn.reconfigure(src);
        }
    }

    private void reconfigureApplicationEntities(Device from) {
        this.aes.keySet().retainAll(from.aes.keySet());
        for (ApplicationEntity src : from.aes.values()) {
            ApplicationEntity ae = this.aes.get(src.getAETitle());
            if (ae == null) {
                ae = new ApplicationEntity(src.getAETitle());
                this.addApplicationEntity(ae);
            }
            ae.reconfigure(src);
        }
    }

    private void reconfigureWebApplications(Device from) {
        this.webapps.keySet().retainAll(from.webapps.keySet());
        for (WebApplication src : from.webapps.values()) {
            WebApplication webapp = this.webapps.get(src.getApplicationName());
            if (webapp == null) {
                webapp = new WebApplication(src.getApplicationName());
                this.addWebApplication(webapp);
            }
            webapp.reconfigure(src);
        }
    }

    private void reconfigureKeycloakClients(Device from) {
        this.keycloakClients.keySet().retainAll(from.keycloakClients.keySet());
        for (KeycloakClient src : from.keycloakClients.values()) {
            KeycloakClient client = this.keycloakClients.get(src.getKeycloakClientID());
            if (client == null) {
                client = new KeycloakClient(src.getKeycloakClientID());
                this.addKeycloakClient(client);
            }
            client.reconfigure(src);
        }
    }

    public void reconfigureConnections(List<Connection> conns, List<Connection> src) {
        conns.clear();
        for (Connection conn : src) {
            conns.add(this.connectionWithEqualsRDN(conn));
        }
    }

    private void reconfigureDeviceExtensions(Device from) {
        Iterator<Class<? extends DeviceExtension>> it = this.extensions.keySet().iterator();
        while (it.hasNext()) {
            if (from.extensions.containsKey(it.next())) continue;
            it.remove();
        }
        for (DeviceExtension src : from.extensions.values()) {
            Class<?> clazz = src.getClass();
            DeviceExtension ext = this.extensions.get(clazz);
            if (ext == null) {
                try {
                    ext = (DeviceExtension)clazz.newInstance();
                    this.addDeviceExtension(ext);
                }
                catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
                }
            }
            ext.reconfigure(src);
        }
    }

    public Collection<DeviceExtension> listDeviceExtensions() {
        return this.extensions.values();
    }

    public <T extends DeviceExtension> T getDeviceExtension(Class<T> clazz) {
        return (T)this.extensions.get(clazz);
    }

    public <T extends DeviceExtension> T getDeviceExtensionNotNull(Class<T> clazz) {
        T devExt = this.getDeviceExtension(clazz);
        if (devExt == null) {
            throw new IllegalStateException("No " + clazz.getName() + " configured for Device: " + this.deviceName);
        }
        return devExt;
    }
}

