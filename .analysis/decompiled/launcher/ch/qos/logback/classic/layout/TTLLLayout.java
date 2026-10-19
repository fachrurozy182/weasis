/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.layout;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.util.CachingDateFormatter;
import java.util.List;
import org.slf4j.event.KeyValuePair;

public class TTLLLayout
extends LayoutBase<ILoggingEvent> {
    CachingDateFormatter cachingDateFormatter = new CachingDateFormatter("HH:mm:ss.SSS");
    ThrowableProxyConverter tpc = new ThrowableProxyConverter();
    static final char DOUBLE_QUOTE_CHAR = '\"';

    @Override
    public void start() {
        this.tpc.start();
        super.start();
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        if (!this.isStarted()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        long timestamp = event.getTimeStamp();
        sb.append(this.cachingDateFormatter.format(timestamp));
        sb.append(" [");
        sb.append(event.getThreadName());
        sb.append("] ");
        sb.append(event.getLevel().toString());
        sb.append(" ");
        sb.append(event.getLoggerName());
        sb.append(" -");
        this.kvp(event, sb);
        sb.append("- ");
        sb.append(event.getFormattedMessage());
        sb.append(CoreConstants.LINE_SEPARATOR);
        IThrowableProxy tp = event.getThrowableProxy();
        if (tp != null) {
            String stackTrace = this.tpc.convert(event);
            sb.append(stackTrace);
        }
        return sb.toString();
    }

    private void kvp(ILoggingEvent event, StringBuilder sb) {
        List<KeyValuePair> kvpList = event.getKeyValuePairs();
        if (kvpList == null || kvpList.isEmpty()) {
            return;
        }
        int len = kvpList.size();
        for (int i = 0; i < len; ++i) {
            KeyValuePair kvp = kvpList.get(i);
            if (i != 0) {
                sb.append(' ');
            }
            sb.append(String.valueOf(kvp.key));
            sb.append('=');
            sb.append('\"');
            sb.append(String.valueOf(kvp.value));
            sb.append('\"');
        }
    }
}

