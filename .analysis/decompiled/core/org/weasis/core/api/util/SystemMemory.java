/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.util;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.util.StringUtil;

public final class SystemMemory {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemMemory.class);
    public static final String NATIVE_MEMORY_PROPERTY = "weasis.native.memory";
    public static final String NATIVE_MEMORY_PERCENT_PROPERTY = "weasis.native.memory.percent";
    public static final String VOLUME_STAGING_MEMORY_PROPERTY = "weasis.volume.staging.memory";
    private static final double DEFAULT_NATIVE_PERCENT = 0.5;
    private static final double DEFAULT_STAGING_PERCENT = 0.05;
    private static final long OS_RESERVE = 0x20000000L;
    private static final long MIN_NATIVE_BUDGET = 0x10000000L;
    private static final long MIN_STAGING_MEMORY = 0x4000000L;
    private static final long MAX_STAGING_MEMORY = 0x20000000L;
    private static final OperatingSystemMXBean OS_BEAN = SystemMemory.resolveOsBean();

    private SystemMemory() {
    }

    private static OperatingSystemMXBean resolveOsBean() {
        try {
            java.lang.management.OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
            if (operatingSystemMXBean instanceof OperatingSystemMXBean) {
                OperatingSystemMXBean bean = (OperatingSystemMXBean)operatingSystemMXBean;
                return bean;
            }
            LOGGER.warn("The operating system memory bean is not available");
        }
        catch (RuntimeException e) {
            LOGGER.warn("Cannot access the operating system memory bean", (Throwable)e);
        }
        return null;
    }

    public static long totalPhysicalMemory() {
        return OS_BEAN == null ? 0L : OS_BEAN.getTotalMemorySize();
    }

    public static long freePhysicalMemory() {
        return OS_BEAN == null ? 0L : OS_BEAN.getFreeMemorySize();
    }

    public static long maxHeapMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public static long getNativeMemoryBudget() {
        long override = SystemMemory.longProperty(NATIVE_MEMORY_PROPERTY);
        if (override > 0L) {
            return Math.max(0x10000000L, override);
        }
        long heap = SystemMemory.maxHeapMemory();
        long total = SystemMemory.totalPhysicalMemory();
        long budget = total <= 0L ? heap / 2L : (long)((double)total * SystemMemory.nativePercent()) - heap - 0x20000000L;
        return Math.max(0x10000000L, budget);
    }

    public static long getVolumeStagingMemory() {
        long override = SystemMemory.longProperty(VOLUME_STAGING_MEMORY_PROPERTY);
        if (override > 0L) {
            return Math.max(0x4000000L, override);
        }
        long total = SystemMemory.totalPhysicalMemory();
        long staging = total <= 0L ? 0x4000000L : (long)((double)total * 0.05);
        return Math.min(0x20000000L, Math.max(0x4000000L, staging));
    }

    private static double nativePercent() {
        long pct = SystemMemory.longProperty(NATIVE_MEMORY_PERCENT_PROPERTY);
        if (pct >= 1L && pct <= 90L) {
            return (double)pct / 100.0;
        }
        return 0.5;
    }

    private static long longProperty(String key) {
        String value = System.getProperty(key);
        if (StringUtil.hasText((CharSequence)value)) {
            try {
                return Long.parseLong(value.trim());
            }
            catch (NumberFormatException e) {
                LOGGER.warn("Invalid value for the system property {}: {}", (Object)key, (Object)value);
            }
        }
        return -1L;
    }
}

