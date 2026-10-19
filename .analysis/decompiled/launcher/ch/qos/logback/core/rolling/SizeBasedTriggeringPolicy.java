/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling;

import ch.qos.logback.core.rolling.TriggeringPolicyBase;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.InvocationGate;
import ch.qos.logback.core.util.SimpleInvocationGate;
import java.io.File;

public class SizeBasedTriggeringPolicy<E>
extends TriggeringPolicyBase<E> {
    public static final String SEE_SIZE_FORMAT = "http://logback.qos.ch/codes.html#sbtp_size_format";
    public static final long DEFAULT_MAX_FILE_SIZE = 0xA00000L;
    FileSize maxFileSize = new FileSize(0xA00000L);
    InvocationGate invocationGate = new SimpleInvocationGate();
    Duration checkIncrement = null;

    @Override
    public void start() {
        if (this.checkIncrement != null) {
            this.invocationGate = new SimpleInvocationGate(this.checkIncrement);
        }
        super.start();
    }

    @Override
    public boolean isTriggeringEvent(File activeFile, E event) {
        long now = System.currentTimeMillis();
        if (this.invocationGate.isTooSoon(now)) {
            return false;
        }
        return activeFile.length() >= this.maxFileSize.getSize();
    }

    public FileSize getMaxFileSize() {
        return this.maxFileSize;
    }

    public void setMaxFileSize(FileSize aMaxFileSize) {
        this.maxFileSize = aMaxFileSize;
    }

    public Duration getCheckIncrement() {
        return this.checkIncrement;
    }

    public void setCheckIncrement(Duration checkIncrement) {
        this.checkIncrement = checkIncrement;
    }
}

