/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatSystemProperties;
import com.formdev.flatlaf.ui.FlatDropShadowBorder;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.formdev.flatlaf.ui.FlatNativeMacLibrary;
import com.formdev.flatlaf.ui.FlatNativeWindowsLibrary;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.StackUtils;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowFocusListener;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.JWindow;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboPopup;

public class FlatPopupFactory
extends PopupFactory {
    static final String KEY_POPUP_USES_NATIVE_BORDER = "FlatLaf.internal.FlatPopupFactory.popupUsesNativeBorder";
    private MethodHandle java8getPopupMethod;
    private MethodHandle java9getPopupMethod;
    private final ArrayList<NonFlashingPopup> stillShownHeavyWeightPopups = new ArrayList();

    @Override
    public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
        NonFlashingPopup popup;
        Point pt = this.fixToolTipLocation(owner, contents, x, y);
        if (pt != null) {
            x = pt.x;
            y = pt.y;
        }
        FlatPopupFactory.fixLinuxWaylandJava21focusIssue(owner);
        for (NonFlashingPopup popup2 : this.stillShownHeavyWeightPopups) {
            if (popup2.delegate == null || popup2.owner != owner || popup2.contents != contents && (!(popup2.contents instanceof JToolTip) || !(contents instanceof JToolTip))) continue;
            this.stillShownHeavyWeightPopups.remove(popup2);
            return FlatPopupFactory.reuseStillShownHeavyWeightPopups(popup2, contents, x, y);
        }
        if (!FlatPopupFactory.isOptionEnabled(owner, contents, "Popup.dropShadowPainted", "Popup.dropShadowPainted") || SystemInfo.isProjector || SystemInfo.isWebswing) {
            return new NonFlashingPopup(this.getPopupForScreenOfOwner(owner, contents, x, y, FlatPopupFactory.isForceHeavyWeight(owner, contents, x, y)), owner, contents);
        }
        if (SystemInfo.isMacOS || SystemInfo.isLinux) {
            popup = new NonFlashingPopup(this.getPopupForScreenOfOwner(owner, contents, x, y, true), owner, contents);
            if (popup.popupWindow != null && FlatPopupFactory.isMacOSBorderSupported()) {
                FlatPopupFactory.setupRoundedBorder(popup.popupWindow, owner, contents);
            }
            return popup;
        }
        if (FlatPopupFactory.isWindows11BorderSupported() && FlatPopupFactory.getBorderCornerRadius(owner, contents) > 0) {
            popup = new NonFlashingPopup(this.getPopupForScreenOfOwner(owner, contents, x, y, true), owner, contents);
            if (popup.popupWindow != null) {
                FlatPopupFactory.setupRoundedBorder(popup.popupWindow, owner, contents);
            }
            return popup;
        }
        Popup popupForScreenOfOwner = this.getPopupForScreenOfOwner(owner, contents, x, y, FlatPopupFactory.isForceHeavyWeight(owner, contents, x, y));
        GraphicsConfiguration gc = owner != null ? owner.getGraphicsConfiguration() : null;
        return gc != null && gc.isTranslucencyCapable() ? new DropShadowPopup(popupForScreenOfOwner, owner, contents) : new NonFlashingPopup(popupForScreenOfOwner, owner, contents);
    }

    private Popup getPopupForScreenOfOwner(Component owner, Component contents, int x, int y, boolean forceHeavyWeight) throws IllegalArgumentException {
        int count = 0;
        while (true) {
            Popup popup = forceHeavyWeight ? this.getHeavyWeightPopup(owner, contents, x, y) : super.getPopup(owner, contents, x, y);
            Window popupWindow = SwingUtilities.windowForComponent(contents);
            if (popupWindow == null || owner == null || popupWindow.getGraphicsConfiguration() == owner.getGraphicsConfiguration()) {
                return popup;
            }
            if (++count > 10) {
                return popup;
            }
            if (popupWindow instanceof JWindow) {
                ((JWindow)popupWindow).getContentPane().removeAll();
            }
            popupWindow.dispose();
        }
    }

    private Popup getHeavyWeightPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
        try {
            if (SystemInfo.isJava_9_orLater) {
                if (this.java9getPopupMethod == null) {
                    MethodType mt = MethodType.methodType(Popup.class, Component.class, Component.class, Integer.TYPE, Integer.TYPE, Boolean.TYPE);
                    this.java9getPopupMethod = MethodHandles.lookup().findVirtual(PopupFactory.class, "getPopup", mt);
                }
                return this.java9getPopupMethod.invoke(this, owner, contents, x, y, true);
            }
            if (this.java8getPopupMethod == null) {
                Method m = PopupFactory.class.getDeclaredMethod("getPopup", Component.class, Component.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
                m.setAccessible(true);
                this.java8getPopupMethod = MethodHandles.lookup().unreflect(m);
            }
            return this.java8getPopupMethod.invoke(this, owner, contents, x, y, 2);
        }
        catch (Throwable ex) {
            return super.getPopup(owner, contents, x, y);
        }
    }

    private static boolean isForceHeavyWeight(Component owner, Component contents, int x, int y) {
        boolean forceHeavyWeight = FlatPopupFactory.isOptionEnabled(owner, contents, "Popup.forceHeavyWeight", "Popup.forceHeavyWeight");
        return forceHeavyWeight || FlatPopupFactory.hasVisibleGlassPane(owner) || FlatPopupFactory.overlapsHeavyWeightComponent(owner, contents, x, y);
    }

    private static boolean isOptionEnabled(Component owner, Component contents, String clientKey, String uiKey) {
        Object value = FlatPopupFactory.getOption(owner, contents, clientKey, uiKey);
        return value instanceof Boolean ? (Boolean)value : false;
    }

    private static Object getOption(Component owner, Component contents, String clientKey, String uiKey) {
        for (Component c : new Component[]{owner, contents}) {
            Object value;
            if (!(c instanceof JComponent) || (value = ((JComponent)c).getClientProperty(clientKey)) == null) continue;
            return value;
        }
        return UIManager.get(uiKey);
    }

    private static NonFlashingPopup reuseStillShownHeavyWeightPopups(NonFlashingPopup reusePopup, Component contents, int ownerX, int ownerY) {
        NonFlashingPopup popup = reusePopup.cloneForReuse();
        popup.reset(contents, ownerX, ownerY);
        return popup;
    }

    private Point fixToolTipLocation(Component owner, Component contents, int x, int y) {
        if (!(contents instanceof JToolTip) || !this.wasInvokedFromToolTipManager() || this.hasTipLocation(owner)) {
            return null;
        }
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo == null) {
            return null;
        }
        Point mouseLocation = pointerInfo.getLocation();
        Dimension tipSize = contents.getPreferredSize();
        Rectangle tipBounds = new Rectangle(x, y, tipSize.width, tipSize.height);
        if (!tipBounds.contains(mouseLocation)) {
            return null;
        }
        GraphicsConfiguration gc = null;
        for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            GraphicsConfiguration dgc = device.getDefaultConfiguration();
            if (!dgc.getBounds().contains(mouseLocation)) continue;
            gc = dgc;
            break;
        }
        if (gc == null && owner != null) {
            gc = owner.getGraphicsConfiguration();
        }
        if (gc == null) {
            return null;
        }
        Rectangle screenBounds = gc.getBounds();
        Insets screenInsets = FlatUIUtils.getScreenInsets(gc);
        int screenTop = screenBounds.y + screenInsets.top;
        int newY = mouseLocation.y - tipSize.height - UIScale.scale(20);
        if (newY < screenTop) {
            return null;
        }
        return new Point(x, newY);
    }

    private boolean wasInvokedFromToolTipManager() {
        return StackUtils.wasInvokedFrom(ToolTipManager.class.getName(), "showTipWindow", 8);
    }

    private boolean hasTipLocation(Component owner) {
        MouseEvent me;
        if (!(owner instanceof JComponent)) {
            return false;
        }
        AWTEvent e = EventQueue.getCurrentEvent();
        if (e instanceof MouseEvent) {
            me = (MouseEvent)e;
        } else {
            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            if (pointerInfo == null) {
                return false;
            }
            Point location = new Point(pointerInfo.getLocation());
            SwingUtilities.convertPointFromScreen(location, owner);
            me = new MouseEvent(owner, 503, System.currentTimeMillis(), 0, location.x, location.y, 0, false);
        }
        return me.getSource() == owner && ((JComponent)owner).getToolTipLocation(me) != null;
    }

    private static boolean isWindows11BorderSupported() {
        return SystemInfo.isWindows_11_orLater && FlatSystemProperties.getBoolean("flatlaf.useRoundedPopupBorder", true) && FlatNativeWindowsLibrary.isLoaded();
    }

    private static boolean isMacOSBorderSupported() {
        return SystemInfo.isMacOS && FlatSystemProperties.getBoolean("flatlaf.useRoundedPopupBorder", true) && FlatNativeMacLibrary.isLoaded();
    }

    private static void setupRoundedBorder(Window popupWindow, Component owner, Component contents) {
        Color borderColor;
        int borderCornerRadius = FlatPopupFactory.getBorderCornerRadius(owner, contents);
        float borderWidth = FlatPopupFactory.getRoundedBorderWidth(owner, contents);
        if (contents instanceof JComponent) {
            Border border = ((JComponent)contents).getBorder();
            borderColor = (border = FlatUIUtils.unwrapNonUIResourceBorder(border)) instanceof FlatLineBorder ? ((FlatLineBorder)border).getLineColor() : (border instanceof LineBorder ? ((LineBorder)border).getLineColor() : (border instanceof EmptyBorder ? FlatNativeWindowsLibrary.COLOR_NONE : null));
            ((JComponent)contents).putClientProperty(KEY_POPUP_USES_NATIVE_BORDER, true);
        } else {
            borderColor = null;
        }
        if (popupWindow.isDisplayable()) {
            FlatPopupFactory.setupRoundedBorderImpl(popupWindow, borderCornerRadius, borderWidth, borderColor);
        } else {
            AtomicReference<HierarchyListener> l = new AtomicReference<HierarchyListener>();
            l.set(e -> {
                if (e.getID() == 1400 && (e.getChangeFlags() & 2L) != 0L) {
                    FlatPopupFactory.setupRoundedBorderImpl(popupWindow, borderCornerRadius, borderWidth, borderColor);
                    popupWindow.removeHierarchyListener((HierarchyListener)l.get());
                }
            });
            popupWindow.addHierarchyListener((HierarchyListener)l.get());
        }
    }

    private static void setupRoundedBorderImpl(Window popupWindow, int borderCornerRadius, float borderWidth, Color borderColor) {
        if (SystemInfo.isWindows) {
            long hwnd = FlatNativeWindowsLibrary.getHWND(popupWindow);
            int cornerPreference = borderCornerRadius <= 4 ? 3 : 2;
            FlatNativeWindowsLibrary.setWindowCornerPreference(hwnd, cornerPreference);
            FlatNativeWindowsLibrary.dwmSetWindowAttributeCOLORREF(hwnd, 34, borderColor);
        } else if (SystemInfo.isMacOS) {
            if (borderColor == null || borderColor == FlatNativeWindowsLibrary.COLOR_NONE) {
                borderWidth = 0.0f;
            }
            FlatNativeMacLibrary.setWindowRoundedBorder(popupWindow, borderCornerRadius, borderWidth, borderColor != null ? borderColor.getRGB() : 0);
        }
    }

    private static void resetWindows11Border(Window popupWindow) {
        long hwnd = FlatNativeWindowsLibrary.getHWND(popupWindow);
        if (hwnd == 0L) {
            return;
        }
        FlatNativeWindowsLibrary.setWindowCornerPreference(hwnd, 1);
    }

    private static int getBorderCornerRadius(Component owner, Component contents) {
        String uiKey = contents instanceof BasicComboPopup ? "ComboBox.borderCornerRadius" : (contents instanceof JPopupMenu ? "PopupMenu.borderCornerRadius" : (contents instanceof JToolTip ? "ToolTip.borderCornerRadius" : "Popup.borderCornerRadius"));
        Object value = FlatPopupFactory.getOption(owner, contents, "Popup.borderCornerRadius", uiKey);
        return value instanceof Integer ? (Integer)value : 0;
    }

    private static float getRoundedBorderWidth(Component owner, Component contents) {
        String uiKey = contents instanceof BasicComboPopup ? "ComboBox.roundedBorderWidth" : (contents instanceof JPopupMenu ? "PopupMenu.roundedBorderWidth" : (contents instanceof JToolTip ? "ToolTip.roundedBorderWidth" : "Popup.roundedBorderWidth"));
        Object value = FlatPopupFactory.getOption(owner, contents, "Popup.roundedBorderWidth", uiKey);
        return value instanceof Number ? ((Number)value).floatValue() : 0.0f;
    }

    private static boolean hasVisibleGlassPane(Component owner) {
        if (owner == null) {
            return false;
        }
        Window window = SwingUtilities.windowForComponent(owner);
        if (!(window instanceof RootPaneContainer)) {
            return false;
        }
        Component glassPane = ((RootPaneContainer)((Object)window)).getGlassPane();
        return glassPane != null && glassPane.isVisible();
    }

    private static boolean overlapsHeavyWeightComponent(Component owner, Component contents, int x, int y) {
        if (owner == null) {
            return false;
        }
        Window window = SwingUtilities.getWindowAncestor(owner);
        if (window == null) {
            return false;
        }
        Rectangle r = new Rectangle(new Point(x, y), contents.getPreferredSize());
        return FlatPopupFactory.overlapsHeavyWeightComponent(window, r);
    }

    private static boolean overlapsHeavyWeightComponent(Component parent, Rectangle r) {
        if (!parent.isVisible() || !r.intersects(parent.getBounds())) {
            return false;
        }
        if (!parent.isLightweight() && !(parent instanceof Window)) {
            return true;
        }
        if (parent instanceof Container) {
            Rectangle r2 = new Rectangle(r.x - parent.getX(), r.y - parent.getY(), r.width, r.height);
            for (Component c : ((Container)parent).getComponents()) {
                if (!FlatPopupFactory.overlapsHeavyWeightComponent(c, r2)) continue;
                return true;
            }
        }
        return false;
    }

    private static void fixLinuxWaylandJava21focusIssue(Component owner) {
        if (owner == null || !SystemInfo.isLinux || SystemInfo.javaVersion < SystemInfo.toVersion(21, 0, 0, 0)) {
            return;
        }
        Window window = SwingUtilities.getWindowAncestor(owner);
        if (window == null) {
            return;
        }
        for (WindowFocusListener l : window.getWindowFocusListeners()) {
            if (!"sun.awt.UNIXToolkit$1".equals(l.getClass().getName())) continue;
            window.removeWindowFocusListener(l);
            break;
        }
    }

    private static void showPopupAndFixLocation(Popup popup, Window popupWindow) {
        if (popupWindow != null) {
            int x = popupWindow.getX();
            int y = popupWindow.getY();
            if (!popupWindow.isVisible()) {
                popup.show();
            } else {
                popupWindow.pack();
            }
            if (popupWindow.getX() != x || popupWindow.getY() != y) {
                popupWindow.setLocation(x, y);
            }
        } else {
            popup.show();
        }
    }

    private class DropShadowPopup
    extends NonFlashingPopup
    implements ComponentListener {
        private JComponent lightComp;
        private Border oldBorder;
        private boolean oldOpaque;
        private boolean mediumWeightShown;
        private Panel mediumWeightPanel;
        private JPanel dropShadowPanel;
        private ComponentListener mediumPanelListener;
        private Popup dropShadowDelegate;
        private Window dropShadowWindow;
        private JPanel dropShadowPanel2;
        private Color oldDropShadowWindowBackground;

        DropShadowPopup(Popup delegate, Component owner, Component contents) {
            super(delegate, owner, contents);
            Dimension size = contents.getPreferredSize();
            if (size.width <= 0 || size.height <= 0) {
                return;
            }
            if (this.popupWindow != null) {
                this.dropShadowPanel2 = new JPanel();
                this.dropShadowPanel2.setBorder(this.createDropShadowBorder());
                this.dropShadowPanel2.setOpaque(false);
                Dimension prefSize = this.popupWindow.getPreferredSize();
                Insets insets = this.dropShadowPanel2.getInsets();
                this.dropShadowPanel2.setPreferredSize(new Dimension(prefSize.width + insets.left + insets.right, prefSize.height + insets.top + insets.bottom));
                int x = this.popupWindow.getX() - insets.left;
                int y = this.popupWindow.getY() - insets.top;
                this.dropShadowDelegate = FlatPopupFactory.this.getPopupForScreenOfOwner(owner, this.dropShadowPanel2, x, y, true);
                this.dropShadowWindow = SwingUtilities.windowForComponent(this.dropShadowPanel2);
                if (this.dropShadowWindow != null) {
                    this.oldDropShadowWindowBackground = this.dropShadowWindow.getBackground();
                    this.dropShadowWindow.setBackground(new Color(0, true));
                }
                if (SystemInfo.isWindows_11_orLater && FlatNativeWindowsLibrary.isLoaded()) {
                    FlatPopupFactory.resetWindows11Border(this.popupWindow);
                    if (this.dropShadowWindow != null) {
                        FlatPopupFactory.resetWindows11Border(this.dropShadowWindow);
                    }
                }
            } else {
                this.mediumWeightPanel = (Panel)SwingUtilities.getAncestorOfClass(Panel.class, contents);
                if (this.mediumWeightPanel != null) {
                    this.dropShadowPanel = new JPanel();
                    this.dropShadowPanel.setBorder(this.createDropShadowBorder());
                    this.dropShadowPanel.setOpaque(false);
                    this.dropShadowPanel.setSize(FlatUIUtils.addInsets(this.mediumWeightPanel.getSize(), this.dropShadowPanel.getInsets()));
                } else {
                    Container p = contents.getParent();
                    if (!(p instanceof JComponent)) {
                        return;
                    }
                    this.lightComp = (JComponent)p;
                    this.oldBorder = this.lightComp.getBorder();
                    this.oldOpaque = this.lightComp.isOpaque();
                    this.lightComp.setBorder(this.createDropShadowBorder());
                    this.lightComp.setOpaque(false);
                    this.lightComp.setSize(this.lightComp.getPreferredSize());
                }
            }
        }

        private DropShadowPopup(DropShadowPopup reusePopup) {
            super(reusePopup);
            this.dropShadowDelegate = reusePopup.dropShadowDelegate;
            this.dropShadowWindow = reusePopup.dropShadowWindow;
            this.dropShadowPanel2 = reusePopup.dropShadowPanel2;
            this.oldDropShadowWindowBackground = reusePopup.oldDropShadowWindowBackground;
        }

        @Override
        NonFlashingPopup cloneForReuse() {
            return new DropShadowPopup(this);
        }

        private Border createDropShadowBorder() {
            return new FlatDropShadowBorder(UIManager.getColor("Popup.dropShadowColor"), UIManager.getInsets("Popup.dropShadowInsets"), FlatUIUtils.getUIFloat("Popup.dropShadowOpacity", 0.5f));
        }

        @Override
        void showImpl() {
            if (this.dropShadowDelegate != null) {
                FlatPopupFactory.showPopupAndFixLocation(this.dropShadowDelegate, this.dropShadowWindow);
            }
            if (this.mediumWeightPanel != null) {
                this.showMediumWeightDropShadow();
            }
            super.showImpl();
            if (this.lightComp != null) {
                Insets insets = this.lightComp.getInsets();
                if (insets.left != 0 || insets.top != 0) {
                    this.lightComp.setLocation(this.lightComp.getX() - insets.left, this.lightComp.getY() - insets.top);
                }
            }
            if (this.popupWindow != null) {
                this.removeAllPopupWindowComponentListeners();
                this.popupWindow.addComponentListener(this);
            }
        }

        @Override
        void hideImpl() {
            if (this.popupWindow != null) {
                this.removeAllPopupWindowComponentListeners();
            }
            if (this.dropShadowDelegate != null) {
                this.dropShadowDelegate.hide();
                this.dropShadowDelegate = null;
                this.dropShadowPanel2 = null;
            }
            if (this.mediumWeightPanel != null) {
                this.hideMediumWeightDropShadow();
                this.dropShadowPanel = null;
                this.mediumWeightPanel = null;
            }
            super.hideImpl();
            if (this.dropShadowWindow != null) {
                this.dropShadowWindow.setBackground(this.oldDropShadowWindowBackground);
                this.dropShadowWindow = null;
            }
            if (this.lightComp != null) {
                this.lightComp.setBorder(this.oldBorder);
                this.lightComp.setOpaque(this.oldOpaque);
                this.lightComp = null;
            }
        }

        private void showMediumWeightDropShadow() {
            if (this.mediumWeightShown) {
                return;
            }
            this.mediumWeightShown = true;
            if (this.owner == null) {
                return;
            }
            Window window = SwingUtilities.windowForComponent(this.owner);
            if (!(window instanceof RootPaneContainer)) {
                return;
            }
            this.dropShadowPanel.setVisible(false);
            JLayeredPane layeredPane = ((RootPaneContainer)((Object)window)).getLayeredPane();
            layeredPane.add(this.dropShadowPanel, JLayeredPane.POPUP_LAYER, 0);
            this.moveMediumWeightDropShadow();
            this.resizeMediumWeightDropShadow();
            this.mediumPanelListener = new ComponentListener(){

                @Override
                public void componentShown(ComponentEvent e) {
                    if (DropShadowPopup.this.dropShadowPanel != null) {
                        DropShadowPopup.this.dropShadowPanel.setVisible(true);
                    }
                }

                @Override
                public void componentHidden(ComponentEvent e) {
                    if (DropShadowPopup.this.dropShadowPanel != null) {
                        DropShadowPopup.this.dropShadowPanel.setVisible(false);
                    }
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    DropShadowPopup.this.moveMediumWeightDropShadow();
                }

                @Override
                public void componentResized(ComponentEvent e) {
                    DropShadowPopup.this.resizeMediumWeightDropShadow();
                }
            };
            this.mediumWeightPanel.addComponentListener(this.mediumPanelListener);
        }

        private void hideMediumWeightDropShadow() {
            this.mediumWeightPanel.removeComponentListener(this.mediumPanelListener);
            Container parent = this.dropShadowPanel.getParent();
            if (parent != null) {
                Rectangle bounds = this.dropShadowPanel.getBounds();
                parent.remove(this.dropShadowPanel);
                parent.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }

        private void moveMediumWeightDropShadow() {
            if (this.dropShadowPanel != null && this.mediumWeightPanel != null) {
                Point location = this.mediumWeightPanel.getLocation();
                Insets insets = this.dropShadowPanel.getInsets();
                this.dropShadowPanel.setLocation(location.x - insets.left, location.y - insets.top);
            }
        }

        private void resizeMediumWeightDropShadow() {
            if (this.dropShadowPanel != null && this.mediumWeightPanel != null) {
                this.dropShadowPanel.setSize(FlatUIUtils.addInsets(this.mediumWeightPanel.getSize(), this.dropShadowPanel.getInsets()));
            }
        }

        @Override
        void reset(Component contents, int ownerX, int ownerY) {
            if (this.popupWindow != null) {
                this.removeAllPopupWindowComponentListeners();
            }
            super.reset(contents, ownerX, ownerY);
            this.updateDropShadowWindowBounds();
        }

        private void updateDropShadowWindowBounds() {
            if (this.dropShadowWindow == null) {
                return;
            }
            Dimension size = this.popupWindow.getSize();
            Insets insets = this.dropShadowPanel2.getInsets();
            int w = size.width + insets.left + insets.right;
            int h = size.height + insets.top + insets.bottom;
            int x = this.popupWindow.getX() - insets.left;
            int y = this.popupWindow.getY() - insets.top;
            this.dropShadowWindow.setBounds(x, y, w, h);
            this.dropShadowWindow.validate();
        }

        private void removeAllPopupWindowComponentListeners() {
            for (ComponentListener l : this.popupWindow.getComponentListeners()) {
                if (!(l instanceof DropShadowPopup)) continue;
                this.popupWindow.removeComponentListener(l);
            }
        }

        @Override
        public void componentResized(ComponentEvent e) {
            if (e.getSource() == this.popupWindow) {
                this.updateDropShadowWindowBounds();
            }
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            if (e.getSource() == this.popupWindow) {
                this.updateDropShadowWindowBounds();
            }
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }
    }

    private class NonFlashingPopup
    extends Popup {
        private Popup delegate;
        Component owner;
        private Component contents;
        Window popupWindow;
        private Color oldPopupWindowBackground;
        private boolean disposed;

        NonFlashingPopup(Popup delegate, Component owner, Component contents) {
            this.delegate = delegate;
            this.owner = owner;
            this.contents = contents;
            this.popupWindow = SwingUtilities.windowForComponent(contents);
            if (this.popupWindow != null) {
                this.oldPopupWindowBackground = this.popupWindow.getBackground();
                this.popupWindow.setBackground(contents.getBackground());
            }
        }

        private NonFlashingPopup(NonFlashingPopup reusePopup) {
            this.delegate = reusePopup.delegate;
            this.owner = reusePopup.owner;
            this.contents = reusePopup.contents;
            this.popupWindow = reusePopup.popupWindow;
            this.oldPopupWindowBackground = reusePopup.oldPopupWindowBackground;
        }

        NonFlashingPopup cloneForReuse() {
            return new NonFlashingPopup(this);
        }

        @Override
        public final void show() {
            if (this.disposed) {
                return;
            }
            this.showImpl();
        }

        void showImpl() {
            if (this.delegate != null) {
                Dimension prefSize;
                Container parent;
                if ((SystemInfo.isMacOS || SystemInfo.isLinux) && this.popupWindow instanceof JWindow) {
                    HierarchyListener l = e -> {
                        if (e.getID() == 1400 && (e.getChangeFlags() & 4L) != 0L) {
                            ((JWindow)this.popupWindow).getRootPane().paintImmediately(0, 0, this.popupWindow.getWidth(), this.popupWindow.getHeight());
                        }
                    };
                    this.popupWindow.addHierarchyListener(l);
                    try {
                        FlatPopupFactory.showPopupAndFixLocation(this.delegate, this.popupWindow);
                    }
                    finally {
                        this.popupWindow.removeHierarchyListener(l);
                    }
                } else {
                    FlatPopupFactory.showPopupAndFixLocation(this.delegate, this.popupWindow);
                }
                if (this.contents instanceof JToolTip && this.popupWindow == null && (parent = this.contents.getParent()) instanceof JPanel && !(prefSize = parent.getPreferredSize()).equals(parent.getSize())) {
                    Container mediumWeightPanel = SwingUtilities.getAncestorOfClass(Panel.class, parent);
                    Container c = mediumWeightPanel != null ? mediumWeightPanel : parent;
                    c.setSize(prefSize);
                    c.validate();
                }
            }
        }

        @Override
        public final void hide() {
            if (this.disposed) {
                return;
            }
            this.disposed = true;
            if (!(this.popupWindow instanceof JWindow) || this.contents instanceof JPopupMenu || !FlatSystemProperties.getBoolean("flatlaf.reuseVisiblePopupWindow", true)) {
                this.hideImpl();
                return;
            }
            ((JWindow)this.popupWindow).getContentPane().removeAll();
            FlatPopupFactory.this.stillShownHeavyWeightPopups.add(this);
            EventQueue.invokeLater(() -> {
                if (FlatPopupFactory.this.stillShownHeavyWeightPopups.remove(this)) {
                    this.hideImpl();
                }
            });
        }

        void hideImpl() {
            if (this.contents instanceof JComponent) {
                ((JComponent)this.contents).putClientProperty(FlatPopupFactory.KEY_POPUP_USES_NATIVE_BORDER, null);
            }
            if (this.delegate != null) {
                this.delegate.hide();
                this.delegate = null;
                this.owner = null;
                this.contents = null;
            }
            if (this.popupWindow != null) {
                this.popupWindow.setBackground(this.oldPopupWindowBackground);
                if (SystemInfo.isMacOS) {
                    this.popupWindow.dispose();
                }
                this.popupWindow = null;
            }
        }

        void reset(Component contents, int ownerX, int ownerY) {
            this.popupWindow.setLocation(ownerX, ownerY);
            Container contentPane = ((JWindow)this.popupWindow).getContentPane();
            contentPane.removeAll();
            contentPane.add(contents, "Center");
            this.popupWindow.pack();
            if (this.contents != contents) {
                Object old;
                Object object = old = this.contents instanceof JComponent ? ((JComponent)this.contents).getClientProperty(FlatPopupFactory.KEY_POPUP_USES_NATIVE_BORDER) : null;
                if (contents instanceof JComponent) {
                    ((JComponent)contents).putClientProperty(FlatPopupFactory.KEY_POPUP_USES_NATIVE_BORDER, old);
                }
                this.contents = contents;
            }
        }
    }
}

