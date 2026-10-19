/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.log4j;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.helpers.Transform;
import java.util.Map;
import java.util.Set;

public class XMLLayout
extends LayoutBase<ILoggingEvent> {
    private static final int DEFAULT_SIZE = 256;
    private boolean locationInfo = false;
    private boolean properties = false;

    @Override
    public void start() {
        super.start();
    }

    public void setLocationInfo(boolean flag) {
        this.locationInfo = flag;
    }

    public boolean getLocationInfo() {
        return this.locationInfo;
    }

    public void setProperties(boolean flag) {
        this.properties = flag;
    }

    public boolean getProperties() {
        return this.properties;
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        Map<String, String> propertyMap;
        StackTraceElement[] callerDataArray;
        StringBuilder buf = new StringBuilder(256);
        buf.append("<log4j:event logger=\"");
        buf.append(Transform.escapeTags(event.getLoggerName()));
        buf.append("\"\r\n");
        buf.append("             timestamp=\"");
        buf.append(event.getTimeStamp());
        buf.append("\" level=\"");
        buf.append(event.getLevel());
        buf.append("\" thread=\"");
        buf.append(Transform.escapeTags(event.getThreadName()));
        buf.append("\">\r\n");
        buf.append("  <log4j:message>");
        buf.append(Transform.escapeTags(event.getFormattedMessage()));
        buf.append("</log4j:message>\r\n");
        IThrowableProxy tp = event.getThrowableProxy();
        if (tp != null) {
            StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
            buf.append("  <log4j:throwable><![CDATA[");
            for (StackTraceElementProxy step : stepArray) {
                buf.append('\t');
                buf.append(step.toString());
                buf.append("\r\n");
            }
            buf.append("]]></log4j:throwable>\r\n");
        }
        if (this.locationInfo && (callerDataArray = event.getCallerData()) != null && callerDataArray.length > 0) {
            StackTraceElement immediateCallerData = callerDataArray[0];
            buf.append("  <log4j:locationInfo class=\"");
            buf.append(immediateCallerData.getClassName());
            buf.append("\"\r\n");
            buf.append("                      method=\"");
            buf.append(Transform.escapeTags(immediateCallerData.getMethodName()));
            buf.append("\" file=\"");
            buf.append(Transform.escapeTags(immediateCallerData.getFileName()));
            buf.append("\" line=\"");
            buf.append(immediateCallerData.getLineNumber());
            buf.append("\"/>\r\n");
        }
        if (this.getProperties() && (propertyMap = event.getMDCPropertyMap()) != null && propertyMap.size() != 0) {
            Set<Map.Entry<String, String>> entrySet = propertyMap.entrySet();
            buf.append("  <log4j:properties>");
            for (Map.Entry<String, String> entry : entrySet) {
                buf.append("\r\n    <log4j:data");
                buf.append(" name='" + Transform.escapeTags(entry.getKey()) + "'");
                buf.append(" value='" + Transform.escapeTags(entry.getValue()) + "'");
                buf.append(" />");
            }
            buf.append("\r\n  </log4j:properties>");
        }
        buf.append("\r\n</log4j:event>\r\n\r\n");
        return buf.toString();
    }

    @Override
    public String getContentType() {
        return "text/xml";
    }
}

