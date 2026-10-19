/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.layer;

import org.weasis.core.Messages;

public enum LayerItem {
    ANNOTATIONS("annotations", Messages.getString("AnnotationsLayer.anno"), true),
    MIN_ANNOTATIONS("minAnnotations", Messages.getString("LayerAnnotation.min_anot"), false),
    ANONYM_ANNOTATIONS("anonym", Messages.getString("AnnotationsLayer.anonym"), false),
    SCALE("scale", Messages.getString("AnnotationsLayer.scale"), true),
    LUT("lut", Messages.getString("AnnotationsLayer.lut"), false),
    FUSION_LUT("fusionLut", Messages.getString("AnnotationsLayer.fusion_lut"), true),
    IMAGE_ORIENTATION("orientation", Messages.getString("AnnotationsLayer.or"), true),
    WINDOW_LEVEL("wl", Messages.getString("AnnotationsLayer.wl"), true),
    ZOOM("zoom", Messages.getString("AnnotationsLayer.zoom"), true),
    ROTATION("rotation", Messages.getString("AnnotationsLayer.rot"), false),
    FRAME("frame", Messages.getString("AnnotationsLayer.fr"), true),
    PIXEL("pixel", Messages.getString("AnnotationsLayer.pix"), true),
    PRELOADING_BAR("loading", Messages.getString("AnnotationsLayer.preload_bar"), false),
    KEY_OBJECT("ko", Messages.getString("AnnotationsLayer.ko"), true);

    private final String key;
    private final String name;
    private final boolean visible;

    private LayerItem(String key, String name, Boolean visible) {
        this.key = key;
        this.name = name;
        this.visible = visible;
    }

    public String getKey() {
        return this.key;
    }

    public String getName() {
        return this.name;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public String toString() {
        return this.name;
    }
}

