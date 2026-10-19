/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
 */
package org.weasis.core.ui.model.layer;

import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.weasis.core.ui.model.layer.AbstractGraphicLayer;
import org.weasis.core.ui.model.layer.Layer;

@XmlJavaTypeAdapter(value=AbstractGraphicLayer.Adapter.class)
public interface GraphicLayer
extends Layer {
    public Boolean getLocked();

    public void setLocked(Boolean var1);

    public Boolean getSelectable();

    public void setSelectable(Boolean var1);

    public Boolean getSerializable();

    public void setSerializable(Boolean var1);
}

