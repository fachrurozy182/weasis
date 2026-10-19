/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.layer;

import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.layer.Layer;

public interface GraphicModelChangeListener {
    default public void handleModelChanged(GraphicModel modelList) {
    }

    default public void handleLayerAdded(GraphicModel modelList, Layer layer) {
    }

    default public void handleLayerRemoved(GraphicModel modelList, Layer layer) {
    }

    default public void handleLayerChanged(GraphicModel modelList, Layer layer) {
    }
}

