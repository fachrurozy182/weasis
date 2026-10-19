/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.encoder;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.EncoderBase;
import ch.qos.logback.core.encoder.JsonEscapeUtil;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

public class JsonEncoder
extends EncoderBase<ILoggingEvent> {
    static final boolean DO_NOT_ADD_QUOTE_KEY = false;
    static final boolean ADD_QUOTE_KEY = true;
    static int DEFAULT_SIZE = 1024;
    static int DEFAULT_SIZE_WITH_THROWABLE = DEFAULT_SIZE * 8;
    static byte[] EMPTY_BYTES = new byte[0];
    public static final String CONTEXT_ATTR_NAME = "context";
    public static final String NAME_ATTR_NAME = "name";
    public static final String BIRTHDATE_ATTR_NAME = "birthdate";
    public static final String CONTEXT_PROPERTIES_ATTR_NAME = "properties";
    public static final String TIMESTAMP_ATTR_NAME = "timestamp";
    public static final String NANOSECONDS_ATTR_NAME = "nanoseconds";
    public static final String SEQUENCE_NUMBER_ATTR_NAME = "sequenceNumber";
    public static final String LEVEL_ATTR_NAME = "level";
    public static final String MARKERS_ATTR_NAME = "markers";
    public static final String THREAD_NAME_ATTR_NAME = "threadName";
    public static final String MDC_ATTR_NAME = "mdc";
    public static final String LOGGER_ATTR_NAME = "loggerName";
    public static final String MESSAGE_ATTR_NAME = "message";
    public static final String FORMATTED_MESSAGE_ATTR_NAME = "formattedMessage";
    public static final String ARGUMENT_ARRAY_ATTR_NAME = "arguments";
    public static final String KEY_VALUE_PAIRS_ATTR_NAME = "kvpList";
    public static final String THROWABLE_ATTR_NAME = "throwable";
    private static final String CYCLIC_THROWABLE_ATTR_NAME = "cyclic";
    public static final String CAUSE_ATTR_NAME = "cause";
    public static final String SUPPRESSED_ATTR_NAME = "suppressed";
    public static final String COMMON_FRAMES_COUNT_ATTR_NAME = "commonFramesCount";
    public static final String CLASS_NAME_ATTR_NAME = "className";
    public static final String METHOD_NAME_ATTR_NAME = "methodName";
    private static final String FILE_NAME_ATTR_NAME = "fileName";
    private static final String LINE_NUMBER_ATTR_NAME = "lineNumber";
    public static final String STEP_ARRAY_NAME_ATTRIBUTE = "stepArray";
    protected static final char OPEN_OBJ = '{';
    protected static final char CLOSE_OBJ = '}';
    protected static final char OPEN_ARRAY = '[';
    protected static final char CLOSE_ARRAY = ']';
    protected static final char QUOTE = '\"';
    protected static final char SP = ' ';
    protected static final char ENTRY_SEPARATOR = ':';
    protected static final String COL_SP = ": ";
    protected static final String QUOTE_COL = "\":";
    protected static final char VALUE_SEPARATOR = ',';
    private boolean withSequenceNumber = true;
    private boolean withTimestamp = true;
    private boolean withNanoseconds = true;
    private boolean withLevel = true;
    private boolean withThreadName = true;
    private boolean withLoggerName = true;
    private boolean withContext = true;
    private boolean withMarkers = true;
    private boolean withMDC = true;
    private boolean withKVPList = true;
    private boolean withMessage = true;
    private boolean withArguments = true;
    private boolean withThrowable = true;
    private boolean withFormattedMessage = false;

    @Override
    public byte[] headerBytes() {
        return EMPTY_BYTES;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        int initialCapacity = event.getThrowableProxy() == null ? DEFAULT_SIZE : DEFAULT_SIZE_WITH_THROWABLE;
        StringBuilder sb = new StringBuilder(initialCapacity);
        sb.append('{');
        if (this.withSequenceNumber) {
            this.appenderMemberWithLongValue(sb, SEQUENCE_NUMBER_ATTR_NAME, event.getSequenceNumber());
        }
        if (this.withTimestamp) {
            this.appendValueSeparator(sb, this.withSequenceNumber);
            this.appenderMemberWithLongValue(sb, TIMESTAMP_ATTR_NAME, event.getTimeStamp());
        }
        if (this.withNanoseconds) {
            this.appendValueSeparator(sb, this.withSequenceNumber, this.withTimestamp);
            this.appenderMemberWithLongValue(sb, NANOSECONDS_ATTR_NAME, event.getNanoseconds());
        }
        if (this.withLevel) {
            this.appendValueSeparator(sb, this.withNanoseconds, this.withSequenceNumber, this.withTimestamp);
            String levelStr = event.getLevel() != null ? event.getLevel().levelStr : "null";
            this.appenderMember(sb, LEVEL_ATTR_NAME, levelStr);
        }
        if (this.withThreadName) {
            this.appendValueSeparator(sb, this.withLevel, this.withNanoseconds, this.withSequenceNumber, this.withTimestamp);
            this.appenderMember(sb, THREAD_NAME_ATTR_NAME, this.jsonEscape(event.getThreadName()));
        }
        if (this.withLoggerName) {
            this.appendValueSeparator(sb, this.withThreadName, this.withLevel, this.withNanoseconds, this.withSequenceNumber, this.withTimestamp);
            this.appenderMember(sb, LOGGER_ATTR_NAME, event.getLoggerName());
        }
        if (this.withContext) {
            sb.append(',');
            this.appendLoggerContext(sb, event.getLoggerContextVO());
        }
        if (this.withMarkers) {
            this.appendMarkers(sb, event);
        }
        if (this.withMDC) {
            this.appendMDC(sb, event);
        }
        if (this.withKVPList) {
            this.appendKeyValuePairs(sb, event);
        }
        if (this.withMessage) {
            sb.append(',');
            this.appenderMember(sb, MESSAGE_ATTR_NAME, this.jsonEscape(event.getMessage()));
        }
        if (this.withFormattedMessage) {
            sb.append(',');
            this.appenderMember(sb, FORMATTED_MESSAGE_ATTR_NAME, this.jsonEscape(event.getFormattedMessage()));
        }
        if (this.withArguments) {
            this.appendArgumentArray(sb, event);
        }
        if (this.withThrowable) {
            this.appendThrowableProxy(sb, THROWABLE_ATTR_NAME, event.getThrowableProxy());
        }
        this.appendCustomFields(sb, event);
        sb.append('}');
        sb.append('\n');
        return sb.toString().getBytes(CoreConstants.UTF_8_CHARSET);
    }

    protected void appendValueSeparator(StringBuilder sb, boolean ... subsequentConditionals) {
        boolean enabled = false;
        for (boolean subsequent : subsequentConditionals) {
            if (!subsequent) continue;
            enabled = true;
            break;
        }
        if (enabled) {
            sb.append(',');
        }
    }

    protected void appendLoggerContext(StringBuilder sb, LoggerContextVO loggerContextVO) {
        sb.append('\"').append(CONTEXT_ATTR_NAME).append(QUOTE_COL);
        if (loggerContextVO == null) {
            sb.append("null");
            return;
        }
        sb.append('{');
        this.appenderMember(sb, NAME_ATTR_NAME, this.nullSafeStr(loggerContextVO.getName()));
        sb.append(',');
        this.appenderMemberWithLongValue(sb, BIRTHDATE_ATTR_NAME, loggerContextVO.getBirthTime());
        sb.append(',');
        this.appendMap(sb, CONTEXT_PROPERTIES_ATTR_NAME, loggerContextVO.getPropertyMap());
        sb.append('}');
    }

    protected void appendMap(StringBuilder sb, String attrName, Map<String, String> map) {
        sb.append('\"').append(attrName).append(QUOTE_COL);
        if (map == null) {
            sb.append("null");
            return;
        }
        sb.append('{');
        boolean addComma = false;
        Set<Map.Entry<String, String>> entries = map.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            if (addComma) {
                sb.append(',');
            }
            addComma = true;
            this.appenderMember(sb, this.jsonEscapedToString(entry.getKey()), this.jsonEscapedToString(entry.getValue()));
        }
        sb.append('}');
    }

    protected void appendThrowableProxy(StringBuilder sb, String attributeName, IThrowableProxy itp) {
        this.appendThrowableProxy(sb, attributeName, itp, true);
    }

    protected void appendThrowableProxy(StringBuilder sb, String attributeName, IThrowableProxy itp, boolean appendValueSeparator) {
        IThrowableProxy[] suppressedArray;
        IThrowableProxy cause;
        if (appendValueSeparator) {
            sb.append(',');
        }
        if (attributeName != null) {
            sb.append('\"').append(attributeName).append(QUOTE_COL);
            if (itp == null) {
                sb.append("null");
                return;
            }
        }
        sb.append('{');
        this.appenderMember(sb, CLASS_NAME_ATTR_NAME, this.nullSafeStr(itp.getClassName()));
        sb.append(',');
        this.appenderMember(sb, MESSAGE_ATTR_NAME, this.jsonEscape(itp.getMessage()));
        if (itp.isCyclic()) {
            sb.append(',');
            this.appenderMember(sb, CYCLIC_THROWABLE_ATTR_NAME, this.jsonEscape("true"));
        }
        sb.append(',');
        this.appendSTEPArray(sb, itp.getStackTraceElementProxyArray(), itp.getCommonFrames());
        if (itp.getCommonFrames() != 0) {
            sb.append(',');
            this.appenderMemberWithIntValue(sb, COMMON_FRAMES_COUNT_ATTR_NAME, itp.getCommonFrames());
        }
        if ((cause = itp.getCause()) != null) {
            this.appendThrowableProxy(sb, CAUSE_ATTR_NAME, cause);
        }
        if ((suppressedArray = itp.getSuppressed()) != null && suppressedArray.length != 0) {
            sb.append(',');
            sb.append('\"').append(SUPPRESSED_ATTR_NAME).append(QUOTE_COL);
            sb.append('[');
            boolean first = true;
            for (IThrowableProxy suppressedITP : suppressedArray) {
                this.appendThrowableProxy(sb, null, suppressedITP, !first);
                if (!first) continue;
                first = false;
            }
            sb.append(']');
        }
        sb.append('}');
    }

    protected void appendSTEPArray(StringBuilder sb, StackTraceElementProxy[] stepArray, int commonFrames) {
        sb.append('\"').append(STEP_ARRAY_NAME_ATTRIBUTE).append(QUOTE_COL).append('[');
        if (stepArray == null || stepArray.length == 0) {
            sb.append(']');
            return;
        }
        int len = stepArray.length;
        if (commonFrames >= len) {
            commonFrames = 0;
        }
        for (int i = 0; i < len - commonFrames; ++i) {
            if (i != 0) {
                sb.append(',');
            }
            StackTraceElementProxy step = stepArray[i];
            sb.append('{');
            StackTraceElement ste = step.getStackTraceElement();
            this.appenderMember(sb, CLASS_NAME_ATTR_NAME, this.nullSafeStr(ste.getClassName()));
            sb.append(',');
            this.appenderMember(sb, METHOD_NAME_ATTR_NAME, this.nullSafeStr(ste.getMethodName()));
            sb.append(',');
            this.appenderMember(sb, FILE_NAME_ATTR_NAME, this.nullSafeStr(ste.getFileName()));
            sb.append(',');
            this.appenderMemberWithIntValue(sb, LINE_NUMBER_ATTR_NAME, ste.getLineNumber());
            sb.append('}');
        }
        sb.append(']');
    }

    protected void appendCustomFields(StringBuilder sb, ILoggingEvent event) {
    }

    protected void appenderMember(StringBuilder sb, String key, String value) {
        sb.append('\"').append(key).append(QUOTE_COL).append('\"').append(value).append('\"');
    }

    protected void appenderMemberWithIntValue(StringBuilder sb, String key, int value) {
        sb.append('\"').append(key).append(QUOTE_COL).append(value);
    }

    protected void appenderMemberWithLongValue(StringBuilder sb, String key, long value) {
        sb.append('\"').append(key).append(QUOTE_COL).append(value);
    }

    protected void appendKeyValuePairs(StringBuilder sb, ILoggingEvent event) {
        List<KeyValuePair> kvpList = event.getKeyValuePairs();
        if (kvpList == null || kvpList.isEmpty()) {
            return;
        }
        sb.append(',');
        sb.append('\"').append(KEY_VALUE_PAIRS_ATTR_NAME).append(QUOTE_COL).append(' ').append('[');
        int len = kvpList.size();
        for (int i = 0; i < len; ++i) {
            if (i != 0) {
                sb.append(',');
            }
            KeyValuePair kvp = kvpList.get(i);
            sb.append('{');
            this.appenderMember(sb, this.jsonEscapedToString(kvp.key), this.jsonEscapedToString(kvp.value));
            sb.append('}');
        }
        sb.append(']');
    }

    protected void appendArgumentArray(StringBuilder sb, ILoggingEvent event) {
        Object[] argumentArray = event.getArgumentArray();
        if (argumentArray == null) {
            return;
        }
        sb.append(',');
        sb.append('\"').append(ARGUMENT_ARRAY_ATTR_NAME).append(QUOTE_COL).append(' ').append('[');
        int len = argumentArray.length;
        for (int i = 0; i < len; ++i) {
            if (i != 0) {
                sb.append(',');
            }
            sb.append('\"').append(this.jsonEscapedToString(argumentArray[i])).append('\"');
        }
        sb.append(']');
    }

    protected void appendMarkers(StringBuilder sb, ILoggingEvent event) {
        List<Marker> markerList = event.getMarkerList();
        if (markerList == null) {
            return;
        }
        sb.append(',');
        sb.append('\"').append(MARKERS_ATTR_NAME).append(QUOTE_COL).append(' ').append('[');
        int len = markerList.size();
        for (int i = 0; i < len; ++i) {
            if (i != 0) {
                sb.append(',');
            }
            sb.append('\"').append(this.jsonEscapedToString(markerList.get(i))).append('\"');
        }
        sb.append(']');
    }

    private String jsonEscapedToString(Object o) {
        if (o == null) {
            return "null";
        }
        return JsonEscapeUtil.jsonEscapeString(o.toString());
    }

    private String nullSafeStr(String s) {
        if (s == null) {
            return "null";
        }
        return s;
    }

    private String jsonEscape(String s) {
        if (s == null) {
            return "null";
        }
        return JsonEscapeUtil.jsonEscapeString(s);
    }

    protected void appendMDC(StringBuilder sb, ILoggingEvent event) {
        Map<String, String> map = event.getMDCPropertyMap();
        sb.append(',');
        sb.append('\"').append(MDC_ATTR_NAME).append(QUOTE_COL).append(' ').append('{');
        if (this.isNotEmptyMap(map)) {
            Set<Map.Entry<String, String>> entrySet = map.entrySet();
            int i = 0;
            for (Map.Entry<String, String> entry : entrySet) {
                if (i != 0) {
                    sb.append(',');
                }
                this.appenderMember(sb, this.jsonEscapedToString(entry.getKey()), this.jsonEscapedToString(entry.getValue()));
                ++i;
            }
        }
        sb.append('}');
    }

    boolean isNotEmptyMap(Map<?, ?> map) {
        if (map == null) {
            return false;
        }
        return !map.isEmpty();
    }

    @Override
    public byte[] footerBytes() {
        return EMPTY_BYTES;
    }

    public void setWithSequenceNumber(boolean withSequenceNumber) {
        this.withSequenceNumber = withSequenceNumber;
    }

    public void setWithTimestamp(boolean withTimestamp) {
        this.withTimestamp = withTimestamp;
    }

    public void setWithNanoseconds(boolean withNanoseconds) {
        this.withNanoseconds = withNanoseconds;
    }

    public void setWithLevel(boolean withLevel) {
        this.withLevel = withLevel;
    }

    public void setWithThreadName(boolean withThreadName) {
        this.withThreadName = withThreadName;
    }

    public void setWithLoggerName(boolean withLoggerName) {
        this.withLoggerName = withLoggerName;
    }

    public void setWithContext(boolean withContext) {
        this.withContext = withContext;
    }

    public void setWithMarkers(boolean withMarkers) {
        this.withMarkers = withMarkers;
    }

    public void setWithMDC(boolean withMDC) {
        this.withMDC = withMDC;
    }

    public void setWithKVPList(boolean withKVPList) {
        this.withKVPList = withKVPList;
    }

    public void setWithMessage(boolean withMessage) {
        this.withMessage = withMessage;
    }

    public void setWithArguments(boolean withArguments) {
        this.withArguments = withArguments;
    }

    public void setWithThrowable(boolean withThrowable) {
        this.withThrowable = withThrowable;
    }

    public void setWithFormattedMessage(boolean withFormattedMessage) {
        this.withFormattedMessage = withFormattedMessage;
    }
}

