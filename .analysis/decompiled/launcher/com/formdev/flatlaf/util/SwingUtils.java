/*
 * Decompiled with CFR 0.152.
 */
package com.formdev.flatlaf.util;

import java.awt.Component;
import java.awt.Container;

public class SwingUtils {
    public static <T extends Component> T getComponentByName(Container parent, String name) {
        for (Component child : parent.getComponents()) {
            T c;
            if (name.equals(child.getName())) {
                return (T)child;
            }
            if (!(child instanceof Container) || (c = SwingUtils.getComponentByName((Container)child, name)) == null) continue;
            return c;
        }
        return null;
    }
}

