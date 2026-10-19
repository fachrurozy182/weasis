/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.Serializable;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;

public class DeviceExtension
implements Serializable {
    private static final long serialVersionUID = -548874586138178220L;
    protected Device device;

    public final Device getDevice() {
        return this.device;
    }

    void setDevice(Device device) {
        if (device != null && this.device != null) {
            throw new IllegalStateException("already owned by Device: " + device.getDeviceName());
        }
        this.device = device;
    }

    public void verifyNotUsed(Connection conn) {
    }

    public void reconfigure(DeviceExtension from) {
    }
}

