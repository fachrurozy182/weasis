/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.FlatLaf
 *  com.formdev.flatlaf.ui.FlatProgressBarUI
 *  com.formdev.flatlaf.util.Animator
 *  com.formdev.flatlaf.util.Animator$TimingTarget
 *  com.formdev.flatlaf.util.ColorFunctions
 *  com.formdev.flatlaf.util.Graphics2DProxy
 *  com.formdev.flatlaf.util.UIScale
 */
package org.weasis.core.ui.tp.raven.spinner;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.ui.FlatProgressBarUI;
import com.formdev.flatlaf.util.Animator;
import com.formdev.flatlaf.util.ColorFunctions;
import com.formdev.flatlaf.util.Graphics2DProxy;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import org.weasis.core.ui.tp.raven.spinner.SpinnerProgress;
import org.weasis.core.ui.tp.raven.spinner.render.RingSpinner;
import org.weasis.core.ui.tp.raven.spinner.render.SpinnerRender;

public class SpinnerProgressUI
extends FlatProgressBarUI {
    protected SpinnerRender render;
    private PropertyChangeListener propertyChangeListener;
    private Animator animator;
    private float lastAnimator;
    private float animateFrame;
    private boolean moreAnimation;
    private final Rectangle iconRect = new Rectangle();
    private final Rectangle textRect = new Rectangle();
    private final Rectangle viewRect = new Rectangle();

    protected void installDefaults() {
        super.installDefaults();
        this.progressBar.setOpaque(false);
        this.render = new RingSpinner(4);
    }

    protected void installListeners() {
        super.installListeners();
        this.propertyChangeListener = evt -> {
            String name;
            if (this.render.isPaintComplete() && (name = evt.getPropertyName()).equals("indeterminate")) {
                this.checkIndeterminate(evt);
            }
        };
        this.progressBar.addPropertyChangeListener(this.propertyChangeListener);
    }

    private void checkIndeterminate(PropertyChangeEvent evt) {
        boolean oldValue = (Boolean)evt.getOldValue();
        boolean newValue = (Boolean)evt.getNewValue();
        if (oldValue && !newValue) {
            if (this.animator == null) {
                this.animator = new Animator(350, new Animator.TimingTarget(this){
                    final /* synthetic */ SpinnerProgressUI this$0;
                    {
                        SpinnerProgressUI spinnerProgressUI = this$0;
                        Objects.requireNonNull(spinnerProgressUI);
                        this.this$0 = spinnerProgressUI;
                    }

                    public void begin() {
                        this.this$0.moreAnimation = true;
                    }

                    public void end() {
                        this.this$0.moreAnimation = false;
                    }

                    public void timingEvent(float f) {
                        this.this$0.animateFrame = f;
                        this.this$0.progressBar.repaint();
                    }
                });
            } else if (this.animator.isRunning()) {
                this.animator.cancel();
            }
            this.moreAnimation = true;
            this.animateFrame = 0.0f;
            this.animator.start();
        }
    }

    protected void uninstallDefaults() {
        super.uninstallDefaults();
        this.render = null;
        if (this.animator != null && this.animator.isRunning()) {
            this.animator.cancel();
        }
        this.animator = null;
    }

    protected void uninstallListeners() {
        super.uninstallListeners();
        this.progressBar.removePropertyChangeListener(this.propertyChangeListener);
        this.propertyChangeListener = null;
    }

    public Dimension getPreferredSize(JComponent c) {
        SpinnerProgress spinner = (SpinnerProgress)c;
        String text = spinner.isStringPainted() ? spinner.getString() : null;
        Icon icon = spinner.getIcon();
        Insets insets = spinner.getInsets(null);
        Font font = spinner.getFont();
        int space = UIScale.scale((int)spinner.getSpace()) * 2;
        int dx = insets.left + insets.right;
        int dy = insets.top + insets.bottom;
        if (icon == null && (text == null || font == null)) {
            int add = UIScale.scale((int)20);
            return new Dimension(dx + add, dy + add);
        }
        if (text == null || icon != null && font == null) {
            int add = UIScale.scale((int)spinner.getSpace()) * 2;
            return new Dimension(icon.getIconWidth() + dx + add, icon.getIconHeight() + dy + add);
        }
        FontMetrics fm = spinner.getFontMetrics(font);
        Rectangle iconR = new Rectangle();
        Rectangle textR = new Rectangle();
        Rectangle viewR = new Rectangle();
        iconR.height = 0;
        iconR.width = 0;
        iconR.y = 0;
        iconR.x = 0;
        textR.height = 0;
        textR.width = 0;
        textR.y = 0;
        textR.x = 0;
        viewR.x = dx;
        viewR.y = dy;
        viewR.height = Short.MAX_VALUE;
        viewR.width = Short.MAX_VALUE;
        this.layoutCL(spinner, fm, text, icon, viewR, iconR, textR);
        int x1 = Math.min(iconR.x, textR.x);
        int x2 = Math.max(iconR.x + iconR.width, textR.x + textR.width);
        int y1 = Math.min(iconR.y, textR.y);
        int y2 = Math.max(iconR.y + iconR.height, textR.y + textR.height);
        int size = Math.max(x2 - x1, y2 - y1);
        Dimension rv = new Dimension(size, size);
        this.viewRect.x = insets.left;
        this.viewRect.y = insets.top;
        this.viewRect.width = this.viewRect.height = size;
        rv.width += dx + space;
        rv.height += dy + space;
        return rv;
    }

    public void paint(Graphics g, JComponent c) {
        JProgressBar jProgressBar = this.progressBar;
        if (jProgressBar instanceof SpinnerProgress) {
            SpinnerProgress spinner = (SpinnerProgress)jProgressBar;
            this.layout(spinner, g.getFontMetrics(), c.getWidth(), c.getHeight());
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle rect = this.getBox(this.viewRect);
            if (this.progressBar.isIndeterminate()) {
                float f = this.getAnimation();
                this.render.paintIndeterminate(g2, c, rect, f);
            } else {
                if (this.progressBar.isStringPainted()) {
                    Color color = c.getBackground();
                    color = FlatLaf.isLafDark() ? ColorFunctions.darken((Color)color, (float)0.3f) : ColorFunctions.lighten((Color)color, (float)0.3f);
                    g2.setColor(color);
                    g2.fillOval(rect.x, rect.y, rect.width, rect.height);
                }
                if (this.moreAnimation) {
                    this.render.paintCompleteIndeterminate(g2, c, rect, this.lastAnimator, this.animateFrame, (float)this.progressBar.getPercentComplete());
                } else {
                    this.render.paintDeterminate(g2, c, rect, (float)this.progressBar.getPercentComplete());
                }
            }
            if (spinner.getIcon() != null) {
                this.paintIcon(g, c, this.iconRect);
            }
            if (spinner.isStringPainted()) {
                this.paintString(g);
            }
        }
    }

    protected String layoutCL(SpinnerProgress spinner, FontMetrics fontMetrics, String text, Icon icon, Rectangle viewR, Rectangle iconR, Rectangle textR) {
        return SwingUtilities.layoutCompoundLabel(spinner, fontMetrics, text, icon, spinner.getVerticalAlignment(), spinner.getHorizontalAlignment(), spinner.getVerticalTextPosition(), spinner.getHorizontalTextPosition(), viewR, iconR, textR, UIScale.scale((int)spinner.getIconTextGap()));
    }

    private String layout(SpinnerProgress spinner, FontMetrics fm, int width, int height) {
        Insets insets = spinner.getInsets(null);
        String text = spinner.isStringPainted() ? this.progressBar.getString() : null;
        Icon icon = spinner.getIcon();
        Rectangle paintViewR = new Rectangle();
        paintViewR.x = insets.left;
        paintViewR.y = insets.top;
        paintViewR.width = width - (insets.left + insets.right);
        paintViewR.height = height - (insets.top + insets.bottom);
        this.iconRect.height = 0;
        this.iconRect.width = 0;
        this.iconRect.y = 0;
        this.iconRect.x = 0;
        this.textRect.height = 0;
        this.textRect.width = 0;
        this.textRect.y = 0;
        this.textRect.x = 0;
        return this.layoutCL(spinner, fm, text, icon, paintViewR, this.iconRect, this.textRect);
    }

    protected void paintString(Graphics g) {
        Graphics2DProxy g2 = new Graphics2DProxy(this, (Graphics2D)g){
            final /* synthetic */ SpinnerProgressUI this$0;
            {
                SpinnerProgressUI spinnerProgressUI = this$0;
                Objects.requireNonNull(spinnerProgressUI);
                this.this$0 = spinnerProgressUI;
                super(arg0);
            }

            public void setColor(Color c) {
                super.setColor(this.this$0.progressBar.getForeground());
            }
        };
        g2.setColor(this.getSelectionBackground());
        this.paintString((Graphics)g2, this.textRect.x, this.textRect.y, this.textRect.width, this.textRect.height, 0, null);
    }

    protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect) {
        SpinnerProgress spinner = (SpinnerProgress)this.progressBar;
        spinner.getIcon().paintIcon(c, g, iconRect.x, iconRect.y);
    }

    private float getAnimation() {
        float animate;
        int index = super.getAnimationIndex();
        this.lastAnimator = animate = (float)index / (float)this.getFrameCount() * 2.0f;
        return animate;
    }

    protected Rectangle getBox(Rectangle r) {
        if (r == null) {
            return null;
        }
        Insets insets = this.progressBar.getInsets();
        int width = this.progressBar.getWidth() - (insets.right + insets.left);
        int height = this.progressBar.getHeight() - (insets.top + insets.bottom);
        int size = Math.min(width, height);
        int x = insets.left + (width - size) / 2;
        int y = insets.top + (height - size) / 2;
        r.setBounds(x, y, size, size);
        return r;
    }
}

