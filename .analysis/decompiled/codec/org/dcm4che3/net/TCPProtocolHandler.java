/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import java.net.Socket;
import org.dcm4che3.net.Connection;

public interface TCPProtocolHandler {
    public void onAccept(Connection var1, Socket var2) throws IOException;
}

