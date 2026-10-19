/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.send;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static final String BUNDLE_NAME = "org.weasis.dicom.send.messages";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("org.weasis.dicom.send.messages");

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        }
        catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }
}

