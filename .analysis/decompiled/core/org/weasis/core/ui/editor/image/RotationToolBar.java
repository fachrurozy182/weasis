/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.util.WtoolBar;

public class RotationToolBar
extends WtoolBar {
    public RotationToolBar(ImageViewerEventManager<?> eventManager, int index) {
        super(Messages.getString("ResetTools.rotation"), index);
        if (eventManager == null) {
            throw new IllegalArgumentException("EventManager cannot be null");
        }
        JButton jButtonRotate90 = new JButton((Icon)ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.ROTATION));
        jButtonRotate90.setToolTipText(Messages.getString("RotationToolBar.90"));
        jButtonRotate90.addActionListener(e -> eventManager.getAction(ActionW.ROTATION).ifPresent(s -> s.setSliderValue((s.getSliderValue() + 90) % 360)));
        eventManager.getAction(ActionW.ROTATION).ifPresent(s -> s.registerActionState(jButtonRotate90));
        this.add(jButtonRotate90);
        JToggleButton jButtonFlip = new JToggleButton((Icon)ResourceUtil.getToolBarIcon(ResourceUtil.ActionIcon.FLIP));
        jButtonFlip.setToolTipText(Messages.getString("RotationToolBar.flip"));
        eventManager.getAction(ActionW.FLIP).ifPresent(b -> b.registerActionState(jButtonFlip));
        this.add(jButtonFlip);
    }
}

