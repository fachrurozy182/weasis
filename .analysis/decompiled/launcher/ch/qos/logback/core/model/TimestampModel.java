/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.NamedModel;
import java.util.Objects;

public class TimestampModel
extends NamedModel {
    private static final long serialVersionUID = 2096655273673863306L;
    public static final String CONTEXT_BIRTH = "contextBirth";
    String datePattern;
    String timeReference;
    String scopeStr;

    @Override
    protected TimestampModel makeNewInstance() {
        return new TimestampModel();
    }

    @Override
    protected void mirror(Model that) {
        TimestampModel actual = (TimestampModel)that;
        super.mirror(actual);
        this.datePattern = actual.datePattern;
        this.timeReference = actual.timeReference;
        this.scopeStr = actual.scopeStr;
    }

    public String getKey() {
        return this.getName();
    }

    public void setKey(String key) {
        this.setName(key);
    }

    public String getDatePattern() {
        return this.datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public String getTimeReference() {
        return this.timeReference;
    }

    public void setTimeReference(String timeReference) {
        this.timeReference = timeReference;
    }

    public String getScopeStr() {
        return this.scopeStr;
    }

    public void setScopeStr(String scopeStr) {
        this.scopeStr = scopeStr;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + Objects.hash(this.datePattern, this.scopeStr, this.timeReference);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        TimestampModel other = (TimestampModel)obj;
        return Objects.equals(this.datePattern, other.datePattern) && Objects.equals(this.scopeStr, other.scopeStr) && Objects.equals(this.timeReference, other.timeReference);
    }
}

