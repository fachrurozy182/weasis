/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.model;

import ch.qos.logback.core.model.Model;
import java.util.Objects;

public class ConfigurationModel
extends Model {
    private static final long serialVersionUID = 1286156598561818515L;
    String debugStr;
    String scanStr;
    String scanPeriodStr;
    String packagingDataStr;

    @Override
    protected ConfigurationModel makeNewInstance() {
        return new ConfigurationModel();
    }

    @Override
    protected void mirror(Model that) {
        ConfigurationModel actual = (ConfigurationModel)that;
        super.mirror(that);
        this.debugStr = actual.debugStr;
        this.scanStr = actual.scanStr;
        this.scanPeriodStr = actual.scanPeriodStr;
        this.packagingDataStr = actual.packagingDataStr;
    }

    public String getDebugStr() {
        return this.debugStr;
    }

    public void setDebugStr(String debugStr) {
        this.debugStr = debugStr;
    }

    public String getScanStr() {
        return this.scanStr;
    }

    public void setScanStr(String scanStr) {
        this.scanStr = scanStr;
    }

    public String getScanPeriodStr() {
        return this.scanPeriodStr;
    }

    public void setScanPeriodStr(String scanPeriodStr) {
        this.scanPeriodStr = scanPeriodStr;
    }

    public String getPackagingDataStr() {
        return this.packagingDataStr;
    }

    public void setPackagingDataStr(String packagingDataStr) {
        this.packagingDataStr = packagingDataStr;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + Objects.hash(this.debugStr, this.packagingDataStr, this.scanPeriodStr, this.scanStr);
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
        ConfigurationModel other = (ConfigurationModel)obj;
        return Objects.equals(this.debugStr, other.debugStr) && Objects.equals(this.packagingDataStr, other.packagingDataStr) && Objects.equals(this.scanPeriodStr, other.scanPeriodStr) && Objects.equals(this.scanStr, other.scanStr);
    }
}

