/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.event.ListDataEvent;
import org.weasis.core.api.gui.util.ComboBoxModelAdapter;
import org.weasis.core.api.gui.util.GUIEntry;
import org.weasis.core.api.gui.util.GuiUtils;

public class JToggleButtonGroup<T>
implements ActionListener,
ComboBoxModelAdapter<T> {
    protected final List<JToggleButton> itemList;
    protected final HashMap<JToggleButton, Object> map = new HashMap();
    protected ComboBoxModel<T> dataModel;

    public JToggleButtonGroup() {
        this.itemList = new ArrayList<JToggleButton>();
    }

    private void init() {
        this.itemList.clear();
        Object selectedItem = this.dataModel.getSelectedItem();
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < this.dataModel.getSize(); ++i) {
            Object object = this.dataModel.getElementAt(i);
            Icon icon = null;
            if (object instanceof GUIEntry) {
                GUIEntry entry = (GUIEntry)object;
                icon = entry.getIcon();
            }
            JToggleButton b = new JToggleButton(icon);
            GuiUtils.applySelectedIconEffect(b);
            b.setToolTipText(object.toString());
            this.map.put(b, object);
            b.setSelected(object == selectedItem);
            group.add(b);
            b.addActionListener(this);
            this.itemList.add(b);
        }
    }

    @Override
    public void setModel(ComboBoxModel<T> dataModel) {
        boolean changeListener;
        boolean bl = changeListener = dataModel != null && dataModel != this.dataModel;
        if (this.dataModel != null) {
            this.dataModel.removeListDataListener(this);
        }
        this.dataModel = dataModel == null ? new DefaultComboBoxModel() : dataModel;
        this.init();
        if (changeListener) {
            this.dataModel.addListDataListener(this);
        }
    }

    public JToggleButton[] getJToggleButtonList() {
        return this.itemList.toArray(new JToggleButton[0]);
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
        JToggleButton item;
        Object object = e.getSource();
        if (object instanceof JToggleButton && (item = (JToggleButton)object).isSelected()) {
            this.dataModel.setSelectedItem(this.map.get(item));
        }
    }

    public void setSelected(Object selected) {
        if (selected != null) {
            for (JToggleButton item : this.itemList) {
                Object itemObj = this.map.get(item);
                if (itemObj != selected) continue;
                item.setSelected(true);
                this.dataModel.setSelectedItem(itemObj);
                return;
            }
        }
    }

    public int getSelectedIndex() {
        Object sObject = this.dataModel.getSelectedItem();
        int c = this.dataModel.getSize();
        for (int i = 0; i < c; ++i) {
            Object obj = this.dataModel.getElementAt(i);
            if (obj == null || !obj.equals(sObject)) continue;
            return i;
        }
        return -1;
    }

    public Object getSelectedItem() {
        return this.dataModel.getSelectedItem();
    }

    @Override
    public void setEnabled(boolean enabled) {
        for (JToggleButton jToggleButton : this.itemList) {
            jToggleButton.setEnabled(enabled);
        }
    }
}

