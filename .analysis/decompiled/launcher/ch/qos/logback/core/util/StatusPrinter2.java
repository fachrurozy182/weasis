/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.helpers.ThrowableToStringArray;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.util.CachingDateFormatter;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

public class StatusPrinter2 {
    private PrintStream ps = System.out;
    static CachingDateFormatter cachingDateFormat = new CachingDateFormatter("HH:mm:ss,SSS");

    public void setPrintStream(PrintStream printStream) {
        this.ps = printStream;
    }

    public void printInCaseOfErrorsOrWarnings(Context context) {
        this.printInCaseOfErrorsOrWarnings(context, 0L);
    }

    public void printInCaseOfErrorsOrWarnings(Context context, long threshold) {
        if (context == null) {
            throw new IllegalArgumentException("Context argument cannot be null");
        }
        StatusManager sm = context.getStatusManager();
        if (sm == null) {
            this.ps.println("WARN: Context named \"" + context.getName() + "\" has no status manager");
        } else {
            StatusUtil statusUtil = new StatusUtil(context);
            if (statusUtil.getHighestLevel(threshold) >= 1) {
                this.print(sm, threshold);
            }
        }
    }

    public void printIfErrorsOccured(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context argument cannot be null");
        }
        StatusManager sm = context.getStatusManager();
        if (sm == null) {
            this.ps.println("WARN: Context named \"" + context.getName() + "\" has no status manager");
        } else {
            StatusUtil statusUtil = new StatusUtil(context);
            if (statusUtil.getHighestLevel(0L) == 2) {
                this.print(sm);
            }
        }
    }

    public void print(Context context) {
        this.print(context, 0L);
    }

    public void print(Context context, long threshold) {
        if (context == null) {
            throw new IllegalArgumentException("Context argument cannot be null");
        }
        StatusManager sm = context.getStatusManager();
        if (sm == null) {
            this.ps.println("WARN: Context named \"" + context.getName() + "\" has no status manager");
        } else {
            this.print(sm, threshold);
        }
    }

    public void print(StatusManager sm) {
        this.print(sm, 0L);
    }

    public void print(StatusManager sm, long threshold) {
        StringBuilder sb = new StringBuilder();
        List<Status> filteredList = StatusUtil.filterStatusListByTimeThreshold(sm.getCopyOfStatusList(), threshold);
        this.buildStrFromStatusList(sb, filteredList);
        this.ps.println(sb.toString());
    }

    public void print(List<Status> statusList) {
        StringBuilder sb = new StringBuilder();
        this.buildStrFromStatusList(sb, statusList);
        this.ps.println(sb.toString());
    }

    private void buildStrFromStatusList(StringBuilder sb, List<Status> statusList) {
        if (statusList == null) {
            return;
        }
        for (Status s : statusList) {
            this.buildStr(sb, "", s);
        }
    }

    private void appendThrowable(StringBuilder sb, Throwable t) {
        String[] stringRep;
        for (String s : stringRep = ThrowableToStringArray.convert(t)) {
            if (!s.startsWith("Caused by: ")) {
                if (Character.isDigit(s.charAt(0))) {
                    sb.append("\t... ");
                } else {
                    sb.append("\tat ");
                }
            }
            sb.append(s).append(CoreConstants.LINE_SEPARATOR);
        }
    }

    public void buildStr(StringBuilder sb, String indentation, Status s) {
        String prefix = s.hasChildren() ? indentation + "+ " : indentation + "|-";
        if (cachingDateFormat != null) {
            String dateStr = cachingDateFormat.format(s.getTimestamp());
            sb.append(dateStr).append(" ");
        }
        sb.append(prefix).append(s).append(CoreConstants.LINE_SEPARATOR);
        if (s.getThrowable() != null) {
            this.appendThrowable(sb, s.getThrowable());
        }
        if (s.hasChildren()) {
            Iterator<Status> ite = s.iterator();
            while (ite.hasNext()) {
                Status child = ite.next();
                this.buildStr(sb, indentation + "  ", child);
            }
        }
    }
}

