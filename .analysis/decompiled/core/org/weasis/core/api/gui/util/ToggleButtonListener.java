/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JToggleButton;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.BasicActionState;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.AuditLog;

public abstract class ToggleButtonListener
extends BasicActionState
implements ActionListener {
    protected ButtonModel model = new JToggleButton.ToggleButtonModel();

    protected ToggleButtonListener(Feature<? extends ActionState> action, boolean selected) {
        super(action);
        this.model.setSelected(selected);
        this.model.addActionListener(this);
    }

    @Override
    public void enableAction(boolean enabled) {
        super.enableAction(enabled);
        this.model.setEnabled(enabled);
    }

    public String toString() {
        return this.action.getTitle();
    }

    @Override
    public Feature<? extends ActionState> getActionW() {
        return this.action;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof ButtonModel) {
            this.actionPerformed(this.model.isSelected());
            AuditLog.LOGGER.info("action:{} val:{}", (Object)this.action.cmd(), (Object)this.model.isSelected());
        }
    }

    public abstract void actionPerformed(boolean var1);

    public synchronized void setSelectedWithoutTriggerAction(boolean selected) {
        this.model.setSelected(selected);
    }

    public synchronized void setSelected(boolean selected) {
        boolean oldVal = this.model.isSelected();
        if (oldVal != selected) {
            this.model.setSelected(selected);
            this.actionPerformed(selected);
            AuditLog.LOGGER.info("action:{} val:{}", (Object)this.action.cmd(), (Object)this.model.isSelected());
        }
    }

    public synchronized boolean isSelected() {
        return this.model.isSelected();
    }

    @Override
    public boolean registerActionState(Object c) {
        if (super.registerActionState(c)) {
            if (c instanceof AbstractButton) {
                AbstractButton button = (AbstractButton)c;
                button.setModel(this.model);
            }
            return true;
        }
        return false;
    }

    public JCheckBoxMenuItem createUnregisteredJCCheckBoxMenuItem(String text) {
        return this.createUnregisteredJCCheckBoxMenuItem(text, null);
    }

    public JCheckBoxMenuItem createUnregisteredJCCheckBoxMenuItem(String text, Icon icon) {
        JCheckBoxMenuItem checkBoxItem = new JCheckBoxMenuItem(text, icon, this.model.isSelected());
        GuiUtils.applySelectedIconEffect(checkBoxItem);
        checkBoxItem.setModel(this.model);
        return checkBoxItem;
    }

    public JCheckBox createCheckBox(String title) {
        JCheckBox check = new JCheckBox(title);
        this.registerActionState(check);
        return check;
    }

    public JCheckBoxMenuItem createMenu(String title) {
        JCheckBoxMenuItem menu = new JCheckBoxMenuItem(title);
        this.registerActionState(menu);
        return menu;
    }

    public JToggleButton createJToggleButton(Icon icon) {
        JToggleButton check = new JToggleButton(icon);
        this.registerActionState(check);
        return check;
    }
}

