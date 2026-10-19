/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.extras;

import com.formdev.flatlaf.FlatSystemProperties;
import com.formdev.flatlaf.util.Animator;
import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.VolatileImage;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;

public class FlatAnimatedLafChange {
    public static int duration = 160;
    public static int resolution = 16;
    private static Animator animator;
    private static final Map<JLayeredPane, SnapshotLayer> snapshots;
    private static float alpha;

    public static void showSnapshot() {
        if (!FlatSystemProperties.getBoolean("flatlaf.animatedLafChange", true)) {
            return;
        }
        if (animator != null) {
            animator.stop();
        }
        alpha = 1.0f;
        FlatAnimatedLafChange.showSnapshot(true);
    }

    private static void showSnapshot(boolean old) {
        Window[] windows;
        for (Window window : windows = Window.getWindows()) {
            SnapshotLayer snapshotLayer;
            JLayeredPane layeredPane;
            VolatileImage snapshotImage;
            if (!(window instanceof RootPaneContainer) || !window.isShowing() || (snapshotImage = (layeredPane = ((RootPaneContainer)((Object)window)).getLayeredPane()).createVolatileImage(layeredPane.getWidth(), layeredPane.getHeight())) == null) continue;
            layeredPane.paint(snapshotImage.getGraphics());
            if (old) {
                snapshotLayer = new SnapshotLayer();
                snapshotLayer.setOpaque(true);
                snapshotLayer.setSize(layeredPane.getSize());
                snapshotLayer.oldSnapshotImage = snapshotImage;
                snapshots.put(layeredPane, snapshotLayer);
                continue;
            }
            snapshotLayer = snapshots.get(layeredPane);
            if (snapshotLayer == null) {
                snapshotImage.flush();
                continue;
            }
            snapshotLayer.newSnapshotImage = snapshotImage;
            layeredPane.add((Component)snapshotLayer, (Object)(JLayeredPane.DRAG_LAYER + 1));
            layeredPane.getRootPane().putClientProperty("FlatLaf.internal.animatedLafChange", true);
        }
    }

    public static void hideSnapshotWithAnimation() {
        if (!FlatSystemProperties.getBoolean("flatlaf.animatedLafChange", true)) {
            return;
        }
        if (snapshots.isEmpty()) {
            return;
        }
        FlatAnimatedLafChange.showSnapshot(false);
        animator = new Animator(duration, fraction -> {
            alpha = 1.0f - fraction;
            for (Map.Entry<JLayeredPane, SnapshotLayer> e : snapshots.entrySet()) {
                if (!e.getKey().isShowing()) continue;
                SnapshotLayer snapshotLayer = e.getValue();
                snapshotLayer.paintImmediately(0, 0, snapshotLayer.getWidth(), snapshotLayer.getHeight());
            }
            Toolkit.getDefaultToolkit().sync();
        }, () -> {
            FlatAnimatedLafChange.hideSnapshot();
            animator = null;
        });
        animator.setResolution(resolution);
        animator.start();
    }

    private static void hideSnapshot() {
        for (Map.Entry<JLayeredPane, SnapshotLayer> e : snapshots.entrySet()) {
            JLayeredPane layeredPane = e.getKey();
            SnapshotLayer snapshotLayer = e.getValue();
            layeredPane.remove(snapshotLayer);
            layeredPane.repaint();
            snapshotLayer.flushSnapshotImages();
            JRootPane rootPane = layeredPane.getRootPane();
            rootPane.putClientProperty("FlatLaf.internal.animatedLafChange", null);
            Runnable r = (Runnable)rootPane.getClientProperty("FlatLaf.internal.animatedLafChange.runWhenFinished");
            if (r == null) continue;
            rootPane.putClientProperty("FlatLaf.internal.animatedLafChange.runWhenFinished", null);
            r.run();
        }
        snapshots.clear();
    }

    public static void stop() {
        if (animator != null) {
            animator.stop();
        } else {
            FlatAnimatedLafChange.hideSnapshot();
        }
    }

    static {
        snapshots = new WeakHashMap<JLayeredPane, SnapshotLayer>();
    }

    private static class SnapshotLayer
    extends JComponent {
        VolatileImage oldSnapshotImage;
        VolatileImage newSnapshotImage;

        private SnapshotLayer() {
        }

        @Override
        public void paint(Graphics g) {
            if (this.oldSnapshotImage.contentsLost() || this.newSnapshotImage == null || this.newSnapshotImage.contentsLost()) {
                return;
            }
            g.drawImage(this.newSnapshotImage, 0, 0, null);
            ((Graphics2D)g).setComposite(AlphaComposite.getInstance(3, alpha));
            g.drawImage(this.oldSnapshotImage, 0, 0, null);
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            this.flushSnapshotImages();
        }

        void flushSnapshotImages() {
            this.oldSnapshotImage.flush();
            if (this.newSnapshotImage != null) {
                this.newSnapshotImage.flush();
            }
        }
    }
}

