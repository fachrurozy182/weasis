/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatWindowAbstractIcon;
import java.awt.Graphics2D;

public class FlatWindowIconifyIcon
extends FlatWindowAbstractIcon {
    public FlatWindowIconifyIcon() {
        this(null);
    }

    public FlatWindowIconifyIcon(String windowStyle) {
        super(windowStyle);
    }

    @Override
    protected void paintIconAt1x(Graphics2D g, int x, int y, int width, int height, double scaleFactor) {
        int iw = (int)((double)this.symbolHeight * scaleFactor);
        int ih = Math.max((int)scaleFactor, 1);
        int ix = x + (width - iw) / 2;
        int iy = y + (height - ih) / 2;
        g.fillRect(ix, iy, iw, ih);
    }
}

