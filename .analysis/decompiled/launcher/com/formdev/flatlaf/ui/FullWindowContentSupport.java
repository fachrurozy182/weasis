/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatNativeMacLibrary;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

class FullWindowContentSupport {
    private static final String KEY_DEBUG_SHOW_PLACEHOLDERS = "FlatLaf.debug.panel.showPlaceholders";
    private static ArrayList<WeakReference<JComponent>> placeholders = new ArrayList();

    FullWindowContentSupport() {
    }

    static Dimension getPlaceholderPreferredSize(JComponent c, String options) {
        Rectangle r;
        Rectangle bounds;
        JRootPane rootPane;
        if (!options.startsWith(SystemInfo.isMacOS ? "mac" : "win") || !c.isDisplayable() || (rootPane = SwingUtilities.getRootPane(c)) == null || (bounds = (Rectangle)rootPane.getClientProperty("FlatLaf.fullWindowContent.buttonsBounds")) == null) {
            return new Dimension(0, 0);
        }
        if (options.length() > 3 && (options.contains("leftToRight") && !c.getComponentOrientation().isLeftToRight() || options.contains("rightToLeft") && c.getComponentOrientation().isLeftToRight())) {
            return new Dimension(0, 0);
        }
        if (SystemInfo.isMacFullWindowContentSupported && FlatNativeMacLibrary.isLoaded() && (r = FlatNativeMacLibrary.getWindowButtonsBounds(SwingUtilities.windowForComponent(c))) != null) {
            bounds = r;
        }
        int width = bounds.width;
        int height = bounds.height;
        if (options.length() > 3) {
            if (width == 0 && options.contains("zeroInFullScreen")) {
                height = 0;
            }
            if (options.contains("horizontal")) {
                height = 0;
            }
            if (options.contains("vertical")) {
                width = 0;
            }
        }
        return new Dimension(width, height);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void registerPlaceholder(JComponent c) {
        ArrayList<WeakReference<JComponent>> arrayList = placeholders;
        synchronized (arrayList) {
            if (FullWindowContentSupport.indexOfPlaceholder(c) < 0) {
                placeholders.add(new WeakReference<JComponent>(c));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void unregisterPlaceholder(JComponent c) {
        ArrayList<WeakReference<JComponent>> arrayList = placeholders;
        synchronized (arrayList) {
            int index = FullWindowContentSupport.indexOfPlaceholder(c);
            if (index >= 0) {
                placeholders.remove(index);
            }
        }
    }

    private static int indexOfPlaceholder(JComponent c) {
        int size = placeholders.size();
        for (int i = 0; i < size; ++i) {
            if (placeholders.get(i).get() != c) continue;
            return i;
        }
        return -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void revalidatePlaceholders(Component container) {
        ArrayList<WeakReference<JComponent>> arrayList = placeholders;
        synchronized (arrayList) {
            if (placeholders.isEmpty()) {
                return;
            }
            Iterator<WeakReference<JComponent>> it = placeholders.iterator();
            while (it.hasNext()) {
                WeakReference<JComponent> ref = it.next();
                JComponent c = (JComponent)ref.get();
                if (c == null) {
                    it.remove();
                    continue;
                }
                if (!SwingUtilities.isDescendingFrom(c, container)) continue;
                c.revalidate();
            }
        }
    }

    static ComponentListener macInstallListeners(final JRootPane rootPane) {
        ComponentAdapter l = new ComponentAdapter(){
            boolean lastFullScreen;

            @Override
            public void componentResized(ComponentEvent e) {
                boolean fullScreen;
                Window window = SwingUtilities.windowForComponent(rootPane);
                if (window == null) {
                    return;
                }
                boolean bl = fullScreen = FlatNativeMacLibrary.isLoaded() && FlatNativeMacLibrary.isWindowFullScreen(window);
                if (fullScreen == this.lastFullScreen) {
                    return;
                }
                this.lastFullScreen = fullScreen;
                FullWindowContentSupport.macUpdateFullWindowContentButtonsBoundsProperty(rootPane);
            }
        };
        rootPane.addComponentListener(l);
        return l;
    }

    static void macUninstallListeners(JRootPane rootPane, ComponentListener l) {
        if (l != null) {
            rootPane.removeComponentListener(l);
        }
    }

    static void macUpdateFullWindowContentButtonsBoundsProperty(JRootPane rootPane) {
        if (!SystemInfo.isMacFullWindowContentSupported || !rootPane.isDisplayable()) {
            return;
        }
        Rectangle bounds = null;
        if (FlatClientProperties.clientPropertyBoolean(rootPane, "apple.awt.fullWindowContent", false)) {
            bounds = FlatNativeMacLibrary.isLoaded() ? FlatNativeMacLibrary.getWindowButtonsBounds(SwingUtilities.windowForComponent(rootPane)) : new Rectangle(68, 28);
        }
        rootPane.putClientProperty("FlatLaf.fullWindowContent.buttonsBounds", bounds);
    }

    static void macUninstallFullWindowContentButtonsBoundsProperty(JRootPane rootPane) {
        if (!SystemInfo.isMacFullWindowContentSupported) {
            return;
        }
        rootPane.putClientProperty("FlatLaf.fullWindowContent.buttonsBounds", null);
    }

    static void debugPaint(Graphics g, JComponent c) {
        Rectangle bounds;
        if (!UIManager.getBoolean(KEY_DEBUG_SHOW_PLACEHOLDERS)) {
            return;
        }
        int width = c.getWidth();
        int height = c.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        g.setColor(Color.red);
        FullWindowContentSupport.debugPaintRect(g, new Rectangle(width, height));
        JRootPane rootPane = SwingUtilities.getRootPane(c);
        if (rootPane != null && (bounds = (Rectangle)rootPane.getClientProperty("FlatLaf.fullWindowContent.buttonsBounds")) != null && (bounds.width != width || bounds.height != height)) {
            g.setColor(Color.magenta);
            FullWindowContentSupport.debugPaintRect(g, SwingUtilities.convertRectangle(rootPane, bounds, c));
        }
    }

    private static void debugPaintRect(Graphics g, Rectangle r) {
        g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
        int x2 = r.x + r.width - 1;
        int y2 = r.y + r.height - 1;
        Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
        g.drawLine(r.x, r.y, x2, y2);
        g.drawLine(r.x, y2, x2, r.y);
        FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
    }
}

