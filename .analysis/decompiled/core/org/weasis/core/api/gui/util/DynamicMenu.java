/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.SystemInfo
 */
package org.weasis.core.api.gui.util;

import com.formdev.flatlaf.util.SystemInfo;
import java.util.Objects;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public abstract class DynamicMenu
extends JMenu {
    protected DynamicMenu() {
    }

    protected DynamicMenu(Action a) {
        super(a);
    }

    protected DynamicMenu(String s, boolean b) {
        super(s, b);
    }

    protected DynamicMenu(String s) {
        super(s);
    }

    public abstract void popupMenuWillBecomeVisible();

    public void popupMenuWillBecomeInvisible() {
        Timer timer = new Timer(250, e -> this.removeAll());
        timer.setRepeats(false);
        timer.start();
    }

    public void popupMenuCanceled() {
    }

    public void addPopupMenuListener() {
        if (SystemInfo.isMacOS && Boolean.TRUE.toString().equals(System.getProperty("apple.laf.useScreenMenuBar", Boolean.FALSE.toString()))) {
            this.addChangeListener(e -> {
                if (this.isSelected()) {
                    this.removeAll();
                    this.popupMenuWillBecomeVisible();
                } else {
                    this.popupMenuWillBecomeInvisible();
                }
            });
        } else {
            JPopupMenu menuExport = this.getPopupMenu();
            menuExport.addPopupMenuListener(new PopupMenuListener(this){
                final /* synthetic */ DynamicMenu this$0;
                {
                    DynamicMenu dynamicMenu = this$0;
                    Objects.requireNonNull(dynamicMenu);
                    this.this$0 = dynamicMenu;
                }

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    this.this$0.popupMenuWillBecomeVisible();
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    this.this$0.popupMenuWillBecomeInvisible();
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    this.this$0.popupMenuCanceled();
                }
            });
        }
    }
}

