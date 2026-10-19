/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceServiceInterface;

public class DeviceService
implements DeviceServiceInterface {
    protected Device device;
    protected ExecutorService executor;
    protected ScheduledExecutorService scheduledExecutor;

    protected void init(Device device) {
        this.setDevice(device);
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    @Override
    public Device getDevice() {
        return this.device;
    }

    @Override
    public boolean isRunning() {
        return this.executor != null;
    }

    @Override
    public void start() throws Exception {
        if (this.device == null) {
            throw new IllegalStateException("Not initialized");
        }
        if (this.executor != null) {
            throw new IllegalStateException("Already started");
        }
        this.executor = this.executerService();
        this.scheduledExecutor = this.scheduledExecuterService();
        try {
            this.device.setExecutor(this.executor);
            this.device.setScheduledExecutor(this.scheduledExecutor);
            this.device.bindConnections();
        }
        catch (Exception e) {
            this.stop();
            throw e;
        }
    }

    @Override
    public void stop() {
        if (this.device != null) {
            this.device.unbindConnections();
        }
        if (this.scheduledExecutor != null) {
            this.scheduledExecutor.shutdown();
        }
        if (this.executor != null) {
            this.executor.shutdown();
        }
        this.executor = null;
        this.scheduledExecutor = null;
    }

    protected ExecutorService executerService() {
        return Executors.newCachedThreadPool();
    }

    protected ScheduledExecutorService scheduledExecuterService() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}

