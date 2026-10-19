/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.spi;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.EventArgUtil;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.spi.SequenceNumberGenerator;
import ch.qos.logback.core.util.EnvUtil;
import ch.qos.logback.core.util.StringUtil;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.MDCAdapter;

public class LoggingEvent
implements ILoggingEvent {
    public static final String VIRTUAL_THREAD_NAME_PREFIX = "virtual-";
    public static final String REGULAR_UNNAMED_THREAD_PREFIX = "unnamed-";
    transient String fqnOfLoggerClass;
    private String threadName;
    private String loggerName;
    private LoggerContext loggerContext;
    private LoggerContextVO loggerContextVO;
    private transient Level level;
    private String message;
    transient String formattedMessage;
    private transient Object[] argumentArray;
    private ThrowableProxy throwableProxy;
    private StackTraceElement[] callerDataArray;
    private List<Marker> markerList;
    private Map<String, String> mdcPropertyMap;
    List<KeyValuePair> keyValuePairs;
    private Instant instant;
    private long timeStamp;
    private int nanoseconds;
    private long sequenceNumber;

    public LoggingEvent() {
    }

    public LoggingEvent(String fqcn, Logger logger, Level level, String message, Throwable throwable, Object[] argArray) {
        SequenceNumberGenerator sequenceNumberGenerator;
        this.fqnOfLoggerClass = fqcn;
        this.loggerName = logger.getName();
        this.loggerContext = logger.getLoggerContext();
        this.loggerContextVO = this.loggerContext.getLoggerContextRemoteView();
        this.level = level;
        this.message = message;
        this.argumentArray = argArray;
        Instant instant = Clock.systemUTC().instant();
        this.initTmestampFields(instant);
        if (this.loggerContext != null && (sequenceNumberGenerator = this.loggerContext.getSequenceNumberGenerator()) != null) {
            this.sequenceNumber = sequenceNumberGenerator.nextSequenceNumber();
        }
        if (throwable == null) {
            throwable = this.extractThrowableAndRearrangeArguments(argArray);
        }
        if (throwable != null) {
            this.throwableProxy = new ThrowableProxy(throwable);
            if (this.loggerContext != null && this.loggerContext.isPackagingDataEnabled()) {
                this.throwableProxy.calculatePackagingData();
            }
        }
    }

    void initTmestampFields(Instant instant) {
        this.instant = instant;
        long epochSecond = instant.getEpochSecond();
        this.nanoseconds = instant.getNano();
        long milliseconds = this.nanoseconds / 1000000;
        this.timeStamp = epochSecond * 1000L + milliseconds;
    }

    private Throwable extractThrowableAndRearrangeArguments(Object[] argArray) {
        Throwable extractedThrowable = EventArgUtil.extractThrowable(argArray);
        if (EventArgUtil.successfulExtraction(extractedThrowable)) {
            this.argumentArray = EventArgUtil.trimmedCopy(argArray);
        }
        return extractedThrowable;
    }

    public void setArgumentArray(Object[] argArray) {
        if (this.argumentArray != null) {
            throw new IllegalStateException("argArray has been already set");
        }
        this.argumentArray = argArray;
    }

    @Override
    public Object[] getArgumentArray() {
        return this.argumentArray;
    }

    public void addKeyValuePair(KeyValuePair kvp) {
        if (this.keyValuePairs == null) {
            this.keyValuePairs = new ArrayList<KeyValuePair>(4);
        }
        this.keyValuePairs.add(kvp);
    }

    public void setKeyValuePairs(List<KeyValuePair> kvpList) {
        this.keyValuePairs = kvpList;
    }

    @Override
    public List<KeyValuePair> getKeyValuePairs() {
        return this.keyValuePairs;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    @Override
    public String getLoggerName() {
        return this.loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    @Override
    public String getThreadName() {
        if (this.threadName == null) {
            this.threadName = this.extractThreadName(Thread.currentThread());
        }
        return this.threadName;
    }

    private String extractThreadName(Thread aThread) {
        if (aThread == null) {
            return "?";
        }
        String threadName = aThread.getName();
        if (StringUtil.notNullNorEmpty(threadName)) {
            return threadName;
        }
        Long virtualThreadId = this.getVirtualThreadId(aThread);
        if (virtualThreadId != null) {
            return VIRTUAL_THREAD_NAME_PREFIX + virtualThreadId;
        }
        return REGULAR_UNNAMED_THREAD_PREFIX + aThread.getId();
    }

    Long getVirtualThreadId(Thread aThread) {
        if (EnvUtil.isJDK21OrHigher()) {
            try {
                Method isVirtualMethod = Thread.class.getMethod("isVirtual", new Class[0]);
                boolean isVirtual = (Boolean)isVirtualMethod.invoke(aThread, new Object[0]);
                if (isVirtual) {
                    return aThread.getId();
                }
            }
            catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                return null;
            }
        }
        return null;
    }

    public void setThreadName(String threadName) throws IllegalStateException {
        if (this.threadName != null) {
            throw new IllegalStateException("threadName has been already set");
        }
        this.threadName = threadName;
    }

    @Override
    public IThrowableProxy getThrowableProxy() {
        return this.throwableProxy;
    }

    public void setThrowableProxy(ThrowableProxy tp) {
        if (this.throwableProxy != null) {
            throw new IllegalStateException("ThrowableProxy has been already set.");
        }
        this.throwableProxy = tp;
    }

    @Override
    public void prepareForDeferredProcessing() {
        this.getFormattedMessage();
        this.getThreadName();
        this.getMDCPropertyMap();
    }

    public void setLoggerContext(LoggerContext lc) {
        this.loggerContext = lc;
    }

    @Override
    public LoggerContextVO getLoggerContextVO() {
        return this.loggerContextVO;
    }

    public void setLoggerContextRemoteView(LoggerContextVO loggerContextVO) {
        this.loggerContextVO = loggerContextVO;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        if (this.message != null) {
            throw new IllegalStateException("The message for this event has been set already.");
        }
        this.message = message;
    }

    @Override
    public Instant getInstant() {
        return this.instant;
    }

    public void setInstant(Instant instant) {
        this.initTmestampFields(instant);
    }

    @Override
    public long getTimeStamp() {
        return this.timeStamp;
    }

    @Override
    public int getNanoseconds() {
        return this.nanoseconds;
    }

    public void setTimeStamp(long timeStamp) {
        Instant instant = Instant.ofEpochMilli(timeStamp);
        this.setInstant(instant);
    }

    @Override
    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    public void setSequenceNumber(long sn) {
        this.sequenceNumber = sn;
    }

    public void setLevel(Level level) {
        if (this.level != null) {
            throw new IllegalStateException("The level has been already set for this event.");
        }
        this.level = level;
    }

    @Override
    public StackTraceElement[] getCallerData() {
        if (this.callerDataArray == null) {
            this.callerDataArray = CallerData.extract(new Throwable(), this.fqnOfLoggerClass, this.loggerContext.getMaxCallerDataDepth(), this.loggerContext.getFrameworkPackages());
        }
        return this.callerDataArray;
    }

    @Override
    public boolean hasCallerData() {
        return this.callerDataArray != null;
    }

    public void setCallerData(StackTraceElement[] callerDataArray) {
        this.callerDataArray = callerDataArray;
    }

    @Override
    public List<Marker> getMarkerList() {
        return this.markerList;
    }

    public void addMarker(Marker marker) {
        if (marker == null) {
            return;
        }
        if (this.markerList == null) {
            this.markerList = new ArrayList<Marker>(4);
        }
        this.markerList.add(marker);
    }

    public long getContextBirthTime() {
        return this.loggerContextVO.getBirthTime();
    }

    @Override
    public String getFormattedMessage() {
        if (this.formattedMessage != null) {
            return this.formattedMessage;
        }
        this.formattedMessage = this.argumentArray != null ? (this.throwableProxy == null ? MessageFormatter.arrayFormat(this.message, this.argumentArray).getMessage() : MessageFormatter.arrayFormat(this.message, this.argumentArray, null).getMessage()) : this.message;
        return this.formattedMessage;
    }

    @Override
    public Map<String, String> getMDCPropertyMap() {
        if (this.mdcPropertyMap == null) {
            MDCAdapter mdcAdapter = this.loggerContext.getMDCAdapter();
            this.mdcPropertyMap = mdcAdapter instanceof LogbackMDCAdapter ? ((LogbackMDCAdapter)mdcAdapter).getPropertyMap() : mdcAdapter.getCopyOfContextMap();
        }
        if (this.mdcPropertyMap == null) {
            this.mdcPropertyMap = Collections.emptyMap();
        }
        return this.mdcPropertyMap;
    }

    public void setMDCPropertyMap(Map<String, String> map) {
        if (this.mdcPropertyMap != null) {
            throw new IllegalStateException("The MDCPropertyMap has been already set for this event.");
        }
        this.mdcPropertyMap = map;
    }

    @Override
    public Map<String, String> getMdc() {
        return this.getMDCPropertyMap();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(this.level).append("] ");
        sb.append(this.getFormattedMessage());
        return sb.toString();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new UnsupportedOperationException(String.valueOf(this.getClass()) + " does not support serialization. Use LoggerEventVO instance instead. See also LoggerEventVO.build method.");
    }
}

