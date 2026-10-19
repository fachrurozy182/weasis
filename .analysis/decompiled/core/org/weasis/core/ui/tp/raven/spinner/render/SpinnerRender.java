/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.tp.raven.spinner.render;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public interface SpinnerRender {
    public boolean isDisplayStringAble();

    public boolean isPaintComplete();

    public void paintCompleteIndeterminate(Graphics2D var1, Component var2, Rectangle var3, float var4, float var5, float var6);

    public void paintIndeterminate(Graphics2D var1, Component var2, Rectangle var3, float var4);

    public void paintDeterminate(Graphics2D var1, Component var2, Rectangle var3, float var4);

    public int getInsets();
}

