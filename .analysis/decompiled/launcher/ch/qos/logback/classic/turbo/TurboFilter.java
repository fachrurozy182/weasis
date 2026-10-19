/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.turbo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.spi.LifeCycle;
import java.util.List;
import org.slf4j.Marker;
import org.slf4j.event.LoggingEvent;

public abstract class TurboFilter
extends ContextAwareBase
implements LifeCycle {
    private String name;
    boolean start = false;

    public abstract FilterReply decide(Marker var1, Logger var2, Level var3, String var4, Object[] var5, Throwable var6);

    public FilterReply decide(Logger logger, LoggingEvent slf4jEvent) {
        List<Marker> markers = slf4jEvent.getMarkers();
        Marker firstMarker = markers != null && !markers.isEmpty() ? markers.get(0) : null;
        Level logbackLevel = Level.convertAnSLF4JLevel(slf4jEvent.getLevel());
        String format = slf4jEvent.getMessage();
        Object[] params = slf4jEvent.getArgumentArray();
        Throwable t = slf4jEvent.getThrowable();
        return this.decide(firstMarker, logger, logbackLevel, format, params, t);
    }

    @Override
    public void start() {
        this.start = true;
    }

    @Override
    public boolean isStarted() {
        return this.start;
    }

    @Override
    public void stop() {
        this.start = false;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

