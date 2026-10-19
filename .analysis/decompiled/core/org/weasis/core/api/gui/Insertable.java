/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui;

public interface Insertable {
    public static final int LAST_FILLER_HEIGHT = 5;
    public static final int BLOCK_SEPARATOR = 15;
    public static final int ITEM_SEPARATOR_SMALL = 2;
    public static final int ITEM_SEPARATOR = 5;
    public static final int ITEM_SEPARATOR_LARGE = 10;

    public String getComponentName();

    public Type getType();

    public boolean isComponentEnabled();

    public void setComponentEnabled(boolean var1);

    public int getComponentPosition();

    public void setComponentPosition(int var1);

    public static enum Type {
        EXPLORER,
        TOOL,
        TOOL_EXT,
        TOOLBAR,
        EMPTY,
        LICENSE,
        PREFERENCES;

    }
}

