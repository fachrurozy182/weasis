/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import java.net.Socket;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.TCPProtocolHandler;

enum DicomProtocolHandler implements TCPProtocolHandler
{
    INSTANCE;


    @Override
    public void onAccept(Connection conn, Socket s) throws IOException {
        new Association(null, conn, s);
    }
}

