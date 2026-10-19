/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.icons;

import com.formdev.flatlaf.icons.FlatWindowAbstractIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

public class FlatWindowCloseIcon
extends FlatWindowAbstractIcon {
    public FlatWindowCloseIcon() {
        this(null);
    }

    public FlatWindowCloseIcon(String windowStyle) {
        super(windowStyle, FlatUIUtils.getSubUIColor("TitlePane.closeBackground", windowStyle), FlatUIUtils.getSubUIColor("TitlePane.closeForeground", windowStyle), FlatUIUtils.getSubUIColor("TitlePane.closeInactiveBackground", windowStyle), FlatUIUtils.getSubUIColor("TitlePane.closeInactiveForeground", windowStyle), FlatUIUtils.getSubUIColor("TitlePane.closeHoverBackground", windowStyle), FlatUIUtils.getSubUIColor("TitlePane.closeHoverForeground", windowStyle), FlatUIUtils.getSubUIColor("TitlePane.closePressedBackground", windowStyle), FlatUIUtils.getSubUIColor("TitlePane.closePressedForeground", windowStyle));
    }

    @Override
    protected void paintIconAt1x(Graphics2D g, int x, int y, int width, int height, double scaleFactor) {
        int iwh = (int)((double)this.symbolHeight * scaleFactor);
        int ix = x + (width - iwh) / 2;
        int iy = y + (height - iwh) / 2;
        int ix2 = ix + iwh - 1;
        int iy2 = iy + iwh - 1;
        boolean isWindows10 = SystemInfo.isWindows_10_orLater && !SystemInfo.isWindows_11_orLater;
        float thickness = Math.max(isWindows10 ? (float)((int)scaleFactor) : (float)scaleFactor, 1.0f);
        Path2D.Float path = new Path2D.Float(0, 4);
        ((Path2D)path).moveTo(ix, iy);
        ((Path2D)path).lineTo(ix2, iy2);
        ((Path2D)path).moveTo(ix, iy2);
        ((Path2D)path).lineTo(ix2, iy);
        g.setStroke(new BasicStroke(thickness));
        g.draw(path);
    }
}

