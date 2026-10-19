/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.spi;

import ch.qos.logback.classic.spi.ClassPackagingData;
import java.io.Serializable;
import java.util.Objects;

public class StackTraceElementProxy
implements Serializable {
    private static final long serialVersionUID = -2374374378980555982L;
    final StackTraceElement ste;
    private transient String steAsString;
    @Deprecated
    ClassPackagingData classPackagingData;
    static final StackTraceElement NA_SUBSTITUTE = new StackTraceElement("declaringClassNA", "methodNA", "fileNA", 1);

    public StackTraceElementProxy(StackTraceElement ste) {
        this.ste = Objects.requireNonNullElse(ste, NA_SUBSTITUTE);
    }

    public String getSTEAsString() {
        if (this.steAsString == null) {
            this.steAsString = "at " + this.ste.toString();
        }
        return this.steAsString;
    }

    public StackTraceElement getStackTraceElement() {
        return this.ste;
    }

    public void setClassPackagingData(ClassPackagingData cpd) {
        if (this.classPackagingData != null) {
            throw new IllegalStateException("Packaging data has been already set");
        }
        this.classPackagingData = cpd;
    }

    public ClassPackagingData getClassPackagingData() {
        return this.classPackagingData;
    }

    public int hashCode() {
        return this.ste.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        StackTraceElementProxy other = (StackTraceElementProxy)obj;
        if (!this.ste.equals(other.ste)) {
            return false;
        }
        return !(this.classPackagingData == null ? other.classPackagingData != null : !this.classPackagingData.equals(other.classPackagingData));
    }

    public String toString() {
        return this.getSTEAsString();
    }
}

