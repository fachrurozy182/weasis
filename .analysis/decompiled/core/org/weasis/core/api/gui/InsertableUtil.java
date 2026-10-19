/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.service.prefs.Preferences
 */
package org.weasis.core.api.gui;

import java.util.List;
import java.util.Properties;
import org.osgi.service.prefs.Preferences;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.BundlePreferences;

public class InsertableUtil {
    public static final String ALL_BUNDLE = "weasis";
    public static final String ALL = "all";

    private InsertableUtil() {
    }

    public static void sortInsertable(List<? extends Insertable> list) {
        list.sort((o1, o2) -> {
            int val1 = o1.getComponentPosition();
            int val2 = o2.getComponentPosition();
            return Integer.compare(val1, val2);
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void applyPreferences(List<? extends Insertable> list, Preferences prefs, String bundleName, String componentName, Insertable.Type type) {
        if (list != null && prefs != null && bundleName != null && componentName != null) {
            Preferences prefNode = prefs.node(componentName).node(type.name().toLowerCase() + "s");
            List<? extends Insertable> list2 = list;
            synchronized (list2) {
                for (Insertable insertable : list) {
                    if (Insertable.Type.EMPTY.equals((Object)insertable.getType())) continue;
                    String nodeName = InsertableUtil.getCName(insertable.getClass());
                    String key = "visible";
                    Preferences node = prefNode.node(nodeName);
                    String valString = node.get(key, null);
                    boolean val = true;
                    if (valString == null) {
                        val = InsertableUtil.getBooleanProperty(GuiUtils.getUICore().getSystemPreferences(), bundleName, componentName, nodeName, key, val);
                    } else if (Boolean.FALSE.toString().equalsIgnoreCase(valString)) {
                        val = false;
                    }
                    insertable.setComponentEnabled(val);
                    key = "cPosition";
                    valString = node.get(key, null);
                    int index = insertable.getComponentPosition();
                    if (valString == null) {
                        index = InsertableUtil.getIntProperty(GuiUtils.getUICore().getSystemPreferences(), bundleName, componentName, nodeName, key, index);
                    } else {
                        try {
                            index = Integer.parseInt(valString);
                        }
                        catch (NumberFormatException numberFormatException) {
                            // empty catch block
                        }
                    }
                    insertable.setComponentPosition(index);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void savePreferences(List<? extends Insertable> list, Preferences prefs, Insertable.Type type) {
        if (list != null && prefs != null) {
            Preferences prefNode = prefs.node(type.name().toLowerCase() + "s");
            List<? extends Insertable> list2 = list;
            synchronized (list2) {
                for (Insertable insertable : list) {
                    if (Insertable.Type.EMPTY.equals((Object)insertable.getType())) continue;
                    String cname = InsertableUtil.getCName(insertable.getClass());
                    Preferences node = prefNode.node(cname);
                    BundlePreferences.putBooleanPreferences(node, "visible", insertable.isComponentEnabled());
                    BundlePreferences.putIntPreferences(node, "cPosition", insertable.getComponentPosition());
                }
            }
        }
    }

    public static boolean getBooleanProperty(Properties props, String bundleName, String className, String componentName, String key, boolean def) {
        if (props != null && bundleName != null && className != null && key != null) {
            for (String bundle : new String[]{bundleName, ALL_BUNDLE}) {
                for (String cl : new String[]{className, ALL}) {
                    String value = InsertableUtil.buildPropertyName(props, componentName, key, bundle, cl);
                    if (value == null) continue;
                    if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
                        return true;
                    }
                    if (!Boolean.FALSE.toString().equalsIgnoreCase(value)) continue;
                    return false;
                }
            }
        }
        return def;
    }

    private static String buildPropertyName(Properties props, String componentName, String key, String bundle, String cl) {
        StringBuilder buf = new StringBuilder(bundle);
        buf.append('.');
        buf.append(cl);
        buf.append('.');
        buf.append(componentName);
        buf.append('.');
        buf.append(key);
        return props.getProperty(buf.toString());
    }

    private static int getIntProperty(Properties props, String bundleName, String className, String componentName, String key, int def) {
        if (props != null && bundleName != null && className != null && key != null) {
            for (String bundle : new String[]{bundleName, ALL_BUNDLE}) {
                for (String cl : new String[]{className, ALL}) {
                    String value = InsertableUtil.buildPropertyName(props, componentName, key, bundle, cl);
                    if (value == null) continue;
                    try {
                        return Integer.parseInt(value);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
            }
        }
        return def;
    }

    public static String getCName(Class<?> clazz) {
        if (clazz != null) {
            return clazz.getSimpleName().toLowerCase();
        }
        return "";
    }
}

