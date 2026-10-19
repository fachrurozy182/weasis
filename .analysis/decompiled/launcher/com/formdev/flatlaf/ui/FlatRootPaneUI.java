/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.ui.FlatEmptyBorder;
import com.formdev.flatlaf.ui.FlatNativeMacLibrary;
import com.formdev.flatlaf.ui.FlatNativeWindowBorder;
import com.formdev.flatlaf.ui.FlatTitlePane;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.FlatWindowResizer;
import com.formdev.flatlaf.ui.FullWindowContentSupport;
import com.formdev.flatlaf.util.HiDPIUtils;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.Window;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.function.Function;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.RootPaneUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicRootPaneUI;

public class FlatRootPaneUI
extends BasicRootPaneUI {
    protected final Color borderColor = UIManager.getColor("TitlePane.borderColor");
    protected JRootPane rootPane;
    protected FlatTitlePane titlePane;
    protected FlatWindowResizer windowResizer;
    private Object nativeWindowBorderData;
    private LayoutManager oldLayout;
    private ComponentListener macFullWindowContentListener;
    private PropertyChangeListener macWindowBackgroundListener;
    protected static final Integer TITLE_PANE_LAYER = JLayeredPane.FRAME_CONTENT_LAYER - 1;
    private static final Integer TITLE_PANE_MOUSE_LAYER = JLayeredPane.FRAME_CONTENT_LAYER - 2;
    private static final Integer WINDOW_TOP_BORDER_LAYER = Integer.MAX_VALUE;
    protected static final Integer TITLE_PANE_FULL_WINDOW_CONTENT_LAYER = JLayeredPane.FRAME_CONTENT_LAYER + 1;

    public static ComponentUI createUI(JComponent c) {
        return new FlatRootPaneUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.rootPane = (JRootPane)c;
        if (this.rootPane.getWindowDecorationStyle() != 0) {
            this.installClientDecorations();
        } else {
            this.installBorder();
        }
        this.installNativeWindowBorder();
        this.macInstallFullWindowContentSupport();
    }

    protected void installBorder() {
        Border b;
        if (this.borderColor != null && ((b = this.rootPane.getBorder()) == null || b instanceof UIResource)) {
            this.rootPane.setBorder(new FlatWindowTitleBorder(this.borderColor));
        }
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        this.uninstallNativeWindowBorder();
        this.uninstallClientDecorations();
        this.macUninstallFullWindowContentSupport();
        this.rootPane = null;
    }

    @Override
    protected void installDefaults(JRootPane c) {
        Color background;
        Container parent;
        super.installDefaults(c);
        if (!c.isBackgroundSet() || c.getBackground() instanceof UIResource) {
            c.setBackground(UIManager.getColor("RootPane.background"));
        }
        if (!c.isForegroundSet() || c.getForeground() instanceof UIResource) {
            c.setForeground(UIManager.getColor("RootPane.foreground"));
        }
        if (!c.isFontSet() || c.getFont() instanceof UIResource) {
            c.setFont(UIManager.getFont("RootPane.font"));
        }
        if (((parent = c.getParent()) instanceof JFrame || parent instanceof JDialog) && ((background = parent.getBackground()) == null || background instanceof UIResource)) {
            if (SystemInfo.isMacOS) {
                Runnable r = () -> {
                    parent.setBackground(UIManager.getColor("control"));
                    c.paintImmediately(0, 0, c.getWidth(), c.getHeight());
                };
                if (c.getClientProperty("FlatLaf.internal.animatedLafChange") != null) {
                    c.putClientProperty("FlatLaf.internal.animatedLafChange.runWhenFinished", r);
                } else {
                    EventQueue.invokeLater(r);
                }
            } else {
                parent.setBackground(UIManager.getColor("control"));
            }
        }
        this.macSetWindowAppearance(c);
        this.macClearBackgroundForTranslucentWindow(c);
    }

    @Override
    protected void uninstallDefaults(JRootPane c) {
        super.uninstallDefaults(c);
        if (c.isBackgroundSet() && c.getBackground() instanceof UIResource) {
            c.setBackground(null);
        }
        if (c.isForegroundSet() && c.getForeground() instanceof UIResource) {
            c.setForeground(null);
        }
        if (c.isFontSet() && c.getFont() instanceof UIResource) {
            c.setFont(null);
        }
    }

    @Override
    protected void installListeners(JRootPane root) {
        super.installListeners(root);
        if (SystemInfo.isMacFullWindowContentSupported) {
            this.macFullWindowContentListener = FullWindowContentSupport.macInstallListeners(root);
        }
        this.macInstallWindowBackgroundListener(root);
    }

    @Override
    protected void uninstallListeners(JRootPane root) {
        super.uninstallListeners(root);
        if (SystemInfo.isMacFullWindowContentSupported) {
            FullWindowContentSupport.macUninstallListeners(root, this.macFullWindowContentListener);
            this.macFullWindowContentListener = null;
        }
        this.macUninstallWindowBackgroundListener(root);
    }

    protected void installNativeWindowBorder() {
        this.nativeWindowBorderData = FlatNativeWindowBorder.install(this.rootPane);
    }

    protected void uninstallNativeWindowBorder() {
        FlatNativeWindowBorder.uninstall(this.rootPane, this.nativeWindowBorderData);
        this.nativeWindowBorderData = null;
    }

    public static void updateNativeWindowBorder(JRootPane rootPane) {
        RootPaneUI rui = rootPane.getUI();
        if (!(rui instanceof FlatRootPaneUI)) {
            return;
        }
        FlatRootPaneUI ui = (FlatRootPaneUI)rui;
        ui.uninstallNativeWindowBorder();
        ui.installNativeWindowBorder();
    }

    protected void installClientDecorations() {
        boolean isNativeWindowBorderSupported = FlatNativeWindowBorder.isSupported();
        if (this.rootPane.getWindowDecorationStyle() != 0 && !isNativeWindowBorderSupported) {
            LookAndFeel.installBorder(this.rootPane, "RootPane.border");
        } else {
            LookAndFeel.uninstallBorder(this.rootPane);
        }
        this.setTitlePane(this.createTitlePane());
        this.oldLayout = this.rootPane.getLayout();
        this.rootPane.setLayout(this.createRootLayout());
        if (!isNativeWindowBorderSupported) {
            this.windowResizer = this.createWindowResizer();
        }
    }

    protected void uninstallClientDecorations() {
        LookAndFeel.uninstallBorder(this.rootPane);
        this.setTitlePane(null);
        if (this.windowResizer != null) {
            this.windowResizer.uninstall();
            this.windowResizer = null;
        }
        if (this.oldLayout != null) {
            this.rootPane.setLayout(this.oldLayout);
            this.oldLayout = null;
        }
        if (this.rootPane.getWindowDecorationStyle() == 0) {
            this.rootPane.revalidate();
            this.rootPane.repaint();
        }
    }

    protected FlatRootLayout createRootLayout() {
        return new FlatRootLayout();
    }

    protected FlatWindowResizer createWindowResizer() {
        return new FlatWindowResizer.WindowResizer(this.rootPane);
    }

    protected FlatTitlePane createTitlePane() {
        return new FlatTitlePane(this.rootPane);
    }

    private Integer getLayerForTitlePane() {
        return FlatRootPaneUI.isFullWindowContent(this.rootPane) ? TITLE_PANE_FULL_WINDOW_CONTENT_LAYER : TITLE_PANE_LAYER;
    }

    protected void setTitlePane(FlatTitlePane newTitlePane) {
        JLayeredPane layeredPane = this.rootPane.getLayeredPane();
        if (this.titlePane != null) {
            layeredPane.remove(this.titlePane);
            layeredPane.remove(this.titlePane.mouseLayer);
            if (this.titlePane.windowTopBorderLayer != null) {
                layeredPane.remove(this.titlePane.windowTopBorderLayer);
            }
        }
        if (newTitlePane != null) {
            layeredPane.add((Component)newTitlePane, this.getLayerForTitlePane());
            layeredPane.add((Component)newTitlePane.mouseLayer, TITLE_PANE_MOUSE_LAYER);
            if (newTitlePane.windowTopBorderLayer != null && newTitlePane.isWindowTopBorderNeeded() && FlatRootPaneUI.isFullWindowContent(this.rootPane)) {
                layeredPane.add((Component)newTitlePane.windowTopBorderLayer, WINDOW_TOP_BORDER_LAYER);
            }
        }
        this.titlePane = newTitlePane;
    }

    private void macInstallFullWindowContentSupport() {
        if (!SystemInfo.isMacOS) {
            return;
        }
        if (this.isMacButtonsSpacingSupported() && this.rootPane.isDisplayable()) {
            int buttonsSpacing = 0;
            String value = (String)this.rootPane.getClientProperty("FlatLaf.macOS.windowButtonsSpacing");
            if (value != null) {
                switch (value) {
                    case "medium": {
                        buttonsSpacing = 1;
                        break;
                    }
                    case "large": {
                        buttonsSpacing = 2;
                    }
                }
            }
            FlatNativeMacLibrary.setWindowButtonsSpacing(this.getParentWindow(this.rootPane), buttonsSpacing);
        }
        FullWindowContentSupport.macUpdateFullWindowContentButtonsBoundsProperty(this.rootPane);
    }

    private void macUninstallFullWindowContentSupport() {
        if (!SystemInfo.isMacOS) {
            return;
        }
        if (UIManager.getLookAndFeel() instanceof FlatLaf) {
            return;
        }
        if (this.isMacButtonsSpacingSupported()) {
            FlatNativeMacLibrary.setWindowButtonsSpacing(this.getParentWindow(this.rootPane), 0);
        }
        FullWindowContentSupport.macUninstallFullWindowContentButtonsBoundsProperty(this.rootPane);
    }

    private boolean isMacButtonsSpacingSupported() {
        return SystemInfo.isMacOS && SystemInfo.isJava_17_orLater && FlatNativeMacLibrary.isLoaded();
    }

    private void macSetWindowAppearance(JRootPane c) {
        if (!SystemInfo.isMacOS) {
            return;
        }
        Window window = this.getParentWindow(c);
        if (window != null && FlatNativeMacLibrary.isLoaded()) {
            FlatNativeMacLibrary.setWindowAppearance(window, FlatLaf.isLafDark() ? "NSAppearanceNameDarkAqua" : "NSAppearanceNameAqua");
        }
    }

    private void macInstallWindowBackgroundListener(JRootPane c) {
        if (!SystemInfo.isMacOS) {
            return;
        }
        Window window = this.getParentWindow(c);
        if (window != null && this.macWindowBackgroundListener == null) {
            this.macWindowBackgroundListener = e -> this.macClearBackgroundForTranslucentWindow(c);
            window.addPropertyChangeListener("background", this.macWindowBackgroundListener);
        }
    }

    private void macUninstallWindowBackgroundListener(JRootPane c) {
        if (!SystemInfo.isMacOS) {
            return;
        }
        Window window = this.getParentWindow(c);
        if (window != null && this.macWindowBackgroundListener != null) {
            window.removePropertyChangeListener("background", this.macWindowBackgroundListener);
            this.macWindowBackgroundListener = null;
        }
    }

    private void macClearBackgroundForTranslucentWindow(JRootPane c) {
        Color windowBackground;
        if (!SystemInfo.isMacOS) {
            return;
        }
        Window window = this.getParentWindow(c);
        if (window != null && (windowBackground = window.getBackground()) != null && windowBackground.getAlpha() < 255 && c.getBackground() instanceof UIResource) {
            c.setBackground(null);
        }
    }

    private Window getParentWindow(JRootPane c) {
        Container parent = c.getParent();
        return parent instanceof Window ? (Window)parent : null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        switch (e.getPropertyName()) {
            case "windowDecorationStyle": {
                this.uninstallClientDecorations();
                if (this.rootPane.getWindowDecorationStyle() != 0) {
                    this.installClientDecorations();
                    break;
                }
                this.installBorder();
                break;
            }
            case "JRootPane.useWindowDecorations": {
                FlatRootPaneUI.updateNativeWindowBorder(this.rootPane);
                break;
            }
            case "JRootPane.menuBarEmbedded": {
                if (this.titlePane == null) break;
                this.titlePane.menuBarChanged();
                this.rootPane.revalidate();
                this.rootPane.repaint();
                break;
            }
            case "JRootPane.titleBarShowIcon": {
                if (this.titlePane == null) break;
                this.titlePane.updateIcon();
                break;
            }
            case "JRootPane.titleBarShowTitle": 
            case "JRootPane.titleBarShowIconify": 
            case "JRootPane.titleBarShowMaximize": 
            case "JRootPane.titleBarShowClose": {
                if (this.titlePane == null) break;
                this.titlePane.updateVisibility();
                break;
            }
            case "JRootPane.titleBarBackground": 
            case "JRootPane.titleBarForeground": {
                if (this.titlePane == null) break;
                this.titlePane.titleBarColorsChanged();
                break;
            }
            case "JRootPane.titleBarHeight": {
                if (this.titlePane == null) break;
                this.titlePane.revalidate();
                break;
            }
            case "FlatLaf.fullWindowContent": {
                if (this.titlePane != null) {
                    this.rootPane.getLayeredPane().setLayer(this.titlePane, this.getLayerForTitlePane());
                    if (this.titlePane.windowTopBorderLayer != null) {
                        JLayeredPane layeredPane = this.rootPane.getLayeredPane();
                        if (this.titlePane.isWindowTopBorderNeeded() && FlatRootPaneUI.isFullWindowContent(this.rootPane)) {
                            layeredPane.add((Component)this.titlePane.windowTopBorderLayer, WINDOW_TOP_BORDER_LAYER);
                        } else {
                            layeredPane.remove(this.titlePane.windowTopBorderLayer);
                        }
                    }
                    this.titlePane.updateIcon();
                    this.titlePane.updateVisibility();
                    this.titlePane.updateFullWindowContentButtonsBoundsProperty();
                }
                FullWindowContentSupport.revalidatePlaceholders(this.rootPane);
                this.rootPane.revalidate();
                break;
            }
            case "FlatLaf.fullWindowContent.buttonsBounds": {
                FullWindowContentSupport.revalidatePlaceholders(this.rootPane);
                break;
            }
            case "JRootPane.glassPaneFullHeight": {
                this.rootPane.revalidate();
                break;
            }
            case "Window.style": {
                if (!this.rootPane.isDisplayable()) break;
                throw new IllegalComponentStateException("The client property 'Window.style' must be set before the window becomes displayable.");
            }
            case "ancestor": {
                if (e.getNewValue() instanceof Window) {
                    if (this.titlePane != null && !Objects.equals(this.titlePane.windowStyle, FlatTitlePane.getWindowStyle(this.rootPane))) {
                        this.setTitlePane(this.createTitlePane());
                    }
                    this.macSetWindowAppearance(this.rootPane);
                    this.macClearBackgroundForTranslucentWindow(this.rootPane);
                }
                this.macUninstallWindowBackgroundListener(this.rootPane);
                this.macInstallWindowBackgroundListener(this.rootPane);
                if (!(e.getNewValue() instanceof Window)) break;
                this.macInstallFullWindowContentSupport();
                break;
            }
            case "FlatLaf.macOS.windowButtonsSpacing": {
                this.macInstallFullWindowContentSupport();
                break;
            }
            case "apple.awt.fullWindowContent": {
                if (!SystemInfo.isMacFullWindowContentSupported) break;
                FullWindowContentSupport.macUpdateFullWindowContentButtonsBoundsProperty(this.rootPane);
            }
        }
    }

    protected static boolean isFullWindowContent(JRootPane rootPane) {
        return FlatClientProperties.clientPropertyBoolean(rootPane, "FlatLaf.fullWindowContent", false);
    }

    protected static boolean isMenuBarEmbedded(JRootPane rootPane) {
        FlatTitlePane titlePane = FlatRootPaneUI.getTitlePane(rootPane);
        return titlePane != null && titlePane.isMenuBarEmbedded();
    }

    protected static FlatTitlePane getTitlePane(JRootPane rootPane) {
        RootPaneUI ui = rootPane.getUI();
        return ui instanceof FlatRootPaneUI ? ((FlatRootPaneUI)ui).titlePane : null;
    }

    private static class FlatWindowTitleBorder
    extends BorderUIResource.EmptyBorderUIResource {
        private final Color borderColor;

        FlatWindowTitleBorder(Color borderColor) {
            super(0, 0, 0, 0);
            this.borderColor = borderColor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            if (this.showBorder(c)) {
                float lineHeight = UIScale.scale(1.0f);
                FlatUIUtils.paintFilledRectangle(g, this.borderColor, x, y, width, lineHeight);
            }
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(this.showBorder(c) ? 1 : 0, 0, 0, 0);
            return insets;
        }

        private boolean showBorder(Component c) {
            Container parent = c.getParent();
            return parent instanceof JFrame && (((JFrame)parent).getJMenuBar() == null || !((JFrame)parent).getJMenuBar().isVisible()) || parent instanceof JDialog && (((JDialog)parent).getJMenuBar() == null || !((JDialog)parent).getJMenuBar().isVisible());
        }
    }

    public static class FlatWindowBorder
    extends FlatEmptyBorder {
        protected final Color activeBorderColor = UIManager.getColor("RootPane.activeBorderColor");
        protected final Color inactiveBorderColor = UIManager.getColor("RootPane.inactiveBorderColor");
        protected final Color baseBorderColor = UIManager.getColor("Panel.background");

        public FlatWindowBorder() {
            super(1, 1, 1, 1);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            if (this.isWindowMaximized(c) || FlatUIUtils.isFullScreen(c)) {
                insets.right = 0;
                insets.bottom = 0;
                insets.left = 0;
                insets.top = 0;
                return insets;
            }
            return super.getBorderInsets(c, insets);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            if (this.isWindowMaximized(c) || FlatUIUtils.isFullScreen(c)) {
                return;
            }
            Container parent = c.getParent();
            boolean active = parent instanceof Window && ((Window)parent).isActive();
            g.setColor(FlatUIUtils.deriveColor(active ? this.activeBorderColor : this.inactiveBorderColor, this.baseBorderColor));
            HiDPIUtils.paintAtScale1x((Graphics2D)g, x, y, width, height, this::paintImpl);
        }

        private void paintImpl(Graphics2D g, int x, int y, int width, int height, double scaleFactor) {
            Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
            float lineWidth = (float)((double)UIScale.scale(1.0f) * scaleFactor);
            g.fill(FlatUIUtils.createRectangle(x, y, width, height, lineWidth));
            FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
        }

        protected boolean isWindowMaximized(Component c) {
            Container parent = c.getParent();
            return parent instanceof Frame && (((Frame)parent).getExtendedState() & 6) == 6;
        }
    }

    protected class FlatRootLayout
    implements LayoutManager2 {
        protected FlatRootLayout() {
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return this.computeLayoutSize(parent, c -> c.getPreferredSize());
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return this.computeLayoutSize(parent, c -> c.getMinimumSize());
        }

        @Override
        public Dimension maximumLayoutSize(Container parent) {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        private Dimension computeLayoutSize(Container parent, Function<Component, Dimension> getSizeFunc) {
            JMenuBar menuBar;
            JRootPane rootPane = (JRootPane)parent;
            Dimension contentSize = rootPane.getContentPane() != null ? getSizeFunc.apply(rootPane.getContentPane()) : rootPane.getSize();
            int width = contentSize.width;
            int height = contentSize.height;
            if (FlatRootPaneUI.this.titlePane != null && !FlatRootPaneUI.isFullWindowContent(rootPane)) {
                height += getSizeFunc.apply((Component)FlatRootPaneUI.this.titlePane).height;
            }
            if ((FlatRootPaneUI.this.titlePane == null || !FlatRootPaneUI.this.titlePane.isMenuBarEmbedded()) && (menuBar = rootPane.getJMenuBar()) != null && menuBar.isVisible()) {
                Dimension menuBarSize = getSizeFunc.apply(menuBar);
                width = Math.max(width, menuBarSize.width);
                height += menuBarSize.height;
            }
            Insets insets = rootPane.getInsets();
            return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
        }

        @Override
        public void layoutContainer(Container parent) {
            Container contentPane;
            JMenuBar menuBar;
            JRootPane rootPane = (JRootPane)parent;
            boolean isFullScreen = FlatUIUtils.isFullScreen(rootPane);
            Insets insets = rootPane.getInsets();
            int x = insets.left;
            int y = insets.top;
            int width = rootPane.getWidth() - insets.left - insets.right;
            int height = rootPane.getHeight() - insets.top - insets.bottom;
            if (rootPane.getLayeredPane() != null) {
                rootPane.getLayeredPane().setBounds(x, y, width, height);
            }
            int nextY = 0;
            if (FlatRootPaneUI.this.titlePane != null) {
                int prefHeight = !isFullScreen ? FlatRootPaneUI.this.titlePane.getPreferredSize().height : 0;
                boolean isFullWindowContent = FlatRootPaneUI.isFullWindowContent(rootPane);
                if (isFullWindowContent && !UIManager.getBoolean("FlatLaf.debug.titlebar.showRectangles")) {
                    int tw = Math.min(FlatRootPaneUI.this.titlePane.getPreferredSize().width, width);
                    int tx = FlatRootPaneUI.this.titlePane.getComponentOrientation().isLeftToRight() ? width - tw : 0;
                    FlatRootPaneUI.this.titlePane.setBounds(tx, 0, tw, prefHeight);
                } else {
                    FlatRootPaneUI.this.titlePane.setBounds(0, 0, width, prefHeight);
                }
                FlatRootPaneUI.this.titlePane.mouseLayer.setBounds(0, 0, width, prefHeight);
                if (FlatRootPaneUI.this.titlePane.windowTopBorderLayer != null) {
                    boolean show;
                    boolean bl = show = isFullWindowContent && !FlatRootPaneUI.this.titlePane.isWindowMaximized() && !isFullScreen;
                    if (show) {
                        FlatRootPaneUI.this.titlePane.windowTopBorderLayer.setBounds(0, 0, width, 1);
                    }
                    FlatRootPaneUI.this.titlePane.windowTopBorderLayer.setVisible(show);
                }
                if (!isFullWindowContent) {
                    nextY += prefHeight;
                }
            }
            if (rootPane.getGlassPane() != null) {
                boolean fullHeight = FlatClientProperties.clientPropertyBoolean(rootPane, "JRootPane.glassPaneFullHeight", false);
                int offset = fullHeight ? 0 : nextY;
                rootPane.getGlassPane().setBounds(x, y + offset, width, height - offset);
            }
            if ((menuBar = rootPane.getJMenuBar()) != null && menuBar.isVisible()) {
                boolean embedded;
                boolean bl = embedded = !isFullScreen && FlatRootPaneUI.this.titlePane != null && FlatRootPaneUI.this.titlePane.isMenuBarEmbedded();
                if (embedded) {
                    FlatRootPaneUI.this.titlePane.validate();
                    menuBar.setBounds(FlatRootPaneUI.this.titlePane.getMenuBarBounds());
                } else {
                    int mx = 0;
                    int mw = width;
                    if (FlatRootPaneUI.this.titlePane != null && FlatRootPaneUI.isFullWindowContent(rootPane)) {
                        int tw = Math.min(FlatRootPaneUI.this.titlePane.getPreferredSize().width, width);
                        mw -= tw;
                        if (!FlatRootPaneUI.this.titlePane.getComponentOrientation().isLeftToRight()) {
                            mx = tw;
                        }
                    }
                    Dimension prefSize = menuBar.getPreferredSize();
                    menuBar.setBounds(mx, nextY, mw, prefSize.height);
                    nextY += prefSize.height;
                }
            }
            if ((contentPane = rootPane.getContentPane()) != null) {
                contentPane.setBounds(0, nextY, width, Math.max(height - nextY, 0));
            }
            if (FlatRootPaneUI.this.titlePane != null) {
                FlatRootPaneUI.this.titlePane.menuBarLayouted();
            }
        }

        @Override
        public void invalidateLayout(Container parent) {
            if (FlatRootPaneUI.this.titlePane != null) {
                FlatRootPaneUI.this.titlePane.menuBarInvalidate();
            }
        }

        @Override
        public float getLayoutAlignmentX(Container target) {
            return 0.0f;
        }

        @Override
        public float getLayoutAlignmentY(Container target) {
            return 0.0f;
        }
    }
}

