/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.extras.components;

import java.util.Map;

public interface FlatStyleableComponent {
    default public String getStyle() {
        return (String)this.getClientProperty("FlatLaf.style");
    }

    default public void setStyle(String style) {
        this.putClientProperty("FlatLaf.style", style);
    }

    default public Map<String, Object> getStyleMap() {
        return (Map)this.getClientProperty("FlatLaf.style");
    }

    default public void setStyleMap(Map<String, Object> styleMap) {
        this.putClientProperty("FlatLaf.style", styleMap);
    }

    default public String getStyleClass() {
        return (String)this.getClientProperty("FlatLaf.styleClass");
    }

    default public void setStyleClass(String styleClass) {
        this.putClientProperty("FlatLaf.styleClass", styleClass);
    }

    public Object getClientProperty(Object var1);

    public void putClientProperty(Object var1, Object var2);
}

