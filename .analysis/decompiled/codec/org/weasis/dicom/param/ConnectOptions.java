/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.param;

public final class ConnectOptions {
    private int maxOpsInvoked = 1;
    private int maxOpsPerformed = 1;
    private int maxPdulenRcv = 16378;
    private int maxPdulenSnd = 16378;
    private boolean packPDV = true;
    private int backlog = 50;
    private boolean tcpNoDelay = true;
    private int sosndBuffer = 0;
    private int sorcvBuffer = 0;
    private int socloseDelay = 50;
    private int connectTimeout = 0;
    private int requestTimeout = 0;
    private int acceptTimeout = 0;
    private int releaseTimeout = 0;
    private int responseTimeout = 0;
    private int retrieveTimeout = 0;
    private int idleTimeout = 0;

    public int getMaxOpsInvoked() {
        return this.maxOpsInvoked;
    }

    public void setMaxOpsInvoked(int maxOpsInvoked) {
        this.maxOpsInvoked = maxOpsInvoked;
    }

    public int getMaxOpsPerformed() {
        return this.maxOpsPerformed;
    }

    public void setMaxOpsPerformed(int maxOpsPerformed) {
        this.maxOpsPerformed = maxOpsPerformed;
    }

    public int getMaxPdulenRcv() {
        return this.maxPdulenRcv;
    }

    public void setMaxPdulenRcv(int maxPdulenRcv) {
        this.maxPdulenRcv = maxPdulenRcv;
    }

    public int getMaxPdulenSnd() {
        return this.maxPdulenSnd;
    }

    public void setMaxPdulenSnd(int maxPdulenSnd) {
        this.maxPdulenSnd = maxPdulenSnd;
    }

    public boolean isPackPDV() {
        return this.packPDV;
    }

    public void setPackPDV(boolean packPDV) {
        this.packPDV = packPDV;
    }

    public int getBacklog() {
        return this.backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public boolean isTcpNoDelay() {
        return this.tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public int getSosndBuffer() {
        return this.sosndBuffer;
    }

    public void setSosndBuffer(int sosndBuffer) {
        this.sosndBuffer = sosndBuffer;
    }

    public int getSorcvBuffer() {
        return this.sorcvBuffer;
    }

    public void setSorcvBuffer(int sorcvBuffer) {
        this.sorcvBuffer = sorcvBuffer;
    }

    public int getSocloseDelay() {
        return this.socloseDelay;
    }

    public void setSocloseDelay(int socloseDelay) {
        this.socloseDelay = socloseDelay;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getRequestTimeout() {
        return this.requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public int getAcceptTimeout() {
        return this.acceptTimeout;
    }

    public void setAcceptTimeout(int acceptTimeout) {
        this.acceptTimeout = acceptTimeout;
    }

    public int getReleaseTimeout() {
        return this.releaseTimeout;
    }

    public void setReleaseTimeout(int releaseTimeout) {
        this.releaseTimeout = releaseTimeout;
    }

    public int getResponseTimeout() {
        return this.responseTimeout;
    }

    public void setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public int getRetrieveTimeout() {
        return this.retrieveTimeout;
    }

    public void setRetrieveTimeout(int retrieveTimeout) {
        this.retrieveTimeout = retrieveTimeout;
    }

    public int getIdleTimeout() {
        return this.idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }
}

