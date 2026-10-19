/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import org.weasis.core.api.gui.util.GuiUtils;

public class TitleMenuItem
extends JLabel
implements MenuElement {
    public TitleMenuItem(String title) {
        this.setBorder(GuiUtils.getEmptyBorder(2, 4, 2, 4));
        this.setText(title);
    }

    @Override
    public void processMouseEvent(MouseEvent e, MenuElement[] path, MenuSelectionManager manager) {
    }

    @Override
    public void processKeyEvent(KeyEvent e, MenuElement[] path, MenuSelectionManager manager) {
    }

    @Override
    public void menuSelectionChanged(boolean isIncluded) {
    }

    @Override
    public MenuElement[] getSubElements() {
        return new MenuElement[0];
    }

    @Override
    public Component getComponent() {
        return this;
    }
}

