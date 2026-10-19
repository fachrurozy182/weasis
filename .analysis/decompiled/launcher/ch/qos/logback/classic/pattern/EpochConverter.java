/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class EpochConverter
extends ClassicConverter {
    boolean inUnitsOfSeconds = false;

    @Override
    public void start() {
        String millisOrSecondsStr = this.getFirstOption();
        if ("seconds".equalsIgnoreCase(millisOrSecondsStr)) {
            this.inUnitsOfSeconds = true;
        }
        super.start();
    }

    @Override
    public String convert(ILoggingEvent event) {
        if (this.inUnitsOfSeconds) {
            return Long.toString(event.getTimeStamp() / 1000L);
        }
        return Long.toString(event.getTimeStamp());
    }
}

