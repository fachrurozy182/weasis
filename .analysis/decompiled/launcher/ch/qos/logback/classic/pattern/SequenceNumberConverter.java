/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class SequenceNumberConverter
extends ClassicConverter {
    @Override
    public void start() {
        if (this.getContext() == null) {
            return;
        }
        if (this.getContext().getSequenceNumberGenerator() == null) {
            this.addWarn("It looks like no <sequenceNumberGenerator> was defined in Logback configuration.");
        }
        super.start();
    }

    @Override
    public String convert(ILoggingEvent event) {
        return Long.toString(event.getSequenceNumber());
    }
}

