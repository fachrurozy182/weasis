/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.model.ComponentModel;
import ch.qos.logback.core.model.INamedModel;
import ch.qos.logback.core.model.Model;
import java.util.Objects;

public class NamedComponentModel
extends ComponentModel
implements INamedModel {
    private static final long serialVersionUID = -6388316680413871442L;
    String name;

    @Override
    protected NamedComponentModel makeNewInstance() {
        return new NamedComponentModel();
    }

    @Override
    protected void mirror(Model that) {
        NamedComponentModel actual = (NamedComponentModel)that;
        super.mirror(actual);
        this.name = actual.name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "NamedComponentModel [name=" + this.name + ", className=" + this.className + ", tag=" + this.tag + ", bodyText=" + this.bodyText + "]";
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + Objects.hash(this.name);
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
        NamedComponentModel other = (NamedComponentModel)obj;
        return Objects.equals(this.name, other.name);
    }
}

