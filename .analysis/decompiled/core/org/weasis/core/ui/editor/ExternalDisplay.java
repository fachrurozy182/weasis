/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.ui.editor;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;

public sealed interface ExternalDisplay {
    public Rectangle screenBounds();

    public static ExternalDisplay onScreen(GraphicsDevice screen) {
        return new OnScreen(screen);
    }

    public static ExternalDisplay onOtherScreen(Rectangle currentWindowBounds) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        if (devices.length <= 1) {
            return null;
        }
        for (GraphicsDevice device : devices) {
            Rectangle bounds = device.getDefaultConfiguration().getBounds();
            if (bounds.contains(currentWindowBounds)) continue;
            return new OnScreen(device);
        }
        return null;
    }

    public static ExternalDisplay atBounds(Rectangle bounds) {
        return new AtBounds(bounds);
    }

    public record OnScreen(GraphicsDevice screen) implements ExternalDisplay
    {
        public OnScreen {
            if (screen == null) {
                throw new IllegalArgumentException("screen must not be null");
            }
        }

        @Override
        public Rectangle screenBounds() {
            GraphicsConfiguration config = this.screen.getDefaultConfiguration();
            Rectangle bounds = config.getBounds();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= insets.left + insets.right;
            bounds.height -= insets.top + insets.bottom;
            return bounds;
        }

        public String toString() {
            return "ExternalDisplay.OnScreen[" + this.screen.getIDstring() + "]";
        }
    }

    public record AtBounds(Rectangle bounds) implements ExternalDisplay
    {
        public AtBounds {
            if (bounds == null) {
                throw new IllegalArgumentException("bounds must not be null");
            }
        }

        @Override
        public Rectangle screenBounds() {
            return new Rectangle(this.bounds);
        }

        public String toString() {
            return "ExternalDisplay.AtBounds[" + String.valueOf(this.bounds) + "]";
        }
    }
}

