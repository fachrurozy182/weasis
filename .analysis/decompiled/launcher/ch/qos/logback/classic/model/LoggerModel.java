/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.model;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.PhaseIndicator;
import ch.qos.logback.core.model.processor.ProcessingPhase;
import java.util.Objects;

@PhaseIndicator(phase=ProcessingPhase.SECOND)
public class LoggerModel
extends Model {
    private static final long serialVersionUID = 5326913660697375316L;
    String name;
    String level;
    String additivity;

    @Override
    protected LoggerModel makeNewInstance() {
        return new LoggerModel();
    }

    @Override
    protected void mirror(Model that) {
        LoggerModel actual = (LoggerModel)that;
        super.mirror(actual);
        this.name = actual.name;
        this.level = actual.level;
        this.additivity = actual.additivity;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return this.level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getAdditivity() {
        return this.additivity;
    }

    public void setAdditivity(String additivity) {
        this.additivity = additivity;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " name=" + this.name + "]";
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + Objects.hash(this.additivity, this.level, this.name);
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
        LoggerModel other = (LoggerModel)obj;
        return Objects.equals(this.additivity, other.additivity) && Objects.equals(this.level, other.level) && Objects.equals(this.name, other.name);
    }
}

