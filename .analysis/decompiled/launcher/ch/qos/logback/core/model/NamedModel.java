/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.model.INamedModel;
import ch.qos.logback.core.model.Model;
import java.util.Objects;

public class NamedModel
extends Model
implements INamedModel {
    private static final long serialVersionUID = 3549881638769570183L;
    String name;

    @Override
    protected NamedModel makeNewInstance() {
        return new NamedModel();
    }

    @Override
    protected void mirror(Model that) {
        NamedModel actual = (NamedModel)that;
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
        NamedModel other = (NamedModel)obj;
        return Objects.equals(this.name, other.name);
    }
}

