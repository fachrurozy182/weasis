/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.status;

import ch.qos.logback.core.status.Status;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public abstract class StatusBase
implements Status {
    private static final List<Status> EMPTY_LIST = new ArrayList<Status>(0);
    int level;
    final String message;
    final Object origin;
    List<Status> childrenList;
    Throwable throwable;
    long timestamp;

    StatusBase(int level, String msg, Object origin) {
        this(level, msg, origin, null);
    }

    StatusBase(int level, String msg, Object origin, Throwable t) {
        this.level = level;
        this.message = msg;
        this.origin = origin;
        this.throwable = t;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public synchronized void add(Status child) {
        if (child == null) {
            throw new NullPointerException("Null values are not valid Status.");
        }
        if (this.childrenList == null) {
            this.childrenList = new ArrayList<Status>();
        }
        this.childrenList.add(child);
    }

    @Override
    public synchronized boolean hasChildren() {
        return this.childrenList != null && this.childrenList.size() > 0;
    }

    @Override
    public synchronized Iterator<Status> iterator() {
        if (this.childrenList != null) {
            return this.childrenList.iterator();
        }
        return EMPTY_LIST.iterator();
    }

    @Override
    public synchronized boolean remove(Status statusToRemove) {
        if (this.childrenList == null) {
            return false;
        }
        return this.childrenList.remove(statusToRemove);
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public synchronized int getEffectiveLevel() {
        int result = this.level;
        Iterator<Status> it = this.iterator();
        while (it.hasNext()) {
            Status s = it.next();
            int effLevel = s.getEffectiveLevel();
            if (effLevel <= result) continue;
            result = effLevel;
        }
        return result;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public Object getOrigin() {
        return this.origin;
    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        switch (this.getEffectiveLevel()) {
            case 0: {
                buf.append("INFO");
                break;
            }
            case 1: {
                buf.append("WARN");
                break;
            }
            case 2: {
                buf.append("ERROR");
            }
        }
        if (this.origin != null) {
            buf.append(" in ");
            buf.append(this.origin);
            buf.append(" -");
        }
        buf.append(" ");
        buf.append(this.message);
        if (this.throwable != null) {
            buf.append(" ");
            buf.append(this.throwable);
        }
        return buf.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        StatusBase that = (StatusBase)o;
        return this.level == that.level && this.timestamp == that.timestamp && Objects.equals(this.message, that.message);
    }

    public int hashCode() {
        return Objects.hash(this.level, this.message, this.timestamp);
    }
}

