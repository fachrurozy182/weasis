/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.extras.FlatSVGIcon
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.weasis.core.api.gui.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.BasicActionState;
import org.weasis.core.api.gui.util.ComboItemListener;
import org.weasis.core.api.gui.util.DropButtonIcon;
import org.weasis.core.api.gui.util.KeyActionValue;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.api.gui.util.SliderCineListener;
import org.weasis.core.api.gui.util.ToggleButtonListener;
import org.weasis.core.api.util.ResourceUtil;
import org.weasis.core.ui.editor.image.CrosshairListener;
import org.weasis.core.ui.editor.image.PannerListener;

public abstract class Feature<T>
implements KeyActionValue {
    private static final Logger LOGGER = LoggerFactory.getLogger(Feature.class);
    public static final String DRAW_CMD_PREFIX = "draw.sub.";
    private final String title;
    private final String command;
    private final FlatSVGIcon icon;
    private int keyCode;
    private int modifier;
    private final Cursor cursor;

    protected Feature(String title, String command, int keyEvent, int modifier, Cursor cursor) {
        this.title = title;
        this.command = command;
        this.keyCode = keyEvent;
        this.modifier = modifier;
        this.cursor = cursor;
        this.icon = ResourceUtil.getIcon("svg/action/" + command + ".svg");
    }

    public T getValue(Map<String, Object> map) {
        if (map != null) {
            try {
                return (T)map.get(this.command);
            }
            catch (ClassCastException e) {
                LOGGER.error("Cannot cast the value of \"{}\"", (Object)this, (Object)e);
            }
        }
        return null;
    }

    public String getTitle() {
        return this.title;
    }

    public String cmd() {
        return this.command;
    }

    public String toString() {
        return this.title;
    }

    public FlatSVGIcon getIcon() {
        return this.icon;
    }

    @Override
    public int getKeyCode() {
        return this.keyCode;
    }

    public Cursor getCursor() {
        return this.cursor;
    }

    @Override
    public int getModifier() {
        return this.modifier;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

    public boolean isDrawingAction() {
        return false;
    }

    public boolean isGraphicListAction() {
        return this.command.startsWith(DRAW_CMD_PREFIX);
    }

    public Icon getDropButtonIcon() {
        return new DropButtonIcon((Icon)this.icon);
    }

    public static Cursor getSvgCursor(String filename, String cursorName, float hotSpotX, float hotSpotY) {
        return Feature.getCursor("svg/cursor/" + filename, cursorName, hotSpotX, hotSpotY);
    }

    public static Cursor getImageCursor(String filename, String cursorName, float hotSpotX, float hotSpotY) {
        return Feature.getCursor("images/cursor/" + filename, cursorName, hotSpotX, hotSpotY);
    }

    private static Cursor getCursor(String path, String cursorName, float hotSpotX, float hotSpotY) {
        ImageIcon icon;
        Dimension bestCursorSize;
        if (GraphicsEnvironment.isHeadless()) {
            return Cursor.getDefaultCursor();
        }
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        if (path.startsWith("svg/")) {
            FlatSVGIcon svgIcon = ResourceUtil.getIcon(path);
            bestCursorSize = defaultToolkit.getBestCursorSize(22, 22);
            icon = svgIcon.derive(bestCursorSize.width, bestCursorSize.height);
        } else {
            icon = new ImageIcon(ResourceUtil.getResource(path).getAbsolutePath());
            bestCursorSize = defaultToolkit.getBestCursorSize(icon.getIconWidth(), icon.getIconHeight());
        }
        Point hotSpot = new Point(Math.round(hotSpotX * (float)bestCursorSize.width), Math.round(hotSpotY * (float)bestCursorSize.height));
        return defaultToolkit.createCustomCursor(icon.getImage(), hotSpot, cursorName);
    }

    public static final class CrosshairListenerValue
    extends Feature<CrosshairListener> {
        public CrosshairListenerValue(String title, String command, int keyEvent, int modifier, Cursor cursor) {
            super(title, command, keyEvent, modifier, cursor);
        }
    }

    public static final class PannerListenerValue
    extends Feature<PannerListener> {
        public PannerListenerValue(String title, String command, int keyEvent, int modifier, Cursor cursor) {
            super(title, command, keyEvent, modifier, cursor);
        }
    }

    public static class BasicActionStateValue
    extends Feature<BasicActionState> {
        public BasicActionStateValue(String title, String command, int keyEvent, int modifier, Cursor cursor) {
            super(title, command, keyEvent, modifier, cursor);
        }
    }

    public static final class ToggleButtonListenerValue
    extends Feature<ToggleButtonListener> {
        public ToggleButtonListenerValue(String title, String command, int keyEvent, int modifier, Cursor cursor) {
            super(title, command, keyEvent, modifier, cursor);
        }
    }

    public static final class ComboItemListenerValue<V>
    extends Feature<ComboItemListener<V>> {
        public ComboItemListenerValue(String title, String command, int keyEvent, int modifier, Cursor cursor) {
            super(title, command, keyEvent, modifier, cursor);
        }
    }

    public static final class SliderCineListenerValue
    extends Feature<SliderCineListener> {
        public SliderCineListenerValue(String title, String command, int keyEvent, int modifier, Cursor cursor) {
            super(title, command, keyEvent, modifier, cursor);
        }
    }

    public static final class SliderChangeListenerValue
    extends Feature<SliderChangeListener> {
        public SliderChangeListenerValue(String title, String command, int keyEvent, int modifier, Cursor cursor) {
            super(title, command, keyEvent, modifier, cursor);
        }
    }

    public static final class DoubleValue
    extends Feature<Double> {
        public DoubleValue(String title, String command, int keyEvent, int modifier, Cursor cursor) {
            super(title, command, keyEvent, modifier, cursor);
        }
    }

    public static final class IntegerValue
    extends Feature<Integer> {
        public IntegerValue(String title, String command, int keyEvent, int modifier, Cursor cursor) {
            super(title, command, keyEvent, modifier, cursor);
        }
    }

    public static final class BooleanValue
    extends Feature<Boolean> {
        public BooleanValue(String title, String command, int keyEvent, int modifier, Cursor cursor) {
            super(title, command, keyEvent, modifier, cursor);
        }
    }
}

