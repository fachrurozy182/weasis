/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.Closeable;
import java.net.SocketAddress;

public interface Listener
extends Closeable {
    public SocketAddress getEndPoint();
}

