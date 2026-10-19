/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.tp.raven.slider;

import java.awt.Graphics;
import java.awt.Image;

public abstract class SliderTransition {
    public abstract void renderImageOld(Graphics var1, Image var2, int var3, int var4, float var5);

    public abstract void renderImageNew(Graphics var1, Image var2, int var3, int var4, float var5);

    public void render(Graphics g, Image imageOld, Image imageNew, int width, int height, float animate) {
        this.renderImageOld(g.create(), imageOld, width, height, animate);
        this.renderImageNew(g.create(), imageNew, width, height, animate);
    }
}

