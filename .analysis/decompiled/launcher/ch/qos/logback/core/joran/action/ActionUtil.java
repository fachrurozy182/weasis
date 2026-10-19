/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.spi.ContextAwarePropertyContainer;
import ch.qos.logback.core.util.OptionHelper;

public class ActionUtil {
    public static Scope stringToScope(String scopeStr) {
        if (Scope.SYSTEM.toString().equalsIgnoreCase(scopeStr)) {
            return Scope.SYSTEM;
        }
        if (Scope.CONTEXT.toString().equalsIgnoreCase(scopeStr)) {
            return Scope.CONTEXT;
        }
        return Scope.LOCAL;
    }

    public static void setProperty(ContextAwarePropertyContainer ic, String key, String value, Scope scope) {
        switch (scope.ordinal()) {
            case 0: {
                ic.addSubstitutionProperty(key, value);
                break;
            }
            case 1: {
                ic.getContext().putProperty(key, value);
                break;
            }
            case 2: {
                OptionHelper.setSystemProperty(ic, key, value);
            }
        }
    }

    public static enum Scope {
        LOCAL,
        CONTEXT,
        SYSTEM;

    }
}

