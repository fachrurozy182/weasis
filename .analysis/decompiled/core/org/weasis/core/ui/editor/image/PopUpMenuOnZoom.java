/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor.image;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.Optional;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.ui.editor.image.ImageViewerEventManager;
import org.weasis.core.ui.editor.image.ZoomWin;

public class PopUpMenuOnZoom
extends JPopupMenu {
    private static final int[] magnify = new int[]{1, 2, 3, 4, 6};
    private final ZoomWin zoomWin;
    private final JMenuItem jMenuItemZoom = new JMenuItem();
    private final ButtonGroup buttonMagnify = new ButtonGroup();
    private JRadioButtonMenuItem[] jRadioButtonMenuItemMagnify;
    private final JMenu jMenuMagnify = new JMenu();
    private final JMenu jMenuImage = new JMenu();
    private final JRadioButtonMenuItem jMenuItemMagnifyOther = new JRadioButtonMenuItem();
    private final JCheckBoxMenuItem jCheckBoxMenuItemDraw = new JCheckBoxMenuItem();
    private final JCheckBoxMenuItem jCheckBoxMenuItemSynchronize = new JCheckBoxMenuItem();
    private final JMenuItem resetFreeze = new JMenuItem(Messages.getString("PopUpMenuOnZoom.reset"));
    private final ActionListener magnifyListener;

    public PopUpMenuOnZoom(ZoomWin zoomWin) {
        this.zoomWin = Objects.requireNonNull(zoomWin);
        this.magnifyListener = this::magnifyActionPerformed;
        this.init();
    }

    private void init() {
        this.jMenuItemZoom.setText(Messages.getString("PopUpMenuOnZoom.hide"));
        this.jMenuItemZoom.addActionListener(e -> this.zoomWin.hideZoom());
        this.jCheckBoxMenuItemDraw.setText(Messages.getString("PopUpMenuOnZoom.showDraw"));
        this.jCheckBoxMenuItemDraw.addActionListener(e -> {
            this.zoomWin.setActionInView(ActionW.DRAWINGS.cmd(), this.jCheckBoxMenuItemDraw.isSelected());
            this.zoomWin.repaint();
        });
        this.jMenuImage.setText(Messages.getString("PopUpMenuOnZoom.image"));
        JMenuItem freesParams = new JMenuItem(Messages.getString("PopUpMenuOnZoom.freeze"));
        freesParams.addActionListener(e -> this.zoomWin.setFreezeImage(ZoomWin.SyncType.PARENT_PARAMETERS));
        this.jMenuImage.add(freesParams);
        JMenuItem freeze = new JMenuItem(Messages.getString("PopUpMenuOnZoom.freezeImg"));
        freeze.addActionListener(e -> this.zoomWin.setFreezeImage(ZoomWin.SyncType.PARENT_IMAGE));
        this.jMenuImage.add(freeze);
        this.jMenuImage.addSeparator();
        this.resetFreeze.addActionListener(e -> this.zoomWin.setFreezeImage(null));
        this.jMenuImage.add(this.resetFreeze);
        this.jMenuMagnify.setText(Messages.getString("PopUpMenuOnZoom.magnify"));
        this.jCheckBoxMenuItemSynchronize.setText(Messages.getString("PopUpMenuOnZoom.synch"));
        this.jCheckBoxMenuItemSynchronize.addActionListener(e -> {
            this.zoomWin.setActionInView("synchronize", this.jCheckBoxMenuItemSynchronize.isSelected());
            this.zoomWin.updateZoom();
        });
        this.add(this.jMenuItemZoom);
        this.addSeparator();
        this.add(this.jCheckBoxMenuItemDraw);
        this.add(this.jCheckBoxMenuItemSynchronize);
        this.add(this.jMenuMagnify);
        this.iniMenuItemZoomMagnify();
        this.add(this.jMenuImage);
    }

    public void iniMenuItemZoomMagnify() {
        this.jRadioButtonMenuItemMagnify = new JRadioButtonMenuItem[magnify.length];
        for (int i = 0; i < this.jRadioButtonMenuItemMagnify.length; ++i) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem();
            item.setText(magnify[i] + "X");
            this.buttonMagnify.add(item);
            item.addActionListener(this.magnifyListener);
            this.jMenuMagnify.add(item);
            this.jRadioButtonMenuItemMagnify[i] = item;
        }
    }

    public void enableMenuItem() {
        Double ratio;
        this.jCheckBoxMenuItemSynchronize.setSelected((Boolean)this.zoomWin.getActionValue("synchronize"));
        this.jCheckBoxMenuItemDraw.setSelected((Boolean)this.zoomWin.getActionValue(ActionW.DRAWINGS.cmd()));
        Object type = this.zoomWin.getActionValue("freeze");
        this.resetFreeze.setEnabled(ZoomWin.SyncType.PARENT_PARAMETERS.equals(type) || ZoomWin.SyncType.PARENT_IMAGE.equals(type));
        boolean noSelection = true;
        if (this.jRadioButtonMenuItemMagnify.length < this.jMenuMagnify.getItemCount()) {
            this.jMenuMagnify.remove(this.jMenuItemMagnifyOther);
        }
        if ((ratio = (Double)this.zoomWin.getActionValue(ActionW.ZOOM.cmd())) == null) {
            ratio = 1.0;
        }
        int currentZoomRatio = (int)(ratio * 100.0);
        for (int i = 0; i < this.jRadioButtonMenuItemMagnify.length; ++i) {
            if (magnify[i] * 100 != currentZoomRatio) continue;
            JRadioButtonMenuItem item3 = this.jRadioButtonMenuItemMagnify[i];
            item3.setSelected(true);
            noSelection = false;
            break;
        }
        if (noSelection) {
            ratio = Math.abs(ratio);
            this.jMenuItemMagnifyOther.setText(ratio + "X");
            this.buttonMagnify.add(this.jMenuItemMagnifyOther);
            if ((double)magnify[magnify.length - 1] < ratio) {
                this.jMenuMagnify.add(this.jMenuItemMagnifyOther);
            } else {
                int k = 0;
                for (int i = 0; i < magnify.length; ++i) {
                    if (!((double)magnify[i] > ratio)) continue;
                    k = i;
                    break;
                }
                this.jMenuMagnify.add((Component)this.jMenuItemMagnifyOther, k);
            }
            this.jMenuItemMagnifyOther.setSelected(true);
        }
    }

    private void magnifyActionPerformed(ActionEvent e) {
        Object object = e.getSource();
        if (object instanceof JRadioButtonMenuItem) {
            JRadioButtonMenuItem item = (JRadioButtonMenuItem)object;
            for (int i = 0; i < this.jRadioButtonMenuItemMagnify.length; ++i) {
                if (!item.equals(this.jRadioButtonMenuItemMagnify[i])) continue;
                ImageViewerEventManager manager = this.zoomWin.getView2d().getEventManager();
                Optional<SliderChangeListener> zoomAction = manager.getAction(ActionW.LENS_ZOOM);
                if (!zoomAction.isPresent()) break;
                zoomAction.get().setRealValue(magnify[i]);
                break;
            }
        }
    }
}

