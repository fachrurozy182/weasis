/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.param;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.SSLManagerFactory;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.UserIdentityRQ;
import org.weasis.dicom.param.ConnectOptions;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.TlsOptions;

public class AdvancedParams {
    public static final String[] IVR_LE_FIRST = new String[]{"1.2.840.10008.1.2", "1.2.840.10008.1.2.1", "1.2.840.10008.1.2.2"};
    public static final String[] EVR_LE_FIRST = new String[]{"1.2.840.10008.1.2.1", "1.2.840.10008.1.2.2", "1.2.840.10008.1.2"};
    public static final String[] EVR_BE_FIRST = new String[]{"1.2.840.10008.1.2.2", "1.2.840.10008.1.2.1", "1.2.840.10008.1.2"};
    public static final String[] IVR_LE_ONLY = new String[]{"1.2.840.10008.1.2"};
    private Object informationModel;
    private final EnumSet<QueryOption> queryOptions = EnumSet.noneOf(QueryOption.class);
    private String[] tsuidOrder = (String[])IVR_LE_FIRST.clone();
    private String proxy;
    private UserIdentityRQ identity;
    private int priority = 0;
    private ConnectOptions connectOptions;
    private TlsOptions tlsOptions;

    public Object getInformationModel() {
        return this.informationModel;
    }

    public void setInformationModel(Object informationModel) {
        this.informationModel = informationModel;
    }

    public EnumSet<QueryOption> getQueryOptions() {
        return this.queryOptions;
    }

    public void setQueryOptions(EnumSet<QueryOption> queryOptions) {
        this.queryOptions.clear();
        if (queryOptions != null) {
            this.queryOptions.addAll(queryOptions);
        }
    }

    public String[] getTsuidOrder() {
        return (String[])this.tsuidOrder.clone();
    }

    public void setTsuidOrder(String[] tsuidOrder) {
        this.tsuidOrder = (String[])Objects.requireNonNull(tsuidOrder, "tsuidOrder cannot be null").clone();
    }

    public String getProxy() {
        return this.proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public UserIdentityRQ getIdentity() {
        return this.identity;
    }

    public void setIdentity(UserIdentityRQ identity) {
        this.identity = identity;
    }

    public ConnectOptions getConnectOptions() {
        return this.connectOptions;
    }

    public void setConnectOptions(ConnectOptions connectOptions) {
        this.connectOptions = connectOptions;
    }

    public TlsOptions getTlsOptions() {
        return this.tlsOptions;
    }

    public void setTlsOptions(TlsOptions tlsOptions) {
        this.tlsOptions = tlsOptions;
    }

    public void configureConnect(AAssociateRQ aAssociateRQ, Connection remote, DicomNode calledNode) {
        aAssociateRQ.setCalledAET(calledNode.getAet());
        Optional.ofNullable(this.identity).ifPresent(aAssociateRQ::setUserIdentityRQ);
        remote.setHostname(calledNode.getHostname());
        remote.setPort(calledNode.getPort());
    }

    public void configureBind(Connection connection, DicomNode callingNode) {
        Optional.ofNullable(callingNode.getHostname()).ifPresent(connection::setHostname);
        Optional.ofNullable(callingNode.getPort()).ifPresent(connection::setPort);
    }

    public void configureBind(ApplicationEntity applicationEntity, Connection connection, DicomNode callingNode) {
        applicationEntity.setAETitle(callingNode.getAet());
        this.configureBind(connection, callingNode);
    }

    public void configure(Connection conn) throws IOException {
        if (this.connectOptions == null) {
            return;
        }
        this.configureBasicOptions(conn);
        this.configureBufferOptions(conn);
        this.configurePduOptions(conn);
        this.configureOperationOptions(conn);
    }

    private void configureBasicOptions(Connection conn) {
        conn.setBacklog(this.connectOptions.getBacklog());
        conn.setConnectTimeout(this.connectOptions.getConnectTimeout());
        conn.setRequestTimeout(this.connectOptions.getRequestTimeout());
        conn.setAcceptTimeout(this.connectOptions.getAcceptTimeout());
        conn.setReleaseTimeout(this.connectOptions.getReleaseTimeout());
        conn.setResponseTimeout(this.connectOptions.getResponseTimeout());
        conn.setRetrieveTimeout(this.connectOptions.getRetrieveTimeout());
        conn.setIdleTimeout(this.connectOptions.getIdleTimeout());
        conn.setSocketCloseDelay(this.connectOptions.getSocloseDelay());
    }

    private void configureBufferOptions(Connection conn) {
        conn.setReceiveBufferSize(this.connectOptions.getSorcvBuffer());
        conn.setSendBufferSize(this.connectOptions.getSosndBuffer());
        conn.setPackPDV(this.connectOptions.isPackPDV());
        conn.setTcpNoDelay(this.connectOptions.isTcpNoDelay());
    }

    private void configurePduOptions(Connection conn) {
        conn.setReceivePDULength(this.connectOptions.getMaxPdulenRcv());
        conn.setSendPDULength(this.connectOptions.getMaxPdulenSnd());
    }

    private void configureOperationOptions(Connection conn) {
        conn.setMaxOpsInvoked(this.connectOptions.getMaxOpsInvoked());
        conn.setMaxOpsPerformed(this.connectOptions.getMaxOpsPerformed());
    }

    public void configureTLS(Connection conn, Connection remote) throws IOException {
        if (this.tlsOptions == null) {
            return;
        }
        this.configureTlsProtocols(conn);
        this.configureDeviceSecurity(conn.getDevice());
        this.syncRemoteConnection(conn, remote);
    }

    private void configureTlsProtocols(Connection conn) {
        Optional.of(this.tlsOptions.cipherSuites()).ifPresent(suites -> conn.setTlsCipherSuites((String[])suites.toArray(String[]::new)));
        Optional.ofNullable(this.tlsOptions.tlsProtocols()).ifPresent(protocols -> conn.setTlsProtocols((String[])protocols.toArray(String[]::new)));
        conn.setTlsNeedClientAuth(this.tlsOptions.tlsNeedClientAuth());
    }

    private void configureDeviceSecurity(Device device) throws IOException {
        try {
            device.setKeyManager(SSLManagerFactory.createKeyManager(this.tlsOptions.keystoreType(), this.tlsOptions.keystoreURL(), this.tlsOptions.keystorePass(), this.tlsOptions.keyPass()));
            device.setTrustManager(SSLManagerFactory.createTrustManager(this.tlsOptions.truststoreType(), this.tlsOptions.truststoreURL(), this.tlsOptions.truststorePass()));
        }
        catch (GeneralSecurityException e) {
            throw new IOException("Failed to configure TLS security", e);
        }
    }

    private void syncRemoteConnection(Connection conn, Connection remote) {
        Optional.ofNullable(remote).ifPresent(r -> {
            r.setTlsProtocols(conn.getTlsProtocols());
            r.setTlsCipherSuites(conn.getTlsCipherSuites());
        });
    }
}

