/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import ch.qos.logback.core.rolling.TimeBasedFileNamingAndTriggeringPolicyBase;
import ch.qos.logback.core.rolling.helper.TimeBasedArchiveRemover;
import java.io.File;
import java.time.Instant;
import java.time.ZonedDateTime;

@NoAutoStart
public class DefaultTimeBasedFileNamingAndTriggeringPolicy<E>
extends TimeBasedFileNamingAndTriggeringPolicyBase<E> {
    @Override
    public void start() {
        super.start();
        if (!super.isErrorFree()) {
            return;
        }
        if (this.tbrp.fileNamePattern.hasIntegerTokenCOnverter()) {
            this.addError("Filename pattern [" + String.valueOf(this.tbrp.fileNamePattern) + "] contains an integer token converter, i.e. %i, INCOMPATIBLE with this configuration. Please remove it.");
            return;
        }
        this.archiveRemover = new TimeBasedArchiveRemover(this.tbrp.fileNamePattern, this.rc);
        this.archiveRemover.setContext(this.context);
        this.started = true;
    }

    @Override
    public boolean isTriggeringEvent(File activeFile, E event) {
        long localNextCheck;
        long currentTime = this.getCurrentTime();
        if (currentTime >= (localNextCheck = this.atomicNextCheck.get())) {
            long nextCheck = this.computeNextCheck(currentTime);
            this.atomicNextCheck.set(nextCheck);
            Instant instantOfElapsedPeriod = this.dateInCurrentPeriod;
            ZonedDateTime ztd = instantOfElapsedPeriod.atZone(this.zoneId);
            this.addInfo("Elapsed period: " + ztd.toString());
            this.elapsedPeriodsFileName = this.tbrp.fileNamePatternWithoutCompSuffix.convert(instantOfElapsedPeriod);
            this.setDateInCurrentPeriod(currentTime);
            return true;
        }
        return false;
    }

    public String toString() {
        return "c.q.l.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy";
    }
}

