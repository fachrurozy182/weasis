/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
 */
package org.weasis.core.ui.model.layer;

import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.weasis.core.ui.model.layer.AbstractGraphicLayer;
import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.model.utils.UUIDable;

@XmlJavaTypeAdapter(value=AbstractGraphicLayer.Adapter.class)
public interface Layer
extends Comparable<Layer>,
UUIDable {
    public void setVisible(Boolean var1);

    public Boolean getVisible();

    public void setLevel(Integer var1);

    public Integer getLevel();

    public LayerType getType();

    public void setType(LayerType var1);

    public void setName(String var1);

    public String getName();

    @Override
    default public int compareTo(Layer obj) {
        if (obj == null) {
            return 1;
        }
        int thisVal = this.getLevel();
        int anotherVal = obj.getLevel();
        return Integer.compare(thisVal, anotherVal);
    }
}

