/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.JToolBar;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.ui.util.Toolbar;

public class WtoolBar
extends JToolBar
implements Toolbar {
    private final String barName;
    private int barPosition;
    private Insertable attachedInsertable;

    public WtoolBar(String barName, int position) {
        this.setFloatable(false);
        this.setBorder(null);
        this.barName = barName;
        this.barPosition = position;
        this.addSeparator();
    }

    @Override
    public void addSeparator() {
        this.add(Box.createRigidArea(new Dimension(5, 0)));
        this.add(new JToolBar.Separator(new Dimension(3, 30)));
    }

    @Override
    public Insertable.Type getType() {
        return Insertable.Type.TOOLBAR;
    }

    @Override
    public int getComponentPosition() {
        return this.barPosition;
    }

    @Override
    public void setComponentPosition(int position) {
        this.barPosition = position;
    }

    public Insertable getAttachedInsertable() {
        return this.attachedInsertable;
    }

    public void setAttachedInsertable(Insertable attachedInsertable) {
        this.attachedInsertable = attachedInsertable;
    }

    @Override
    public String toString() {
        return this.barName;
    }

    @Override
    public String getComponentName() {
        return this.barName;
    }

    @Override
    public final WtoolBar getComponent() {
        return this;
    }

    @Override
    public boolean isComponentEnabled() {
        return this.isEnabled();
    }

    @Override
    public void setComponentEnabled(boolean enabled) {
        if (this.isComponentEnabled() != enabled) {
            this.setEnabled(enabled);
        }
    }
}

