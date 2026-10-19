/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.util;

import java.util.List;

public interface PageItem {
    public void resetToDefaultValues();

    public String getTitle();

    public List<PageItem> getSubPages();

    public void closeAdditionalWindow();
}

