/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.utils;

import org.weasis.core.api.image.util.ImageLayer;
import org.weasis.core.api.media.data.ImageElement;

@FunctionalInterface
public interface ImageLayerChangeListener<E extends ImageElement> {
    public void handleLayerChanged(ImageLayer<E> var1);
}

