/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.PageItem;

public abstract class AbstractItemDialogPage
extends JPanel
implements PageItem,
Insertable {
    private final String title;
    private final List<PageItem> subPageList = new ArrayList<PageItem>();
    private int pagePosition;
    private final Properties properties = new Properties();

    protected AbstractItemDialogPage(String title) {
        this(title, 1000);
    }

    protected AbstractItemDialogPage(String title, int pagePosition) {
        this.title = title == null ? "item" : title;
        this.pagePosition = pagePosition;
        this.setBorder(GuiUtils.getEmptyBorder(15, 10, 0, 10));
        this.setLayout(new BoxLayout(this, 1));
    }

    public void deselectPageAction() {
    }

    public void selectPageAction() {
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public void addSubPage(PageItem subPage) {
        this.subPageList.add(subPage);
    }

    public void addSubPage(PageItem subPage, ActionListener actionListener, JComponent menuPanel) {
        this.subPageList.add(subPage);
        if (actionListener != null && menuPanel != null) {
            JButton button = new JButton();
            button.putClientProperty("JButton.buttonType", "roundRect");
            button.setText(subPage.getTitle());
            button.addActionListener(actionListener);
            menuPanel.add(button);
        }
    }

    public void removeSubPage(PageItem subPage) {
        this.subPageList.remove(subPage);
    }

    @Override
    public List<PageItem> getSubPages() {
        return new ArrayList<PageItem>(this.subPageList);
    }

    public void sortSubPages() {
        this.subPageList.sort((o1, o2) -> {
            int n;
            int val1;
            if (o1 instanceof AbstractItemDialogPage) {
                AbstractItemDialogPage page = (AbstractItemDialogPage)o1;
                v0 = page.getComponentPosition();
            } else {
                v0 = val1 = Integer.MAX_VALUE;
            }
            if (o2 instanceof AbstractItemDialogPage) {
                AbstractItemDialogPage page = (AbstractItemDialogPage)o2;
                n = page.getComponentPosition();
            } else {
                n = Integer.MAX_VALUE;
            }
            int val2 = n;
            return Integer.compare(val1, val2);
        });
    }

    public void resetAllSubPagesToDefaultValues() {
        for (PageItem subPage : this.subPageList) {
            subPage.resetToDefaultValues();
        }
    }

    public Properties getProperties() {
        return this.properties;
    }

    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return this.properties.getProperty(key, defaultValue);
    }

    @Override
    public String toString() {
        return this.title;
    }

    @Override
    public Insertable.Type getType() {
        return Insertable.Type.PREFERENCES;
    }

    @Override
    public String getComponentName() {
        return this.title;
    }

    @Override
    public boolean isComponentEnabled() {
        return this.isEnabled();
    }

    @Override
    public void setComponentEnabled(boolean enabled) {
        if (enabled != this.isComponentEnabled()) {
            this.setEnabled(enabled);
        }
    }

    @Override
    public int getComponentPosition() {
        return this.pagePosition;
    }

    @Override
    public void setComponentPosition(int position) {
        this.pagePosition = position;
    }

    public JComponent getMenuPanel() {
        return null;
    }
}

