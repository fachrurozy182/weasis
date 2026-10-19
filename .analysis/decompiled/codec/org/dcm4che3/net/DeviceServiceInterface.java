/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import org.dcm4che3.net.Device;

public interface DeviceServiceInterface {
    public Device getDevice();

    public boolean isRunning();

    public void start() throws Exception;

    public void stop();
}

