/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.media.data.Series
 *  org.weasis.core.api.service.WProperties
 *  org.weasis.core.ui.tp.raven.spinner.SpinnerProgress
 *  org.weasis.core.util.StringUtil
 *  org.weasis.dicom.codec.DicomSeries
 */
package org.weasis.dicom.explorer.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.ui.tp.raven.spinner.SpinnerProgress;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.codec.DicomSeries;
import org.weasis.dicom.explorer.main.DicomExplorer;

public class DicomTaskManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomTaskManager.class);
    private static final int DEFAULT_CORE_POOL_SIZE = 2;
    private static final int DEFAULT_MAX_POOL_SIZE = 8;
    private static final int DEFAULT_THUMBNAIL_POOL_SIZE = 4;
    private static final int DEFAULT_LOADING_POOL_SIZE = 6;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int PROGRESS_UPDATE_DELAY_MS = 100;
    private final ExecutorService generalExecutor;
    private final ExecutorService thumbnailExecutor;
    private final ExecutorService loadingExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    private final ConcurrentHashMap<String, DicomTask> activeTasks = new ConcurrentHashMap();
    private final ConcurrentHashMap<String, TaskGroup> taskGroups = new ConcurrentHashMap();
    private final AtomicLong taskIdCounter = new AtomicLong(0L);
    private final SpinnerProgress globalProgressBar;
    private final AtomicInteger activeThumbnailTasks = new AtomicInteger(0);
    private final AtomicInteger activeLoadingTasks = new AtomicInteger(0);
    private final DicomExplorer explorer;
    private volatile boolean shutdownRequested = false;

    public DicomTaskManager(DicomExplorer explorer) {
        this.explorer = explorer;
        this.generalExecutor = this.createGeneralExecutor();
        this.thumbnailExecutor = this.createThumbnailExecutor();
        this.loadingExecutor = this.createLoadingExecutor();
        this.scheduledExecutor = Executors.newScheduledThreadPool(2, new DicomThreadFactory("DicomScheduled"));
        this.globalProgressBar = new SpinnerProgress();
        this.setupGlobalProgressBar();
        this.startProgressUpdateTask();
        LOGGER.info("DicomTaskManager initialized with pools: general={}, thumbnail={}, loading={}", new Object[]{this.getPoolSize("general"), this.getPoolSize("thumbnail"), this.getPoolSize("loading")});
    }

    private ExecutorService createGeneralExecutor() {
        int corePoolSize = this.getConfiguredPoolSize("weasis.dicom.general.pool.core", 2);
        int maxPoolSize = this.getConfiguredPoolSize("weasis.dicom.general.pool.max", 8);
        return Executors.newFixedThreadPool(maxPoolSize, new DicomThreadFactory("DicomGeneral"));
    }

    private ExecutorService createThumbnailExecutor() {
        int poolSize = this.getConfiguredPoolSize("weasis.dicom.thumbnail.pool.size", 4);
        return Executors.newFixedThreadPool(poolSize, new DicomThreadFactory("DicomThumbnail"));
    }

    private ExecutorService createLoadingExecutor() {
        int poolSize = this.getConfiguredPoolSize("weasis.dicom.loading.pool.size", 6);
        return Executors.newFixedThreadPool(poolSize, new DicomThreadFactory("DicomLoading"));
    }

    private int getConfiguredPoolSize(String property, int defaultValue) {
        try {
            WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
            return preferences.getIntProperty(property, defaultValue);
        }
        catch (Exception e) {
            LOGGER.warn("Failed to get configured pool size for {}, using default: {}", (Object)property, (Object)defaultValue);
            return defaultValue;
        }
    }

    private int getPoolSize(String poolType) {
        return switch (poolType) {
            case "thumbnail" -> 4;
            case "loading" -> 6;
            default -> 8;
        };
    }

    public CompletableFuture<Void> executeGeneralTask(String taskName, Runnable task) {
        return this.executeTask(taskName, task, this.generalExecutor, TaskType.GENERAL);
    }

    public CompletableFuture<Void> executeThumbnailTask(String taskName, Runnable task) {
        return this.executeTask(taskName, task, this.thumbnailExecutor, TaskType.THUMBNAIL);
    }

    public CompletableFuture<Void> executeLoadingTask(String taskName, Runnable task) {
        return this.executeTask(taskName, task, this.loadingExecutor, TaskType.LOADING);
    }

    public <T> CompletableFuture<T> executeGeneralTask(String taskName, Callable<T> task) {
        return this.executeTask(taskName, task, this.generalExecutor, TaskType.GENERAL);
    }

    public <T> CompletableFuture<T> executeThumbnailTask(String taskName, Callable<T> task) {
        return this.executeTask(taskName, task, this.thumbnailExecutor, TaskType.THUMBNAIL);
    }

    public <T> CompletableFuture<T> executeLoadingTask(String taskName, Callable<T> task) {
        return this.executeTask(taskName, task, this.loadingExecutor, TaskType.LOADING);
    }

    private CompletableFuture<Void> executeTask(String taskName, Runnable task, ExecutorService executor, TaskType type) {
        if (this.shutdownRequested) {
            return CompletableFuture.completedFuture(null);
        }
        String taskId = this.generateTaskId();
        DicomTask dicomTask = new DicomTask(taskId, taskName, type);
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> this.executeWithTracking(dicomTask, task), executor);
        dicomTask.setFuture(future);
        this.activeTasks.put(taskId, dicomTask);
        future.whenComplete((result, throwable) -> this.handleTaskCompletion(taskId, (Throwable)throwable));
        this.updateTaskCounters(type, 1);
        this.updateGlobalProgress();
        LOGGER.debug("Submitted {} task: {} [{}]", new Object[]{type, taskName, taskId});
        return future;
    }

    private <T> CompletableFuture<T> executeTask(String taskName, Callable<T> task, ExecutorService executor, TaskType type) {
        if (this.shutdownRequested) {
            return CompletableFuture.completedFuture(null);
        }
        String taskId = this.generateTaskId();
        DicomTask dicomTask = new DicomTask(taskId, taskName, type);
        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
            try {
                return this.executeWithTracking(dicomTask, task);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
        dicomTask.setFuture(future);
        this.activeTasks.put(taskId, dicomTask);
        future.whenComplete((result, throwable) -> this.handleTaskCompletion(taskId, (Throwable)throwable));
        this.updateTaskCounters(type, 1);
        this.updateGlobalProgress();
        LOGGER.debug("Submitted {} task: {} [{}]", new Object[]{type, taskName, taskId});
        return future;
    }

    private void executeWithTracking(DicomTask task, Runnable runnable) {
        try {
            task.markStarted();
            runnable.run();
            task.markCompleted();
        }
        catch (Exception e) {
            task.markFailed(e);
            throw e;
        }
    }

    private <T> T executeWithTracking(DicomTask task, Callable<T> callable) throws Exception {
        try {
            task.markStarted();
            T result = callable.call();
            task.markCompleted();
            return result;
        }
        catch (Exception e) {
            task.markFailed(e);
            throw e;
        }
    }

    private void handleTaskCompletion(String taskId, Throwable throwable) {
        DicomTask task = this.activeTasks.remove(taskId);
        if (task != null) {
            this.updateTaskCounters(task.getType(), -1);
            this.updateGlobalProgress();
            if (throwable != null && !(throwable instanceof CancellationException)) {
                LOGGER.warn("Task failed: {} [{}]", new Object[]{task.getName(), taskId, throwable});
            } else {
                LOGGER.debug("Task completed: {} [{}]", (Object)task.getName(), (Object)taskId);
            }
        }
    }

    private void updateTaskCounters(TaskType type, int delta) {
        switch (type.ordinal()) {
            case 1: {
                this.activeThumbnailTasks.addAndGet(delta);
                break;
            }
            case 2: {
                this.activeLoadingTasks.addAndGet(delta);
                break;
            }
        }
    }

    public CompletableFuture<Void> loadSeriesInBackground(DicomSeries series) {
        String taskName = "Load Series: " + this.getSeriesDisplayName((Series<?>)series);
        return this.executeLoadingTask(taskName, () -> {
            try {
                LOGGER.debug("Loading series in background: {}", (Object)this.getSeriesDisplayName((Series<?>)series));
                series.getMedia(0, null, null);
                SwingUtilities.invokeLater(() -> this.explorer.updateSplitSeries(series));
            }
            catch (Exception e) {
                LOGGER.error("Failed to load series: {}", (Object)this.getSeriesDisplayName((Series<?>)series), (Object)e);
            }
        });
    }

    public CompletableFuture<Void> buildThumbnailInBackground(DicomSeries series) {
        String taskName = "Build Thumbnail: " + this.getSeriesDisplayName((Series<?>)series);
        return this.executeThumbnailTask(taskName, () -> {
            try {
                LOGGER.debug("Building thumbnail in background: {}", (Object)this.getSeriesDisplayName((Series<?>)series));
            }
            catch (Exception e) {
                LOGGER.error("Failed to build thumbnail for series: {}", (Object)this.getSeriesDisplayName((Series<?>)series), (Object)e);
            }
        });
    }

    public CompletableFuture<Void> refreshSeriesInBackground(DicomSeries series) {
        String taskName = "Refresh Series: " + this.getSeriesDisplayName((Series<?>)series);
        return this.executeGeneralTask(taskName, () -> {
            try {
                LOGGER.debug("Refreshing series in background: {}", (Object)this.getSeriesDisplayName((Series<?>)series));
                SwingUtilities.invokeLater(() -> this.explorer.updateSplitSeries(series));
            }
            catch (Exception e) {
                LOGGER.error("Failed to refresh series: {}", (Object)this.getSeriesDisplayName((Series<?>)series), (Object)e);
            }
        });
    }

    public TaskGroup createTaskGroup(String groupName) {
        String groupId = this.generateTaskId();
        TaskGroup group = new TaskGroup(groupId, groupName);
        this.taskGroups.put(groupId, group);
        LOGGER.debug("Created task group: {} [{}]", (Object)groupName, (Object)groupId);
        return group;
    }

    public CompletableFuture<Void> executeTaskGroup(TaskGroup group) {
        ArrayList<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
        for (TaskGroupItem item : group.getTasks()) {
            CompletableFuture<Void> future = switch (item.getType().ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> this.executeGeneralTask(item.getName(), item.getRunnable());
                case 1 -> this.executeThumbnailTask(item.getName(), item.getRunnable());
                case 2 -> this.executeLoadingTask(item.getName(), item.getRunnable());
            };
            futures.add(future);
        }
        CompletableFuture<Void> groupFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        groupFuture.whenComplete((result, throwable) -> {
            this.taskGroups.remove(group.getId());
            if (throwable != null) {
                LOGGER.warn("Task group failed: {} [{}]", new Object[]{group.getName(), group.getId(), throwable});
            } else {
                LOGGER.debug("Task group completed: {} [{}]", (Object)group.getName(), (Object)group.getId());
            }
        });
        return groupFuture;
    }

    public boolean cancelTask(String taskId) {
        DicomTask task = this.activeTasks.get(taskId);
        if (task != null && task.getFuture() != null) {
            boolean cancelled = task.getFuture().cancel(true);
            if (cancelled) {
                this.activeTasks.remove(taskId);
                this.updateTaskCounters(task.getType(), -1);
                this.updateGlobalProgress();
                LOGGER.debug("Cancelled task: {} [{}]", (Object)task.getName(), (Object)taskId);
            }
            return cancelled;
        }
        return false;
    }

    public void cancelAllTasks() {
        LOGGER.info("Cancelling all active tasks ({} tasks)", (Object)this.activeTasks.size());
        ArrayList taskIds = new ArrayList(this.activeTasks.keySet());
        for (String taskId : taskIds) {
            this.cancelTask(taskId);
        }
    }

    public void cancelTasksByType(TaskType type) {
        List taskIds = this.activeTasks.values().stream().filter(task -> task.getType() == type).map(DicomTask::getId).toList();
        LOGGER.debug("Cancelling {} tasks of type {}", (Object)taskIds.size(), (Object)type);
        taskIds.forEach(this::cancelTask);
    }

    private void setupGlobalProgressBar() {
        this.globalProgressBar.setStringPainted(false);
        this.globalProgressBar.setVisible(false);
        this.globalProgressBar.setIndeterminate(true);
    }

    private void startProgressUpdateTask() {
        this.scheduledExecutor.scheduleWithFixedDelay(this::updateGlobalProgress, 100L, 100L, TimeUnit.MILLISECONDS);
    }

    private void updateGlobalProgress() {
        if (this.shutdownRequested) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            int totalActiveTasks = this.activeTasks.size();
            boolean hasActiveTasks = totalActiveTasks > 0;
            this.globalProgressBar.setVisible(hasActiveTasks);
            if (hasActiveTasks) {
                String tooltip = this.buildProgressTooltip();
                this.globalProgressBar.setToolTipText(tooltip);
            }
        });
    }

    private String buildProgressTooltip() {
        int thumbnailTasks = this.activeThumbnailTasks.get();
        int loadingTasks = this.activeLoadingTasks.get();
        int generalTasks = this.activeTasks.size() - thumbnailTasks - loadingTasks;
        StringBuilder sb = new StringBuilder("<html>");
        sb.append("<b>Active Tasks:</b><br>");
        if (generalTasks > 0) {
            sb.append("General: ").append(generalTasks).append("<br>");
        }
        if (thumbnailTasks > 0) {
            sb.append("Thumbnails: ").append(thumbnailTasks).append("<br>");
        }
        if (loadingTasks > 0) {
            sb.append("Loading: ").append(loadingTasks).append("<br>");
        }
        sb.append("</html>");
        return sb.toString();
    }

    public SpinnerProgress getGlobalProgressBar() {
        return this.globalProgressBar;
    }

    private String generateTaskId() {
        return "task-" + this.taskIdCounter.incrementAndGet();
    }

    private String getSeriesDisplayName(Series<?> series) {
        if (series == null) {
            return "Unknown Series";
        }
        String description = series.toString();
        return StringUtil.hasText((CharSequence)description) ? description : "Series " + series.getSeriesNumber();
    }

    public List<DicomTask> getActiveTasks() {
        return new ArrayList<DicomTask>(this.activeTasks.values());
    }

    public List<DicomTask> getActiveTasksByType(TaskType type) {
        return this.activeTasks.values().stream().filter(task -> task.getType() == type).toList();
    }

    public boolean hasActiveTasks() {
        return !this.activeTasks.isEmpty();
    }

    public boolean hasActiveTasksOfType(TaskType type) {
        return this.activeTasks.values().stream().anyMatch(task -> task.getType() == type);
    }

    public void shutdown() {
        LOGGER.info("Shutting down DicomTaskManager...");
        this.shutdownRequested = true;
        this.cancelAllTasks();
        this.shutdownExecutor(this.generalExecutor, "General");
        this.shutdownExecutor(this.thumbnailExecutor, "Thumbnail");
        this.shutdownExecutor(this.loadingExecutor, "Loading");
        this.shutdownExecutor(this.scheduledExecutor, "Scheduled");
        this.activeTasks.clear();
        this.taskGroups.clear();
        LOGGER.info("DicomTaskManager shutdown completed");
    }

    private void shutdownExecutor(ExecutorService executor, String name) {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(5L, TimeUnit.SECONDS)) {
                LOGGER.warn("{} executor did not terminate gracefully, forcing shutdown", (Object)name);
                executor.shutdownNow();
                if (!executor.awaitTermination(2L, TimeUnit.SECONDS)) {
                    LOGGER.error("{} executor did not terminate after forced shutdown", (Object)name);
                }
            }
            LOGGER.debug("{} executor shutdown completed", (Object)name);
        }
        catch (InterruptedException e) {
            LOGGER.warn("{} executor shutdown interrupted", (Object)name);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static class DicomThreadFactory
    implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DicomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, this.namePrefix + this.threadNumber.getAndIncrement());
            t.setDaemon(true);
            t.setPriority(5);
            return t;
        }
    }

    public static enum TaskType {
        GENERAL,
        THUMBNAIL,
        LOADING;

    }

    public static class DicomTask {
        private final String id;
        private final String name;
        private final TaskType type;
        private final long creationTime;
        private long startTime;
        private long completionTime;
        private TaskStatus status;
        private Exception exception;
        private Future<?> future;

        public DicomTask(String id, String name, TaskType type) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.creationTime = System.currentTimeMillis();
            this.status = TaskStatus.PENDING;
        }

        public void markStarted() {
            this.startTime = System.currentTimeMillis();
            this.status = TaskStatus.RUNNING;
        }

        public void markCompleted() {
            this.completionTime = System.currentTimeMillis();
            this.status = TaskStatus.COMPLETED;
        }

        public void markFailed(Exception exception) {
            this.completionTime = System.currentTimeMillis();
            this.status = TaskStatus.FAILED;
            this.exception = exception;
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public TaskType getType() {
            return this.type;
        }

        public long getCreationTime() {
            return this.creationTime;
        }

        public long getStartTime() {
            return this.startTime;
        }

        public long getCompletionTime() {
            return this.completionTime;
        }

        public TaskStatus getStatus() {
            return this.status;
        }

        public Exception getException() {
            return this.exception;
        }

        public Future<?> getFuture() {
            return this.future;
        }

        public void setFuture(Future<?> future) {
            this.future = future;
        }

        public long getDuration() {
            if (this.startTime == 0L) {
                return 0L;
            }
            long endTime = this.completionTime > 0L ? this.completionTime : System.currentTimeMillis();
            return endTime - this.startTime;
        }

        public String toString() {
            return String.format("DicomTask[id=%s, name=%s, type=%s, status=%s]", new Object[]{this.id, this.name, this.type, this.status});
        }
    }

    public static class TaskGroup {
        private final String id;
        private final String name;
        private final List<TaskGroupItem> tasks = new ArrayList<TaskGroupItem>();
        private final long creationTime;

        public TaskGroup(String id, String name) {
            this.id = id;
            this.name = name;
            this.creationTime = System.currentTimeMillis();
        }

        public TaskGroup addTask(String taskName, Runnable task, TaskType type) {
            this.tasks.add(new TaskGroupItem(taskName, task, type));
            return this;
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public List<TaskGroupItem> getTasks() {
            return Collections.unmodifiableList(this.tasks);
        }

        public long getCreationTime() {
            return this.creationTime;
        }

        public int getTaskCount() {
            return this.tasks.size();
        }
    }

    public static class TaskGroupItem {
        private final String name;
        private final Runnable runnable;
        private final TaskType type;

        public TaskGroupItem(String name, Runnable runnable, TaskType type) {
            this.name = name;
            this.runnable = runnable;
            this.type = type;
        }

        public String getName() {
            return this.name;
        }

        public Runnable getRunnable() {
            return this.runnable;
        }

        public TaskType getType() {
            return this.type;
        }
    }

    public static enum TaskStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED;

    }
}

