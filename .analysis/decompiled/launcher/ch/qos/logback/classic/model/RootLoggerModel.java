/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.model;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.PhaseIndicator;
import ch.qos.logback.core.model.processor.ProcessingPhase;
import java.util.Objects;

@PhaseIndicator(phase=ProcessingPhase.SECOND)
public class RootLoggerModel
extends Model {
    private static final long serialVersionUID = -2811453129653502831L;
    String level;

    @Override
    protected RootLoggerModel makeNewInstance() {
        return new RootLoggerModel();
    }

    @Override
    protected void mirror(Model that) {
        RootLoggerModel actual = (RootLoggerModel)that;
        super.mirror(actual);
        this.level = actual.level;
    }

    public String getLevel() {
        return this.level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + Objects.hash(this.level);
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
        RootLoggerModel other = (RootLoggerModel)obj;
        return Objects.equals(this.level, other.level);
    }
}

