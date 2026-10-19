/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.net.Socket;
import org.dcm4che3.net.Connection;

public interface ConnectionMonitor {
    public void onConnectionEstablished(Connection var1, Connection var2, Socket var3);

    public void onConnectionFailed(Connection var1, Connection var2, Socket var3, Throwable var4);

    public void onConnectionRejectedBlacklisted(Connection var1, Socket var2);

    public void onConnectionRejected(Connection var1, Socket var2, Throwable var3);

    public void onConnectionAccepted(Connection var1, Socket var2);
}

