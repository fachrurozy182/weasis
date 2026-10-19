/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.model.Model;
import java.util.Objects;

public class ComponentModel
extends Model {
    private static final long serialVersionUID = -7117814935763453139L;
    String className;

    @Override
    protected ComponentModel makeNewInstance() {
        return new ComponentModel();
    }

    @Override
    protected void mirror(Model that) {
        ComponentModel actual = (ComponentModel)that;
        super.mirror(actual);
        this.className = actual.className;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [tag=" + this.tag + ", className=" + this.className + ", bodyText=" + this.bodyText + "]";
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + Objects.hash(this.className);
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
        ComponentModel other = (ComponentModel)obj;
        return Objects.equals(this.className, other.className);
    }
}

