/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.model.Model;

public class ImportModel
extends Model {
    private static final long serialVersionUID = 1L;
    String className;

    @Override
    protected ImportModel makeNewInstance() {
        return new ImportModel();
    }

    @Override
    protected void mirror(Model that) {
        ImportModel actual = (ImportModel)that;
        super.mirror(actual);
        this.className = actual.className;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}

