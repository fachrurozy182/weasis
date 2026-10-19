/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.formdev.flatlaf.util.SystemInfo
 *  org.osgi.service.prefs.Preferences
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.EscapeChars
 */
package org.weasis.core.api.gui.util;

import com.formdev.flatlaf.util.SystemInfo;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.swing.KeyStroke;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.Messages;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.gui.util.Feature;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.util.EscapeChars;

public final class ShortcutManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShortcutManager.class);
    public static final String PREFERENCE_NODE = "keyboard.shortcuts";
    public static final String PROPERTY_SHORTCUTS_CHANGED = "shortcuts.changed";
    public static final String KEY_CODE_SUFFIX = ".keyCode";
    public static final String MODIFIER_SUFFIX = ".modifier";
    private static final ShortcutManager INSTANCE = new ShortcutManager();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Map<String, ShortcutEntry> shortcuts = new LinkedHashMap<String, ShortcutEntry>();
    public static final String CATEGORY_MOUSE_ACTIONS = Messages.getString("ShortcutManager.cat.mouse_actions");
    public static final String CATEGORY_VIEWER = Messages.getString("ShortcutManager.cat.viewer");
    public static final String CATEGORY_NAVIGATION = Messages.getString("ShortcutManager.cat.navigation");
    public static final String CATEGORY_MEASURES = Messages.getString("ShortcutManager.cat.measures");
    public static final String CATEGORY_DRAWINGS = Messages.getString("ShortcutManager.cat.drawings");
    public static final String CATEGORY_DISPLAY = Messages.getString("ShortcutManager.cat.display");
    public static final String CATEGORY_DICOM_NAV = Messages.getString("ShortcutManager.cat.dicom_nav");
    public static final String CATEGORY_MPR = Messages.getString("ShortcutManager.cat.mpr");
    public static final String CATEGORY_PAN = Messages.getString("ShortcutManager.cat.pan");
    public static final String CATEGORY_DOCKING = Messages.getString("ShortcutManager.cat.docking");
    public static final String CATEGORY_DICOM_EXPLORER = Messages.getString("ShortcutManager.cat.dicom_explorer");
    public static final String CATEGORY_OTHER = Messages.getString("ShortcutManager.cat.other");
    public static final String ID_VIEWER_PRINT = "viewer.print";
    public static final String ID_VIEWER_ESCAPE = "viewer.escape";
    public static final String ID_VIEWER_ZOOM_OUT = "viewer.zoomOut";
    public static final String ID_VIEWER_ZOOM_IN = "viewer.zoomIn";
    public static final String ID_VIEWER_BEST_FIT = "viewer.bestFit";
    public static final String ID_VIEWER_ROTATE_LEFT = "viewer.rotateLeft";
    public static final String ID_VIEWER_ROTATE_RIGHT = "viewer.rotateRight";
    public static final String ID_VIEWER_FLIP_HORIZONTAL = "viewer.flipHorizontal";
    public static final String ID_VIEWER_SCROLL_UP = "viewer.scrollUp";
    public static final String ID_VIEWER_SCROLL_DOWN = "viewer.scrollDown";
    public static final String ID_VIEWER_SCROLL_UP_FAST = "viewer.scrollUpFast";
    public static final String ID_VIEWER_SCROLL_DOWN_FAST = "viewer.scrollDownFast";
    public static final String ID_VIEWER_SCROLL_FIRST = "viewer.scrollFirst";
    public static final String ID_VIEWER_SCROLL_LAST = "viewer.scrollLast";
    public static final String ID_VIEWER_NEXT_MOUSE_ACTION = "viewer.nextMouseAction";
    public static final String ID_VIEWER_NEXT_VIEW = "viewer.nextView";
    public static final String ID_VIEWER_PREV_VIEW = "viewer.prevView";
    public static final String ID_VIEWER_TOGGLE_INFO = "viewer.toggleInfo";
    public static final String ID_VIEWER_TOGGLE_INFO_ALT = "viewer.toggleInfoAlt";
    public static final String ID_VIEWER_FULLSCREEN = "viewer.fullscreen";
    public static final String ID_GRAPHIC_LINE = "graphic.line";
    public static final String ID_GRAPHIC_ANGLE = "graphic.angle";
    public static final String ID_GRAPHIC_POLYGON = "graphic.polygon";
    public static final String ID_GRAPHIC_ANNOTATION = "graphic.annotation";
    public static final String ID_DRAW_DELETE = "draw.delete";
    public static final String ID_DRAW_DESELECT_ALL = "draw.deselectAll";
    public static final String ID_DRAW_SELECT_ALL = "draw.selectAll";
    public static final String ID_PAN_LEFT = "pan.left";
    public static final String ID_PAN_RIGHT = "pan.right";
    public static final String ID_PAN_UP = "pan.up";
    public static final String ID_PAN_DOWN = "pan.down";
    public static final String ID_PAN_LEFT_FAST = "pan.leftFast";
    public static final String ID_PAN_RIGHT_FAST = "pan.rightFast";
    public static final String ID_PAN_UP_FAST = "pan.upFast";
    public static final String ID_PAN_DOWN_FAST = "pan.downFast";
    public static final String ID_DICOM_PREV_SERIES = "dicom.prevSeries";
    public static final String ID_DICOM_NEXT_SERIES = "dicom.nextSeries";
    public static final String ID_DICOM_PREV_STUDY = "dicom.prevStudy";
    public static final String ID_DICOM_NEXT_STUDY = "dicom.nextStudy";
    public static final String ID_DICOM_PREV_PATIENT = "dicom.prevPatient";
    public static final String ID_DICOM_NEXT_PATIENT = "dicom.nextPatient";
    public static final String ID_DICOM_FIRST_SERIES = "dicom.firstSeries";
    public static final String ID_DICOM_LAST_SERIES = "dicom.lastSeries";
    public static final String ID_DICOM_FIRST_STUDY = "dicom.firstStudy";
    public static final String ID_DICOM_LAST_STUDY = "dicom.lastStudy";
    public static final String ID_DICOM_FIRST_PATIENT = "dicom.firstPatient";
    public static final String ID_DICOM_LAST_PATIENT = "dicom.lastPatient";
    public static final String ID_DICOM_TOGGLE_SEG = "dicom.toggleSegmentation";
    public static final String ID_DOCKING_MAXIMIZE = "docking.maximize";
    public static final String ID_DOCKING_EXTERNALIZE = "docking.externalize";
    public static final String ID_DOCKING_NORMALIZE = "docking.normalize";
    public static final String ID_DOCKING_CLOSE = "docking.close";
    public static final String ID_DOCKING_PANEL_LIST = "docking.panelList";
    public static final String ID_DOCKING_NEXT_TAB = "docking.nextTab";
    public static final String ID_DOCKING_PREV_TAB = "docking.prevTab";
    public static final String ID_MPR_RECENTER = "mpr.recenter";
    public static final String ID_MPR_RECENTER_ALL = "mpr.recenterAll";
    public static final String ID_MPR_TOGGLE_CENTER = "mpr.toggleCenter";
    public static final String ID_MPR_TOGGLE_CENTER_ALL = "mpr.toggleCenterAll";
    public static final String ID_MPR_TOGGLE_CROSS_LINES = "mpr.toggleCrossLines";
    public static final String ID_MPR_TOGGLE_CROSS_LINES_ALL = "mpr.toggleCrossLinesAll";
    public static final String ID_MPR_CYCLE_MIP = "mpr.cycleMip";
    public static final String ID_EXPLORER_OPEN = "explorer.open";
    public static final String ID_EXPLORER_SELECT_NEXT = "explorer.selectNext";
    public static final String ID_EXPLORER_SELECT_PREVIOUS = "explorer.selectPrevious";
    public static final String ID_EXPLORER_SELECT_FIRST = "explorer.selectFirst";
    public static final String ID_EXPLORER_SELECT_FIRST_ALT = "explorer.selectFirstAlt";
    public static final String ID_EXPLORER_SELECT_LAST = "explorer.selectLast";
    public static final String ID_EXPLORER_SELECT_LAST_ALT = "explorer.selectLastAlt";
    public static final String ID_EXPLORER_SELECT_ALL = "explorer.selectAll";

    private ShortcutManager() {
    }

    public static ShortcutManager getInstance() {
        return INSTANCE;
    }

    public static String getPreferenceNodeName() {
        String os = SystemInfo.isMacOS ? "macos" : (SystemInfo.isWindows ? "windows" : "linux");
        return "keyboard.shortcuts." + os;
    }

    public void registerDefaults() {
        this.shortcuts.clear();
        this.registerActionWFeatures();
        this.registerViewerShortcuts();
    }

    private void registerActionWFeatures() {
        for (Field field : ActionW.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !Modifier.isFinal(field.getModifiers()) || !Feature.class.isAssignableFrom(field.getType())) continue;
            try {
                Feature feature = (Feature)field.get(null);
                if (feature == null || feature.getKeyCode() == 0) continue;
                String category = this.determineFeatureCategory(feature);
                this.register(feature.cmd(), feature.getTitle().isEmpty() ? field.getName() : feature.getTitle(), category, feature.getKeyCode(), feature.getModifier());
            }
            catch (IllegalAccessException e) {
                LOGGER.warn("Cannot access ActionW field: {}", (Object)field.getName(), (Object)e);
            }
        }
    }

    private String determineFeatureCategory(Feature<?> feature) {
        if (feature.isDrawingAction()) {
            return CATEGORY_MEASURES;
        }
        if (feature.isGraphicListAction()) {
            return CATEGORY_DRAWINGS;
        }
        return CATEGORY_MOUSE_ACTIONS;
    }

    private void registerViewerShortcuts() {
        this.register(ActionW.CINESTART.cmd(), Messages.getString("ShortcutManager.cine_start_stop"), CATEGORY_MOUSE_ACTIONS, 67, 0);
        this.register(ID_VIEWER_PRINT, Messages.getString("ShortcutManager.print_views"), CATEGORY_VIEWER, 80, 0);
        this.register(ID_VIEWER_ESCAPE, Messages.getString("ShortcutManager.reset_view"), CATEGORY_VIEWER, 27, 0);
        this.register(ID_VIEWER_ZOOM_OUT, Messages.getString("ShortcutManager.zoom_out"), CATEGORY_VIEWER, 109, 2);
        this.register(ID_VIEWER_ZOOM_IN, Messages.getString("ShortcutManager.zoom_in"), CATEGORY_VIEWER, 107, 2);
        this.register(ID_VIEWER_BEST_FIT, Messages.getString("ShortcutManager.best_fit"), CATEGORY_VIEWER, 10, 2);
        this.register(ID_VIEWER_SCROLL_UP, Messages.getString("ShortcutManager.prev_image"), CATEGORY_NAVIGATION, 38, 0);
        this.register(ID_VIEWER_SCROLL_DOWN, Messages.getString("ShortcutManager.next_image"), CATEGORY_NAVIGATION, 40, 0);
        this.register(ID_VIEWER_SCROLL_UP_FAST, Messages.getString("ShortcutManager.back_10"), CATEGORY_NAVIGATION, 38, 1);
        this.register(ID_VIEWER_SCROLL_DOWN_FAST, Messages.getString("ShortcutManager.fwd_10"), CATEGORY_NAVIGATION, 40, 1);
        this.register(ID_VIEWER_SCROLL_FIRST, Messages.getString("ShortcutManager.first_image"), CATEGORY_NAVIGATION, 36, 0);
        this.register(ID_VIEWER_SCROLL_LAST, Messages.getString("ShortcutManager.last_image"), CATEGORY_NAVIGATION, 35, 0);
        this.register(ID_VIEWER_NEXT_VIEW, Messages.getString("ShortcutManager.next_view"), CATEGORY_NAVIGATION, 9, 0);
        this.register(ID_VIEWER_PREV_VIEW, Messages.getString("ShortcutManager.prev_view"), CATEGORY_NAVIGATION, 9, 1);
        this.register(ID_VIEWER_NEXT_MOUSE_ACTION, Messages.getString("ShortcutManager.next_mouse_action"), CATEGORY_MOUSE_ACTIONS, 32, 2);
        this.register(ID_VIEWER_TOGGLE_INFO, Messages.getString("ShortcutManager.toggle_info"), CATEGORY_DISPLAY, 32, 0);
        this.register(ID_VIEWER_TOGGLE_INFO_ALT, Messages.getString("ShortcutManager.toggle_info"), CATEGORY_DISPLAY, 73, 0);
        this.register(ID_VIEWER_FULLSCREEN, Messages.getString("ShortcutManager.fullscreen"), CATEGORY_DISPLAY, 122, 0);
        this.register(ID_VIEWER_ROTATE_LEFT, Messages.getString("ShortcutManager.rotate_left"), CATEGORY_VIEWER, 76, 8);
        this.register(ID_VIEWER_ROTATE_RIGHT, Messages.getString("ShortcutManager.rotate_right"), CATEGORY_VIEWER, 82, 8);
        this.register(ID_VIEWER_FLIP_HORIZONTAL, Messages.getString("ShortcutManager.flip_horiz"), CATEGORY_VIEWER, 70, 8);
        this.register(ID_GRAPHIC_LINE, Messages.getString("ShortcutManager.dist_measure"), CATEGORY_MEASURES, 68, 0);
        this.register(ID_GRAPHIC_ANGLE, Messages.getString("ShortcutManager.angle_measure"), CATEGORY_MEASURES, 65, 0);
        this.register(ID_GRAPHIC_POLYGON, Messages.getString("ShortcutManager.polyline_measure"), CATEGORY_MEASURES, 89, 0);
        this.register(ID_GRAPHIC_ANNOTATION, Messages.getString("ShortcutManager.textbox"), CATEGORY_DRAWINGS, 66, 0);
        this.register(ID_DRAW_DELETE, Messages.getString("ShortcutManager.delete_graphics"), CATEGORY_DRAWINGS, 127, 0);
        this.register(ID_DRAW_DESELECT_ALL, Messages.getString("ShortcutManager.deselect_all"), CATEGORY_DRAWINGS, 68, 2);
        this.register(ID_DRAW_SELECT_ALL, Messages.getString("ShortcutManager.select_all_graphics"), CATEGORY_DRAWINGS, 65, 2);
        this.register(ID_PAN_LEFT, Messages.getString("ShortcutManager.pan_left_5"), CATEGORY_PAN, 37, 8);
        this.register(ID_PAN_RIGHT, Messages.getString("ShortcutManager.pan_right_5"), CATEGORY_PAN, 39, 8);
        this.register(ID_PAN_UP, Messages.getString("ShortcutManager.pan_up_5"), CATEGORY_PAN, 38, 8);
        this.register(ID_PAN_DOWN, Messages.getString("ShortcutManager.pan_down_5"), CATEGORY_PAN, 40, 8);
        this.register(ID_PAN_LEFT_FAST, Messages.getString("ShortcutManager.pan_left_10"), CATEGORY_PAN, 37, 9);
        this.register(ID_PAN_RIGHT_FAST, Messages.getString("ShortcutManager.pan_right_10"), CATEGORY_PAN, 39, 9);
        this.register(ID_PAN_UP_FAST, Messages.getString("ShortcutManager.pan_up_10"), CATEGORY_PAN, 38, 9);
        this.register(ID_PAN_DOWN_FAST, Messages.getString("ShortcutManager.pan_down_10"), CATEGORY_PAN, 40, 9);
        this.register(ID_DICOM_PREV_SERIES, Messages.getString("ShortcutManager.prev_series"), CATEGORY_DICOM_NAV, ShortcutContext.DICOM_VIEWER, 37, 0);
        this.register(ID_DICOM_NEXT_SERIES, Messages.getString("ShortcutManager.next_series"), CATEGORY_DICOM_NAV, ShortcutContext.DICOM_VIEWER, 39, 0);
        this.register(ID_DICOM_PREV_STUDY, Messages.getString("ShortcutManager.prev_study"), CATEGORY_DICOM_NAV, ShortcutContext.DICOM_VIEWER, 37, 2);
        this.register(ID_DICOM_NEXT_STUDY, Messages.getString("ShortcutManager.next_study"), CATEGORY_DICOM_NAV, ShortcutContext.DICOM_VIEWER, 39, 2);
        this.register(ID_DICOM_PREV_PATIENT, Messages.getString("ShortcutManager.prev_patient"), CATEGORY_DICOM_NAV, ShortcutContext.DICOM_VIEWER, 38, 2);
        this.register(ID_DICOM_NEXT_PATIENT, Messages.getString("ShortcutManager.next_patient"), CATEGORY_DICOM_NAV, ShortcutContext.DICOM_VIEWER, 40, 2);
        this.register(ID_DICOM_FIRST_SERIES, Messages.getString("ShortcutManager.first_series"), CATEGORY_DICOM_NAV, ShortcutContext.DICOM_VIEWER, 33, 0);
        this.register(ID_DICOM_LAST_SERIES, Messages.getString("ShortcutManager.last_series"), CATEGORY_DICOM_NAV, ShortcutContext.DICOM_VIEWER, 34, 0);
        this.register(ID_DICOM_FIRST_STUDY, Messages.getString("ShortcutManager.first_study"), CATEGORY_DICOM_NAV, ShortcutContext.DICOM_VIEWER, 33, 2);
        this.register(ID_DICOM_LAST_STUDY, Messages.getString("ShortcutManager.last_study"), CATEGORY_DICOM_NAV, ShortcutContext.DICOM_VIEWER, 34, 2);
        this.register(ID_DICOM_FIRST_PATIENT, Messages.getString("ShortcutManager.first_patient"), CATEGORY_DICOM_NAV, ShortcutContext.DICOM_VIEWER, 36, 2);
        this.register(ID_DICOM_LAST_PATIENT, Messages.getString("ShortcutManager.last_patient"), CATEGORY_DICOM_NAV, ShortcutContext.DICOM_VIEWER, 35, 2);
        this.register(ID_DICOM_TOGGLE_SEG, Messages.getString("ShortcutManager.toggle_seg"), CATEGORY_DISPLAY, ShortcutContext.DICOM_VIEWER, 83, 8);
        this.register(ID_MPR_RECENTER, Messages.getString("ShortcutManager.mpr_recenter"), CATEGORY_MPR, ShortcutContext.MPR, 88, 8);
        this.register(ID_MPR_RECENTER_ALL, Messages.getString("ShortcutManager.mpr_recenter_all"), CATEGORY_MPR, ShortcutContext.MPR, 88, 10);
        this.register(ID_MPR_TOGGLE_CENTER, Messages.getString("ShortcutManager.mpr_toggle_center"), CATEGORY_MPR, ShortcutContext.MPR, 67, 8);
        this.register(ID_MPR_TOGGLE_CENTER_ALL, Messages.getString("ShortcutManager.mpr_toggle_center_all"), CATEGORY_MPR, ShortcutContext.MPR, 67, 10);
        this.register(ID_MPR_TOGGLE_CROSS_LINES, Messages.getString("ShortcutManager.mpr_toggle_cross"), CATEGORY_MPR, ShortcutContext.MPR, 86, 8);
        this.register(ID_MPR_TOGGLE_CROSS_LINES_ALL, Messages.getString("ShortcutManager.mpr_toggle_cross_all"), CATEGORY_MPR, ShortcutContext.MPR, 86, 10);
        this.register(ID_MPR_CYCLE_MIP, Messages.getString("ShortcutManager.mpr_cycle_mip"), CATEGORY_MPR, ShortcutContext.MPR, 66, 10);
        this.register(ID_DOCKING_MAXIMIZE, Messages.getString("ShortcutManager.dock_maximize"), CATEGORY_DOCKING, 77, 2);
        this.register(ID_DOCKING_EXTERNALIZE, Messages.getString("ShortcutManager.dock_externalize"), CATEGORY_DOCKING, 69, 2);
        this.register(ID_DOCKING_NORMALIZE, Messages.getString("ShortcutManager.dock_normalize"), CATEGORY_DOCKING, 78, 2);
        this.register(ID_DOCKING_CLOSE, Messages.getString("ShortcutManager.dock_close"), CATEGORY_DOCKING, 87, 2);
        this.register(ID_DOCKING_PANEL_LIST, Messages.getString("ShortcutManager.dock_panel_list"), CATEGORY_DOCKING, 69, 3);
        this.register(ID_DOCKING_NEXT_TAB, Messages.getString("ShortcutManager.dock_next_tab"), CATEGORY_DOCKING, 9, 2);
        this.register(ID_DOCKING_PREV_TAB, Messages.getString("ShortcutManager.dock_prev_tab"), CATEGORY_DOCKING, 9, 3);
        this.register(ID_EXPLORER_OPEN, Messages.getString("ShortcutManager.explorer_open"), CATEGORY_DICOM_EXPLORER, ShortcutContext.DICOM_EXPLORER, 10, 0);
        this.register(ID_EXPLORER_SELECT_NEXT, Messages.getString("ShortcutManager.explorer_next"), CATEGORY_DICOM_EXPLORER, ShortcutContext.DICOM_EXPLORER, 40, 0);
        this.register(ID_EXPLORER_SELECT_PREVIOUS, Messages.getString("ShortcutManager.explorer_prev"), CATEGORY_DICOM_EXPLORER, ShortcutContext.DICOM_EXPLORER, 38, 0);
        this.register(ID_EXPLORER_SELECT_FIRST, Messages.getString("ShortcutManager.explorer_first"), CATEGORY_DICOM_EXPLORER, ShortcutContext.DICOM_EXPLORER, 36, 0);
        this.register(ID_EXPLORER_SELECT_FIRST_ALT, Messages.getString("ShortcutManager.explorer_first"), CATEGORY_DICOM_EXPLORER, ShortcutContext.DICOM_EXPLORER, 33, 0);
        this.register(ID_EXPLORER_SELECT_LAST, Messages.getString("ShortcutManager.explorer_last"), CATEGORY_DICOM_EXPLORER, ShortcutContext.DICOM_EXPLORER, 35, 0);
        this.register(ID_EXPLORER_SELECT_LAST_ALT, Messages.getString("ShortcutManager.explorer_last"), CATEGORY_DICOM_EXPLORER, ShortcutContext.DICOM_EXPLORER, 34, 0);
        this.register(ID_EXPLORER_SELECT_ALL, Messages.getString("ShortcutManager.explorer_select_all"), CATEGORY_DICOM_EXPLORER, ShortcutContext.DICOM_EXPLORER, 65, 2);
    }

    public void register(String id, String description, String category, ShortcutContext context, int defaultKeyCode, int defaultModifier) {
        this.shortcuts.put(id, new ShortcutEntry(id, description, category, context, defaultKeyCode, defaultModifier));
    }

    public void register(String id, String description, String category, int defaultKeyCode, int defaultModifier) {
        this.register(id, description, category, ShortcutContext.VIEW_CANVAS, defaultKeyCode, defaultModifier);
    }

    public boolean matches(String id, int keyCode, int modifier) {
        ShortcutEntry entry = this.shortcuts.get(id);
        if (entry == null || entry.getKeyCode() == 0) {
            return false;
        }
        return entry.getKeyCode() == keyCode && entry.getModifier() == modifier;
    }

    public boolean matches(String id, KeyEvent e) {
        return this.matches(id, e.getKeyCode(), e.getModifiers());
    }

    public ShortcutEntry getEntry(String id) {
        return this.shortcuts.get(id);
    }

    public int getKeyCode(String id) {
        ShortcutEntry entry = this.shortcuts.get(id);
        return entry != null ? entry.getKeyCode() : 0;
    }

    public int getModifier(String id) {
        ShortcutEntry entry = this.shortcuts.get(id);
        return entry != null ? entry.getModifier() : 0;
    }

    public Map<String, ShortcutEntry> getShortcuts() {
        return Collections.unmodifiableMap(this.shortcuts);
    }

    public List<ShortcutEntry> getShortcutList() {
        ArrayList<ShortcutEntry> list = new ArrayList<ShortcutEntry>(this.shortcuts.values());
        list.sort(Comparator.comparing(ShortcutEntry::getCategory).thenComparing(e -> e.getContext().getDisplayName()).thenComparing(ShortcutEntry::getDescription));
        return list;
    }

    public boolean setShortcut(String id, int keyCode, int modifier) {
        ShortcutEntry entry = this.shortcuts.get(id);
        if (entry == null) {
            return false;
        }
        int oldKeyCode = entry.getKeyCode();
        int oldModifier = entry.getModifier();
        entry.setKeyCode(keyCode);
        entry.setModifier(modifier);
        if (oldKeyCode != keyCode || oldModifier != modifier) {
            this.pcs.firePropertyChange(PROPERTY_SHORTCUTS_CHANGED, null, entry);
        }
        return true;
    }

    public void resetAllToDefaults() {
        for (ShortcutEntry entry : this.shortcuts.values()) {
            entry.resetToDefault();
        }
        this.pcs.firePropertyChange(PROPERTY_SHORTCUTS_CHANGED, null, null);
    }

    public List<ShortcutEntry> findConflicts(String excludeId, int keyCode, int modifier) {
        if (keyCode == 0) {
            return Collections.emptyList();
        }
        ShortcutEntry source = this.shortcuts.get(excludeId);
        ShortcutContext sourceContext = source != null ? source.getContext() : ShortcutContext.VIEW_CANVAS;
        return this.findConflicts(excludeId, keyCode, modifier, sourceContext);
    }

    public List<ShortcutEntry> findConflicts(String excludeId, int keyCode, int modifier, ShortcutContext context) {
        if (keyCode == 0) {
            return Collections.emptyList();
        }
        ArrayList<ShortcutEntry> conflicts = new ArrayList<ShortcutEntry>();
        for (ShortcutEntry entry : this.shortcuts.values()) {
            if (entry.getId().equals(excludeId) || entry.getKeyCode() != keyCode || entry.getModifier() != modifier || !context.overlapsWith(entry.getContext())) continue;
            conflicts.add(entry);
        }
        return conflicts;
    }

    public void applyToFeatures() {
        for (Field field : ActionW.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !Modifier.isFinal(field.getModifiers()) || !Feature.class.isAssignableFrom(field.getType())) continue;
            try {
                ShortcutEntry entry;
                Feature feature = (Feature)field.get(null);
                if (feature == null || (entry = this.shortcuts.get(feature.cmd())) == null) continue;
                feature.setKeyCode(entry.getKeyCode());
                feature.setModifier(entry.getModifier());
            }
            catch (IllegalAccessException e) {
                LOGGER.warn("Cannot access ActionW field: {}", (Object)field.getName(), (Object)e);
            }
        }
        this.pcs.firePropertyChange(PROPERTY_SHORTCUTS_CHANGED, null, null);
    }

    public void loadPreferences(Preferences prefs) {
        if (prefs == null) {
            return;
        }
        Preferences node = prefs.node(ShortcutManager.getPreferenceNodeName());
        for (ShortcutEntry entry : this.shortcuts.values()) {
            int kc = node.getInt(entry.getId() + KEY_CODE_SUFFIX, -1);
            int mod = node.getInt(entry.getId() + MODIFIER_SUFFIX, -1);
            if (kc >= 0) {
                entry.setKeyCode(kc);
            }
            if (mod < 0) continue;
            entry.setModifier(mod);
        }
    }

    public void savePreferences(Preferences prefs) {
        if (prefs == null) {
            return;
        }
        Preferences node = prefs.node(ShortcutManager.getPreferenceNodeName());
        for (ShortcutEntry entry : this.shortcuts.values()) {
            if (entry.isModified()) {
                BundlePreferences.putIntPreferences(node, entry.getId() + KEY_CODE_SUFFIX, entry.getKeyCode());
                BundlePreferences.putIntPreferences(node, entry.getId() + MODIFIER_SUFFIX, entry.getModifier());
                continue;
            }
            node.remove(entry.getId() + KEY_CODE_SUFFIX);
            node.remove(entry.getId() + MODIFIER_SUFFIX);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    public String generateHtmlPage() {
        List<ShortcutEntry> entries = this.getShortcutList();
        String lang = Locale.getDefault().toLanguageTag();
        String helpBaseUrl = GuiUtils.getUICore().getSystemPreferences().getProperty("weasis.help.online", "https://weasis.org/en/tutorials/");
        Map<String, String> categoryTopics = ShortcutManager.buildCategoryDocTopics();
        StringBuilder html = new StringBuilder(4096);
        html.append("<!DOCTYPE html>\n<html lang=\"%s\">\n<head>\n<meta charset=\"UTF-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n<title>%s</title>\n".formatted(new Object[]{ShortcutManager.escapeHtml(lang), ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.title"))}));
        html.append("<style>\n  :root {\n    --bg: #1e1e2e; --surface: #313244; --text: #cdd6f4;\n    --subtext: #a6adc8; --accent: #89b4fa; --green: #a6e3a1;\n    --border: #45475a; --modified: #f9e2af; --header-bg: #181825;\n  }\n  @media (prefers-color-scheme: light) {\n    :root {\n      --bg: #eff1f5; --surface: #ffffff; --text: #4c4f69;\n      --subtext: #6c6f85; --accent: #1e66f5; --green: #40a02b;\n      --border: #ccd0da; --modified: #df8e1d; --header-bg: #e6e9ef;\n    }\n  }\n  * { box-sizing: border-box; margin: 0; padding: 0; }\n  body {\n    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n    background: var(--bg); color: var(--text); padding: 2rem; line-height: 1.6;\n  }\n  h1 {\n    text-align: center; color: var(--accent); margin-bottom: .3rem;\n    font-size: 1.8rem; font-weight: 700;\n  }\n  .subtitle {\n    text-align: center; color: var(--subtext); margin-bottom: 2rem;\n    font-size: .9rem;\n  }\n  .category { margin-bottom: 2rem; }\n  .category h2 {\n    color: var(--green); font-size: 1.15rem; font-weight: 600;\n    border-bottom: 2px solid var(--border); padding-bottom: .4rem;\n    margin-bottom: .6rem;\n  }\n  table {\n    width: 100%%; border-collapse: collapse;\n    background: var(--surface); border-radius: 8px; overflow: hidden;\n  }\n  th {\n    background: var(--header-bg); text-align: left; padding: .55rem .8rem;\n    font-size: .8rem; text-transform: uppercase; letter-spacing: .04em;\n    color: var(--subtext); border-bottom: 1px solid var(--border);\n  }\n  td {\n    padding: .5rem .8rem; border-bottom: 1px solid var(--border);\n    font-size: .88rem;\n  }\n  tr:last-child td { border-bottom: none; }\n  tr:hover td { background: var(--header-bg); }\n  .kbd {\n    display: inline-block; background: var(--header-bg); color: var(--accent);\n    border: 1px solid var(--border); border-radius: 4px;\n    padding: .1rem .45rem; font-family: 'SF Mono', Consolas, monospace;\n    font-size: .82rem; white-space: nowrap;\n  }\n  .modified .kbd { color: var(--modified); border-color: var(--modified); }\n  .modified td:first-child::after {\n    content: ' \u270e'; color: var(--modified); font-size: .75rem;\n  }\n  .info-note {\n    background: var(--surface); border-left: 4px solid var(--accent);\n    border-radius: 6px; padding: .8rem 1rem; margin-bottom: 1.5rem;\n    color: var(--subtext); font-size: .85rem; line-height: 1.5;\n  }\n  .info-note strong { color: var(--text); }\n  .fixed-section { margin-bottom: 2rem; }\n  .fixed-section h2 {\n    color: var(--accent); font-size: 1.15rem; font-weight: 600;\n    border-bottom: 2px solid var(--border); padding-bottom: .4rem;\n    margin-bottom: .6rem;\n  }\n  .fixed-section h3 {\n    color: var(--green); font-size: 1rem; font-weight: 600;\n    margin: 1rem 0 .4rem;\n  }\n  footer {\n    text-align: center; color: var(--subtext); margin-top: 2rem;\n    font-size: .78rem;\n  }\n  a.doc-link {\n    color: var(--accent); text-decoration: none; font-size: .82rem;\n    margin-left: .4rem; opacity: .7; transition: opacity .15s;\n  }\n  a.doc-link:hover { opacity: 1; text-decoration: underline; }\n  .doc-bar {\n    text-align: center; margin-bottom: 1.5rem;\n  }\n  .doc-bar a {\n    color: var(--accent); text-decoration: none; font-size: .92rem;\n  }\n  .doc-bar a:hover { text-decoration: underline; }\n</style>\n</head>\n<body>\n");
        html.append("<h1>\u2328 ").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.title"))).append("</h1>\n");
        html.append("<div class=\"subtitle\">").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.subtitle"))).append("</div>\n");
        html.append("<div class=\"doc-bar\"><a href=\"").append(ShortcutManager.escapeHtml(helpBaseUrl)).append("index.html\" target=\"_blank\">\u2197 ").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.online_doc"))).append("</a></div>\n");
        LinkedHashMap<String, List> byCategory = new LinkedHashMap<String, List>();
        for (ShortcutEntry shortcutEntry : entries) {
            byCategory.computeIfAbsent(shortcutEntry.getCategory(), k -> new ArrayList()).add(shortcutEntry);
        }
        for (Map.Entry entry : byCategory.entrySet()) {
            html.append("<div class=\"category\">\n");
            html.append("<h2>").append(ShortcutManager.escapeHtml((String)entry.getKey()));
            ShortcutManager.appendDocLink(html, helpBaseUrl, categoryTopics.get(entry.getKey()));
            html.append("</h2>\n");
            html.append("<table>\n");
            html.append("<tr><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.action"))).append("</th><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.context"))).append("</th><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.shortcut"))).append("</th><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.default"))).append("</th></tr>\n");
            for (ShortcutEntry entry2 : (List)entry.getValue()) {
                String rowClass = entry2.isModified() ? " class=\"modified\"" : "";
                String shortcutText = entry2.getShortcutText();
                String defaultText = entry2.getDefaultShortcutText();
                String noneText = Messages.getString("ShortcutManager.html.none");
                String shortcutHtml = shortcutText.isEmpty() ? "<em style=\"color:var(--subtext)\">" + ShortcutManager.escapeHtml(noneText) + "</em>" : "<span class=\"kbd\">" + ShortcutManager.escapeHtml(shortcutText) + "</span>";
                String defaultHtml = defaultText.isEmpty() ? "\u2014" : "<span class=\"kbd\">" + ShortcutManager.escapeHtml(defaultText) + "</span>";
                html.append("<tr").append(rowClass).append(">");
                html.append("<td>").append(ShortcutManager.escapeHtml(entry2.getDescription())).append("</td>");
                html.append("<td>").append(ShortcutManager.escapeHtml(entry2.getContext().getDisplayName())).append("</td>");
                html.append("<td>").append(shortcutHtml).append("</td>");
                html.append("<td>").append(entry2.isModified() ? defaultHtml : "\u2014").append("</td>");
                html.append("</tr>\n");
            }
            html.append("</table>\n</div>\n");
        }
        ShortcutManager.appendFixedShortcutsSection(html, helpBaseUrl);
        html.append("<footer>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.footer"))).append(" \u00b7 ").append(AppProperties.WEASIS_NAME).append(" ").append(AppProperties.WEASIS_VERSION).append("</footer>\n");
        html.append("</body>\n</html>\n");
        return html.toString();
    }

    public Path writeHtmlToTempFile(Path tempDir) throws IOException {
        String html = this.generateHtmlPage();
        Path file = tempDir != null ? Files.createTempFile(tempDir, "weasis-shortcuts-", ".html", new FileAttribute[0]) : Files.createTempFile("weasis-shortcuts-", ".html", new FileAttribute[0]);
        Files.writeString(file, (CharSequence)html, StandardCharsets.UTF_8, new OpenOption[0]);
        return file;
    }

    private static void appendFixedShortcutsSection(StringBuilder html, String helpBaseUrl) {
        html.append("<div class=\"fixed-section\">\n");
        html.append("<h2>").append("\ud83d\udd12 ").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.fixed_title"))).append("</h2>\n");
        html.append("<h3>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.dicom_presets"))).append("</h3>\n");
        html.append("<div class=\"info-note\">").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.dicom_presets_note"))).append("</div>\n");
        html.append("<table>\n");
        html.append("<tr><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.shortcut"))).append("</th><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.action"))).append("</th><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.note"))).append("</th></tr>\n");
        html.append("<tr><td><span class=\"kbd\">").append(ShortcutManager.escapeHtml("0 1 2 3 \u2026 9")).append("</span></td><td>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.dicom_presets_action"))).append("</td><td>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.dicom_presets_note_col"))).append("</td></tr>\n");
        html.append("</table>\n");
        html.append("</div>\n");
        html.append("<div class=\"fixed-section\">\n");
        html.append("<h2>").append("\ud83d\uddb1 ").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.mouse_title"))).append("</h2>\n");
        html.append("<div class=\"info-note\">").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.mouse_note"))).append("</div>\n");
        html.append("<h3>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.dicom_2d_viewer")));
        ShortcutManager.appendDocLink(html, helpBaseUrl, "dicom-2d-viewer");
        html.append("</h3>\n");
        html.append("<table>\n");
        html.append("<tr><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.shortcut"))).append("</th><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.action"))).append("</th></tr>\n");
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.left_drag"), Messages.getString("ShortcutManager.html.mouse.left_drag_action"));
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.middle_drag"), Messages.getString("ShortcutManager.html.mouse.middle_drag_action"));
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.right_click"), Messages.getString("ShortcutManager.html.mouse.right_click_action"));
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.scroll"), Messages.getString("ShortcutManager.html.mouse.scroll_action"));
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.ctrl_drag"), Messages.getString("ShortcutManager.html.mouse.ctrl_drag_action"));
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.ctrl_shift_drag"), Messages.getString("ShortcutManager.html.mouse.ctrl_shift_drag_action"));
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.dblclick_f11"), Messages.getString("ShortcutManager.html.mouse.dblclick_f11_action"));
        html.append("</table>\n");
        html.append("<h3>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.mpr_viewer")));
        ShortcutManager.appendDocLink(html, helpBaseUrl, "mpr");
        html.append("</h3>\n");
        html.append("<table>\n");
        html.append("<tr><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.shortcut"))).append("</th><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.action"))).append("</th></tr>\n");
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.alt_scroll"), Messages.getString("ShortcutManager.html.mouse.alt_scroll_action"));
        html.append("</table>\n");
        html.append("<h3>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.graphics")));
        ShortcutManager.appendDocLink(html, helpBaseUrl, "draw-measure");
        html.append("</h3>\n");
        html.append("<table>\n");
        html.append("<tr><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.shortcut"))).append("</th><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.action"))).append("</th></tr>\n");
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.click_graphic"), Messages.getString("ShortcutManager.html.mouse.click_graphic_action"));
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.click_drag"), Messages.getString("ShortcutManager.html.mouse.click_drag_action"));
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.shift_click_graphic"), Messages.getString("ShortcutManager.html.mouse.shift_click_graphic_action"));
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.dblclick"), Messages.getString("ShortcutManager.html.mouse.dblclick_action"));
        html.append("</table>\n");
        html.append("<h3>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.ctx.dicom_explorer")));
        ShortcutManager.appendDocLink(html, helpBaseUrl, "dicom-explorer");
        html.append("</h3>\n");
        html.append("<table>\n");
        html.append("<tr><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.shortcut"))).append("</th><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.action"))).append("</th></tr>\n");
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.ctrl_click_thumb"), Messages.getString("ShortcutManager.html.mouse.ctrl_click_thumb_action"));
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.shift_click_thumb"), Messages.getString("ShortcutManager.html.mouse.shift_click_thumb_action"));
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.right_click"), Messages.getString("ShortcutManager.html.mouse.right_click_explorer_action"));
        html.append("</table>\n");
        html.append("<h3>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.drag_drop"))).append("</h3>\n");
        html.append("<table>\n");
        html.append("<tr><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.shortcut"))).append("</th><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.action"))).append("</th></tr>\n");
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.drag_files"), Messages.getString("ShortcutManager.html.mouse.drag_files_action"));
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.drag_thumb"), Messages.getString("ShortcutManager.html.mouse.drag_thumb_action"));
        html.append("</table>\n");
        html.append("<h3>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.tabs"))).append("</h3>\n");
        html.append("<table>\n");
        html.append("<tr><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.shortcut"))).append("</th><th>").append(ShortcutManager.escapeHtml(Messages.getString("ShortcutManager.html.col.action"))).append("</th></tr>\n");
        ShortcutManager.appendFixedRow(html, Messages.getString("ShortcutManager.html.mouse.tab_right_click"), Messages.getString("ShortcutManager.html.mouse.tab_right_click_action"));
        html.append("</table>\n");
        html.append("</div>\n");
    }

    private static void appendFixedRow(StringBuilder html, String shortcut, String action) {
        html.append("<tr><td><span class=\"kbd\">").append(ShortcutManager.escapeHtml(shortcut)).append("</span></td><td>").append(ShortcutManager.escapeHtml(action)).append("</td></tr>\n");
    }

    private static void appendDocLink(StringBuilder html, String helpBaseUrl, String topic) {
        if (topic != null && helpBaseUrl != null) {
            html.append(" <a class=\"doc-link\" href=\"").append(ShortcutManager.escapeHtml(helpBaseUrl)).append(ShortcutManager.escapeHtml(topic)).append("\" target=\"_blank\">\u2197</a>");
        }
    }

    private static Map<String, String> buildCategoryDocTopics() {
        LinkedHashMap<String, String> topics = new LinkedHashMap<String, String>();
        topics.put(CATEGORY_MOUSE_ACTIONS, "dicom-2d-viewer");
        topics.put(CATEGORY_VIEWER, "dicom-2d-viewer");
        topics.put(CATEGORY_NAVIGATION, "dicom-2d-viewer");
        topics.put(CATEGORY_MEASURES, "draw-measure");
        topics.put(CATEGORY_DRAWINGS, "draw-measure");
        topics.put(CATEGORY_DISPLAY, "dicom-2d-viewer");
        topics.put(CATEGORY_DICOM_NAV, "dicom-2d-viewer");
        topics.put(CATEGORY_MPR, "mpr");
        topics.put(CATEGORY_PAN, "dicom-2d-viewer");
        topics.put(CATEGORY_DICOM_EXPLORER, "dicom-explorer");
        return topics;
    }

    private static String escapeHtml(String s) {
        return EscapeChars.forHTML((String)s);
    }

    public static String formatKeyStroke(KeyStroke ks) {
        if (ks == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int mod = ks.getModifiers();
        if ((mod & 0x80) != 0 || (mod & 2) != 0) {
            sb.append("Ctrl+");
        }
        if ((mod & 0x200) != 0 || (mod & 8) != 0) {
            sb.append("Alt+");
        }
        if ((mod & 0x40) != 0 || (mod & 1) != 0) {
            sb.append("Shift+");
        }
        if ((mod & 0x100) != 0 || (mod & 4) != 0) {
            sb.append("Meta+");
        }
        sb.append(KeyEvent.getKeyText(ks.getKeyCode()));
        return sb.toString();
    }

    public static enum ShortcutContext {
        VIEW_CANVAS("ShortcutManager.ctx.view_canvas", null),
        DICOM_VIEWER("ShortcutManager.ctx.dicom_viewer", VIEW_CANVAS),
        MPR("ShortcutManager.ctx.mpr", DICOM_VIEWER),
        VIEWER_3D("ShortcutManager.ctx.viewer_3d", VIEW_CANVAS),
        DICOM_EXPLORER("ShortcutManager.ctx.dicom_explorer", null);

        private final String displayKey;
        private final ShortcutContext parent;

        private ShortcutContext(String displayKey, ShortcutContext parent) {
            this.displayKey = displayKey;
            this.parent = parent;
        }

        public String getDisplayName() {
            return Messages.getString(this.displayKey);
        }

        public ShortcutContext getParent() {
            return this.parent;
        }

        public boolean overlapsWith(ShortcutContext other) {
            if (other == null) {
                return false;
            }
            if (this == other) {
                return true;
            }
            ShortcutContext ancestor = this.parent;
            while (ancestor != null) {
                if (ancestor == other) {
                    return true;
                }
                ancestor = ancestor.parent;
            }
            ancestor = other.parent;
            while (ancestor != null) {
                if (ancestor == this) {
                    return true;
                }
                ancestor = ancestor.parent;
            }
            return false;
        }
    }

    public static final class ShortcutEntry {
        private final String id;
        private final String description;
        private final String category;
        private final ShortcutContext context;
        private final int defaultKeyCode;
        private final int defaultModifier;
        private int keyCode;
        private int modifier;

        public ShortcutEntry(String id, String description, String category, ShortcutContext context, int defaultKeyCode, int defaultModifier) {
            this.id = Objects.requireNonNull(id);
            this.description = description != null ? description : id;
            this.category = category != null ? category : "";
            this.context = context != null ? context : ShortcutContext.VIEW_CANVAS;
            this.defaultKeyCode = defaultKeyCode;
            this.defaultModifier = defaultModifier;
            this.keyCode = defaultKeyCode;
            this.modifier = defaultModifier;
        }

        public String getId() {
            return this.id;
        }

        public String getDescription() {
            return this.description;
        }

        public String getCategory() {
            return this.category;
        }

        public ShortcutContext getContext() {
            return this.context;
        }

        public int getDefaultKeyCode() {
            return this.defaultKeyCode;
        }

        public int getDefaultModifier() {
            return this.defaultModifier;
        }

        public int getKeyCode() {
            return this.keyCode;
        }

        public void setKeyCode(int keyCode) {
            this.keyCode = keyCode;
        }

        public int getModifier() {
            return this.modifier;
        }

        public void setModifier(int modifier) {
            this.modifier = modifier;
        }

        public boolean isModified() {
            return this.keyCode != this.defaultKeyCode || this.modifier != this.defaultModifier;
        }

        public void resetToDefault() {
            this.keyCode = this.defaultKeyCode;
            this.modifier = this.defaultModifier;
        }

        public String getShortcutText() {
            return ShortcutEntry.getKeyStrokeText(this.keyCode, this.modifier);
        }

        public String getDefaultShortcutText() {
            return ShortcutEntry.getKeyStrokeText(this.defaultKeyCode, this.defaultModifier);
        }

        private static String getKeyStrokeText(int kc, int mod) {
            if (kc == 0) {
                return "";
            }
            KeyStroke ks = KeyStroke.getKeyStroke(kc, mod);
            return ks == null ? "" : ShortcutManager.formatKeyStroke(ks);
        }

        public String toString() {
            return this.description + " [" + this.getShortcutText() + "]";
        }
    }
}

