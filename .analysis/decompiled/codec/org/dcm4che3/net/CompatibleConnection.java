/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import org.dcm4che3.net.Connection;

public class CompatibleConnection {
    private final Connection localConn;
    private final Connection remoteConn;

    public CompatibleConnection(Connection localConn, Connection remoteConn) {
        this.localConn = localConn;
        this.remoteConn = remoteConn;
    }

    public final Connection getLocalConnection() {
        return this.localConn;
    }

    public final Connection getRemoteConnection() {
        return this.remoteConn;
    }
}

