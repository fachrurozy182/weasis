/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.launcher;

import java.awt.Window;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import org.weasis.launcher.WeasisMainFrameMBean;
import org.weasis.pref.ConfigData;

public class WeasisMainFrame
implements WeasisMainFrameMBean {
    private RootPaneContainer rootPaneContainer;
    private ConfigData configData;

    public void setRootPaneContainer(RootPaneContainer rootPaneContainer) {
        this.rootPaneContainer = rootPaneContainer;
    }

    @Override
    public RootPaneContainer getRootPaneContainer() {
        return this.rootPaneContainer;
    }

    @Override
    public ConfigData getConfigData() {
        return this.configData;
    }

    public void setConfigData(ConfigData configData) {
        this.configData = configData;
    }

    public Window getWindow() {
        if (this.rootPaneContainer == null) {
            return null;
        }
        Window window = SwingUtilities.getWindowAncestor(this.rootPaneContainer.getRootPane());
        if (window != null && window.isDisplayable()) {
            return window;
        }
        return null;
    }
}

