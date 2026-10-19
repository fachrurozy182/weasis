/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.extras;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.MigLayoutVisualPadding;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.UIResource;
import javax.swing.text.JTextComponent;

public class FlatInspector {
    private static final Integer HIGHLIGHT_LAYER = JLayeredPane.POPUP_LAYER - 1;
    private static final int KEY_MODIFIERS_MASK = 960;
    private final JRootPane rootPane;
    private final MouseMotionListener mouseMotionListener;
    private final AWTEventListener keyListener;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final WindowListener windowListener;
    private Window window;
    private boolean enabled;
    private Object oldGlassPaneFullHeight;
    private Component lastComponent;
    private int lastX;
    private int lastY;
    private int inspectParentLevel;
    private boolean wasModifierKeyPressed;
    private boolean showClassHierarchy;
    private long lastWhen;
    private JComponent highlightFigure;
    private Popup popup;

    public static void install(String activationKeys) {
        if (activationKeys == null) {
            activationKeys = "ctrl shift alt X";
        }
        KeyStroke keyStroke = KeyStroke.getKeyStroke(activationKeys);
        Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            if (e.getID() == 402 && ((KeyEvent)e).getKeyCode() == keyStroke.getKeyCode() && (((KeyEvent)e).getModifiersEx() & 0x3C0) == (keyStroke.getModifiers() & 0x3C0)) {
                Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
                RootPaneContainer rootPaneContainer = null;
                if (activeWindow instanceof RootPaneContainer) {
                    rootPaneContainer = (RootPaneContainer)((Object)activeWindow);
                } else {
                    for (Component child : activeWindow.getComponents()) {
                        if (!(child instanceof RootPaneContainer)) continue;
                        rootPaneContainer = (RootPaneContainer)((Object)child);
                        break;
                    }
                }
                if (rootPaneContainer != null) {
                    JRootPane rootPane = rootPaneContainer.getRootPane();
                    FlatInspector inspector = (FlatInspector)rootPane.getClientProperty(FlatInspector.class);
                    if (inspector == null) {
                        inspector = new FlatInspector(rootPane);
                        rootPane.putClientProperty(FlatInspector.class, inspector);
                        inspector.setEnabled(true);
                    } else {
                        inspector.setEnabled(false);
                        rootPane.putClientProperty(FlatInspector.class, null);
                    }
                }
            }
        }, 8L);
    }

    public FlatInspector(JRootPane rootPane) {
        this.rootPane = rootPane;
        this.mouseMotionListener = new MouseMotionAdapter(){

            @Override
            public void mouseMoved(MouseEvent e) {
                FlatInspector.this.lastX = e.getX();
                FlatInspector.this.lastY = e.getY();
                FlatInspector.this.inspect(FlatInspector.this.lastX, FlatInspector.this.lastY);
            }
        };
        this.keyListener = e -> {
            KeyEvent keyEvent = (KeyEvent)e;
            int keyCode = keyEvent.getKeyCode();
            int id = e.getID();
            if (id == 401) {
                if (keyCode == 17 || keyCode == 16 || keyCode == 18) {
                    this.wasModifierKeyPressed = true;
                }
            } else if (id == 402 && this.wasModifierKeyPressed) {
                int parentLevel;
                if (keyEvent.getWhen() - this.lastWhen <= 5L) {
                    return;
                }
                this.lastWhen = keyEvent.getWhen();
                if (keyCode == 17) {
                    ++this.inspectParentLevel;
                    parentLevel = this.inspect(this.lastX, this.lastY);
                    if (this.inspectParentLevel > parentLevel) {
                        this.inspectParentLevel = parentLevel;
                    }
                } else if (keyCode == 16 && this.inspectParentLevel > 0) {
                    --this.inspectParentLevel;
                    parentLevel = this.inspect(this.lastX, this.lastY);
                    if (this.inspectParentLevel > parentLevel) {
                        this.inspectParentLevel = Math.max(parentLevel - 1, 0);
                        this.inspect(this.lastX, this.lastY);
                    }
                } else if (keyCode == 18 && this.lastComponent != null) {
                    this.showClassHierarchy = !this.showClassHierarchy;
                    this.showToolTip(this.lastComponent, this.lastX, this.lastY, this.inspectParentLevel);
                }
            }
            if (keyCode == 27) {
                keyEvent.consume();
                if (id == 401) {
                    this.setEnabled(false);
                    FlatInspector inspector = (FlatInspector)rootPane.getClientProperty(FlatInspector.class);
                    if (inspector == this) {
                        rootPane.putClientProperty(FlatInspector.class, null);
                    }
                }
            }
        };
        this.windowListener = new WindowAdapter(){

            @Override
            public void windowActivated(WindowEvent e) {
                FlatInspector.this.update();
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                FlatInspector.this.hidePopup();
            }
        };
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        this.propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        this.propertyChangeSupport.removePropertyChangeListener(l);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            this.oldGlassPaneFullHeight = this.rootPane.getClientProperty("JRootPane.glassPaneFullHeight");
            this.rootPane.putClientProperty("JRootPane.glassPaneFullHeight", true);
            this.rootPane.validate();
        } else {
            this.rootPane.putClientProperty("JRootPane.glassPaneFullHeight", this.oldGlassPaneFullHeight);
        }
        ((JComponent)this.rootPane.getGlassPane()).setOpaque(false);
        this.rootPane.getGlassPane().setVisible(enabled);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if (enabled) {
            toolkit.addAWTEventListener(this.keyListener, 8L);
        } else {
            toolkit.removeAWTEventListener(this.keyListener);
        }
        if (enabled) {
            this.rootPane.getGlassPane().addMouseMotionListener(this.mouseMotionListener);
        } else {
            this.rootPane.getGlassPane().removeMouseMotionListener(this.mouseMotionListener);
        }
        if (enabled) {
            this.window = SwingUtilities.windowForComponent(this.rootPane);
            if (this.window != null) {
                this.window.addWindowListener(this.windowListener);
            }
        } else if (this.window != null) {
            this.window.removeWindowListener(this.windowListener);
            this.window = null;
        }
        if (enabled) {
            Point pt = new Point(MouseInfo.getPointerInfo().getLocation());
            SwingUtilities.convertPointFromScreen(pt, this.rootPane);
            this.lastX = pt.x;
            this.lastY = pt.y;
            this.inspect(this.lastX, this.lastY);
        } else {
            this.lastComponent = null;
            this.inspectParentLevel = 0;
            if (this.highlightFigure != null) {
                this.highlightFigure.getParent().remove(this.highlightFigure);
            }
            this.highlightFigure = null;
            this.hidePopup();
        }
        this.propertyChangeSupport.firePropertyChange("enabled", !enabled, enabled);
    }

    private void hidePopup() {
        if (this.popup != null) {
            this.popup.hide();
            this.popup = null;
        }
    }

    public void update() {
        if (!this.rootPane.getGlassPane().isVisible()) {
            return;
        }
        EventQueue.invokeLater(() -> {
            this.setEnabled(false);
            this.setEnabled(true);
            this.inspect(this.lastX, this.lastY);
        });
    }

    private int inspect(int x, int y) {
        Container parent;
        Point pt = SwingUtilities.convertPoint(this.rootPane.getGlassPane(), x, y, this.rootPane);
        Component c = this.getDeepestComponentAt(this.rootPane, pt.x, pt.y);
        int parentLevel = 0;
        for (int i = 0; i < this.inspectParentLevel && c != null && (parent = c.getParent()) != null; ++i) {
            c = parent;
            ++parentLevel;
        }
        if (c == this.lastComponent) {
            return parentLevel;
        }
        this.lastComponent = c;
        this.highlight(c);
        this.showToolTip(c, x, y, parentLevel);
        return parentLevel;
    }

    private Component getDeepestComponentAt(Component parent, int x, int y) {
        if (!parent.contains(x, y)) {
            return null;
        }
        if (parent instanceof Container) {
            for (Component child : ((Container)parent).getComponents()) {
                Component c;
                if (child == null || !child.isVisible()) continue;
                int cx = x - child.getX();
                int cy = y - child.getY();
                Component component = c = child instanceof Container ? this.getDeepestComponentAt(child, cx, cy) : child.getComponentAt(cx, cy);
                if (c == null || !c.isVisible() || c == this.highlightFigure || c.getParent() instanceof JRootPane && c == ((JRootPane)c.getParent()).getGlassPane() || "com.formdev.flatlaf.ui.FlatWindowResizer".equals(c.getClass().getName())) continue;
                return c;
            }
        }
        return parent;
    }

    private void highlight(Component c) {
        if (this.highlightFigure == null) {
            this.highlightFigure = this.createHighlightFigure();
            this.rootPane.getLayeredPane().add((Component)this.highlightFigure, HIGHLIGHT_LAYER);
        }
        this.highlightFigure.setVisible(c != null);
        if (c != null) {
            Insets insets = this.rootPane.getInsets();
            this.highlightFigure.setBounds(new Rectangle(SwingUtilities.convertPoint(c, -insets.left, -insets.top, this.rootPane), c.getSize()));
        }
    }

    private JComponent createHighlightFigure() {
        JComponent c = new JComponent(){

            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(this.getBackground());
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
            }

            @Override
            protected void paintBorder(Graphics g) {
                Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
                super.paintBorder(g);
                FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
            }
        };
        c.setBackground(new Color(255, 0, 0, 32));
        c.setBorder(new LineBorder(Color.red));
        return c;
    }

    private void showToolTip(Component c, int x, int y, int parentLevel) {
        this.hidePopup();
        if (c == null || this.window != null && !this.window.isActive()) {
            return;
        }
        JToolTip tip = new JToolTip();
        tip.setTipText(FlatInspector.buildToolTipText(c, parentLevel, this.showClassHierarchy));
        tip.putClientProperty("Popup.forceHeavyWeight", true);
        Point pt = new Point(x, y);
        SwingUtilities.convertPointToScreen(pt, this.rootPane.getGlassPane());
        int tx = pt.x + UIScale.scale(8);
        int ty = pt.y + UIScale.scale(16);
        Dimension size = tip.getPreferredSize();
        GraphicsConfiguration gc = this.rootPane.getGraphicsConfiguration();
        if (gc != null) {
            Rectangle visibleRect = gc.getBounds();
            if (tx + size.width > visibleRect.x + visibleRect.width) {
                tx -= size.width + UIScale.scale(16);
            }
            if (ty + size.height > visibleRect.y + visibleRect.height) {
                ty -= size.height + UIScale.scale(32);
            }
            if (tx < visibleRect.x) {
                tx = visibleRect.x;
            }
            if (ty < visibleRect.y) {
                ty = visibleRect.y;
            }
        }
        PopupFactory popupFactory = PopupFactory.getSharedInstance();
        this.popup = popupFactory.getPopup(c, tip, tx, ty);
        this.popup.show();
    }

    private static String buildToolTipText(Component c, int parentLevel, boolean classHierarchy) {
        Object style;
        LayoutManager layout;
        StringBuilder buf = new StringBuilder(1500);
        buf.append("<html><style>");
        buf.append("td { padding: 0 10 0 0; }");
        buf.append("</style><table>");
        FlatInspector.appendRow(buf, "Class", FlatInspector.toString(c.getClass(), classHierarchy));
        FlatInspector.appendRow(buf, "Size", c.getWidth() + ", " + c.getHeight() + "&nbsp;&nbsp; @ " + c.getX() + ", " + c.getY());
        if (c instanceof Container) {
            FlatInspector.appendRow(buf, "Insets", FlatInspector.toString(((Container)c).getInsets()));
        }
        Insets margin = null;
        if (c instanceof AbstractButton) {
            margin = ((AbstractButton)c).getMargin();
        } else if (c instanceof JTextComponent) {
            margin = ((JTextComponent)c).getMargin();
        } else if (c instanceof JMenuBar) {
            margin = ((JMenuBar)c).getMargin();
        } else if (c instanceof JToolBar) {
            margin = ((JToolBar)c).getMargin();
        }
        if (margin != null) {
            FlatInspector.appendRow(buf, "Margin", FlatInspector.toString(margin));
        }
        if (c instanceof JComponent) {
            Insets visualPadding;
            Object value = ((JComponent)c).getClientProperty(MigLayoutVisualPadding.VISUAL_PADDING_PROPERTY);
            Insets insets = value instanceof int[] ? new Insets(((int[])value)[0], ((int[])value)[1], ((int[])value)[2], ((int[])value)[3]) : (visualPadding = value instanceof Insets ? (Insets)value : null);
            if (visualPadding != null) {
                FlatInspector.appendRow(buf, "Mig visual padding", FlatInspector.toString(visualPadding));
            }
        }
        Dimension prefSize = c.getPreferredSize();
        Dimension minSize = c.getMinimumSize();
        Dimension maxSize = c.getMaximumSize();
        FlatInspector.appendRow(buf, "Pref size", prefSize.width + ", " + prefSize.height);
        FlatInspector.appendRow(buf, "Min size", minSize.width + ", " + minSize.height);
        FlatInspector.appendRow(buf, "Max size", maxSize.width + ", " + maxSize.height);
        if (c instanceof JComponent) {
            FlatInspector.appendRow(buf, "Border", FlatInspector.toString(((JComponent)c).getBorder(), classHierarchy));
        }
        FlatInspector.appendRow(buf, "Background", FlatInspector.toString(c.getBackground()) + (c.isBackgroundSet() ? "" : "  NOT SET"));
        FlatInspector.appendRow(buf, "Foreground", FlatInspector.toString(c.getForeground()) + (c.isForegroundSet() ? "" : "  NOT SET"));
        FlatInspector.appendRow(buf, "Font", FlatInspector.toString(c.getFont()) + (c.isFontSet() ? "" : "  NOT SET"));
        if (c instanceof JComponent) {
            try {
                Object ui;
                if (SystemInfo.isJava_9_orLater) {
                    Method m = JComponent.class.getMethod("getUI", new Class[0]);
                    ui = m.invoke(c, new Object[0]);
                } else {
                    Field f = JComponent.class.getDeclaredField("ui");
                    f.setAccessible(true);
                    ui = f.get(c);
                }
                FlatInspector.appendRow(buf, "UI", ui != null ? FlatInspector.toString(ui.getClass(), classHierarchy) : "null");
            }
            catch (Exception ui) {
                // empty catch block
            }
        }
        if (c instanceof Container && (layout = ((Container)c).getLayout()) != null) {
            FlatInspector.appendRow(buf, "Layout", FlatInspector.toString(layout.getClass(), classHierarchy));
        }
        FlatInspector.appendRow(buf, "Enabled", String.valueOf(c.isEnabled()));
        FlatInspector.appendRow(buf, "Opaque", String.valueOf(c.isOpaque()) + (c instanceof JComponent && FlatUIUtils.hasOpaqueBeenExplicitlySet((JComponent)c) ? " EXPLICIT" : ""));
        if (c instanceof AbstractButton) {
            FlatInspector.appendRow(buf, "ContentAreaFilled", String.valueOf(((AbstractButton)c).isContentAreaFilled()));
        }
        FlatInspector.appendRow(buf, "Focusable", String.valueOf(c.isFocusable()));
        FlatInspector.appendRow(buf, "Left-to-right", String.valueOf(c.getComponentOrientation().isLeftToRight()));
        FlatInspector.appendRow(buf, "Parent", c.getParent() != null ? FlatInspector.toString(c.getParent().getClass(), classHierarchy) : "null");
        if (c instanceof JComponent && (style = ((JComponent)c).getClientProperty("FlatLaf.style")) != null) {
            FlatInspector.appendRow(buf, "FlatLaf Style", style.toString());
        }
        buf.append("<tr><td colspan=\"2\">");
        if (parentLevel > 0) {
            buf.append("<br>Parent level: " + parentLevel);
        }
        buf.append("<br>(").append(parentLevel > 0 ? "press <b>Ctrl/Shift</b> to increase/decrease level" : "press <b>Ctrl</b> key to inspect parent").append("; &nbsp;").append(classHierarchy ? "press <b>Alt</b> key to hide class hierarchy" : "press <b>Alt</b> key to show class hierarchy").append(')');
        buf.append("</td></tr>");
        buf.append("</table></html>");
        return buf.toString();
    }

    private static void appendRow(StringBuilder buf, String key, String value) {
        buf.append("<tr><td valign=\"top\">").append(key).append(":</td><td>").append(value).append("</td></tr>");
    }

    private static String toString(Class<?> cls, boolean classHierarchy) {
        StringBuilder buf = new StringBuilder(100);
        int level = 0;
        while (cls != null) {
            if (level > 0) {
                if (cls == Object.class) break;
                buf.append("<br>&nbsp;");
                for (int i = 1; i < level; ++i) {
                    buf.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                }
                buf.append("\u2570 ");
            }
            ++level;
            String name = cls.getName();
            int dot = name.lastIndexOf(46);
            String pkg = dot >= 0 ? name.substring(0, dot) : "-";
            String simpleName = dot >= 0 ? name.substring(dot + 1) : name;
            buf.append(simpleName).append(' ').append(FlatInspector.toDimmedText("(" + pkg + ")"));
            if (UIResource.class.isAssignableFrom(cls)) {
                buf.append(" UI");
            }
            if (!classHierarchy) break;
            cls = cls.getSuperclass();
        }
        return buf.toString();
    }

    private static String toString(Insets insets) {
        if (insets == null) {
            return "null";
        }
        return insets.top + ", " + insets.left + ", " + insets.bottom + ", " + insets.right + (insets instanceof UIResource ? " UI" : "");
    }

    private static String toString(Color c) {
        if (c == null) {
            return "null";
        }
        StringBuilder buf = new StringBuilder(150);
        buf.append("<tt>");
        buf.append(c.getAlpha() != 255 ? String.format("#%06x%02x", c.getRGB() & 0xFFFFFF, c.getRGB() >> 24 & 0xFF) : String.format("#%06x", c.getRGB() & 0xFFFFFF));
        buf.append("</tt>");
        if (c instanceof UIResource) {
            buf.append(" UI");
        }
        buf.append("&nbsp; &nbsp;").append("<span style=\"background: ").append(String.format("#%06x", c.getRGB() & 0xFFFFFF)).append(";\">").append("&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;").append("</span>");
        if (c.getAlpha() != 255) {
            buf.append(" ").append(Math.round((float)c.getAlpha() / 2.55f)).append('%');
        }
        return buf.toString();
    }

    private static String toString(Font f) {
        if (f == null) {
            return "null";
        }
        return f.getFamily() + " " + f.getSize() + " " + f.getStyle() + (f instanceof UIResource ? " UI" : "");
    }

    private static String toString(Border b, boolean classHierarchy) {
        if (b == null) {
            return "null";
        }
        String s = FlatInspector.toString(b.getClass(), classHierarchy);
        if (b instanceof EmptyBorder) {
            String borderInsets = " (" + FlatInspector.toString(((EmptyBorder)b).getBorderInsets()) + ')';
            int brIndex = s.indexOf("<br>");
            s = brIndex >= 0 ? s.substring(0, brIndex) + borderInsets + s.substring(brIndex) : s + borderInsets;
        }
        return s;
    }

    private static String toDimmedText(String text) {
        Color color = UIManager.getColor("Label.disabledForeground");
        if (color == null) {
            color = UIManager.getColor("Label.disabledText");
        }
        if (color == null) {
            color = Color.GRAY;
        }
        return String.format("<span color=\"#%06x\">%s</span>", color.getRGB() & 0xFFFFFF, text);
    }
}

