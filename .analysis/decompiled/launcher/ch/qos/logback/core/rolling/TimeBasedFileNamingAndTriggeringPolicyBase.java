/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling;

import ch.qos.logback.core.rolling.TimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.helper.ArchiveRemover;
import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.RollingCalendar;
import ch.qos.logback.core.spi.ContextAwareBase;
import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

public abstract class TimeBasedFileNamingAndTriggeringPolicyBase<E>
extends ContextAwareBase
implements TimeBasedFileNamingAndTriggeringPolicy<E> {
    private static String COLLIDING_DATE_FORMAT_URL = "https://logback.qos.ch/codes.html#rfa_collision_in_dateFormat";
    protected TimeBasedRollingPolicy<E> tbrp;
    protected ArchiveRemover archiveRemover = null;
    protected String elapsedPeriodsFileName;
    protected RollingCalendar rc;
    protected long artificialCurrentTime = -1L;
    protected AtomicLong atomicNextCheck = new AtomicLong(0L);
    protected Instant dateInCurrentPeriod = null;
    protected boolean started = false;
    protected boolean errorFree = true;
    protected ZoneId zoneId = ZoneId.systemDefault();

    @Override
    public boolean isStarted() {
        return this.started;
    }

    @Override
    public void start() {
        File currentFile;
        DateTokenConverter<Object> dtc = this.tbrp.fileNamePattern.getPrimaryDateTokenConverter();
        if (dtc == null) {
            throw new IllegalStateException("FileNamePattern [" + this.tbrp.fileNamePattern.getPattern() + "] does not contain a valid DateToken");
        }
        if (dtc.getZoneId() != null) {
            this.zoneId = dtc.getZoneId();
            TimeZone tz = TimeZone.getTimeZone(this.zoneId);
            this.rc = new RollingCalendar(dtc.getDatePattern(), tz, Locale.getDefault());
        } else {
            this.rc = new RollingCalendar(dtc.getDatePattern());
        }
        this.addInfo("The date pattern is '" + dtc.getDatePattern() + "' from file name pattern '" + this.tbrp.fileNamePattern.getPattern() + "'.");
        this.rc.printPeriodicity(this);
        if (!this.rc.isCollisionFree()) {
            this.addError("The date format in FileNamePattern will result in collisions in the names of archived log files.");
            this.addError("For more information, please visit " + COLLIDING_DATE_FORMAT_URL);
            this.withErrors();
            return;
        }
        long timestamp = this.getCurrentTime();
        this.setDateInCurrentPeriod(timestamp);
        if (this.tbrp.getParentsRawFileProperty() != null && (currentFile = new File(this.tbrp.getParentsRawFileProperty())).canRead()) {
            timestamp = currentFile.lastModified();
            this.setDateInCurrentPeriod(timestamp);
        }
        this.addInfo("Setting initial period to " + String.valueOf(this.dateInCurrentPeriod));
        long nextCheck = this.computeNextCheck(timestamp);
        this.atomicNextCheck.set(nextCheck);
    }

    @Override
    public void stop() {
        this.started = false;
    }

    protected long computeNextCheck(long timestamp) {
        return this.rc.getNextTriggeringDate(Instant.ofEpochMilli(timestamp)).toEpochMilli();
    }

    @Override
    public String getElapsedPeriodsFileName() {
        return this.elapsedPeriodsFileName;
    }

    @Override
    public String getCurrentPeriodsFileNameWithoutCompressionSuffix() {
        return this.tbrp.fileNamePatternWithoutCompSuffix.convert(this.dateInCurrentPeriod);
    }

    protected void setDateInCurrentPeriod(long timestamp) {
        this.dateInCurrentPeriod = Instant.ofEpochMilli(timestamp);
    }

    @Override
    public void setCurrentTime(long timeInMillis) {
        this.artificialCurrentTime = timeInMillis;
    }

    @Override
    public long getCurrentTime() {
        if (this.artificialCurrentTime >= 0L) {
            return this.artificialCurrentTime;
        }
        return System.currentTimeMillis();
    }

    @Override
    public void setTimeBasedRollingPolicy(TimeBasedRollingPolicy<E> _tbrp) {
        this.tbrp = _tbrp;
    }

    @Override
    public ArchiveRemover getArchiveRemover() {
        return this.archiveRemover;
    }

    protected void withErrors() {
        this.errorFree = false;
    }

    protected boolean isErrorFree() {
        return this.errorFree;
    }
}

