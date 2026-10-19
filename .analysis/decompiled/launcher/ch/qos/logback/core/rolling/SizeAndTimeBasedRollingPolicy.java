/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling;

import ch.qos.logback.core.rolling.SizeAndTimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;

public class SizeAndTimeBasedRollingPolicy<E>
extends TimeBasedRollingPolicy<E> {
    static long MAX_COMPRESSION_FACTOR = 20L;
    FileSize maxFileSize;

    @Override
    public void start() {
        SizeAndTimeBasedFileNamingAndTriggeringPolicy sizeAndTimeBasedFNATP = new SizeAndTimeBasedFileNamingAndTriggeringPolicy(SizeAndTimeBasedFileNamingAndTriggeringPolicy.Usage.EMBEDDED);
        if (this.maxFileSize == null) {
            this.addError("maxFileSize property is mandatory.");
            return;
        }
        this.addInfo("Archive files will be limited to [" + String.valueOf(this.maxFileSize) + "] each.");
        sizeAndTimeBasedFNATP.setMaxFileSize(this.maxFileSize);
        this.timeBasedFileNamingAndTriggeringPolicy = sizeAndTimeBasedFNATP;
        if (!this.isUnboundedTotalSizeCap() && this.totalSizeCap.getSize() < this.maxFileSize.getSize() / MAX_COMPRESSION_FACTOR) {
            this.addWarn("totalSizeCap of [" + String.valueOf(this.totalSizeCap) + "] is much smaller than maxFileSize [" + String.valueOf(this.maxFileSize) + "] which is non-sensical, even taking compression into account.");
        }
        super.start();
    }

    public void setMaxFileSize(FileSize aMaxFileSize) {
        this.maxFileSize = aMaxFileSize;
    }

    @Override
    public String toString() {
        return "c.q.l.core.rolling.SizeAndTimeBasedRollingPolicy@" + this.hashCode();
    }
}

