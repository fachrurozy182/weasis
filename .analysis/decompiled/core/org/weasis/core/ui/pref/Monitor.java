/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.pref;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.JFrame;
import org.weasis.core.ui.editor.image.dockable.MeasureTool;
import org.weasis.core.ui.pref.ScreenPrefView;

public class Monitor {
    private final GraphicsDevice graphicsDevice;
    private double realScaleFactor = 0.0;
    private Rectangle fullscreenBounds;

    public Monitor(GraphicsDevice graphicsDevice) {
        this.graphicsDevice = graphicsDevice;
    }

    public synchronized double getRealScaleFactor() {
        return this.realScaleFactor;
    }

    public synchronized void setRealScaleFactor(double realScaleFactor) {
        this.realScaleFactor = realScaleFactor;
    }

    public String getMonitorID() {
        return this.graphicsDevice.getIDstring();
    }

    public Rectangle getBounds() {
        return this.graphicsDevice.getDefaultConfiguration().getBounds();
    }

    public GraphicsConfiguration getGraphicsConfiguration() {
        return this.graphicsDevice.getDefaultConfiguration();
    }

    public GraphicsDevice getGraphicsDevice() {
        return this.graphicsDevice;
    }

    public Rectangle getFullscreenBounds() {
        if (this.fullscreenBounds == null) {
            JFrame frame = new JFrame(this.getGraphicsConfiguration());
            Rectangle bound = this.getBounds();
            frame.setBounds(bound.x, bound.y, bound.width - 150, bound.height - 150);
            frame.setVisible(true);
            frame.setExtendedState(frame.getExtendedState() | 6);
            try {
                Thread.sleep(500L);
            }
            catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
            }
            this.fullscreenBounds = frame.getBounds();
            frame.dispose();
        }
        return this.fullscreenBounds;
    }

    public static Monitor getMonitor(GraphicsConfiguration gconfig) {
        if (gconfig != null) {
            List<Monitor> monitors = MeasureTool.viewSetting.getMonitors();
            for (Monitor monitor : monitors) {
                if (!gconfig.equals(monitor.getGraphicsConfiguration())) continue;
                return monitor;
            }
        }
        return null;
    }

    public static Monitor getDefaultMonitor() {
        int defIndex = ScreenPrefView.getDefaultMonitor();
        List<Monitor> monitors = MeasureTool.viewSetting.getMonitors();
        if (monitors.isEmpty()) {
            return null;
        }
        if (defIndex < 0 || defIndex >= monitors.size()) {
            defIndex = 0;
        }
        return monitors.get(defIndex);
    }
}

