/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.api.util;

import com.sun.management.OperatingSystemMXBean;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.util.GraphicsInfo;
import org.weasis.core.api.util.MemoryManager;
import org.weasis.core.api.util.ResourceAdvisor;
import org.weasis.core.api.util.SystemMemory;

public final class ResourceMonitor {
    public static final String CLEAN_PREVIOUS_VERSION_PROPERTY = "weasis.resource.stats.clean.previous.version";
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceMonitor.class);
    private static final ResourceMonitor INSTANCE = new ResourceMonitor();
    private static final long SAMPLE_INTERVAL_SECONDS = 5L;
    private static final long SAVE_INTERVAL_SECONDS = 60L;
    private static final String STATS_FILE = "resource-stats.properties";
    private static final String VERSION_KEY = "weasisVersion";
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean osBean = ResourceMonitor.resolveOsBean();
    private final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    private final AtomicLong cacheEvictions = new AtomicLong();
    private final AtomicLong outOfMemoryEvents = new AtomicLong();
    private final AtomicLong volumeDiskFallbacks = new AtomicLong();
    private final AtomicLong largestImageBytes = new AtomicLong();
    private final AtomicInteger largestVolumeSlices = new AtomicInteger();
    private volatile long heapPeakUsed;
    private volatile double peakProcessCpuLoad;
    private volatile double nativePeakPressure;
    private volatile double peakGcOverhead;
    private volatile long previousSessions;
    private volatile long previousUptimeMillis;
    private volatile ResourceAdvisor.Level lastOverallLevel = ResourceAdvisor.Level.COLLECTING;
    private final ScheduledExecutorService scheduler;

    private ResourceMonitor() {
        Properties history = ResourceMonitor.loadHistory();
        if (ResourceMonitor.supersededByUpgrade(history)) {
            ResourceMonitor.logDiscardedHistory(history);
        } else {
            this.seedMeasuredHistory(history);
        }
        this.largestImageBytes.set(ResourceMonitor.parseLong(history, "largestImageBytes"));
        this.largestVolumeSlices.set((int)ResourceMonitor.parseLong(history, "largestVolumeSlices"));
        String gpuRenderer = history.getProperty("gpuRenderer", "");
        if (!gpuRenderer.isBlank()) {
            GraphicsInfo.set(history.getProperty("gpuVendor", ""), gpuRenderer, history.getProperty("gpuGlVersion", ""), Boolean.parseBoolean(history.getProperty("gpuSoftware", "false")));
        }
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "weasis-resource-monitor");
            thread.setDaemon(true);
            return thread;
        });
        this.scheduler.scheduleWithFixedDelay(this::sample, 5L, 5L, TimeUnit.SECONDS);
        this.scheduler.scheduleWithFixedDelay(this::saveHistory, 60L, 60L, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveHistory, "weasis-resource-save"));
        this.installOutOfMemoryDetector();
    }

    private void installOutOfMemoryDetector() {
        Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (ResourceMonitor.isOutOfMemory(throwable)) {
                this.recordOutOfMemory();
            }
            if (previous != null) {
                previous.uncaughtException(thread, throwable);
            } else {
                System.err.print("Exception in thread \"" + thread.getName() + "\" ");
                throwable.printStackTrace();
            }
        });
    }

    private static boolean isOutOfMemory(Throwable throwable) {
        Throwable cause = throwable;
        for (int depth = 0; cause != null && depth < 32; cause = cause.getCause(), ++depth) {
            if (!(cause instanceof OutOfMemoryError)) continue;
            return true;
        }
        return false;
    }

    public static ResourceMonitor getInstance() {
        return INSTANCE;
    }

    public void recordCacheEviction(int count) {
        if (count > 0) {
            this.cacheEvictions.addAndGet(count);
        }
    }

    public void recordOutOfMemory() {
        this.outOfMemoryEvents.incrementAndGet();
    }

    public void recordVolumeDiskFallback() {
        this.volumeDiskFallbacks.incrementAndGet();
        LOGGER.warn("A volume did not fit in memory and spilled to disk: more RAM would speed up 3D/MPR (see Help > System resources)");
    }

    public void recordImageLoaded(long physicalBytes) {
        if (physicalBytes > 0L) {
            this.largestImageBytes.accumulateAndGet(physicalBytes, Math::max);
        }
    }

    public void recordVolume(int sliceCount) {
        if (sliceCount > 0) {
            this.largestVolumeSlices.accumulateAndGet(sliceCount, Math::max);
        }
    }

    public void resetStatistics() {
        this.previousSessions = 0L;
        this.previousUptimeMillis = 0L;
        this.heapPeakUsed = 0L;
        this.peakProcessCpuLoad = 0.0;
        this.nativePeakPressure = 0.0;
        this.peakGcOverhead = 0.0;
        this.cacheEvictions.set(0L);
        this.outOfMemoryEvents.set(0L);
        this.volumeDiskFallbacks.set(0L);
        this.lastOverallLevel = ResourceAdvisor.Level.COLLECTING;
        this.saveHistory();
    }

    public Snapshot snapshot() {
        long uptime = Math.max(1L, ManagementFactory.getRuntimeMXBean().getUptime());
        long heapUsed = this.memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = this.memoryBean.getHeapMemoryUsage().getMax();
        long gcCount = 0L;
        long gcTime = 0L;
        for (GarbageCollectorMXBean bean : this.gcBeans) {
            gcCount += Math.max(0L, bean.getCollectionCount());
            gcTime += Math.max(0L, bean.getCollectionTime());
        }
        MemoryManager memoryManager = MemoryManager.getInstance();
        return new Snapshot(uptime, this.previousUptimeMillis + uptime, this.previousSessions + 1L, heapUsed, heapMax, Math.max(this.heapPeakUsed, heapUsed), memoryManager.getUsedNativeMemory(), memoryManager.getNativeBudget(), this.nativePeakPressure, Runtime.getRuntime().availableProcessors(), this.osBean == null ? -1.0 : this.osBean.getProcessCpuLoad(), this.peakProcessCpuLoad, SystemMemory.totalPhysicalMemory(), SystemMemory.freePhysicalMemory(), gcCount, gcTime, Math.min(1.0, (double)gcTime / (double)uptime), this.peakGcOverhead, this.cacheEvictions.get(), this.outOfMemoryEvents.get(), this.volumeDiskFallbacks.get(), this.largestImageBytes.get(), this.largestVolumeSlices.get());
    }

    private void sample() {
        try {
            Snapshot snapshot;
            double pressure;
            double cpu;
            long heapUsed = this.memoryBean.getHeapMemoryUsage().getUsed();
            if (heapUsed > this.heapPeakUsed) {
                this.heapPeakUsed = heapUsed;
            }
            if (this.osBean != null && (cpu = this.osBean.getProcessCpuLoad()) > this.peakProcessCpuLoad) {
                this.peakProcessCpuLoad = cpu;
            }
            if ((pressure = MemoryManager.getInstance().getPressure()) > this.nativePeakPressure) {
                this.nativePeakPressure = pressure;
            }
            if ((snapshot = this.snapshot()).gcOverhead() > this.peakGcOverhead) {
                this.peakGcOverhead = snapshot.gcOverhead();
            }
            this.alertOnDegradation(snapshot);
        }
        catch (Throwable e) {
            LOGGER.debug("Resource sampling failed", e);
        }
    }

    private void alertOnDegradation(Snapshot snapshot) {
        ResourceAdvisor.Report report = ResourceAdvisor.evaluate(snapshot);
        ResourceAdvisor.Level level = report.overall().level();
        if (level == ResourceAdvisor.Level.SUBOPTIMAL && this.lastOverallLevel != ResourceAdvisor.Level.SUBOPTIMAL) {
            LOGGER.warn("Hardware resources became sub-optimal for this workload ({}); see Help > System resources", (Object)report.overall().reason());
        }
        this.lastOverallLevel = level;
    }

    private void seedMeasuredHistory(Properties history) {
        this.previousSessions = ResourceMonitor.parseLong(history, "sessionCount");
        this.previousUptimeMillis = ResourceMonitor.parseLong(history, "totalUptimeMillis");
        this.heapPeakUsed = ResourceMonitor.parseLong(history, "peakHeapUsed");
        this.peakProcessCpuLoad = ResourceMonitor.parseDouble(history, "peakProcessCpuLoad");
        this.nativePeakPressure = ResourceMonitor.parseDouble(history, "peakNativePressure");
        this.peakGcOverhead = ResourceMonitor.parseDouble(history, "peakGcOverhead");
        this.cacheEvictions.set(ResourceMonitor.parseLong(history, "cacheEvictions"));
        this.outOfMemoryEvents.set(ResourceMonitor.parseLong(history, "outOfMemoryEvents"));
        this.volumeDiskFallbacks.set(ResourceMonitor.parseLong(history, "volumeDiskFallbacks"));
    }

    private static void logDiscardedHistory(Properties history) {
        if (!history.isEmpty()) {
            LOGGER.info("Discarding the resource statistics measured by version {}: they no longer describe how {} manages memory", (Object)history.getProperty(VERSION_KEY, "?"), (Object)AppProperties.WEASIS_VERSION);
        }
    }

    private static boolean supersededByUpgrade(Properties history) {
        return Boolean.parseBoolean(System.getProperty(CLEAN_PREVIOUS_VERSION_PROPERTY, "false")) && !AppProperties.WEASIS_VERSION.equals(history.getProperty(VERSION_KEY, ""));
    }

    private static Properties loadHistory() {
        Properties properties;
        block8: {
            properties = new Properties();
            try {
                Path file = ResourceMonitor.statsFile();
                if (!Files.isReadable(file)) break block8;
                try (BufferedReader reader = Files.newBufferedReader(file);){
                    properties.load(reader);
                }
            }
            catch (Exception e) {
                LOGGER.debug("No resource history available", (Throwable)e);
            }
        }
        return properties;
    }

    private void saveHistory() {
        try {
            Path file = ResourceMonitor.statsFile();
            Snapshot s = this.snapshot();
            Properties properties = new Properties();
            properties.setProperty(VERSION_KEY, AppProperties.WEASIS_VERSION);
            properties.setProperty("sessionCount", Long.toString(s.sessionCount()));
            properties.setProperty("totalUptimeMillis", Long.toString(s.totalUptimeMillis()));
            properties.setProperty("peakHeapUsed", Long.toString(s.heapPeakUsed()));
            properties.setProperty("peakNativePressure", Double.toString(s.nativePeakPressure()));
            properties.setProperty("peakProcessCpuLoad", Double.toString(s.peakProcessCpuLoad()));
            properties.setProperty("peakGcOverhead", Double.toString(s.peakGcOverhead()));
            properties.setProperty("cacheEvictions", Long.toString(s.cacheEvictions()));
            properties.setProperty("outOfMemoryEvents", Long.toString(s.outOfMemoryEvents()));
            properties.setProperty("volumeDiskFallbacks", Long.toString(s.volumeDiskFallbacks()));
            properties.setProperty("largestImageBytes", Long.toString(s.largestImageBytes()));
            properties.setProperty("largestVolumeSlices", Long.toString(s.largestVolumeSlices()));
            GraphicsInfo.get().ifPresent(gpu -> {
                properties.setProperty("gpuVendor", gpu.vendor());
                properties.setProperty("gpuRenderer", gpu.renderer());
                properties.setProperty("gpuGlVersion", gpu.glVersion());
                properties.setProperty("gpuSoftware", Boolean.toString(gpu.softwareRendered()));
            });
            try (BufferedWriter writer = Files.newBufferedWriter(file, new OpenOption[0]);){
                properties.store(writer, "Weasis resource statistics");
            }
        }
        catch (Throwable e) {
            LOGGER.debug("Cannot save resource history", e);
        }
    }

    private static Path statsFile() {
        return AppProperties.WEASIS_PATH.resolve(STATS_FILE);
    }

    private static long parseLong(Properties properties, String key) {
        try {
            return Long.parseLong(properties.getProperty(key, "0").trim());
        }
        catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static double parseDouble(Properties properties, String key) {
        try {
            return Double.parseDouble(properties.getProperty(key, "0").trim());
        }
        catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static OperatingSystemMXBean resolveOsBean() {
        try {
            java.lang.management.OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
            if (operatingSystemMXBean instanceof OperatingSystemMXBean) {
                OperatingSystemMXBean bean = (OperatingSystemMXBean)operatingSystemMXBean;
                return bean;
            }
        }
        catch (RuntimeException e) {
            LOGGER.warn("Cannot access the operating system bean", (Throwable)e);
        }
        return null;
    }

    public record Snapshot(long uptimeMillis, long totalUptimeMillis, long sessionCount, long heapUsed, long heapMax, long heapPeakUsed, long nativeUsed, long nativeBudget, double nativePeakPressure, int cpuCores, double processCpuLoad, double peakProcessCpuLoad, long physicalTotalMemory, long physicalFreeMemory, long gcCount, long gcTimeMillis, double gcOverhead, double peakGcOverhead, long cacheEvictions, long outOfMemoryEvents, long volumeDiskFallbacks, long largestImageBytes, int largestVolumeSlices) {
    }
}

