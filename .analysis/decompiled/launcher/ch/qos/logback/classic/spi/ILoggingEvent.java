/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.spi;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

public interface ILoggingEvent
extends DeferredProcessingAware {
    public String getThreadName();

    public Level getLevel();

    public String getMessage();

    public Object[] getArgumentArray();

    public String getFormattedMessage();

    public String getLoggerName();

    public LoggerContextVO getLoggerContextVO();

    public IThrowableProxy getThrowableProxy();

    public StackTraceElement[] getCallerData();

    public boolean hasCallerData();

    default public Marker getMarker() {
        List<Marker> markers = this.getMarkerList();
        if (markers == null || markers.isEmpty()) {
            return null;
        }
        return markers.get(0);
    }

    public List<Marker> getMarkerList();

    public Map<String, String> getMDCPropertyMap();

    public Map<String, String> getMdc();

    public long getTimeStamp();

    public int getNanoseconds();

    default public Instant getInstant() {
        return Instant.ofEpochMilli(this.getTimeStamp());
    }

    public long getSequenceNumber();

    public List<KeyValuePair> getKeyValuePairs();

    @Override
    public void prepareForDeferredProcessing();
}

