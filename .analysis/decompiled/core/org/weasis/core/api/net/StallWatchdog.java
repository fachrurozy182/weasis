/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.net;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

final class StallWatchdog {
    private static final long MIN_POLL_MS = 200L;
    static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(runnable -> Thread.ofPlatform().daemon().name("weasis-stall-watchdog").unstarted(runnable));

    private StallWatchdog() {
    }

    static long pollIntervalMillis(long stallTimeoutMillis) {
        return Math.max(200L, stallTimeoutMillis / 4L);
    }
}

