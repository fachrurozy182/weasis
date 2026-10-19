/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.util;

import ch.qos.logback.core.model.Model;

public class TagUtil {
    public static String unifiedTag(Model aModel) {
        String tag = aModel.getTag();
        char first = tag.charAt(0);
        if (Character.isUpperCase(first)) {
            char lower = Character.toLowerCase(first);
            return lower + tag.substring(1);
        }
        return tag;
    }
}

