/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.util.OptionHelper;

public class LevelUtil {
    public static boolean isInheritedLevelString(String levelStr) {
        return "INHERITED".equalsIgnoreCase(levelStr) || JoranConstants.NULL.equalsIgnoreCase(levelStr);
    }

    public static Level levelStringToLevel(String levelStr) {
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(levelStr)) {
            if (LevelUtil.isInheritedLevelString(levelStr)) {
                return null;
            }
            Level level = Level.toLevel(levelStr);
            return level;
        }
        return null;
    }
}

