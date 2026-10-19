/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.util;

import ch.qos.logback.classic.ClassicConstants;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClassicVersionUtil {
    static String CLASSIC_MODULE_NAME = "logback-classic";
    static String CLASSIC_MODULE_VERSION_PROPERTIES_FILE = CLASSIC_MODULE_NAME + "-version.properties";
    static String CLASSIC_MODULE_VERSION_PROPERTY_KEY = CLASSIC_MODULE_NAME + "-version";

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static String getVersionBySelfDeclaredProperties(Class<?> aClass, String moduleName) {
        Properties props = new Properties();
        String propertiesFileName = moduleName + "-version.properties";
        String propertyKey = moduleName + "-version";
        try (InputStream is = aClass.getResourceAsStream(propertiesFileName);){
            if (is != null) {
                props.load(is);
                String string2 = props.getProperty(propertyKey);
                return string2;
            }
            String string = null;
            return string;
        }
        catch (IOException e) {
            return null;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static String getVersionBySelfDeclaredProperties() {
        Properties props = new Properties();
        try (InputStream is = ClassicConstants.class.getResourceAsStream(CLASSIC_MODULE_VERSION_PROPERTIES_FILE);){
            if (is != null) {
                props.load(is);
                String string2 = props.getProperty(CLASSIC_MODULE_VERSION_PROPERTY_KEY);
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

