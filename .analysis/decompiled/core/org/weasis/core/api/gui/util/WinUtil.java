/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import javax.swing.JFrame;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import org.weasis.core.api.gui.util.GuiUtils;

public class WinUtil {
    private WinUtil() {
    }

    public static JFrame getParentJFrame(Component c) {
        for (Container p = c.getParent(); p != null; p = p.getParent()) {
            if (!(p instanceof JFrame)) continue;
            JFrame frame = (JFrame)p;
            return frame;
        }
        return null;
    }

    public static Frame getParentFrame(Component c) {
        for (Container p = c.getParent(); p != null; p = p.getParent()) {
            if (!(p instanceof Frame)) continue;
            Frame frame = (Frame)p;
            return frame;
        }
        return null;
    }

    public static Dialog getParentDialog(Component c) {
        for (Container p = c.getParent(); p != null; p = p.getParent()) {
            if (!(p instanceof Dialog)) continue;
            Dialog dialog = (Dialog)p;
            return dialog;
        }
        return null;
    }

    public static Component getValidComponent(Component c) {
        if (c != null && c.isDisplayable()) {
            return c;
        }
        return GuiUtils.getUICore().getApplicationWindow();
    }

    public static RootPaneContainer getRootPaneContainer(Component c) {
        if (c instanceof RootPaneContainer) {
            RootPaneContainer container = (RootPaneContainer)((Object)c);
            return container;
        }
        for (Container p = c.getParent(); p != null; p = p.getParent()) {
            if (!(p instanceof RootPaneContainer)) continue;
            RootPaneContainer container = (RootPaneContainer)((Object)p);
            return container;
        }
        return null;
    }

    public static Window getParentWindow(Component component) {
        return SwingUtilities.getWindowAncestor(component);
    }

    public static <T> T getParentOfClass(Component component, Class<T> class1) {
        Component obj;
        for (obj = component; obj != null && !class1.isAssignableFrom(obj.getClass()); obj = obj.getParent()) {
        }
        return (T)obj;
    }

    public static void center(Component component, int i, int j, int k, int l) {
        int i1 = component.getSize().width;
        int j1 = component.getSize().height;
        int k1 = (k - i1) / 2;
        int l1 = (l - j1) / 2;
        int i2 = i + k1;
        int j2 = j + l1;
        Dimension dimension = WinUtil.getScreenSize();
        if (i2 + i1 >= dimension.width) {
            i2 = dimension.width - i1;
        }
        if (j2 + j1 >= dimension.height) {
            j2 = dimension.height - j1;
        }
        if (i2 < 0) {
            i2 = 0;
        }
        if (j2 < 0) {
            j2 = 0;
        }
        component.setLocation(i2, j2);
    }

    public static GraphicsConfiguration getGraphicsDeviceConfig(Point p) {
        GraphicsDevice[] gd;
        GraphicsConfiguration gc = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice graphicsDevice : gd = ge.getScreenDevices()) {
            GraphicsConfiguration dgc;
            if (graphicsDevice.getType() != 0 || !(dgc = graphicsDevice.getDefaultConfiguration()).getBounds().contains(p)) continue;
            gc = dgc;
            break;
        }
        return gc;
    }

    public static Rectangle getClosedScreenBound(Rectangle bound) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        Rectangle screenBounds = null;
        Rectangle intersect = null;
        for (GraphicsDevice graphicsDevice : gd) {
            GraphicsConfiguration config = graphicsDevice.getDefaultConfiguration();
            Rectangle b = config.getBounds();
            Rectangle intersection = bound.intersection(b);
            if (intersection.width <= 0 || intersection.height <= 0 || intersect != null && intersect.width * intersect.height >= intersection.width * intersection.height) continue;
            Insets inset = toolkit.getScreenInsets(config);
            b.x += inset.left;
            b.y += inset.top;
            b.width -= inset.left + inset.right;
            b.height -= inset.top + inset.bottom;
            screenBounds = b;
            intersect = intersection;
        }
        return screenBounds;
    }

    public static void adjustLocationToFitScreen(Component cp, Point p) {
        Rectangle screenBounds;
        Insets screenInsets;
        if (cp == null) {
            return;
        }
        if (p == null) {
            WinUtil.centerOnScreen(cp);
            return;
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        GraphicsConfiguration gc = WinUtil.getGraphicsDeviceConfig(p);
        if (gc != null) {
            screenInsets = toolkit.getScreenInsets(gc);
            screenBounds = gc.getBounds();
        } else {
            screenInsets = new Insets(0, 0, 0, 0);
            screenBounds = new Rectangle(toolkit.getScreenSize());
        }
        int scrWidth = screenBounds.width - (screenInsets.left + screenInsets.right);
        int scrHeight = screenBounds.height - (screenInsets.top + screenInsets.bottom);
        Dimension size = cp.getPreferredSize();
        if (p.x + size.width > screenBounds.x + scrWidth) {
            p.x = screenBounds.x + scrWidth - (size.width + 2);
        }
        if (p.y + size.height > screenBounds.y + scrHeight) {
            p.y = screenBounds.y + scrHeight - (size.height + 2);
        }
        if (p.x < screenBounds.x) {
            p.x = screenBounds.x;
        }
        if (p.y < screenBounds.y) {
            p.y = screenBounds.y;
        }
        cp.setLocation(p);
    }

    public static void center(Component component, Point point, Dimension dimension) {
        WinUtil.center(component, point.x, point.y, dimension.width, dimension.height);
    }

    public static void center(Component component, Component component1) {
        if (component1 == null) {
            WinUtil.center(component);
        } else {
            WinUtil.center(component, component1.getLocation(), component1.getSize());
        }
    }

    private static void center(Component component) {
        Container container;
        Container container2 = container = component != null ? component.getParent() : null;
        if (container != null) {
            Window window = SwingUtilities.getWindowAncestor(container);
            if (window == null) {
                WinUtil.centerOnScreen(component);
            } else {
                WinUtil.center(component, window);
            }
        }
    }

    public static void centerOnScreen(Component component) {
        Dimension dimension = WinUtil.getScreenSize();
        WinUtil.center(component, 0, 0, dimension.width, dimension.height);
    }

    public static Point translate(Component component, Point point) {
        if (point == null || component == null) {
            return null;
        }
        Point point1 = component.getLocationOnScreen();
        point1.translate(point.x, point.y);
        return point1;
    }

    public static Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
}

