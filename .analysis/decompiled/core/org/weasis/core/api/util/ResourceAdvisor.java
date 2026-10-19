/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 */
package org.weasis.core.api.util;

import org.weasis.core.api.util.ResourceMonitor;

public final class ResourceAdvisor {
    private static final long GB = 0x40000000L;
    private static final long MIN_UPTIME_MS = 120000L;
    private static final double MIN_ACTIVITY_PRESSURE = 0.02;
    private static final double MIN_ACTIVITY_CPU = 0.15;
    private static final double HIGH_GC_OVERHEAD = 0.1;
    private static final double LOW_GC_OVERHEAD = 0.02;
    private static final double LOW_HEAP_FRACTION = 0.4;
    private static final double LOW_NATIVE_PRESSURE = 0.5;
    private static final int FEW_CORES = 2;
    private static final int ABUNDANT_CORES = 8;
    private static final double LOW_CPU_LOAD = 0.4;
    private static final int RECOMMENDED_MIN_CORES = 8;
    private static final double MIN_RATE_HOURS = 1.0;
    private static final double OOM_RATE_LIMIT = 0.5;
    private static final double DISK_FALLBACK_RATE_LIMIT = 1.0;
    private static final double HEAP_HEADROOM_FRACTION = 0.2;
    private static final double TARGET_HEAP_FRACTION = 0.25;
    private static final Recommendation NO_RECOMMENDATION = new Recommendation(0L, 0L, 0);
    private static final int[] RAM_TIERS_GB = new int[]{8, 16, 32, 64, 128};

    private ResourceAdvisor() {
    }

    public static Report evaluate(ResourceMonitor.Snapshot snapshot) {
        if (snapshot.totalUptimeMillis() < 120000L || !ResourceAdvisor.hasActivity(snapshot)) {
            Assessment collecting = new Assessment(Level.COLLECTING, Reason.COLLECTING);
            return new Report(false, collecting, collecting, collecting, NO_RECOMMENDATION);
        }
        Assessment memory = ResourceAdvisor.assessMemory(snapshot);
        Assessment cpu = ResourceAdvisor.assessCpu(snapshot);
        return new Report(true, memory, cpu, ResourceAdvisor.worst(memory, cpu), ResourceAdvisor.recommend(snapshot, memory, cpu));
    }

    private static boolean hasActivity(ResourceMonitor.Snapshot s) {
        return s.nativePeakPressure() > 0.02 || s.peakProcessCpuLoad() > 0.15;
    }

    private static double perHour(long count, long totalUptimeMillis) {
        double hours = Math.max(1.0, (double)totalUptimeMillis / 3600000.0);
        return (double)count / hours;
    }

    private static Assessment assessMemory(ResourceMonitor.Snapshot s) {
        double heapFraction;
        if (ResourceAdvisor.perHour(s.outOfMemoryEvents(), s.totalUptimeMillis()) > 0.5) {
            return new Assessment(Level.SUBOPTIMAL, Reason.MEM_OUT_OF_MEMORY);
        }
        if (ResourceAdvisor.perHour(s.volumeDiskFallbacks(), s.totalUptimeMillis()) > 1.0) {
            return new Assessment(Level.SUBOPTIMAL, Reason.MEM_VOLUME_DISK_FALLBACK);
        }
        if (s.peakGcOverhead() > 0.1) {
            return new Assessment(Level.SUBOPTIMAL, Reason.MEM_HIGH_GC);
        }
        double d = heapFraction = s.heapMax() > 0L ? (double)s.heapPeakUsed() / (double)s.heapMax() : 1.0;
        if (heapFraction < 0.4 && s.nativePeakPressure() < 0.5 && s.cacheEvictions() == 0L && s.peakGcOverhead() < 0.02) {
            return new Assessment(Level.ABUNDANT, Reason.MEM_LIGHTLY_USED);
        }
        return new Assessment(Level.OPTIMAL, Reason.MEM_WITHIN_LIMITS);
    }

    private static Assessment assessCpu(ResourceMonitor.Snapshot s) {
        if (s.cpuCores() <= 2) {
            return new Assessment(Level.SUBOPTIMAL, Reason.CPU_FEW_CORES);
        }
        double peak = s.peakProcessCpuLoad();
        if (s.cpuCores() >= 8 && peak >= 0.0 && peak < 0.4) {
            return new Assessment(Level.ABUNDANT, Reason.CPU_MANY_IDLE_CORES);
        }
        return new Assessment(Level.OPTIMAL, Reason.CPU_ADEQUATE);
    }

    private static Recommendation recommend(ResourceMonitor.Snapshot s, Assessment memory, Assessment cpu) {
        long heap = 0L;
        long ram = 0L;
        if (memory.level() == Level.SUBOPTIMAL) {
            if (ResourceAdvisor.heapUndersized(s)) {
                heap = ResourceAdvisor.recommendHeap(s);
            } else {
                ram = ResourceAdvisor.recommendRam(s.physicalTotalMemory());
            }
        }
        int cores = cpu.level() == Level.SUBOPTIMAL ? ResourceAdvisor.recommendCores(s.cpuCores()) : 0;
        return heap == 0L && ram == 0L && cores == 0 ? NO_RECOMMENDATION : new Recommendation(heap, ram, cores);
    }

    private static boolean heapUndersized(ResourceMonitor.Snapshot s) {
        return s.physicalTotalMemory() > 0L && (double)s.heapMax() < (double)s.physicalTotalMemory() * 0.2;
    }

    private static long recommendHeap(ResourceMonitor.Snapshot s) {
        long gb = Math.round((double)s.physicalTotalMemory() * 0.25 / 1.073741824E9);
        return Math.max(1L, gb) * 0x40000000L;
    }

    private static long recommendRam(long currentBytes) {
        long currentGb = Math.round((double)currentBytes / 1.073741824E9);
        for (int i = 0; i < RAM_TIERS_GB.length; ++i) {
            if ((long)RAM_TIERS_GB[i] < currentGb) continue;
            long next = i + 1 < RAM_TIERS_GB.length ? (long)RAM_TIERS_GB[i + 1] : (long)RAM_TIERS_GB[i] * 2L;
            return next * 0x40000000L;
        }
        return currentGb * 2L * 0x40000000L;
    }

    private static int recommendCores(int currentCores) {
        return Math.max(8, currentCores * 2);
    }

    private static Assessment worst(Assessment a, Assessment b) {
        return ResourceAdvisor.rank(a.level()) <= ResourceAdvisor.rank(b.level()) ? a : b;
    }

    private static int rank(Level level) {
        return switch (level.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 1 -> 0;
            case 2 -> 1;
            case 3 -> 2;
        };
    }

    public record Assessment(Level level, Reason reason) {
    }

    public static enum Level {
        COLLECTING,
        SUBOPTIMAL,
        OPTIMAL,
        ABUNDANT;

    }

    public static enum Reason {
        COLLECTING,
        MEM_OUT_OF_MEMORY,
        MEM_VOLUME_DISK_FALLBACK,
        MEM_HIGH_GC,
        MEM_LIGHTLY_USED,
        MEM_WITHIN_LIMITS,
        CPU_FEW_CORES,
        CPU_MANY_IDLE_CORES,
        CPU_ADEQUATE;

    }

    public record Report(boolean ready, Assessment memory, Assessment cpu, Assessment overall, Recommendation recommendation) {
    }

    public record Recommendation(long recommendedHeapBytes, long recommendedRamBytes, int recommendedCores) {
        public boolean hasHeapAdvice() {
            return this.recommendedHeapBytes > 0L;
        }

        public boolean hasRamAdvice() {
            return this.recommendedRamBytes > 0L;
        }

        public boolean hasCpuAdvice() {
            return this.recommendedCores > 0;
        }

        public boolean isEmpty() {
            return !this.hasHeapAdvice() && !this.hasRamAdvice() && !this.hasCpuAdvice();
        }
    }
}

