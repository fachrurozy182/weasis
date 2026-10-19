/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.dicom.param;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.dcm4che3.net.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.util.ServiceUtil;

public class DeviceOpService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceOpService.class);
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 30L;
    protected final Device device;
    private final AtomicReference<ExecutorService> executor = new AtomicReference();
    private final AtomicReference<ScheduledExecutorService> scheduledExecutor = new AtomicReference();

    public DeviceOpService(Device device) {
        this.device = Objects.requireNonNull(device, "Device cannot be null");
    }

    public Device getDevice() {
        return this.device;
    }

    public boolean isRunning() {
        ExecutorService exec = this.executor.get();
        return exec != null && !exec.isShutdown();
    }

    public synchronized void start() {
        if (this.isRunning()) {
            LOGGER.debug("Device service is already running");
            return;
        }
        this.createExecutorServices();
        this.configureDevice();
        LOGGER.info("Device service started for device: {}", (Object)this.device.getDeviceName());
    }

    public synchronized void stop() {
        if (!this.isRunning()) {
            LOGGER.debug("Device service is not running");
            return;
        }
        this.shutdownExecutorServices();
        this.clearExecutorReferences();
        LOGGER.info("Device service stopped for device: {}", (Object)this.device.getDeviceName());
    }

    private void createExecutorServices() {
        String deviceName = this.device.getDeviceName();
        this.executor.set(Executors.newSingleThreadExecutor(ServiceUtil.getThreadFactory(deviceName + "-executor")));
        this.scheduledExecutor.set(Executors.newSingleThreadScheduledExecutor(ServiceUtil.getThreadFactory(deviceName + "-scheduled")));
    }

    private void configureDevice() {
        this.device.setExecutor(this.executor.get());
        this.device.setScheduledExecutor(this.scheduledExecutor.get());
    }

    private void shutdownExecutorServices() {
        this.shutdownExecutor(this.scheduledExecutor.get(), "Scheduled executor");
        this.shutdownExecutor(this.executor.get(), "Main executor");
    }

    private void shutdownExecutor(ExecutorService executor, String executorType) {
        if (executor == null) {
            return;
        }
        try {
            executor.shutdown();
            if (!executor.awaitTermination(30L, TimeUnit.SECONDS)) {
                LOGGER.warn("{} did not terminate gracefully, forcing shutdown", (Object)executorType);
                executor.shutdownNow();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("{} shutdown interrupted, forcing shutdown", (Object)executorType);
            executor.shutdownNow();
        }
        catch (Exception e) {
            LOGGER.error("Error during {} shutdown", (Object)executorType, (Object)e);
        }
    }

    private void clearExecutorReferences() {
        this.executor.set(null);
        this.scheduledExecutor.set(null);
    }
}

