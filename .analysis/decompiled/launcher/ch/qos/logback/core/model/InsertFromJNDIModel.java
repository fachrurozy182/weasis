/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.model.Model;
import java.util.Objects;

public class InsertFromJNDIModel
extends Model {
    private static final long serialVersionUID = -7803377963650426197L;
    public static final String ENV_ENTRY_NAME_ATTR = "env-entry-name";
    public static final String AS_ATTR = "as";
    String as;
    String envEntryName;
    String scopeStr;

    @Override
    protected InsertFromJNDIModel makeNewInstance() {
        return new InsertFromJNDIModel();
    }

    @Override
    protected void mirror(Model that) {
        InsertFromJNDIModel actual = (InsertFromJNDIModel)that;
        super.mirror(actual);
        this.as = actual.as;
        this.envEntryName = actual.envEntryName;
        this.scopeStr = actual.scopeStr;
    }

    public String getScopeStr() {
        return this.scopeStr;
    }

    public void setScopeStr(String scopeStr) {
        this.scopeStr = scopeStr;
    }

    public String getAs() {
        return this.as;
    }

    public void setAs(String as) {
        this.as = as;
    }

    public String getEnvEntryName() {
        return this.envEntryName;
    }

    public void setEnvEntryName(String envEntryName) {
        this.envEntryName = envEntryName;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + Objects.hash(this.as, this.envEntryName, this.scopeStr);
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
        InsertFromJNDIModel other = (InsertFromJNDIModel)obj;
        return Objects.equals(this.as, other.as) && Objects.equals(this.envEntryName, other.envEntryName) && Objects.equals(this.scopeStr, other.scopeStr);
    }
}

