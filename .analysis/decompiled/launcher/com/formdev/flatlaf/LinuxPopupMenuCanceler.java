/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class LinuxPopupMenuCanceler
extends WindowAdapter
implements ChangeListener,
ComponentListener {
    private MenuElement[] lastPathSelectedPath;
    private Window window;

    LinuxPopupMenuCanceler() {
        MenuSelectionManager msm = MenuSelectionManager.defaultManager();
        msm.addChangeListener(this);
        this.lastPathSelectedPath = msm.getSelectedPath();
        if (this.lastPathSelectedPath.length > 0) {
            this.addWindowListeners(this.lastPathSelectedPath[0]);
        }
    }

    void uninstall() {
        MenuSelectionManager.defaultManager().removeChangeListener(this);
    }

    private void addWindowListeners(MenuElement selected) {
        this.removeWindowListeners();
        Component invoker = selected.getComponent();
        if (invoker instanceof JPopupMenu) {
            invoker = ((JPopupMenu)invoker).getInvoker();
        }
        Window window = this.window = invoker instanceof Window ? (Window)invoker : SwingUtilities.windowForComponent(invoker);
        if (this.window != null) {
            this.window.addWindowListener(this);
            this.window.addComponentListener(this);
        }
    }

    private void removeWindowListeners() {
        if (this.window != null) {
            this.window.removeWindowListener(this);
            this.window.removeComponentListener(this);
            this.window = null;
        }
    }

    private void cancelPopupMenu() {
        try {
            MenuElement[] selectedPath;
            MenuSelectionManager msm = MenuSelectionManager.defaultManager();
            for (MenuElement e : selectedPath = msm.getSelectedPath()) {
                if (!(e instanceof JPopupMenu)) continue;
                ((JPopupMenu)e).putClientProperty("JPopupMenu.firePopupMenuCanceled", true);
            }
            msm.clearSelectedPath();
        }
        catch (RuntimeException ex) {
            this.removeWindowListeners();
            throw ex;
        }
        catch (Error ex) {
            this.removeWindowListeners();
            throw ex;
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        MenuElement[] selectedPath = MenuSelectionManager.defaultManager().getSelectedPath();
        if (selectedPath.length == 0) {
            this.removeWindowListeners();
        } else if (this.lastPathSelectedPath.length == 0) {
            this.addWindowListeners(selectedPath[0]);
        }
        this.lastPathSelectedPath = selectedPath;
    }

    @Override
    public void windowIconified(WindowEvent e) {
        this.cancelPopupMenu();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        this.cancelPopupMenu();
    }

    @Override
    public void windowClosing(WindowEvent e) {
        this.cancelPopupMenu();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        this.cancelPopupMenu();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        this.cancelPopupMenu();
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        this.cancelPopupMenu();
    }
}

