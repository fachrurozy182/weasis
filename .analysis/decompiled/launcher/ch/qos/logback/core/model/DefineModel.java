/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.NamedComponentModel;
import java.util.Objects;

public class DefineModel
extends NamedComponentModel {
    private static final long serialVersionUID = 6209642548924431065L;
    String scopeStr;

    @Override
    protected DefineModel makeNewInstance() {
        return new DefineModel();
    }

    @Override
    protected void mirror(Model that) {
        DefineModel actual = (DefineModel)that;
        super.mirror(actual);
        this.scopeStr = actual.scopeStr;
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
        result = 31 * result + Objects.hash(this.scopeStr);
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
        DefineModel other = (DefineModel)obj;
        return Objects.equals(this.scopeStr, other.scopeStr);
    }
}

