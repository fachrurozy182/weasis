/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlAccessType
 *  jakarta.xml.bind.annotation.XmlAccessorType
 *  jakarta.xml.bind.annotation.XmlAttribute
 *  jakarta.xml.bind.annotation.adapters.XmlAdapter
 */
package org.weasis.core.ui.model.layer;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Objects;
import java.util.Optional;
import org.weasis.core.ui.model.layer.GraphicLayer;
import org.weasis.core.ui.model.layer.Layer;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.model.utils.imp.DefaultUUID;

@XmlAccessorType(value=XmlAccessType.NONE)
public abstract class AbstractGraphicLayer
extends DefaultUUID
implements GraphicLayer {
    private String name;
    private LayerType type;
    private Boolean visible;
    private Boolean locked;
    private Integer level;
    private Boolean serializable;
    private Boolean selectable;

    protected AbstractGraphicLayer(LayerType type) {
        this.setType(type);
        this.level = type.getLevel();
        this.visible = type.getVisible();
        this.locked = type.getLocked();
        this.serializable = type.getSerializable();
        this.selectable = type.getSelectable();
    }

    @Override
    @XmlAttribute(name="locked")
    public Boolean getLocked() {
        return this.locked;
    }

    @Override
    public void setLocked(Boolean locked) {
        this.locked = Optional.ofNullable(locked).orElse(this.getType().getLocked());
    }

    @Override
    public void setVisible(Boolean visible) {
        this.visible = Optional.ofNullable(visible).orElse(this.getType().getVisible());
    }

    @Override
    @XmlAttribute(name="visible")
    public Boolean getVisible() {
        return this.visible;
    }

    @Override
    public void setLevel(Integer level) {
        this.level = Optional.ofNullable(level).orElse(this.getType().getLevel());
    }

    @Override
    @XmlAttribute(name="level")
    public Integer getLevel() {
        return this.level;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    @XmlAttribute(name="name")
    public String getName() {
        return this.name;
    }

    @Override
    @XmlAttribute(name="type")
    public LayerType getType() {
        return this.type;
    }

    @Override
    public void setType(LayerType type) {
        this.type = Objects.requireNonNull(type);
    }

    @Override
    @XmlAttribute(name="selectable")
    public Boolean getSelectable() {
        return this.selectable;
    }

    @Override
    public void setSelectable(Boolean selectable) {
        this.selectable = Optional.ofNullable(selectable).orElse(this.getType().getSelectable());
    }

    @Override
    public Boolean getSerializable() {
        return this.serializable;
    }

    @Override
    public void setSerializable(Boolean serializable) {
        this.serializable = serializable;
    }

    @Override
    public String toString() {
        return Optional.ofNullable(this.getName()).orElse(this.getType().getDefaultName());
    }

    static class Adapter
    extends XmlAdapter<AbstractGraphicLayer, Layer> {
        Adapter() {
        }

        public Layer unmarshal(AbstractGraphicLayer v) throws Exception {
            return v;
        }

        public AbstractGraphicLayer marshal(Layer v) throws Exception {
            return (AbstractGraphicLayer)v;
        }
    }
}

