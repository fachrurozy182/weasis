/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.param;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.dcm4che3.net.Device;

public class DeviceListenerService {
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 10;
    private final Device device;
    private final AtomicReference<ExecutorService> executor = new AtomicReference();
    private final AtomicReference<ScheduledExecutorService> scheduledExecutor = new AtomicReference();

    public DeviceListenerService(Device device) {
        this.device = Objects.requireNonNull(device, "Device must not be null");
    }

    public Device getDevice() {
        return this.device;
    }

    public boolean isRunning() {
        ExecutorService exec = this.executor.get();
        return exec != null && !exec.isShutdown();
    }

    public synchronized void start() throws IOException, GeneralSecurityException {
        if (this.isRunning()) {
            throw new IllegalStateException("Service is already running");
        }
        this.initializeExecutors();
        this.configureDevice();
        try {
            this.device.bindConnections();
        }
        catch (IOException | GeneralSecurityException e) {
            this.stop();
            throw e;
        }
    }

    public synchronized void stop() {
        this.unbindDeviceConnections();
        this.shutdownExecutors();
        this.clearExecutorReferences();
    }

    private void initializeExecutors() {
        this.executor.set(Executors.newCachedThreadPool());
        this.scheduledExecutor.set(Executors.newSingleThreadScheduledExecutor());
    }

    private void configureDevice() {
        this.device.setExecutor(this.executor.get());
        this.device.setScheduledExecutor(this.scheduledExecutor.get());
    }

    private void unbindDeviceConnections() {
        try {
            this.device.unbindConnections();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void shutdownExecutors() {
        this.shutdownExecutor(this.scheduledExecutor.get());
        this.shutdownExecutor(this.executor.get());
    }

    private void shutdownExecutor(ExecutorService executorService) {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10L, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            }
            catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void clearExecutorReferences() {
        this.executor.set(null);
        this.scheduledExecutor.set(null);
    }
}

