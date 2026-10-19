/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.status;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatusUtil {
    StatusManager sm;

    public StatusUtil(StatusManager sm) {
        this.sm = sm;
    }

    public StatusUtil(Context context) {
        this.sm = context.getStatusManager();
    }

    public static boolean contextHasStatusListener(Context context) {
        StatusManager sm = context.getStatusManager();
        if (sm == null) {
            return false;
        }
        List<StatusListener> listeners = sm.getCopyOfStatusListenerList();
        return listeners != null && listeners.size() != 0;
    }

    public static List<Status> filterStatusListByTimeThreshold(List<Status> rawList, long threshold) {
        ArrayList<Status> filteredList = new ArrayList<Status>();
        for (Status s : rawList) {
            if (s.getTimestamp() < threshold) continue;
            filteredList.add(s);
        }
        return filteredList;
    }

    public void addStatus(Status status) {
        if (this.sm != null) {
            this.sm.add(status);
        }
    }

    public void addInfo(Object caller, String msg) {
        this.addStatus(new InfoStatus(msg, caller));
    }

    public void addWarn(Object caller, String msg) {
        this.addStatus(new WarnStatus(msg, caller));
    }

    public void addError(Object caller, String msg, Throwable t) {
        this.addStatus(new ErrorStatus(msg, caller, t));
    }

    public boolean hasXMLParsingErrors(long threshold) {
        return this.containsMatch(threshold, 2, "XML_PARSING");
    }

    public boolean noXMLParsingErrorsOccurred(long threshold) {
        return !this.hasXMLParsingErrors(threshold);
    }

    public int getHighestLevel(long threshold) {
        List<Status> filteredList = StatusUtil.filterStatusListByTimeThreshold(this.sm.getCopyOfStatusList(), threshold);
        int maxLevel = 0;
        for (Status s : filteredList) {
            if (s.getLevel() <= maxLevel) continue;
            maxLevel = s.getLevel();
        }
        return maxLevel;
    }

    public boolean isErrorFree(long threshold) {
        return this.getHighestLevel(threshold) < 2;
    }

    public boolean isWarningOrErrorFree(long threshold) {
        return 1 > this.getHighestLevel(threshold);
    }

    public boolean containsMatch(long threshold, int level, String regex) {
        List<Status> filteredList = StatusUtil.filterStatusListByTimeThreshold(this.sm.getCopyOfStatusList(), threshold);
        Pattern p = Pattern.compile(regex);
        for (Status status : filteredList) {
            String msg;
            Matcher matcher;
            if (level != status.getLevel() || !(matcher = p.matcher(msg = status.getMessage())).lookingAt()) continue;
            return true;
        }
        return false;
    }

    public boolean containsMatch(int level, String regex) {
        return this.containsMatch(0L, level, regex);
    }

    public boolean containsMatch(String regex) {
        Pattern p = Pattern.compile(regex);
        for (Status status : this.sm.getCopyOfStatusList()) {
            String msg = status.getMessage();
            Matcher matcher = p.matcher(msg);
            if (!matcher.lookingAt()) continue;
            return true;
        }
        return false;
    }

    public int levelCount(int level, long threshold) {
        List<Status> filteredList = StatusUtil.filterStatusListByTimeThreshold(this.sm.getCopyOfStatusList(), threshold);
        int count = 0;
        for (Status status : filteredList) {
            if (status.getLevel() != level) continue;
            ++count;
        }
        return count;
    }

    public int matchCount(String regex) {
        int count = 0;
        Pattern p = Pattern.compile(regex);
        for (Status status : this.sm.getCopyOfStatusList()) {
            String msg = status.getMessage();
            Matcher matcher = p.matcher(msg);
            if (!matcher.lookingAt()) continue;
            ++count;
        }
        return count;
    }

    public boolean containsException(Class<?> exceptionType) {
        return this.containsException(exceptionType, null);
    }

    public boolean containsException(Class<?> exceptionType, String msgRegex) {
        for (Status status : this.sm.getCopyOfStatusList()) {
            for (Throwable t = status.getThrowable(); t != null; t = t.getCause()) {
                if (!t.getClass().getName().equals(exceptionType.getName())) continue;
                if (msgRegex == null) {
                    return true;
                }
                if (!this.checkRegexMatch(t.getMessage(), msgRegex)) continue;
                return true;
            }
        }
        return false;
    }

    private boolean checkRegexMatch(String message, String msgRegex) {
        Pattern p = Pattern.compile(msgRegex);
        Matcher matcher = p.matcher(message);
        return matcher.lookingAt();
    }

    public long timeOfLastReset() {
        List<Status> statusList = this.sm.getCopyOfStatusList();
        if (statusList == null) {
            return -1L;
        }
        int len = statusList.size();
        for (int i = len - 1; i >= 0; --i) {
            Status s = statusList.get(i);
            if (!"Will reset and reconfigure context ".equals(s.getMessage())) continue;
            return s.getTimestamp();
        }
        return -1L;
    }

    public static String diff(Status left, Status right) {
        StringBuilder sb = new StringBuilder();
        if (left.getLevel() != right.getLevel()) {
            sb.append(" left.level ").append(left.getLevel()).append(" != right.level ").append(right.getLevel());
        }
        if (left.getTimestamp() != right.getTimestamp()) {
            sb.append(" left.timestamp ").append(left.getTimestamp()).append(" != right.timestamp ").append(right.getTimestamp());
        }
        if (!Objects.equals(left.getMessage(), right.getMessage())) {
            sb.append(" left.message ").append(left.getMessage()).append(" != right.message ").append(right.getMessage());
        }
        return sb.toString();
    }
}

