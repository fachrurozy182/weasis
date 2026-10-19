/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  bibliothek.gui.dock.common.DefaultSingleCDockable
 */
package org.weasis.core.ui.docking;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import java.awt.Component;
import org.weasis.core.api.gui.Insertable;

public interface DockableTool
extends Insertable {
    public DefaultSingleCDockable getDockable();

    public void showDockable();

    public void closeDockable();

    public Component getToolComponent();

    public int getDockableWidth();
}

