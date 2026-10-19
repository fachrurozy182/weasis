/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.PhaseIndicator;
import ch.qos.logback.core.model.processor.ProcessingPhase;
import java.util.Objects;

@PhaseIndicator(phase=ProcessingPhase.SECOND)
public class AppenderRefModel
extends Model {
    private static final long serialVersionUID = 5238705468395447547L;
    String ref;

    @Override
    protected AppenderRefModel makeNewInstance() {
        return new AppenderRefModel();
    }

    @Override
    protected void mirror(Model that) {
        AppenderRefModel actual = (AppenderRefModel)that;
        super.mirror(actual);
        this.ref = actual.ref;
    }

    public String getRef() {
        return this.ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + Objects.hash(this.ref);
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
        AppenderRefModel other = (AppenderRefModel)obj;
        return Objects.equals(this.ref, other.ref);
    }
}

