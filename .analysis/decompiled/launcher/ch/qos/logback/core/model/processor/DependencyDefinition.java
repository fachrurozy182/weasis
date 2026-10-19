/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.model.Model;

public class DependencyDefinition {
    Model depender;
    String dependency;

    public DependencyDefinition(Model depender, String dependency) {
        this.depender = depender;
        this.dependency = dependency;
    }

    public String getDependency() {
        return this.dependency;
    }

    public Model getDepender() {
        return this.depender;
    }

    public String toString() {
        return "DependencyDefinition{depender=" + String.valueOf(this.depender) + ", dependency='" + this.dependency + "'}";
    }
}

