/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.io.Serializable;
import java.util.Date;

public class DateRange
implements Serializable {
    private static final long serialVersionUID = 88574297440294935L;
    private final Date start;
    private final Date end;

    public DateRange(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public final Date getStartDate() {
        return this.start;
    }

    public final Date getEndDate() {
        return this.end;
    }

    public boolean isStartDateExeedsEndDate() {
        return this.start != null && this.end != null && this.start.after(this.end);
    }

    public boolean contains(Date when) {
        return !(this.start != null && this.start.after(when) || this.end != null && this.end.before(when));
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DateRange)) {
            return false;
        }
        DateRange other = (DateRange)obj;
        return (this.start == null ? other.start == null : this.start.equals(other.start)) && (this.end == null ? other.end == null : this.end.equals(other.end));
    }

    public int hashCode() {
        int code = 0;
        if (this.start != null) {
            code = this.start.hashCode();
        }
        if (this.end != null) {
            code ^= this.start.hashCode();
        }
        return code;
    }

    public String toString() {
        return "[" + this.start + ", " + this.end + "]";
    }
}

