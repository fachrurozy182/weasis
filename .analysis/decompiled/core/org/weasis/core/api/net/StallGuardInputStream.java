/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.StreamUtil
 */
package org.weasis.core.api.net;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.weasis.core.api.net.StallTimeoutException;
import org.weasis.core.api.net.StallWatchdog;
import org.weasis.core.util.StreamUtil;

public final class StallGuardInputStream
extends FilterInputStream {
    private static final long IDLE = Long.MIN_VALUE;
    private final int stallTimeoutMillis;
    private final long stallNanos;
    private final AtomicLong readStartNanos = new AtomicLong(Long.MIN_VALUE);
    private final AtomicBoolean stalled = new AtomicBoolean();
    private final ScheduledFuture<?> watchdog;

    public static InputStream wrap(InputStream in, int stallTimeoutMillis) {
        return stallTimeoutMillis > 0 ? new StallGuardInputStream(in, stallTimeoutMillis) : in;
    }

    private StallGuardInputStream(InputStream in, int stallTimeoutMillis) {
        super(Objects.requireNonNull(in, "in"));
        this.stallTimeoutMillis = stallTimeoutMillis;
        this.stallNanos = TimeUnit.MILLISECONDS.toNanos(stallTimeoutMillis);
        long poll = StallWatchdog.pollIntervalMillis(stallTimeoutMillis);
        this.watchdog = StallWatchdog.EXECUTOR.scheduleWithFixedDelay(this::abortIfStalled, poll, poll, TimeUnit.MILLISECONDS);
    }

    @Override
    public int read() throws IOException {
        this.readStartNanos.set(System.nanoTime());
        try {
            int n = this.in.read();
            return n;
        }
        catch (IOException e) {
            throw this.translate(e);
        }
        finally {
            this.readStartNanos.set(Long.MIN_VALUE);
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        this.readStartNanos.set(System.nanoTime());
        try {
            int n = this.in.read(b, off, len);
            return n;
        }
        catch (IOException e) {
            throw this.translate(e);
        }
        finally {
            this.readStartNanos.set(Long.MIN_VALUE);
        }
    }

    @Override
    public long skip(long n) throws IOException {
        this.readStartNanos.set(System.nanoTime());
        try {
            long l = this.in.skip(n);
            return l;
        }
        catch (IOException e) {
            throw this.translate(e);
        }
        finally {
            this.readStartNanos.set(Long.MIN_VALUE);
        }
    }

    @Override
    public void close() throws IOException {
        this.watchdog.cancel(false);
        super.close();
    }

    public boolean isStalled() {
        return this.stalled.get();
    }

    private void abortIfStalled() {
        long start = this.readStartNanos.get();
        if (start != Long.MIN_VALUE && System.nanoTime() - start >= this.stallNanos && this.stalled.compareAndSet(false, true)) {
            this.watchdog.cancel(false);
            StreamUtil.safeClose((AutoCloseable)this.in);
        }
    }

    private IOException translate(IOException e) {
        return this.stalled.get() ? new StallTimeoutException("No data received for " + this.stallTimeoutMillis + " ms", e) : e;
    }
}

