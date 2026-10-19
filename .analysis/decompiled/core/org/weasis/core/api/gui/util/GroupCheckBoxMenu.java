/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import org.weasis.core.api.gui.util.CheckBoxModel;
import org.weasis.core.api.gui.util.GUIEntry;
import org.weasis.core.api.gui.util.GroupPopup;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.JScrollPopupMenu;
import org.weasis.core.api.util.FontItem;

public class GroupCheckBoxMenu
implements GroupPopup {
    protected final List<CheckBoxModel> itemList = new ArrayList<CheckBoxModel>();
    private boolean startBySelectAll = false;

    public List<CheckBoxModel> getModelList() {
        return new ArrayList<CheckBoxModel>(this.itemList);
    }

    @Override
    public JPopupMenu createJPopupMenu() {
        JScrollPopupMenu popupMouseButtons = new JScrollPopupMenu();
        int startIndex = 0;
        if (this.startBySelectAll) {
            boolean all = true;
            for (int i = 1; i < this.itemList.size(); ++i) {
                if (this.itemList.get(i).isSelected()) continue;
                all = false;
                break;
            }
            CheckBoxModel item = this.itemList.get(0);
            item.setSelected(all);
            startIndex = 1;
            JCheckBox box = new JCheckBox(item.getObject().toString(), null, item.isSelected());
            box.setFont(FontItem.DEFAULT_BOLD.getFont());
            box.addActionListener(e -> {
                Object patt0$temp = e.getSource();
                if (patt0$temp instanceof JCheckBox) {
                    JCheckBox btn = (JCheckBox)patt0$temp;
                    this.selectAll(popupMouseButtons, btn.isSelected());
                }
            });
            popupMouseButtons.add(box);
        }
        for (int i = startIndex; i < this.itemList.size(); ++i) {
            Icon icon = null;
            CheckBoxModel item = this.itemList.get(i);
            Object object = item.getObject();
            if (object instanceof GUIEntry) {
                GUIEntry entry = (GUIEntry)object;
                icon = entry.getIcon();
            }
            JCheckBox box = new JCheckBox(item.getObject().toString(), icon, item.isSelected());
            GuiUtils.applySelectedIconEffect(box);
            box.addActionListener(e -> {
                Object patt0$temp = e.getSource();
                if (patt0$temp instanceof JCheckBox) {
                    JCheckBox btn = (JCheckBox)patt0$temp;
                    item.setSelected(btn.isSelected());
                }
            });
            popupMouseButtons.add(box);
        }
        return popupMouseButtons;
    }

    private void selectAll(JComponent parent, boolean selected) {
        for (int i = 0; i < parent.getComponentCount(); ++i) {
            Component c = parent.getComponent(i);
            if (!(c instanceof AbstractButton)) continue;
            AbstractButton button = (AbstractButton)c;
            button.setSelected(selected);
        }
        this.itemList.forEach(m -> m.setSelected(selected));
    }

    public void selectAll() {
        this.itemList.forEach(m -> m.setSelected(true));
    }

    @Override
    public JMenu createMenu(String title) {
        JMenu menu = new JMenu(title);
        for (CheckBoxModel item : this.itemList) {
            Icon icon = null;
            Object object = item.getObject();
            if (object instanceof GUIEntry) {
                GUIEntry entry = (GUIEntry)object;
                icon = entry.getIcon();
            }
            JCheckBoxMenuItem box = new JCheckBoxMenuItem(item.getObject().toString(), icon, item.isSelected());
            box.addActionListener(e -> {
                Object patt0$temp = e.getSource();
                if (patt0$temp instanceof JCheckBoxMenuItem) {
                    JCheckBoxMenuItem btn = (JCheckBoxMenuItem)patt0$temp;
                    if (this.startBySelectAll && this.itemList.get(0).getObject().toString().equals(btn.getText())) {
                        this.selectAll(menu, btn.isSelected());
                    } else {
                        item.setSelected(btn.isSelected());
                    }
                }
            });
            menu.add(box);
        }
        return menu;
    }

    public void setModel(List<?> model, boolean initValue, boolean startBySelectAll) {
        this.startBySelectAll = startBySelectAll;
        this.itemList.clear();
        for (Object object : model) {
            CheckBoxModel m = new CheckBoxModel(object, initValue);
            this.itemList.add(m);
        }
    }

    public void setModel(List<CheckBoxModel> model) {
        this.itemList.clear();
        this.itemList.addAll(model);
    }
}

