/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.dicom.codec.seg;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SegBuildScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SegBuildScheduler.class);
    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final int MAX_CONCURRENT_BUILDS_CAP = 3;
    private static final int MAX_CONCURRENT_BUILDS = Math.clamp((long)(CORES / 4), (int)1, (int)3);
    private static final int MAX_CONCURRENT_CANONICAL_BUILDS = Math.clamp((long)(CORES / 2), (int)2, (int)6);
    private static final Semaphore PERMITS = new Semaphore(MAX_CONCURRENT_BUILDS);
    private static final Semaphore CANONICAL_PERMITS = new Semaphore(MAX_CONCURRENT_CANONICAL_BUILDS);
    private static final Queue<Runnable> PENDING = new ConcurrentLinkedQueue<Runnable>();
    private static final ExecutorService POOL = Executors.newFixedThreadPool(MAX_CONCURRENT_BUILDS, SegBuildScheduler.daemonThreadFactory("seg-volume-build"));
    private static final ExecutorService INIT_POOL = Executors.newFixedThreadPool(MAX_CONCURRENT_CANONICAL_BUILDS, SegBuildScheduler.daemonThreadFactory("seg-contour-init"));

    private SegBuildScheduler() {
    }

    private static ThreadFactory daemonThreadFactory(String name) {
        AtomicInteger counter = new AtomicInteger();
        return r -> {
            Thread t = new Thread(r, name + "-" + counter.incrementAndGet());
            t.setDaemon(true);
            t.setPriority(4);
            return t;
        };
    }

    public static void submitContourInit(Runnable task) {
        if (task != null) {
            INIT_POOL.execute(task);
        }
    }

    public static int getMaxConcurrentBuilds() {
        return MAX_CONCURRENT_BUILDS;
    }

    public static int getMaxConcurrentCanonicalBuilds() {
        return MAX_CONCURRENT_CANONICAL_BUILDS;
    }

    public static void submitAsync(Runnable starter) {
        if (starter == null) {
            return;
        }
        PENDING.add(starter);
        SegBuildScheduler.drain();
    }

    public static void release() {
        CANONICAL_PERMITS.release();
        SegBuildScheduler.drain();
    }

    private static void drain() {
        while (CANONICAL_PERMITS.tryAcquire()) {
            Runnable next = PENDING.poll();
            if (next == null) {
                CANONICAL_PERMITS.release();
                return;
            }
            try {
                next.run();
            }
            catch (RuntimeException e) {
                CANONICAL_PERMITS.release();
                LOGGER.error("Starting a segmentation volume build", (Throwable)e);
            }
        }
    }

    public static <T, R> List<R> mapBounded(List<T> items, Function<T, R> builder) {
        if (items == null || items.isEmpty() || builder == null) {
            return List.of();
        }
        ArrayList<Future<Object>> futures = new ArrayList<Future<Object>>(items.size());
        for (T item : items) {
            futures.add(POOL.submit(() -> SegBuildScheduler.callBounded(item, builder)));
        }
        ArrayList results = new ArrayList(items.size());
        for (Future future : futures) {
            try {
                Object value = future.get();
                if (value == null) continue;
                results.add(value);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                futures.forEach(f -> f.cancel(true));
                return results;
            }
            catch (ExecutionException e) {
                LOGGER.error("Building a segmentation volume", e.getCause());
            }
        }
        return results;
    }

    private static <T, R> R callBounded(T item, Function<T, R> builder) throws InterruptedException {
        PERMITS.acquire();
        try {
            R r = builder.apply(item);
            return r;
        }
        finally {
            PERMITS.release();
        }
    }
}

