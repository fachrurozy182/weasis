/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import javax.swing.Icon;
import javax.swing.JButton;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ScreenshotDialog;
import org.weasis.core.ui.util.WtoolBar;

public class ScreenshotToolBar<I extends ImageElement>
extends WtoolBar {
    public ScreenshotToolBar(ImageViewerEventManager<I> eventManager, int index) {
        super(Messages.getString("screenshot.bar"), index);
        if (eventManager == null) {
            throw new IllegalArgumentException("EventManager cannot be null");
        }
        JButton metaButton = new JButton((Icon)ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.EXPORT_IMAGE));
        metaButton.setToolTipText(Messages.getString("capture.the.selected.view"));
        metaButton.addActionListener(e -> ScreenshotDialog.showDialog(eventManager.getSelectedViewPane()));
        this.add(metaButton);
        eventManager.getAction(ActionW.EXPORT_VIEW).ifPresent(s -> s.registerActionState(metaButton));
    }
}

