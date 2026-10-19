/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.ui.FlatLineBorder;
import com.formdev.flatlaf.ui.FlatStylingSupport;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.ui.FullWindowContentSupport;
import com.formdev.flatlaf.util.HiDPIUtils;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPanelUI;

public class FlatPanelUI
extends BasicPanelUI
implements FlatStylingSupport.StyleableUI,
PropertyChangeListener {
    @FlatStylingSupport.Styleable
    protected int arc = -1;
    private final boolean shared;
    private Map<String, Object> oldStyleValues;

    public static ComponentUI createUI(JComponent c) {
        return FlatUIUtils.canUseSharedUI(c) ? FlatUIUtils.createSharedUI(FlatPanelUI.class, () -> new FlatPanelUI(true)) : new FlatPanelUI(false);
    }

    protected FlatPanelUI(boolean shared) {
        this.shared = shared;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        c.addPropertyChangeListener(this);
        if (c.getClientProperty("FlatLaf.fullWindowContent.buttonsPlaceholder") != null) {
            FullWindowContentSupport.registerPlaceholder(c);
        }
        this.installStyle((JPanel)c);
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        c.removePropertyChangeListener(this);
        if (c.getClientProperty("FlatLaf.fullWindowContent.buttonsPlaceholder") != null) {
            FullWindowContentSupport.unregisterPlaceholder(c);
        }
        this.oldStyleValues = null;
    }

    @Override
    protected void installDefaults(JPanel p) {
        super.installDefaults(p);
        if (p.getClientProperty("FlatLaf.fullWindowContent.buttonsPlaceholder") != null) {
            LookAndFeel.installProperty(p, "opaque", false);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        switch (e.getPropertyName()) {
            case "FlatLaf.style": 
            case "FlatLaf.styleClass": {
                JPanel c = (JPanel)e.getSource();
                if (this.shared && FlatStylingSupport.hasStyleProperty(c)) {
                    c.updateUI();
                } else {
                    this.installStyle(c);
                }
                c.revalidate();
                HiDPIUtils.repaint(c);
                break;
            }
            case "FlatLaf.fullWindowContent.buttonsPlaceholder": {
                JPanel p = (JPanel)e.getSource();
                if (e.getOldValue() != null) {
                    FullWindowContentSupport.unregisterPlaceholder(p);
                }
                if (e.getNewValue() != null) {
                    FullWindowContentSupport.registerPlaceholder(p);
                }
                LookAndFeel.installProperty(p, "opaque", e.getNewValue() == null);
            }
        }
    }

    protected void installStyle(JPanel c) {
        try {
            this.applyStyle(c, FlatStylingSupport.getResolvedStyle(c, "Panel"));
        }
        catch (RuntimeException ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    protected void applyStyle(JPanel c, Object style) {
        this.oldStyleValues = FlatStylingSupport.parseAndApply(this.oldStyleValues, style, (key, value) -> this.applyStyleProperty(c, (String)key, value));
    }

    protected Object applyStyleProperty(JPanel c, String key, Object value) {
        return FlatStylingSupport.applyToAnnotatedObjectOrComponent(this, c, key, value);
    }

    @Override
    public Map<String, Class<?>> getStyleableInfos(JComponent c) {
        return FlatStylingSupport.getAnnotatedStyleableInfos(this);
    }

    @Override
    public Object getStyleableValue(JComponent c, String key) {
        return FlatStylingSupport.getAnnotatedStyleableValue(this, key);
    }

    @Override
    public void update(Graphics g, JComponent c) {
        FlatPanelUI.fillRoundedBackground(g, c, this.arc);
        this.paint(g, c);
    }

    public static void fillRoundedBackground(Graphics g, JComponent c, int arc) {
        if (arc < 0) {
            Border border = c.getBorder();
            int n = arc = border instanceof FlatLineBorder ? ((FlatLineBorder)border).getArc() : 0;
        }
        if (c.isOpaque()) {
            if (arc > 0) {
                FlatUIUtils.paintParentBackground(g, c);
            } else {
                g.setColor(c.getBackground());
                g.fillRect(0, 0, c.getWidth(), c.getHeight());
            }
        }
        if (arc > 0) {
            g.setColor(c.getBackground());
            Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
            FlatUIUtils.paintComponentBackground((Graphics2D)g, 0, 0, c.getWidth(), c.getHeight(), 0.0f, UIScale.scale(arc));
            FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
        }
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Object value = c.getClientProperty("FlatLaf.fullWindowContent.buttonsPlaceholder");
        if (value != null) {
            return FullWindowContentSupport.getPlaceholderPreferredSize(c, (String)value);
        }
        return super.getPreferredSize(c);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (c.getClientProperty("FlatLaf.fullWindowContent.buttonsPlaceholder") != null) {
            FullWindowContentSupport.debugPaint(g, c);
        }
    }
}

