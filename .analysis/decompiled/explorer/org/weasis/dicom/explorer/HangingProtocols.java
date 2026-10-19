/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.explorer;

import org.weasis.dicom.explorer.LocalPersistence;

public class HangingProtocols {
    public static final String CLOSE_PREVIOUS_KEY = "weasis.open.viewer.clean";

    public static boolean isClosePreviousFromPreferences() {
        return Boolean.parseBoolean(LocalPersistence.getProperties().getProperty(CLOSE_PREVIOUS_KEY, "false"));
    }

    public static void setClosePrevious(boolean closePrevious) {
        LocalPersistence.getProperties().setProperty(CLOSE_PREVIOUS_KEY, Boolean.toString(closePrevious));
    }

    public static enum OpeningViewer {
        NONE,
        ALL_PATIENTS;

    }
}

