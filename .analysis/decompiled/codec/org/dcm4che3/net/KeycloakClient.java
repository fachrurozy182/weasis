/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import org.dcm4che3.net.Device;

public class KeycloakClient {
    private Device device;
    private String keycloakClientID;
    private String keycloakServerURL;
    private String keycloakRealm;
    private String keycloakClientSecret;
    private String userID;
    private String password;
    private GrantType keycloakGrantType = GrantType.client_credentials;
    private boolean tlsAllowAnyHostname;
    private boolean tlsDisableTrustManager;

    public KeycloakClient() {
    }

    public KeycloakClient(String keycloakClientID) {
        this.setKeycloakClientID(keycloakClientID);
    }

    public Device getDevice() {
        return this.device;
    }

    void setDevice(Device device) {
        if (device != null && this.device != null) {
            throw new IllegalStateException("already owned by " + this.device.getDeviceName());
        }
        this.device = device;
    }

    public String getKeycloakClientID() {
        return this.keycloakClientID;
    }

    public void setKeycloakClientID(String keycloakClientID) {
        this.keycloakClientID = keycloakClientID;
    }

    public String getKeycloakServerURL() {
        return this.keycloakServerURL;
    }

    public void setKeycloakServerURL(String keycloakServerURL) {
        this.keycloakServerURL = keycloakServerURL;
    }

    public String getKeycloakRealm() {
        return this.keycloakRealm;
    }

    public void setKeycloakRealm(String keycloakRealm) {
        this.keycloakRealm = keycloakRealm;
    }

    public GrantType getKeycloakGrantType() {
        return this.keycloakGrantType;
    }

    public void setKeycloakGrantType(GrantType keycloakGrantType) {
        this.keycloakGrantType = keycloakGrantType;
    }

    public String getKeycloakClientSecret() {
        return this.keycloakClientSecret;
    }

    public void setKeycloakClientSecret(String keycloakClientSecret) {
        this.keycloakClientSecret = keycloakClientSecret;
    }

    public String getUserID() {
        return this.userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isTLSAllowAnyHostname() {
        return this.tlsAllowAnyHostname;
    }

    public void setTLSAllowAnyHostname(boolean tlsAllowAnyHostname) {
        this.tlsAllowAnyHostname = tlsAllowAnyHostname;
    }

    public boolean isTLSDisableTrustManager() {
        return this.tlsDisableTrustManager;
    }

    public void setTLSDisableTrustManager(boolean tlsDisableTrustManager) {
        this.tlsDisableTrustManager = tlsDisableTrustManager;
    }

    public KeycloakClient clone() {
        KeycloakClient clone = new KeycloakClient();
        clone.device = this.device;
        clone.keycloakClientID = this.keycloakClientID;
        clone.keycloakServerURL = this.keycloakServerURL;
        clone.keycloakRealm = this.keycloakRealm;
        clone.keycloakClientSecret = this.keycloakClientSecret;
        clone.userID = this.userID;
        clone.password = this.password;
        clone.keycloakGrantType = this.keycloakGrantType;
        clone.tlsAllowAnyHostname = this.tlsAllowAnyHostname;
        clone.tlsDisableTrustManager = this.tlsDisableTrustManager;
        return clone;
    }

    public void reconfigure(KeycloakClient src) {
        this.keycloakServerURL = src.keycloakServerURL;
        this.keycloakRealm = src.keycloakRealm;
        this.keycloakGrantType = src.keycloakGrantType;
        this.keycloakClientSecret = src.keycloakClientSecret;
        this.userID = src.userID;
        this.password = src.password;
        this.tlsAllowAnyHostname = src.tlsAllowAnyHostname;
        this.tlsDisableTrustManager = src.tlsDisableTrustManager;
    }

    public String toString() {
        return "KeycloakClient[keycloakClientID=" + this.keycloakClientID + ",keycloakServerURL=" + this.keycloakServerURL + ",keycloakRealm=" + this.keycloakRealm + ",keycloakGrantType=" + (Object)((Object)this.keycloakGrantType) + ",keycloakClientSecret=" + this.keycloakClientSecret + ",userID=" + this.userID + ",password=" + this.password + ",tlsAllowAnyHostname=" + this.tlsAllowAnyHostname + ",tlsDisableTrustManager=" + this.tlsDisableTrustManager + ']';
    }

    public static enum GrantType {
        client_credentials,
        password;

    }
}

