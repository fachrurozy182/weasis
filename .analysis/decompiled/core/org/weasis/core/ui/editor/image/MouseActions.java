/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.service.prefs.Preferences
 */
package org.weasis.core.ui.editor.image;

import java.awt.MouseInfo;
import org.osgi.service.prefs.Preferences;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.AuditLog;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.api.service.WProperties;

public class MouseActions {
    public static final int SCROLL_MASK = 2;
    public static final String PREFERENCE_NODE = "mouse.action";
    public static final String P_MOUSE_LEFT = "mouse_left";
    public static final String P_MOUSE_MIDDLE = "mouse_middle";
    public static final String P_MOUSE_RIGHT = "mouse_right";
    public static final String P_MOUSE_WHEEL = "mouse_wheel";
    public static final String T_LEFT = "left";
    public static final String T_MIDDLE = "middle";
    public static final String T_RIGHT = "right";
    public static final String T_WHEEL = "wheel";
    private String left;
    private String middle;
    private String right;
    private String wheel;
    private int activeButtons;

    public MouseActions(Preferences prefs) {
        WProperties preferences = GuiUtils.getUICore().getSystemPreferences();
        this.activeButtons = preferences.getIntProperty("weasis.toolbar.mouse.buttons", 7170);
        this.left = preferences.getProperty("weasis.toolbar.mouse.left", ActionW.WINLEVEL.cmd());
        this.middle = preferences.getProperty("weasis.toolbar.mouse.middle", ActionW.PAN.cmd());
        this.right = preferences.getProperty("weasis.toolbar.mouse.right", ActionW.CONTEXTMENU.cmd());
        this.wheel = preferences.getProperty("weasis.toolbar.mouse.wheel", ActionW.SCROLL_SERIES.cmd());
        this.applyPreferences(prefs);
        int numberOfButtons = MouseInfo.getNumberOfButtons();
        if (numberOfButtons < 3) {
            this.activeButtons &= 0xFFFFF7FF;
        }
    }

    public String getWheel() {
        return this.wheel;
    }

    public void setWheel(String wheel) {
        this.wheel = wheel;
    }

    public String toString() {
        return this.left + "/" + this.right + "/" + this.wheel;
    }

    public String getLeft() {
        return this.left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return this.right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public String getMiddle() {
        return this.middle;
    }

    public void setMiddle(String middle) {
        this.middle = middle;
    }

    public String getAction(String type) {
        if (T_LEFT.equals(type)) {
            return this.left;
        }
        if (T_MIDDLE.equals(type)) {
            return this.middle;
        }
        if (T_RIGHT.equals(type)) {
            return this.right;
        }
        if (T_WHEEL.equals(type)) {
            return this.wheel;
        }
        return null;
    }

    public int getActiveButtons() {
        return this.activeButtons;
    }

    public void setActiveButtons(int activeButtons) {
        this.activeButtons = activeButtons;
    }

    public void setAction(String type, String action) {
        if (T_LEFT.equals(type)) {
            this.setLeft(action);
        } else if (T_MIDDLE.equals(type)) {
            this.setMiddle(action);
        } else if (T_RIGHT.equals(type)) {
            this.setRight(action);
        } else if (T_WHEEL.equals(type)) {
            this.setWheel(action);
        }
        AuditLog.LOGGER.info("mouse:{} action:{}", (Object)type, (Object)action);
    }

    public void applyPreferences(Preferences prefs) {
        if (prefs != null) {
            Preferences p = prefs.node(PREFERENCE_NODE);
            this.left = p.get(P_MOUSE_LEFT, this.left);
            this.middle = p.get(P_MOUSE_MIDDLE, this.middle);
            this.right = p.get(P_MOUSE_RIGHT, this.right);
            this.wheel = p.get(P_MOUSE_WHEEL, this.wheel);
        }
    }

    public void savePreferences(Preferences prefs) {
        if (prefs != null) {
            Preferences p = prefs.node(PREFERENCE_NODE);
            BundlePreferences.putStringPreferences(p, P_MOUSE_LEFT, this.left);
            BundlePreferences.putStringPreferences(p, P_MOUSE_MIDDLE, this.middle);
            BundlePreferences.putStringPreferences(p, P_MOUSE_RIGHT, this.right);
            BundlePreferences.putStringPreferences(p, P_MOUSE_WHEEL, this.wheel);
        }
    }
}

