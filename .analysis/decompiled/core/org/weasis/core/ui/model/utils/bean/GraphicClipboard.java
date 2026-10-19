/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.model.utils.bean;

import java.util.List;
import org.weasis.core.ui.model.graphic.Graphic;

public class GraphicClipboard {
    private List<Graphic> graphics;

    public synchronized List<Graphic> getGraphics() {
        return this.graphics;
    }

    public synchronized void setGraphics(List<Graphic> graphics) {
        this.graphics = graphics;
    }

    public boolean hasGraphics() {
        return this.graphics != null && !this.graphics.isEmpty();
    }
}

