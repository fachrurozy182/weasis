/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.awt.BorderLayout;
import java.util.Locale;
import javax.swing.JPanel;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.util.GUIEntry;
import org.weasis.core.api.gui.util.GuiUtils;

public abstract class AbstractTabLicense
extends JPanel
implements Insertable {
    protected final GUIEntry guiEntry;

    public AbstractTabLicense(GUIEntry entry) {
        this.guiEntry = entry;
        this.setBorder(GuiUtils.getEmptyBorder(10));
        this.setLayout(new BorderLayout());
    }

    public GUIEntry getGuiEntry() {
        return this.guiEntry;
    }

    public String getFileName() {
        return this.guiEntry.getUIName().toLowerCase(Locale.US);
    }

    @Override
    public String getComponentName() {
        return this.guiEntry.getUIName();
    }

    @Override
    public final Insertable.Type getType() {
        return Insertable.Type.LICENSE;
    }

    @Override
    public boolean isComponentEnabled() {
        return true;
    }

    @Override
    public void setComponentEnabled(boolean enabled) {
    }

    @Override
    public int getComponentPosition() {
        return 0;
    }

    @Override
    public void setComponentPosition(int position) {
    }

    public abstract void closing();
}

