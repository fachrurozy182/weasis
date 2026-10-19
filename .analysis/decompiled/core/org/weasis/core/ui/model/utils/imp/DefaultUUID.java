/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlAttribute
 *  jakarta.xml.bind.annotation.XmlID
 */
package org.weasis.core.ui.model.utils.imp;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlID;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.weasis.core.ui.model.utils.UUIDable;

public class DefaultUUID
implements UUIDable {
    private String uuid;

    public DefaultUUID() {
        this.uuid = UUID.randomUUID().toString();
    }

    public DefaultUUID(String uuid) {
        this.setUuid(uuid);
    }

    @Override
    @XmlID
    @XmlAttribute(name="uuid", required=true)
    public String getUuid() {
        return this.uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = Optional.ofNullable(uuid).orElseGet(UUID.randomUUID()::toString);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DefaultUUID that = (DefaultUUID)o;
        return this.uuid.equals(that.uuid);
    }

    public int hashCode() {
        return Objects.hash(this.uuid);
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[" + this.uuid + "]";
    }
}

