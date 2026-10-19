/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.api.gui.util.GuiUtils
 *  org.weasis.core.api.service.WProperties
 */
package org.weasis.dicom.explorer;

import org.weasis.core.api.gui.util.GuiUtils;
import org.weasis.core.api.service.WProperties;

public class LocalPersistence {
    private LocalPersistence() {
    }

    public static WProperties getProperties() {
        return GuiUtils.getUICore().getPluginPersistence(LocalPersistence.class.getPackageName());
    }
}
