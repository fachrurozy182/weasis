/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.model.Model;
import java.util.Objects;

public class SerializeModelModel
extends Model {
    private static final long serialVersionUID = 16385651235687L;
    String file;

    @Override
    protected SerializeModelModel makeNewInstance() {
        return new SerializeModelModel();
    }

    public String getFile() {
        return this.file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        SerializeModelModel that = (SerializeModelModel)o;
        return Objects.equals(this.file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.file);
    }
}

