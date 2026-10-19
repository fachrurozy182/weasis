/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.net.DatagramPacket;
import org.dcm4che3.net.Connection;

public interface UDPProtocolHandler {
    public void onReceive(Connection var1, DatagramPacket var2);
}

