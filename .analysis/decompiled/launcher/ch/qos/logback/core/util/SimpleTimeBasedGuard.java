/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

import java.util.concurrent.atomic.AtomicLong;

public class SimpleTimeBasedGuard {
    private final long windowDurationMs;
    private final int maxAllows;
    public static final long DEFAULT_WINDOW_MS = 1800000L;
    public static final int DEFAULT_MAX_ALLOWS = 2;
    private final AtomicLong artificialTime = new AtomicLong(-1L);
    private volatile long windowStartMs = 0L;
    private volatile int allowsUsed = 0;

    public SimpleTimeBasedGuard(long windowDurationMs, int maxAllows) {
        if (windowDurationMs <= 0L) {
            throw new IllegalArgumentException("windowDurationMs must be > 0");
        }
        if (maxAllows < 1) {
            throw new IllegalArgumentException("maxAllows must be >= 1");
        }
        this.windowDurationMs = windowDurationMs;
        this.maxAllows = maxAllows;
    }

    public SimpleTimeBasedGuard() {
        this(1800000L, 2);
    }

    public synchronized boolean allow() {
        long now = this.currentTimeMillis();
        if (this.windowStartMs == 0L) {
            this.windowStartMs = now;
            this.allowsUsed = 1;
            return true;
        }
        if (now < this.windowStartMs + this.windowDurationMs) {
            if (this.allowsUsed < this.maxAllows) {
                ++this.allowsUsed;
                return true;
            }
            return false;
        }
        this.windowStartMs = now;
        this.allowsUsed = 1;
        return true;
    }

    public void setCurrentTimeMillis(long timestamp) {
        this.artificialTime.set(timestamp);
    }

    public void clearCurrentTime() {
        this.artificialTime.set(-1L);
    }

    private long currentTimeMillis() {
        long t = this.artificialTime.get();
        return t >= 0L ? t : System.currentTimeMillis();
    }

    void incCurrentTimeMillis(long increment) {
        this.artificialTime.getAndAdd(increment);
    }

    public int getAllowsUsed() {
        return this.allowsUsed;
    }

    public int getAllowsRemaining() {
        return Math.max(0, this.maxAllows - this.allowsUsed);
    }

    public long getWindowDuration() {
        return this.windowDurationMs;
    }

    public int getMaxAllows() {
        return this.maxAllows;
    }

    public long getMillisUntilNextWindow() {
        if (this.windowStartMs == 0L) {
            return this.windowDurationMs;
        }
        long nextWindowStart = this.windowStartMs + this.windowDurationMs;
        long now = this.currentTimeMillis();
        return Math.max(0L, nextWindowStart - now);
    }
}

