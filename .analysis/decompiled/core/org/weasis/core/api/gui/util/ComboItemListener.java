/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.BasicActionState;
import org.weasis.core.api.gui.util.ComboBoxModelAdapter;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.GroupPopup;
import org.weasis.core.api.gui.util.GroupRadioMenu;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.JToggleButtonGroup;
import org.weasis.core.api.service.AuditLog;

public abstract class ComboItemListener<T>
extends BasicActionState
implements ListDataListener,
ChangeListener {
    protected final DefaultComboBoxModel<T> model;

    protected ComboItemListener(Feature<? extends ActionState> action, T[] objects) {
        super(action);
        this.model = objects != null ? new DefaultComboBoxModel<T>(objects) : new DefaultComboBoxModel();
        this.model.addListDataListener(this);
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        Object val = this.model.getSelectedItem();
        this.itemStateChanged(val);
        AuditLog.LOGGER.info("action:{} val:{}", (Object)this.action.cmd(), val);
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
    }

    @Override
    public void stateChanged(ChangeEvent evt) {
    }

    public String toString() {
        return this.action.getTitle();
    }

    @Override
    public Feature<? extends ActionState> getActionW() {
        return this.action;
    }

    public abstract void itemStateChanged(Object var1);

    @Override
    public boolean registerActionState(Object c) {
        if (super.registerActionState(c)) {
            if (c instanceof ComboBoxModelAdapter) {
                ((ComboBoxModelAdapter)c).setModel(this.model);
            }
            return true;
        }
        return false;
    }

    @Override
    public void unregisterActionState(Object c) {
        super.unregisterActionState(c);
        if (c instanceof ComboBoxModelAdapter) {
            ((ComboBoxModelAdapter)c).setModel(new DefaultComboBoxModel());
        }
    }

    public synchronized Object[] getAllItem() {
        Object[] array = new Object[this.model.getSize()];
        for (int i = 0; i < array.length; ++i) {
            array[i] = this.model.getElementAt(i);
        }
        return array;
    }

    public synchronized Object getFirstItem() {
        return this.model.getElementAt(0);
    }

    public synchronized Object getSelectedItem() {
        return this.model.getSelectedItem();
    }

    public synchronized void setSelectedItem(Object object) {
        this.model.setSelectedItem(object);
    }

    public synchronized void setSelectedItemWithoutTriggerAction(Object object) {
        this.model.removeListDataListener(this);
        this.model.setSelectedItem(object);
        this.model.addListDataListener(this);
    }

    public synchronized void setDataListWithoutTriggerAction(T[] objects) {
        this.setDataList(objects, false);
    }

    public synchronized void setDataList(T[] objects) {
        this.setDataList(objects, true);
    }

    private boolean isDataEquals(Object[] objects) {
        if (this.model.getSize() != objects.length) {
            return false;
        }
        Object[] data = new Object[objects.length];
        for (int i = 0; i < data.length; ++i) {
            data[i] = this.model.getElementAt(i);
        }
        return Arrays.equals(data, objects);
    }

    protected synchronized void setDataList(T[] objects, boolean doTriggerAction) {
        if (this.isDataEquals(objects)) {
            return;
        }
        Object oldSelection = this.model.getSelectedItem();
        this.model.removeListDataListener(this);
        this.model.removeAllElements();
        if (objects.length > 0) {
            boolean oldSelectionStillExist = false;
            for (T object : objects) {
                this.model.addElement(object);
                if (!object.equals(oldSelection)) continue;
                oldSelectionStillExist = true;
            }
            for (Object c : this.components) {
                if (!(c instanceof ComboBoxModelAdapter)) continue;
                ((ComboBoxModelAdapter)c).setModel(this.model);
            }
            this.model.setSelectedItem(null);
            if (doTriggerAction) {
                this.model.addListDataListener(this);
            }
            if (oldSelection != null && oldSelectionStillExist) {
                this.model.setSelectedItem(oldSelection);
            } else {
                this.model.setSelectedItem(objects[0]);
            }
            if (!doTriggerAction) {
                this.model.addListDataListener(this);
            }
        }
    }

    public DefaultComboBoxModel<T> getModel() {
        return this.model;
    }

    public JToggleButtonGroup<T> createButtonGroup() {
        JToggleButtonGroup group = new JToggleButtonGroup();
        this.registerActionState(group);
        return group;
    }

    public JComboBox<T> createCombo() {
        ComboItems combo = new ComboItems(this);
        this.registerActionState(combo);
        return combo;
    }

    public JComboBox<T> createCombo(int width) {
        ComboItems combo = new ComboItems(this);
        this.registerActionState(combo);
        GuiUtils.setPreferredWidth(combo, width, width);
        return combo;
    }

    public JMenu createUnregisteredRadioMenu(String title) {
        return this.createUnregisteredRadioMenu(title, null);
    }

    public JMenu createUnregisteredRadioMenu(String title, Icon icon) {
        GroupRadioMenu<T> radioMenu = new GroupRadioMenu<T>();
        radioMenu.setModel(this.model);
        JMenu menu = radioMenu.createMenu(title, icon);
        if (!this.enabled) {
            menu.setEnabled(false);
        }
        return menu;
    }

    public GroupPopup createGroupRadioMenu() {
        GroupRadioMenu<T> radioMenu = new GroupRadioMenu<T>();
        radioMenu.setModel(this.model);
        this.registerActionState(radioMenu);
        return radioMenu;
    }

    public GroupPopup createUnregisteredGroupRadioMenu() {
        GroupRadioMenu<T> radioMenu = new GroupRadioMenu<T>();
        radioMenu.setModel(this.model);
        return radioMenu;
    }

    class ComboItems
    extends JComboBox<T>
    implements ComboBoxModelAdapter<T> {
        final /* synthetic */ ComboItemListener this$0;

        ComboItems(ComboItemListener this$0) {
            ComboItemListener comboItemListener = this$0;
            Objects.requireNonNull(comboItemListener);
            this.this$0 = comboItemListener;
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension max = super.getMaximumSize();
            max.height = this.getPreferredSize().height;
            return max;
        }
    }
}

