/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

import ch.qos.logback.core.CoreConstants;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CoreVersionUtil {
    static String CORE_MODULE_NAME = "logback-core";
    static String CORE_MODULE_VERSION_PROPERTIES_FILE = CORE_MODULE_NAME + "-version.properties";
    static String CORE_MODULE_VERSION_PROPERTY_KEY = CORE_MODULE_NAME + "-version";

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static String getCoreVersionBySelfDeclaredProperties() {
        Properties props = new Properties();
        try (InputStream is = CoreConstants.class.getResourceAsStream(CORE_MODULE_VERSION_PROPERTIES_FILE);){
            if (is != null) {
                props.load(is);
                String string2 = props.getProperty(CORE_MODULE_VERSION_PROPERTY_KEY);
                return string2;
            }
            String string = null;
            return string;
        }
        catch (IOException e) {
            return null;
        }
    }
}

