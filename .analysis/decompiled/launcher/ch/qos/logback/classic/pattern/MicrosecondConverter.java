/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class MicrosecondConverter
extends ClassicConverter {
    @Override
    public String convert(ILoggingEvent event) {
        int nanos = event.getNanoseconds();
        int millis_and_micros = nanos / 1000;
        int micros = millis_and_micros % 1000;
        if (micros >= 100) {
            return Integer.toString(micros);
        }
        if (micros >= 10) {
            return "0" + Integer.toString(micros);
        }
        return "00" + Integer.toString(micros);
    }
}

