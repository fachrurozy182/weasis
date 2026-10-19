/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.pref;

import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.launcher.Launcher;
import org.weasis.core.ui.util.DefaultAction;
import org.weasis.core.ui.util.DynamicToolbar;
import org.weasis.core.ui.util.WtoolBar;

public class LauncherToolBar
extends WtoolBar
implements DynamicToolbar {
    private final ImageViewerEventManager<?> eventManager;

    public LauncherToolBar(ImageViewerEventManager<?> eventManager, int index) {
        super(Messages.getString("launcher"), index);
        this.eventManager = eventManager;
        this.updateLaunchers(eventManager);
    }

    public void updateLaunchers(ImageViewerEventManager<?> eventManager) {
        this.removeAll();
        this.addLaunchers(GuiUtils.getUICore().getDicomLaunchers(), eventManager);
        this.addLaunchers(GuiUtils.getUICore().getOtherLaunchers(), eventManager);
    }

    private void addLaunchers(List<Launcher> launchers, ImageViewerEventManager<?> eventManager) {
        int size = 30;
        for (Launcher launcher : launchers) {
            if (!launcher.isEnable() || !launcher.isButton() || !launcher.getConfiguration().isValid()) continue;
            Icon icon = launcher.getResizeIcon(size, size);
            DefaultAction action = new DefaultAction(null, icon, actionEvent -> launcher.execute(eventManager));
            JButton launcherButton = new JButton(action);
            launcherButton.setToolTipText(String.valueOf((Object)Launcher.Type.DICOM) + ": " + launcher.getName());
            this.add(launcherButton);
        }
    }

    @Override
    public void updateToolbar() {
        this.updateLaunchers(this.eventManager);
    }
}

