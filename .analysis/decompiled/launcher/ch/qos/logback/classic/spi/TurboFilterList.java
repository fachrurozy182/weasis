/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.spi;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Marker;
import org.slf4j.event.LoggingEvent;

public final class TurboFilterList
extends CopyOnWriteArrayList<TurboFilter> {
    private static final long serialVersionUID = 1L;

    public FilterReply getTurboFilterChainDecision(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        int size = this.size();
        if (size == 0) {
            return FilterReply.NEUTRAL;
        }
        if (size == 1) {
            try {
                TurboFilter tf = (TurboFilter)this.get(0);
                return tf.decide(marker, logger, level, format, params, t);
            }
            catch (IndexOutOfBoundsException iobe) {
                return FilterReply.NEUTRAL;
            }
        }
        for (TurboFilter tf : this) {
            FilterReply r = tf.decide(marker, logger, level, format, params, t);
            if (r != FilterReply.DENY && r != FilterReply.ACCEPT) continue;
            return r;
        }
        return FilterReply.NEUTRAL;
    }

    public FilterReply getTurboFilterChainDecision(Logger logger, LoggingEvent slf4jEvent) {
        int size = this.size();
        if (size == 0) {
            return FilterReply.NEUTRAL;
        }
        if (size == 1) {
            try {
                TurboFilter tf = (TurboFilter)this.get(0);
                return tf.decide(logger, slf4jEvent);
            }
            catch (IndexOutOfBoundsException iobe) {
                return FilterReply.NEUTRAL;
            }
        }
        for (TurboFilter tf : this) {
            FilterReply r = tf.decide(logger, slf4jEvent);
            if (r != FilterReply.DENY && r != FilterReply.ACCEPT) continue;
            return r;
        }
        return FilterReply.NEUTRAL;
    }
}

