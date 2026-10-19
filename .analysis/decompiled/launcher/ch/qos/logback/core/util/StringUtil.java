/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

public class StringUtil {
    public static String nullStringToEmpty(String input) {
        if (input != null) {
            return input;
        }
        return "";
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean notNullNorEmpty(String str) {
        return !StringUtil.isNullOrEmpty(str);
    }

    public static String capitalizeFirstLetter(String name) {
        if (StringUtil.isNullOrEmpty(name)) {
            return name;
        }
        if (name.length() == 1) {
            return name.toUpperCase();
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String lowercaseFirstLetter(String name) {
        if (StringUtil.isNullOrEmpty(name)) {
            return name;
        }
        if (name.length() == 1) {
            return name.toLowerCase();
        }
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }
}

