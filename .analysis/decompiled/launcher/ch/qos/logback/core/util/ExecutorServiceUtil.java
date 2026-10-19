/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorServiceUtil {
    private static final ThreadFactory THREAD_FACTORY_FOR_SCHEDULED_EXECUTION_SERVICE = new ThreadFactory(){
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadFactory defaultFactory = this.makeThreadFactory();

        private ThreadFactory makeThreadFactory() {
            return Executors.defaultThreadFactory();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = this.defaultFactory.newThread(r);
            if (!thread.isDaemon()) {
                thread.setDaemon(true);
            }
            thread.setName("logback-" + this.threadNumber.getAndIncrement());
            return thread;
        }
    };

    public static ScheduledExecutorService newScheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(4, THREAD_FACTORY_FOR_SCHEDULED_EXECUTION_SERVICE);
    }

    public static ThreadPoolExecutor newThreadPoolExecutor() {
        int maximumPoolSize = 5;
        long keepAliveMillis = 100L;
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 5, 100L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), THREAD_FACTORY_FOR_SCHEDULED_EXECUTION_SERVICE);
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return threadPoolExecutor;
    }

    public static void shutdown(ExecutorService executorService) {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public static ExecutorService newAlternateThreadPoolExecutor() {
        return ExecutorServiceUtil.newThreadPoolExecutor();
    }
}

