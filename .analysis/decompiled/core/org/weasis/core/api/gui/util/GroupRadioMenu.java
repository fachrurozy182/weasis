/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.opencv.op.lut.ByteLut
 */
package org.weasis.core.api.gui.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.ListDataEvent;
import org.weasis.core.api.gui.util.ComboBoxModelAdapter;
import org.weasis.core.api.gui.util.GUIEntry;
import org.weasis.core.api.gui.util.GroupPopup;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.RadioMenuItem;
import org.weasis.opencv.op.lut.ByteLut;

public class GroupRadioMenu<T>
implements ActionListener,
ComboBoxModelAdapter<T>,
GroupPopup {
    protected final List<RadioMenuItem> itemList = new ArrayList<RadioMenuItem>();
    protected final ButtonGroup group = new ButtonGroup();
    protected ComboBoxModel<T> dataModel;

    protected void init() {
        for (RadioMenuItem item : this.itemList) {
            this.group.remove(item);
        }
        this.itemList.clear();
        Object selectedItem = this.dataModel.getSelectedItem();
        for (int i = 0; i < this.dataModel.getSize(); ++i) {
            Object object = this.dataModel.getElementAt(i);
            RadioMenuItem radioMenuItem = new RadioMenuItem(object.toString(), object);
            Icon icon = null;
            if (object instanceof GUIEntry) {
                GUIEntry entry = (GUIEntry)object;
                icon = entry.getIcon();
            } else if (object instanceof ByteLut) {
                ByteLut lut = (ByteLut)object;
                icon = lut.getIcon(GuiUtils.getBigIconButtonSize((JComponent)radioMenuItem).height);
            }
            radioMenuItem.setIcon(icon);
            GuiUtils.applySelectedIconEffect(radioMenuItem);
            radioMenuItem.setSelected(object == selectedItem);
            this.group.add(radioMenuItem);
            this.itemList.add(radioMenuItem);
            radioMenuItem.addActionListener(this);
        }
    }

    public List<RadioMenuItem> getRadioMenuItemListCopy() {
        return new ArrayList<RadioMenuItem>(this.itemList);
    }

    @Override
    public JPopupMenu createJPopupMenu() {
        JPopupMenu popupMouseButtons = new JPopupMenu();
        for (RadioMenuItem radioMenuItem : this.itemList) {
            popupMouseButtons.add(radioMenuItem);
        }
        return popupMouseButtons;
    }

    @Override
    public JMenu createMenu(String title) {
        return this.createMenu(title, null);
    }

    public JMenu createMenu(String title, Icon icon) {
        JMenu menu = new JMenu(title);
        if (icon != null) {
            menu.setIcon(icon);
            GuiUtils.applySelectedIconEffect(menu);
        }
        for (RadioMenuItem radioMenuItem : this.itemList) {
            menu.add(radioMenuItem);
        }
        return menu;
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        this.setSelected(this.dataModel.getSelectedItem());
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RadioMenuItem item;
        Object object = e.getSource();
        if (object instanceof RadioMenuItem && (item = (RadioMenuItem)object).isSelected()) {
            this.dataModel.setSelectedItem(item.getUserObject());
        }
    }

    public void setSelected(Object selected) {
        if (selected == null) {
            this.group.clearSelection();
            this.dataModel.setSelectedItem(null);
        } else {
            for (RadioMenuItem item : this.itemList) {
                if (item.getUserObject() != selected) continue;
                item.setSelected(true);
                this.dataModel.setSelectedItem(item.getUserObject());
                return;
            }
        }
    }

    public int getSelectedIndex() {
        Object sObject = this.dataModel.getSelectedItem();
        for (int i = 0; i < this.dataModel.getSize(); ++i) {
            if (!Objects.equals(this.dataModel.getElementAt(i), sObject)) continue;
            return i;
        }
        return -1;
    }

    public T getSelectedItem() {
        return (T)this.dataModel.getSelectedItem();
    }

    public ComboBoxModel<T> getModel() {
        return this.dataModel;
    }

    @Override
    public void setModel(ComboBoxModel<T> dataModel) {
        if (this.dataModel != null) {
            this.dataModel.removeListDataListener(this);
        }
        if (dataModel != null) {
            dataModel.removeListDataListener(this);
        }
        this.dataModel = Optional.ofNullable(dataModel).orElseGet(DefaultComboBoxModel::new);
        this.init();
        this.dataModel.addListDataListener(this);
    }

    @Override
    public void setEnabled(boolean enabled) {
        for (RadioMenuItem radioMenuItem : this.itemList) {
            radioMenuItem.setEnabled(enabled);
        }
    }
}

