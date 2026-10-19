/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.xml.bind.annotation.XmlRootElement
 */
package org.weasis.core.ui.model.layer.imp;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.weasis.core.ui.model.layer.AbstractGraphicLayer;
import org.weasis.core.ui.model.layer.LayerType;

@XmlRootElement(name="layer")
public class DefaultLayer
extends AbstractGraphicLayer {
    public DefaultLayer() {
        super(LayerType.TEMP_DRAW);
    }

    public DefaultLayer(LayerType type) {
        super(type);
    }
}

