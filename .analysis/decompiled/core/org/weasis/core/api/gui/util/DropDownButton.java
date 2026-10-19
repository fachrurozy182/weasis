/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.weasis.core.api.gui.util.GroupPopup;

public abstract class DropDownButton
extends JButton
implements PopupMenuListener,
ActionListener {
    private final String type;
    private final GroupPopup menuModel;

    protected DropDownButton(String type, Icon icon) {
        this(type, icon, null);
    }

    protected DropDownButton(String type, Icon icon, GroupPopup model) {
        super(icon);
        this.menuModel = model;
        this.type = type;
        this.init();
    }

    protected DropDownButton(String type, String text, Icon icon, GroupPopup model) {
        super(text, icon);
        this.type = type;
        this.menuModel = model;
        this.init();
    }

    private void init() {
        this.addActionListener(this);
    }

    public GroupPopup getMenuModel() {
        return this.menuModel;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        JPopupMenu popup = this.getPopupMenu();
        popup.addPopupMenuListener(this);
        popup.show(this, 0, this.getHeight());
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        this.getModel().setRollover(true);
        this.getModel().setSelected(true);
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        this.getModel().setRollover(false);
        this.getModel().setSelected(false);
        ((JPopupMenu)e.getSource()).removePopupMenuListener(this);
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    public String getType() {
        return this.type;
    }

    protected abstract JPopupMenu getPopupMenu();
}

