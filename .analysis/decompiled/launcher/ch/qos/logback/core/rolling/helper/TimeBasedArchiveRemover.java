/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling.helper;

import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.LiteralConverter;
import ch.qos.logback.core.rolling.helper.ArchiveRemover;
import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.rolling.helper.RollingCalendar;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.FileSize;
import java.io.File;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class TimeBasedArchiveRemover
extends ContextAwareBase
implements ArchiveRemover {
    protected static final long UNINITIALIZED = -1L;
    protected static final long INACTIVITY_TOLERANCE_IN_MILLIS = 2764800000L;
    static final int MAX_VALUE_FOR_INACTIVITY_PERIODS = 336;
    final FileNamePattern fileNamePattern;
    final RollingCalendar rc;
    private int maxHistory = 0;
    private long totalSizeCap = 0L;
    final boolean parentClean;
    long lastHeartBeat = -1L;
    int callCount = 0;

    public TimeBasedArchiveRemover(FileNamePattern fileNamePattern, RollingCalendar rc) {
        this.fileNamePattern = fileNamePattern;
        this.rc = rc;
        this.parentClean = this.computeParentCleaningFlag(fileNamePattern);
    }

    @Override
    public Future<?> cleanAsynchronously(Instant now) {
        ArchiveRemoverRunnable runnable = new ArchiveRemoverRunnable(now);
        ExecutorService alternateExecutorService = this.context.getAlternateExecutorService();
        Future<?> future = alternateExecutorService.submit(runnable);
        return future;
    }

    @Override
    public void clean(Instant now) {
        long nowInMillis = now.toEpochMilli();
        int periodsElapsed = this.computeElapsedPeriodsSinceLastClean(nowInMillis);
        this.lastHeartBeat = nowInMillis;
        if (periodsElapsed > 1) {
            this.addInfo("Multiple periods, i.e. " + periodsElapsed + " periods, seem to have elapsed. This can happen at application start.");
        }
        for (int i = 0; i < periodsElapsed; ++i) {
            int offset = this.getPeriodOffsetForDeletionTarget() - i;
            Instant instantOfPeriodToClean = this.rc.getEndOfNextNthPeriod(now, offset);
            this.cleanPeriod(instantOfPeriodToClean);
        }
    }

    protected File[] getFilesInPeriod(Instant instantOfPeriodToClean) {
        String filenameToDelete = this.fileNamePattern.convert(instantOfPeriodToClean);
        File file2Delete = new File(filenameToDelete);
        if (this.fileExistsAndIsFile(file2Delete)) {
            return new File[]{file2Delete};
        }
        return new File[0];
    }

    private boolean fileExistsAndIsFile(File file2Delete) {
        return file2Delete.exists() && file2Delete.isFile();
    }

    public void cleanPeriod(Instant instantOfPeriodToClean) {
        File[] matchingFileArray;
        for (File f : matchingFileArray = this.getFilesInPeriod(instantOfPeriodToClean)) {
            this.addInfo("deleting historically stale " + String.valueOf(f));
            this.checkAndDeleteFile(f);
        }
        if (this.parentClean && matchingFileArray.length > 0) {
            File parentDir = this.getParentDir(matchingFileArray[0]);
            this.removeFolderIfEmpty(parentDir);
        }
    }

    private boolean checkAndDeleteFile(File f) {
        if (f == null) {
            this.addWarn("Cannot delete empty file");
            return false;
        }
        if (!f.exists()) {
            this.addWarn("Cannot delete non existent file");
            return false;
        }
        boolean result = f.delete();
        if (!result) {
            this.addWarn("Failed to delete file " + f.toString());
        }
        return result;
    }

    void capTotalSize(Instant now) {
        long totalSize = 0L;
        long totalRemoved = 0L;
        int successfulDeletions = 0;
        int failedDeletions = 0;
        for (int offset = 0; offset < this.maxHistory; ++offset) {
            Instant instant = this.rc.getEndOfNextNthPeriod(now, -offset);
            File[] matchingFileArray = this.getFilesInPeriod(instant);
            this.descendingSort(matchingFileArray, instant);
            for (File f : matchingFileArray) {
                long size = f.length();
                if ((totalSize += size) <= this.totalSizeCap) continue;
                this.addInfo("Deleting [" + String.valueOf(f) + "] of size " + size + " on account of totalSizeCap " + this.totalSizeCap);
                boolean success = this.checkAndDeleteFile(f);
                if (success) {
                    ++successfulDeletions;
                    totalRemoved += size;
                    continue;
                }
                ++failedDeletions;
            }
        }
        if (successfulDeletions + failedDeletions == 0) {
            this.addInfo("No removal attempts were made on account of totalSizeCap=" + this.totalSizeCap);
        } else {
            this.addInfo("Removed  " + String.valueOf(new FileSize(totalRemoved)) + " of files in " + successfulDeletions + " files on account of totalSizeCap=" + this.totalSizeCap);
            if (failedDeletions != 0) {
                this.addInfo("There were " + failedDeletions + " failed deletion attempts.");
            }
        }
    }

    protected void descendingSort(File[] matchingFileArray, Instant instant) {
    }

    File getParentDir(File file) {
        File absolute = file.getAbsoluteFile();
        File parentDir = absolute.getParentFile();
        return parentDir;
    }

    int computeElapsedPeriodsSinceLastClean(long nowInMillis) {
        long periodsElapsed = 0L;
        if (this.lastHeartBeat == -1L) {
            this.addInfo("first clean up after appender initialization");
            periodsElapsed = this.rc.periodBarriersCrossed(nowInMillis, nowInMillis + 2764800000L);
            periodsElapsed = Math.min(periodsElapsed, 336L);
        } else {
            periodsElapsed = this.rc.periodBarriersCrossed(this.lastHeartBeat, nowInMillis);
        }
        return (int)periodsElapsed;
    }

    boolean computeParentCleaningFlag(FileNamePattern fileNamePattern) {
        Converter<Object> p;
        DateTokenConverter<Object> dtc = fileNamePattern.getPrimaryDateTokenConverter();
        if (dtc.getDatePattern().indexOf(47) != -1) {
            return true;
        }
        for (p = fileNamePattern.headTokenConverter; p != null && !(p instanceof DateTokenConverter); p = p.getNext()) {
        }
        while (p != null) {
            String s;
            if (p instanceof LiteralConverter && (s = p.convert(null)).indexOf(47) != -1) {
                return true;
            }
            p = p.getNext();
        }
        return false;
    }

    void removeFolderIfEmpty(File dir) {
        this.removeFolderIfEmpty(dir, 0);
    }

    private void removeFolderIfEmpty(File dir, int depth) {
        if (depth >= 3) {
            return;
        }
        if (dir.isDirectory() && FileFilterUtil.isEmptyDirectory(dir)) {
            this.addInfo("deleting folder [" + String.valueOf(dir) + "]");
            this.checkAndDeleteFile(dir);
            this.removeFolderIfEmpty(dir.getParentFile(), depth + 1);
        }
    }

    @Override
    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    protected int getPeriodOffsetForDeletionTarget() {
        return -this.maxHistory - 1;
    }

    @Override
    public void setTotalSizeCap(long totalSizeCap) {
        this.totalSizeCap = totalSizeCap;
    }

    public String toString() {
        return "c.q.l.core.rolling.helper.TimeBasedArchiveRemover";
    }

    public class ArchiveRemoverRunnable
    implements Runnable {
        Instant now;

        ArchiveRemoverRunnable(Instant now) {
            this.now = now;
        }

        @Override
        public void run() {
            TimeBasedArchiveRemover.this.clean(this.now);
            if (TimeBasedArchiveRemover.this.totalSizeCap != 0L && TimeBasedArchiveRemover.this.totalSizeCap > 0L) {
                TimeBasedArchiveRemover.this.capTotalSize(this.now);
            }
        }
    }
}

