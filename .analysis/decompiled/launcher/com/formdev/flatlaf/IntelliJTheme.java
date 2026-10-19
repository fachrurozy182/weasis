/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.UIDefaultsLoader;
import com.formdev.flatlaf.json.Json;
import com.formdev.flatlaf.json.ParseException;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.StringUtils;
import com.formdev.flatlaf.util.SystemInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class IntelliJTheme {
    public final String name;
    public final boolean dark;
    public final String author;
    private final Map<String, String> jsonColors;
    private final Map<String, Object> jsonUI;
    private final Map<String, Object> jsonIcons;
    private Map<String, String> namedColors = Collections.emptyMap();
    private static final Set<String> uiKeyExcludesStartsWith;
    private static final String[] uiKeyExcludesContains;
    private static final Set<String> uiKeyDoNotOverride;
    private static final Map<String, String> uiKeyMapping;
    private static final Map<String, String> uiKeyCopying;
    private static final Map<String, String> uiKeyInverseMapping;
    private static final Map<String, String> checkboxKeyMapping;
    private static final Map<String, String> checkboxDuplicateColors;

    public static boolean setup(InputStream in) {
        try {
            return FlatLaf.setup(IntelliJTheme.createLaf(in));
        }
        catch (Exception ex) {
            LoggingFacade.INSTANCE.logSevere("FlatLaf: Failed to load IntelliJ theme", ex);
            return false;
        }
    }

    @Deprecated
    public static boolean install(InputStream in) {
        return IntelliJTheme.setup(in);
    }

    public static FlatLaf createLaf(InputStream in) throws IOException {
        return IntelliJTheme.createLaf(new IntelliJTheme(in));
    }

    public static FlatLaf createLaf(IntelliJTheme theme) {
        return new ThemeLaf(theme);
    }

    public IntelliJTheme(InputStream in) throws IOException {
        Map json;
        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);){
            json = (Map)Json.parse(reader);
        }
        catch (ParseException ex) {
            throw new IOException(ex.getMessage(), ex);
        }
        this.name = (String)json.get("name");
        this.dark = Boolean.parseBoolean((String)json.get("dark"));
        this.author = (String)json.get("author");
        this.jsonColors = (Map)json.get("colors");
        this.jsonUI = (Map)json.get("ui");
        this.jsonIcons = (Map)json.get("icons");
    }

    private void applyProperties(Properties properties) {
        int rowHeight;
        if (this.jsonUI == null) {
            return;
        }
        this.put(properties, "Component.isIntelliJTheme", "true");
        this.put(properties, "Button.paintShadow", "true");
        this.put(properties, "Button.shadowWidth", this.dark ? "2" : "1");
        Map<String, String> themeSpecificProps = this.removeThemeSpecificProps(properties);
        HashSet<String> jsonUIKeys = new HashSet<String>();
        this.loadNamedColors(properties, jsonUIKeys);
        for (Map.Entry<String, Object> entry : this.jsonUI.entrySet()) {
            this.apply(entry.getKey(), entry.getValue(), properties, jsonUIKeys);
        }
        this.copyIfSetInJson(properties, jsonUIKeys, "@background", "Panel.background", "*.background");
        this.copyIfSetInJson(properties, jsonUIKeys, "@foreground", "CheckBox.foreground", "*.foreground");
        this.copyIfSetInJson(properties, jsonUIKeys, "@accentBaseColor", "ColorPalette.accent", "ColorPalette.accentColor", "ProgressBar.foreground", "*.selectionBackground");
        this.copyIfSetInJson(properties, jsonUIKeys, "@accentUnderlineColor", "*.underlineColor", "TabbedPane.underlineColor");
        this.copyIfSetInJson(properties, jsonUIKeys, "@selectionBackground", "*.selectionBackground");
        this.copyIfSetInJson(properties, jsonUIKeys, "@selectionForeground", "*.selectionForeground");
        this.copyIfSetInJson(properties, jsonUIKeys, "@selectionInactiveBackground", "*.selectionInactiveBackground");
        this.copyIfSetInJson(properties, jsonUIKeys, "@selectionInactiveForeground", "*.selectionInactiveForeground");
        this.applyIconsColorPalette(properties);
        this.applyCheckBoxColors(properties);
        for (Map.Entry<String, Object> entry : uiKeyCopying.entrySet()) {
            Object value = properties.get(entry.getValue());
            if (value == null) continue;
            this.put(properties, entry.getKey(), value);
        }
        this.put(properties, "Button.disabledBackground", "@disabledBackground");
        this.put(properties, "ToggleButton.disabledBackground", "@disabledBackground");
        this.fixStartEnd(properties, jsonUIKeys, "Button.startBackground", "Button.endBackground", "Button.background");
        this.fixStartEnd(properties, jsonUIKeys, "Button.startBorderColor", "Button.endBorderColor", "Button.borderColor");
        this.fixStartEnd(properties, jsonUIKeys, "Button.default.startBackground", "Button.default.endBackground", "Button.default.background");
        this.fixStartEnd(properties, jsonUIKeys, "Button.default.startBorderColor", "Button.default.endBorderColor", "Button.default.borderColor");
        String textFieldBackground = this.get(properties, themeSpecificProps, "TextField.background");
        this.put(properties, "ComboBox.editableBackground", textFieldBackground);
        this.put(properties, "Spinner.background", textFieldBackground);
        this.putAll(properties, textFieldBackground, "EditorPane.background", "FormattedTextField.background", "PasswordField.background", "TextArea.background", "TextPane.background");
        this.putAll(properties, this.get(properties, themeSpecificProps, "TextField.selectionBackground"), "EditorPane.selectionBackground", "FormattedTextField.selectionBackground", "PasswordField.selectionBackground", "TextArea.selectionBackground", "TextPane.selectionBackground");
        this.putAll(properties, this.get(properties, themeSpecificProps, "TextField.selectionForeground"), "EditorPane.selectionForeground", "FormattedTextField.selectionForeground", "PasswordField.selectionForeground", "TextArea.selectionForeground", "TextPane.selectionForeground");
        this.putAll(properties, "@disabledBackground", "ComboBox.disabledBackground", "EditorPane.disabledBackground", "EditorPane.inactiveBackground", "FormattedTextField.disabledBackground", "FormattedTextField.inactiveBackground", "PasswordField.disabledBackground", "PasswordField.inactiveBackground", "Spinner.disabledBackground", "TextArea.disabledBackground", "TextArea.inactiveBackground", "TextField.disabledBackground", "TextField.inactiveBackground", "TextPane.disabledBackground", "TextPane.inactiveBackground");
        this.put(properties, "Desktop.background", this.dark ? "lighten($Panel.background,5%)" : "darken($Panel.background,5%)");
        String string = (String)properties.get("Tree.rowHeight");
        int n = rowHeight = string != null ? Integer.parseInt(string) : 0;
        if (rowHeight > 22) {
            this.put(properties, "Tree.rowHeight", "22");
        }
        HashMap<String, String> wildcardProps = new HashMap<String, String>();
        Iterator<Map.Entry<String, String>> it = themeSpecificProps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> e = it.next();
            String string2 = e.getKey();
            if (!string2.startsWith("*.")) continue;
            wildcardProps.put(string2, e.getValue());
            it.remove();
        }
        if (!wildcardProps.isEmpty()) {
            for (Map.Entry<Object, Object> entry : wildcardProps.entrySet()) {
                this.applyWildcard(properties, (String)entry.getKey(), (String)entry.getValue());
            }
        }
        for (Map.Entry<Object, Object> entry : themeSpecificProps.entrySet()) {
            String oldValue;
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            if (key.startsWith("[style]") && (oldValue = (String)properties.get(key)) != null) {
                value = oldValue + "; " + value;
            }
            this.put(properties, key, value);
        }
    }

    private String get(Properties properties, Map<String, String> themeSpecificProps, String key) {
        return themeSpecificProps.getOrDefault(key, (String)properties.get(key));
    }

    private void put(Properties properties, Object key, Object value) {
        if (value != null) {
            properties.put(key, value);
        } else {
            properties.remove(key);
        }
    }

    private void putAll(Properties properties, Object value, String ... keys) {
        for (String key : keys) {
            this.put(properties, key, value);
        }
    }

    private void copyIfSetInJson(Properties properties, Set<String> jsonUIKeys, String destKey, String ... srcKeys) {
        for (String srcKey : srcKeys) {
            Object value;
            if (!jsonUIKeys.contains(srcKey) || (value = properties.get(srcKey)) == null) continue;
            this.put(properties, destKey, value);
            break;
        }
    }

    private void fixStartEnd(Properties properties, Set<String> jsonUIKeys, String startKey, String endKey, String key) {
        if (jsonUIKeys.contains(startKey) && jsonUIKeys.contains(endKey)) {
            this.put(properties, key, "$" + startKey);
        }
    }

    private Map<String, String> removeThemeSpecificProps(Properties properties) {
        ArrayList<String> themeSpecificKeys = new ArrayList<String>();
        for (Object key : properties.keySet()) {
            if (!((String)key).startsWith("{")) continue;
            themeSpecificKeys.add((String)key);
        }
        String currentThemePrefix = '{' + this.name.replace(' ', '_') + '}';
        String currentThemeAndAuthorPrefix = '{' + this.name.replace(' ', '_') + "---" + this.author.replace(' ', '_') + '}';
        String currentAuthorPrefix = "{author-" + this.author.replace(' ', '_') + '}';
        String lightOrDarkPrefix = this.dark ? "{*-dark}" : "{*-light}";
        String allThemesPrefix = "{*}";
        String[] prefixes = new String[]{currentThemePrefix, currentThemeAndAuthorPrefix, currentAuthorPrefix, lightOrDarkPrefix, allThemesPrefix};
        Map[] maps = new Map[prefixes.length];
        for (int i = 0; i < maps.length; ++i) {
            maps[i] = new HashMap();
        }
        block2: for (String key : themeSpecificKeys) {
            String value = (String)properties.remove(key);
            for (int i = 0; i < prefixes.length; ++i) {
                String prefix = prefixes[i];
                if (!key.startsWith(prefix)) continue;
                maps[i].put(key.substring(prefix.length()), value);
                continue block2;
            }
        }
        HashMap<String, String> themeSpecificProps = new HashMap<String, String>();
        for (int i = maps.length - 1; i >= 0; --i) {
            themeSpecificProps.putAll(maps[i]);
        }
        return themeSpecificProps;
    }

    private void loadNamedColors(Properties properties, Set<String> jsonUIKeys) {
        if (this.jsonColors == null) {
            return;
        }
        this.namedColors = new HashMap<String, String>();
        for (Map.Entry<String, String> e : this.jsonColors.entrySet()) {
            String value = e.getValue();
            if (!this.canParseColor(value)) continue;
            String key = e.getKey();
            this.namedColors.put(key, value);
            String uiKey = "ColorPalette." + key;
            this.put(properties, uiKey, value);
            jsonUIKeys.add(uiKey);
        }
    }

    private void apply(String key, Object value, Properties properties, Set<String> jsonUIKeys) {
        if (value instanceof Map) {
            Map map = (Map)value;
            if (map.containsKey("os.default") || map.containsKey("os.windows") || map.containsKey("os.mac") || map.containsKey("os.linux")) {
                String osKey;
                String string = SystemInfo.isWindows ? "os.windows" : (SystemInfo.isMacOS ? "os.mac" : (osKey = SystemInfo.isLinux ? "os.linux" : null));
                if (osKey != null && map.containsKey(osKey)) {
                    this.apply(key, map.get(osKey), properties, jsonUIKeys);
                } else if (map.containsKey("os.default")) {
                    this.apply(key, map.get("os.default"), properties, jsonUIKeys);
                }
            } else {
                for (Map.Entry e : map.entrySet()) {
                    this.apply(key + '.' + (String)e.getKey(), e.getValue(), properties, jsonUIKeys);
                }
            }
        } else {
            int dot2;
            if ("".equals(value)) {
                return;
            }
            if (key.endsWith(".border") || key.endsWith(".rowHeight") || key.equals("ComboBox.padding") || key.equals("Spinner.padding") || key.equals("Tree.leftChildIndent") || key.equals("Tree.rightChildIndent")) {
                return;
            }
            if (key.endsWith("Icon")) {
                return;
            }
            if ((key = uiKeyMapping.getOrDefault(key, key)).isEmpty()) {
                return;
            }
            int dot = key.indexOf(46);
            if (dot > 0 && uiKeyExcludesStartsWith.contains(key.substring(0, dot + 1))) {
                return;
            }
            int n = dot2 = dot > 0 ? key.indexOf(46, dot + 1) : -1;
            if (dot2 > 0 && uiKeyExcludesStartsWith.contains(key.substring(0, dot2 + 1))) {
                return;
            }
            for (String s : uiKeyExcludesContains) {
                if (!key.contains(s)) continue;
                return;
            }
            if (uiKeyDoNotOverride.contains(key) && jsonUIKeys.contains(key)) {
                return;
            }
            jsonUIKeys.add(key);
            String valueStr = value.toString().trim();
            String uiValue = this.namedColors.get(valueStr);
            if (uiValue == null) {
                List<String> parts;
                if (!valueStr.startsWith("#") && (key.endsWith("ground") || key.endsWith("Color"))) {
                    valueStr = this.fixColorIfValid("#" + valueStr, valueStr);
                } else if (valueStr.startsWith("##")) {
                    valueStr = this.fixColorIfValid(valueStr.substring(1), valueStr);
                } else if ((key.endsWith(".border") || key.endsWith("Border")) && (parts = StringUtils.split(valueStr, ',')).size() == 5 && !parts.get(4).startsWith("#")) {
                    parts.set(4, "#" + parts.get(4));
                    valueStr = String.join((CharSequence)",", parts);
                }
                try {
                    UIDefaultsLoader.parseValue(key, valueStr, null);
                    uiValue = valueStr;
                }
                catch (RuntimeException ex) {
                    UIDefaultsLoader.logParseError(key, valueStr, ex, true);
                    return;
                }
            }
            if (this.applyWildcard(properties, key, uiValue)) {
                return;
            }
            this.put(properties, key, uiValue);
        }
    }

    private boolean applyWildcard(Properties properties, String key, String value) {
        String[] keys;
        if (!key.startsWith("*.")) {
            return false;
        }
        String tail = key.substring(1);
        for (String k : keys = properties.keySet().toArray(new String[properties.size()])) {
            String km;
            if (k.startsWith("*") || k.startsWith("@") || k.startsWith("HelpButton.") || k.startsWith("JX") || k.startsWith("Jide") || k.startsWith("ProgressBar.selection") || k.startsWith("TitlePane.") || k.startsWith("ToggleButton.tab.") || k.equals("Desktop.background") || k.equals("DesktopIcon.background") || k.equals("TabbedPane.focusColor") || k.endsWith(".hoverBackground") || k.endsWith(".pressedBackground") || !(km = uiKeyInverseMapping.getOrDefault(k, k)).endsWith(tail) || k.startsWith("CheckBox.icon.")) continue;
            this.put(properties, k, value);
        }
        this.put(properties, key, value);
        return true;
    }

    private String fixColorIfValid(String newColorStr, String colorStr) {
        try {
            UIDefaultsLoader.parseColorRGBA(newColorStr);
            return newColorStr;
        }
        catch (IllegalArgumentException ex) {
            return colorStr;
        }
    }

    private void applyIconsColorPalette(Properties properties) {
        if (this.jsonIcons == null) {
            return;
        }
        Object palette = this.jsonIcons.get("ColorPalette");
        if (!(palette instanceof Map)) {
            return;
        }
        Map colorPalette = (Map)palette;
        for (Map.Entry e : colorPalette.entrySet()) {
            String color;
            String key = (String)e.getKey();
            Object value = e.getValue();
            if (key.startsWith("Checkbox.") || key.startsWith("#") || !(value instanceof String)) continue;
            if (this.dark) {
                key = StringUtils.removeTrailing(key, ".Dark");
            }
            if ((color = this.toColor((String)value)) == null) continue;
            this.put(properties, key, color);
        }
    }

    private String toColor(String value) {
        String color;
        if (value.startsWith("##")) {
            value = this.fixColorIfValid(value.substring(1), value);
        }
        return (color = this.namedColors.get(value)) != null ? color : (this.canParseColor(value) ? value : null);
    }

    private boolean canParseColor(String value) {
        try {
            return UIDefaultsLoader.parseColor(value) != null;
        }
        catch (IllegalArgumentException ex) {
            LoggingFacade.INSTANCE.logSevere("FlatLaf: Failed to parse color: '" + value + '\'', ex);
            return false;
        }
    }

    private void applyCheckBoxColors(Properties properties) {
        if (this.jsonIcons == null) {
            return;
        }
        Object palette = this.jsonIcons.get("ColorPalette");
        if (!(palette instanceof Map)) {
            return;
        }
        boolean checkboxModified = false;
        Map colorPalette = (Map)palette;
        for (Map.Entry e : colorPalette.entrySet()) {
            String color;
            String newKey;
            String key = (String)e.getKey();
            Object value = e.getValue();
            if (!key.startsWith("Checkbox.") || !(value instanceof String)) continue;
            if (this.dark) {
                key = StringUtils.removeTrailing(key, ".Dark");
            }
            if ((newKey = checkboxKeyMapping.get(key)) == null) continue;
            String checkBoxIconPrefix = "CheckBox.icon.";
            if (!this.dark && newKey.startsWith(checkBoxIconPrefix)) {
                newKey = "CheckBox.icon[filled].".concat(newKey.substring(checkBoxIconPrefix.length()));
            }
            if ((color = this.toColor((String)value)) != null) {
                this.put(properties, newKey, color);
                String key2 = checkboxDuplicateColors.get(key + ".Dark");
                if (key2 != null) {
                    String newKey2;
                    if (this.dark) {
                        key2 = StringUtils.removeTrailing(key2, ".Dark");
                    }
                    if ((newKey2 = checkboxKeyMapping.get(key2)) != null) {
                        this.put(properties, newKey2, color);
                    }
                }
            }
            checkboxModified = true;
        }
        if (checkboxModified) {
            properties.remove("CheckBox.icon.focusWidth");
            this.put(properties, "CheckBox.icon.hoverBorderColor", properties.get("CheckBox.icon.focusedBorderColor"));
            properties.remove("CheckBox.icon[filled].focusWidth");
            this.put(properties, "CheckBox.icon[filled].hoverBorderColor", properties.get("CheckBox.icon[filled].focusedBorderColor"));
            this.put(properties, "CheckBox.icon[filled].focusedSelectedBackground", properties.get("CheckBox.icon[filled].selectedBackground"));
            if (this.dark) {
                String[] focusedBorderColorKeys;
                for (String key : focusedBorderColorKeys = new String[]{"CheckBox.icon.focusedBorderColor", "CheckBox.icon.focusedSelectedBorderColor", "CheckBox.icon[filled].focusedBorderColor", "CheckBox.icon[filled].focusedSelectedBorderColor"}) {
                    Object color = properties.get(key);
                    if (color == null) continue;
                    this.put(properties, key, "fade(" + color + ", 65%)");
                }
            }
        }
    }

    static {
        Map.Entry[] entries;
        uiKeyMapping = new HashMap<String, String>();
        uiKeyCopying = new LinkedHashMap<String, String>();
        uiKeyInverseMapping = new HashMap<String, String>();
        checkboxKeyMapping = new HashMap<String, String>();
        checkboxDuplicateColors = new HashMap<String, String>();
        uiKeyExcludesStartsWith = new HashSet<String>(Arrays.asList("ActionButton.", "ActionToolbar.", "ActionsList.", "AppInspector.", "AssignedMnemonic.", "Autocomplete.", "AvailableMnemonic.", "Badge.", "Banner.", "BigSpinner.", "Bookmark.", "BookmarkIcon.", "BookmarkMnemonicAssigned.", "BookmarkMnemonicAvailable.", "BookmarkMnemonicCurrent.", "BookmarkMnemonicIcon.", "Borders.", "Breakpoint.", "Canvas.", "CellEditor.", "Code.", "CodeWithMe.", "ColumnControlButton.", "CombinedDiff.", "ComboBoxButton.", "CompilationCharts.", "CompletionPopup.", "ComplexPopup.", "Content.", "ContextHelp.", "CurrentMnemonic.", "Counter.", "Debugger.", "DebuggerPopup.", "DebuggerTabs.", "DefaultTabs.", "Dialog.", "DialogWrapper.", "DisclosureButton.", "DragAndDrop.", "Editor.", "EditorGroupsTabs.", "EditorTabs.", "FileColor.", "FindPopup.", "FlameGraph.", "Focus.", "Git.", "Github.", "GotItTooltip.", "Group.", "Gutter.", "GutterTooltip.", "HeaderColor.", "HelpTooltip.", "Hg.", "IconBadge.", "InformationHint.", "InlineBanner.", "InplaceRefactoringPopup.", "Lesson.", "LineProfiler.", "Link.", "LiveIndicator.", "MainMenu.", "MainToolbar.", "MainWindow.", "MemoryIndicator.", "MlModelBinding.", "MnemonicIcon.", "NavBar.", "NewClass.", "NewPSD.", "Notification.", "Notifications.", "NotificationsToolwindow.", "OnePixelDivider.", "OptionButton.", "Outline.", "ParameterInfo.", "PresentationAssistant.", "Plugins.", "Profiler.", "ProgressIcon.", "PsiViewer.", "Resizable.", "Review.", "ReviewList.", "RunToolbar.", "RunWidget.", "ScreenView.", "SearchEverywhere.", "SearchFieldWithExtension.", "SearchMatch.", "SearchOption.", "SearchResults.", "SegmentedButton.", "Settings.", "SidePanel.", "Space.", "SpeedSearch.", "StateWidget.", "StatusBar.", "StripeToolbar.", "Tag.", "TipOfTheDay.", "ToolbarComboWidget.", "ToolWindow.", "TrialWidget.", "UIDesigner.", "UnattendedHostStatus.", "ValidationTooltip.", "VersionControl.", "WelcomeScreen.", "darcula.", "dropArea.", "icons.", "intellijlaf.", "macOSWindow.", "material.", "tooltips.", "Checkbox.", "Toolbar.", "Tooltip.", "UiDesigner.", "link."));
        uiKeyExcludesContains = new String[]{".darcula."};
        uiKeyDoNotOverride = new HashSet<String>(Arrays.asList("TabbedPane.selectedForeground"));
        uiKeyMapping.put("*.fontFace", "");
        uiKeyMapping.put("*.fontSize", "");
        uiKeyMapping.put("Button.minimumSize", "");
        uiKeyMapping.put("CheckBox.iconSize", "");
        uiKeyMapping.put("ComboBox.background", "");
        uiKeyMapping.put("ComboBox.buttonBackground", "");
        uiKeyMapping.put("ComboBox.nonEditableBackground", "ComboBox.background");
        uiKeyMapping.put("ComboBox.ArrowButton.background", "ComboBox.buttonEditableBackground");
        uiKeyMapping.put("ComboBox.ArrowButton.disabledIconColor", "ComboBox.buttonDisabledArrowColor");
        uiKeyMapping.put("ComboBox.ArrowButton.iconColor", "ComboBox.buttonArrowColor");
        uiKeyMapping.put("ComboBox.ArrowButton.nonEditableBackground", "ComboBox.buttonBackground");
        uiKeyMapping.put("Component.inactiveErrorFocusColor", "Component.error.borderColor");
        uiKeyMapping.put("Component.errorFocusColor", "Component.error.focusedBorderColor");
        uiKeyMapping.put("Component.inactiveWarningFocusColor", "Component.warning.borderColor");
        uiKeyMapping.put("Component.warningFocusColor", "Component.warning.focusedBorderColor");
        uiKeyMapping.put("Component.inactiveSuccessFocusColor", "Component.success.borderColor");
        uiKeyMapping.put("Component.successFocusColor", "Component.success.focusedBorderColor");
        uiKeyMapping.put("Label.disabledForegroundColor", "");
        uiKeyMapping.put("Link.activeForeground", "Component.linkColor");
        uiKeyMapping.put("Menu.border", "Menu.margin");
        uiKeyMapping.put("MenuItem.border", "MenuItem.margin");
        uiKeyMapping.put("PopupMenu.border", "PopupMenu.borderInsets");
        uiKeyCopying.put("Menu.selectionBackground", "List.selectionBackground");
        uiKeyCopying.put("MenuItem.selectionBackground", "List.selectionBackground");
        uiKeyCopying.put("CheckBoxMenuItem.selectionBackground", "List.selectionBackground");
        uiKeyCopying.put("RadioButtonMenuItem.selectionBackground", "List.selectionBackground");
        uiKeyMapping.put("ProgressBar.background", "");
        uiKeyMapping.put("ProgressBar.foreground", "");
        uiKeyMapping.put("ProgressBar.trackColor", "ProgressBar.background");
        uiKeyMapping.put("ProgressBar.progressColor", "ProgressBar.foreground");
        uiKeyMapping.put("RadioButton.iconSize", "");
        uiKeyMapping.put("ScrollBar.trackColor", "ScrollBar.track");
        uiKeyMapping.put("ScrollBar.thumbColor", "ScrollBar.thumb");
        uiKeyMapping.put("Separator.separatorColor", "Separator.foreground");
        uiKeyMapping.put("Slider.buttonColor", "Slider.thumbColor");
        uiKeyMapping.put("Slider.buttonBorderColor", "");
        uiKeyMapping.put("Slider.thumb", "");
        uiKeyMapping.put("Slider.track", "");
        uiKeyMapping.put("Slider.trackDisabled", "");
        uiKeyMapping.put("Slider.trackWidth", "");
        uiKeyMapping.put("DefaultTabs.underlinedTabBackground", "TabbedPane.selectedBackground");
        uiKeyMapping.put("DefaultTabs.underlinedTabForeground", "TabbedPane.selectedForeground");
        uiKeyMapping.put("DefaultTabs.inactiveUnderlineColor", "TabbedPane.inactiveUnderlineColor");
        uiKeyMapping.put("TabbedPane.tabAreaInsets", "");
        uiKeyMapping.put("TableHeader.cellBorder", "");
        uiKeyMapping.put("TableHeader.height", "");
        uiKeyMapping.put("TitlePane.infoForeground", "TitlePane.foreground");
        uiKeyMapping.put("TitlePane.inactiveInfoForeground", "TitlePane.inactiveForeground");
        for (Map.Entry<String, String> e : uiKeyMapping.entrySet()) {
            uiKeyInverseMapping.put(e.getValue(), e.getKey());
        }
        checkboxKeyMapping.put("Checkbox.Background.Default", "CheckBox.icon.background");
        checkboxKeyMapping.put("Checkbox.Background.Disabled", "CheckBox.icon.disabledBackground");
        checkboxKeyMapping.put("Checkbox.Border.Default", "CheckBox.icon.borderColor");
        checkboxKeyMapping.put("Checkbox.Border.Disabled", "CheckBox.icon.disabledBorderColor");
        checkboxKeyMapping.put("Checkbox.Focus.Thin.Default", "CheckBox.icon.focusedBorderColor");
        checkboxKeyMapping.put("Checkbox.Focus.Wide", "CheckBox.icon.focusColor");
        checkboxKeyMapping.put("Checkbox.Foreground.Disabled", "CheckBox.icon.disabledCheckmarkColor");
        checkboxKeyMapping.put("Checkbox.Background.Selected", "CheckBox.icon.selectedBackground");
        checkboxKeyMapping.put("Checkbox.Border.Selected", "CheckBox.icon.selectedBorderColor");
        checkboxKeyMapping.put("Checkbox.Foreground.Selected", "CheckBox.icon.checkmarkColor");
        checkboxKeyMapping.put("Checkbox.Focus.Thin.Selected", "CheckBox.icon.focusedSelectedBorderColor");
        checkboxDuplicateColors.put("Checkbox.Background.Default.Dark", "Checkbox.Background.Selected.Dark");
        checkboxDuplicateColors.put("Checkbox.Border.Default.Dark", "Checkbox.Border.Selected.Dark");
        checkboxDuplicateColors.put("Checkbox.Focus.Thin.Default.Dark", "Checkbox.Focus.Thin.Selected.Dark");
        for (Map.Entry e : entries = checkboxDuplicateColors.entrySet().toArray(new Map.Entry[checkboxDuplicateColors.size()])) {
            checkboxDuplicateColors.put((String)e.getValue(), (String)e.getKey());
        }
    }

    public static class ThemeLaf
    extends FlatLaf {
        private final IntelliJTheme theme;

        public ThemeLaf(IntelliJTheme theme) {
            this.theme = theme;
        }

        @Override
        public String getName() {
            return this.theme.name;
        }

        @Override
        public String getDescription() {
            return this.getName();
        }

        @Override
        public boolean isDark() {
            return this.theme.dark;
        }

        public IntelliJTheme getTheme() {
            return this.theme;
        }

        @Override
        void applyAdditionalProperties(Properties properties) {
            this.theme.applyProperties(properties);
        }

        protected ArrayList<Class<?>> getLafClassesForDefaultsLoading() {
            ArrayList<Class<?>> lafClasses = UIDefaultsLoader.getLafClassesForDefaultsLoading(this.getClass());
            lafClasses.add(1, this.theme.dark ? FlatDarkLaf.class : FlatLightLaf.class);
            lafClasses.add(2, this.theme.dark ? FlatDarculaLaf.class : FlatIntelliJLaf.class);
            return lafClasses;
        }
    }
}

