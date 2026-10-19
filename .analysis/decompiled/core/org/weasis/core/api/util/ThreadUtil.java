/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.util;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadUtil {
    private ThreadUtil() {
    }

    public static ExecutorService newSingleThreadExecutor(String name) {
        return Executors.newSingleThreadExecutor(ThreadUtil.namedThreadFactory(name));
    }

    public static ExecutorService newFixedThreadPool(int nThreads, String name) {
        return Executors.newFixedThreadPool(nThreads, ThreadUtil.namedThreadFactory(name));
    }

    public static ExecutorService newCachedThreadPool(String name) {
        return Executors.newCachedThreadPool(ThreadUtil.namedThreadFactory(name));
    }

    public static ExecutorService newVirtualThreadPerTaskExecutor(String threadName) {
        return Executors.newThreadPerTaskExecutor((ThreadFactory)Thread.ofVirtual().name(threadName, 0L).factory());
    }

    public static ExecutorService newImageIOThreadPool(String name) {
        PoolConfig config = ThreadUtil.calculateIOPoolConfig();
        return new ThreadPoolExecutor(config.poolSize(), config.poolSize(), 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(config.queueCapacity()), ThreadUtil.namedDaemonThreadFactory(name, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static ExecutorService newImageProcessingThreadPool(String name) {
        PoolConfig config = ThreadUtil.calculateProcessingPoolConfig();
        return new ThreadPoolExecutor(config.poolSize(), config.poolSize(), 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(config.queueCapacity()), ThreadUtil.namedDaemonThreadFactory(name, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private static PoolConfig calculateIOPoolConfig() {
        int cores = Runtime.getRuntime().availableProcessors();
        long totalMemoryMB = Runtime.getRuntime().maxMemory() / 0x100000L;
        int poolSize = Math.max(2, Math.min(ThreadUtil.calculateMaxIOThreads(totalMemoryMB), cores * 2));
        int queueCapacity = ThreadUtil.calculateIOQueueCapacity(totalMemoryMB, poolSize);
        return new PoolConfig(poolSize, queueCapacity);
    }

    private static PoolConfig calculateProcessingPoolConfig() {
        int cores = Runtime.getRuntime().availableProcessors();
        long totalMemoryMB = Runtime.getRuntime().maxMemory() / 0x100000L;
        int poolSize = Math.max(1, Math.min(cores + 1, ThreadUtil.calculateMaxProcessingThreads(totalMemoryMB)));
        int queueCapacity = ThreadUtil.calculateProcessingQueueCapacity(totalMemoryMB, poolSize);
        return new PoolConfig(poolSize, queueCapacity);
    }

    private static int calculateMaxIOThreads(long totalMemoryMB) {
        return Math.max(2, Math.min(16, (int)(totalMemoryMB / 100L)));
    }

    private static int calculateMaxProcessingThreads(long totalMemoryMB) {
        return Math.max(1, Math.min(12, (int)(totalMemoryMB / 300L)));
    }

    private static int calculateIOQueueCapacity(long totalMemoryMB, int poolSize) {
        int baseCapacity = (int)Math.min(128L, totalMemoryMB / 32L);
        return Math.max(poolSize * 4, Math.min(poolSize * 8, baseCapacity));
    }

    private static int calculateProcessingQueueCapacity(long totalMemoryMB, int poolSize) {
        int baseCapacity = (int)Math.min(64L, totalMemoryMB / 64L);
        return Math.max(poolSize * 2, Math.min(poolSize * 4, baseCapacity));
    }

    public static ThreadFactory namedThreadFactory(String name) {
        return ThreadUtil.namedDaemonThreadFactory(name, false);
    }

    public static ThreadFactory namedDaemonThreadFactory(String name, boolean daemon) {
        Objects.requireNonNull(name, "Thread name cannot be null");
        AtomicInteger threadCounter = new AtomicInteger(1);
        return runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setName(name + "-" + threadCounter.getAndIncrement());
            thread.setDaemon(daemon);
            return thread;
        };
    }

    public static ExecutorService newSingleThreadDaemonExecutor(String name) {
        return Executors.newSingleThreadExecutor(ThreadUtil.namedDaemonThreadFactory(name, true));
    }

    public static ExecutorService newFixedDaemonThreadPool(int nThreads, String name) {
        return Executors.newFixedThreadPool(nThreads, ThreadUtil.namedDaemonThreadFactory(name, true));
    }

    private static Thread createShutdownHook(ExecutorService executor, String name, long timeoutSeconds) {
        return new Thread(() -> {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
        }, name + "-shutdown");
    }

    public static ExecutorService newManagedImageIOThreadPool(String name) {
        ExecutorService executor = ThreadUtil.newImageIOThreadPool(name);
        Runtime.getRuntime().addShutdownHook(ThreadUtil.createShutdownHook(executor, name, 5L));
        return executor;
    }

    public static ExecutorService newManagedImageProcessingThreadPool(String name) {
        ExecutorService executor = ThreadUtil.newImageProcessingThreadPool(name);
        Runtime.getRuntime().addShutdownHook(ThreadUtil.createShutdownHook(executor, name, 10L));
        return executor;
    }

    private record PoolConfig(int poolSize, int queueCapacity) {
    }
}

