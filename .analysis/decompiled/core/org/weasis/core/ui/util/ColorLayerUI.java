/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.util;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.Timer;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.gui.util.WinUtil;

public class ColorLayerUI {
    protected static final float MAX_ALPHA = 0.75f;
    private static final int ANIMATION_DURATION = 300;
    private static final int FRAME_RATE = 60;
    private static final int FRAME_DELAY = 16;
    private final RootPaneContainer parent;
    private final AtomicReference<Float> alpha;
    private TransparentGlassPane glassPane;
    private Component originalGlassPane;
    private Timer animationTimer;

    public ColorLayerUI(RootPaneContainer parent) {
        if (parent == null) {
            throw new IllegalArgumentException();
        }
        this.parent = parent;
        this.alpha = new AtomicReference<Float>(Float.valueOf(0.0f));
    }

    public static ColorLayerUI createTransparentLayerUI(RootPaneContainer parent) {
        if (parent != null && parent.getRootPane() != null) {
            ColorLayerUI ui = new ColorLayerUI(parent);
            ui.installGlassPane();
            ui.showUI();
            return ui;
        }
        return null;
    }

    public static ColorLayerUI createTransparentLayerUI(Component parent) {
        if (parent != null) {
            return ColorLayerUI.createTransparentLayerUI(WinUtil.getRootPaneContainer(parent));
        }
        return null;
    }

    public static void showCenterScreen(Window window, ColorLayerUI layer) {
        Container container = ColorLayerUI.getContentPane(layer);
        GuiUtils.showCenterScreen(window, container);
        if (layer != null) {
            layer.hideUI();
        }
    }

    private void installGlassPane() {
        JRootPane rootPane = this.parent.getRootPane();
        if (rootPane != null) {
            this.originalGlassPane = rootPane.getGlassPane();
            this.glassPane = new TransparentGlassPane();
            rootPane.setGlassPane(this.glassPane);
        }
    }

    private void uninstallGlassPane() {
        JRootPane rootPane;
        if (this.glassPane != null) {
            this.glassPane.setVisible(false);
        }
        if ((rootPane = this.parent.getRootPane()) != null && this.originalGlassPane != null) {
            rootPane.setGlassPane(this.originalGlassPane);
            this.glassPane = null;
            this.originalGlassPane = null;
        }
    }

    private float easeOutCubic(float t) {
        return 1.0f - (float)Math.pow(1.0f - t, 3.0);
    }

    public synchronized void showUI() {
        if (this.glassPane == null) {
            return;
        }
        if (this.animationTimer != null && this.animationTimer.isRunning()) {
            this.animationTimer.stop();
        }
        this.glassPane.setVisible(true);
        long startTime = System.currentTimeMillis();
        this.animationTimer = new Timer(16, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1.0f, (float)elapsed / 300.0f);
            float newAlpha = this.easeOutCubic(progress);
            this.alpha.set(Float.valueOf(newAlpha));
            if (this.glassPane != null) {
                this.glassPane.setAlpha(newAlpha);
            }
            if (progress >= 1.0f) {
                this.animationTimer.stop();
                this.animationTimer = null;
            }
        });
        this.animationTimer.setRepeats(true);
        this.animationTimer.start();
    }

    public synchronized void hideUI() {
        if (this.glassPane == null) {
            return;
        }
        if (this.animationTimer != null && this.animationTimer.isRunning()) {
            this.animationTimer.stop();
        }
        float startAlpha = this.alpha.get().floatValue();
        long startTime = System.currentTimeMillis();
        this.animationTimer = new Timer(16, actionEvent -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1.0f, (float)elapsed / 300.0f);
            float easedProgress = this.easeOutCubic(progress);
            float newAlpha = startAlpha * (1.0f - easedProgress);
            this.alpha.set(Float.valueOf(newAlpha));
            if (this.glassPane != null) {
                this.glassPane.setAlpha(newAlpha);
            }
            if (progress >= 1.0f) {
                this.animationTimer.stop();
                this.animationTimer = null;
                this.uninstallGlassPane();
            }
        });
        this.animationTimer.setRepeats(true);
        this.animationTimer.start();
    }

    public static Container getContentPane(ColorLayerUI layer) {
        if (layer != null && layer.parent != null) {
            return layer.parent.getRootPane();
        }
        return null;
    }

    private static class TransparentGlassPane
    extends JComponent {
        private float currentAlpha = 0.0f;

        public TransparentGlassPane() {
            this.setOpaque(false);
        }

        public void setAlpha(float alpha) {
            if (Math.abs(this.currentAlpha - alpha) > 0.001f) {
                this.currentAlpha = alpha;
                this.repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (this.currentAlpha > 0.0f) {
                Graphics2D g2d = (Graphics2D)g.create();
                try {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setComposite(AlphaComposite.SrcOver.derive(this.currentAlpha * 0.75f));
                    g2d.setColor(this.getBackground());
                    g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
                }
                finally {
                    g2d.dispose();
                }
            }
        }
    }
}

