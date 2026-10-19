/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.NamedModel;
import java.util.Objects;

public class PropertyModel
extends NamedModel {
    private static final long serialVersionUID = 1494176979175092052L;
    String value;
    String scopeStr;
    String file;
    String resource;

    @Override
    protected PropertyModel makeNewInstance() {
        return new PropertyModel();
    }

    @Override
    protected void mirror(Model that) {
        PropertyModel actual = (PropertyModel)that;
        super.mirror(actual);
        this.value = actual.value;
        this.scopeStr = actual.scopeStr;
        this.file = actual.file;
        this.resource = actual.resource;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getScopeStr() {
        return this.scopeStr;
    }

    public void setScopeStr(String scopeStr) {
        this.scopeStr = scopeStr;
    }

    public String getFile() {
        return this.file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getResource() {
        return this.resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + Objects.hash(this.file, this.resource, this.scopeStr, this.value);
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
        PropertyModel other = (PropertyModel)obj;
        return Objects.equals(this.file, other.file) && Objects.equals(this.resource, other.resource) && Objects.equals(this.scopeStr, other.scopeStr) && Objects.equals(this.value, other.value);
    }
}

