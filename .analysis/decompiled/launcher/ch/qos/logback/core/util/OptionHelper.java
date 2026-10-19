/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.subst.NodeToStringTransformer;
import ch.qos.logback.core.util.DynamicClassLoadingException;
import ch.qos.logback.core.util.IncompatibleClassException;
import ch.qos.logback.core.util.Loader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

public class OptionHelper {
    static final String DELIM_START = "${";
    static final char DELIM_STOP = '}';
    static final String DELIM_DEFAULT = ":-";
    static final int DELIM_START_LEN = 2;
    static final int DELIM_STOP_LEN = 1;
    static final int DELIM_DEFAULT_LEN = 2;
    static final String _IS_UNDEFINED = "_IS_UNDEFINED";

    public static Object instantiateByClassName(String className, Class<?> superClass, Context context) throws IncompatibleClassException, DynamicClassLoadingException {
        ClassLoader classLoader = Loader.getClassLoaderOfObject(context);
        return OptionHelper.instantiateByClassName(className, superClass, classLoader);
    }

    public static Object instantiateByClassNameAndParameter(String className, Class<?> superClass, Context context, Class<?> type, Object param) throws IncompatibleClassException, DynamicClassLoadingException {
        ClassLoader classLoader = Loader.getClassLoaderOfObject(context);
        return OptionHelper.instantiateByClassNameAndParameter(className, superClass, classLoader, type, param);
    }

    public static Object instantiateByClassName(String className, Class<?> superClass, ClassLoader classLoader) throws IncompatibleClassException, DynamicClassLoadingException {
        return OptionHelper.instantiateByClassNameAndParameter(className, superClass, classLoader, null, null);
    }

    public static Object instantiateClassWithSuperclassRestriction(Class<?> classObj, Class<?> superClass) throws IncompatibleClassException, DynamicClassLoadingException {
        if (!superClass.isAssignableFrom(classObj)) {
            throw new IncompatibleClassException(superClass, classObj);
        }
        try {
            return classObj.getConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new DynamicClassLoadingException("Failed to instantiate type " + classObj.getName(), e);
        }
    }

    public static Object instantiateByClassNameAndParameter(String className, Class<?> superClass, ClassLoader classLoader, Class<?> type, Object parameter) throws IncompatibleClassException, DynamicClassLoadingException {
        if (className == null) {
            throw new NullPointerException();
        }
        try {
            Class<?> classObj = null;
            classObj = classLoader.loadClass(className);
            if (!superClass.isAssignableFrom(classObj)) {
                throw new IncompatibleClassException(superClass, classObj);
            }
            if (type == null) {
                return classObj.getConstructor(new Class[0]).newInstance(new Object[0]);
            }
            Constructor<?> constructor = classObj.getConstructor(type);
            return constructor.newInstance(parameter);
        }
        catch (IncompatibleClassException ice) {
            throw ice;
        }
        catch (Throwable t) {
            throw new DynamicClassLoadingException("Failed to instantiate type " + className, t);
        }
    }

    public static String substVars(String val, PropertyContainer pc1) throws ScanException {
        return OptionHelper.substVars(val, pc1, null);
    }

    public static String substVars(String input, PropertyContainer pc0, PropertyContainer pc1) throws ScanException {
        return NodeToStringTransformer.substituteVariable(input, pc0, pc1);
    }

    public static String propertyLookup(String key, PropertyContainer pc1, PropertyContainer pc2) {
        String value = null;
        value = pc1.getProperty(key);
        if (value == null && pc2 != null) {
            value = pc2.getProperty(key);
        }
        if (value == null) {
            value = OptionHelper.getSystemProperty(key, null);
        }
        if (value == null) {
            value = OptionHelper.getEnv(key);
        }
        return value;
    }

    public static String getSystemProperty(String key, String def) {
        try {
            return System.getProperty(key, def);
        }
        catch (SecurityException e) {
            return def;
        }
    }

    public static String getEnv(String key) {
        try {
            return System.getenv(key);
        }
        catch (SecurityException e) {
            return null;
        }
    }

    public static String getSystemProperty(String key) {
        try {
            return System.getProperty(key);
        }
        catch (SecurityException e) {
            return null;
        }
    }

    public static void setSystemProperties(ContextAware contextAware, Properties props) {
        for (Object o : props.keySet()) {
            String key = (String)o;
            String value = props.getProperty(key);
            OptionHelper.setSystemProperty(contextAware, key, value);
        }
    }

    public static void setSystemProperty(ContextAware contextAware, String key, String value) {
        try {
            System.setProperty(key, value);
        }
        catch (SecurityException e) {
            contextAware.addError("Failed to set system property [" + key + "]", e);
        }
    }

    public static Properties getSystemProperties() {
        try {
            return System.getProperties();
        }
        catch (SecurityException e) {
            return new Properties();
        }
    }

    public static String[] extractDefaultReplacement(String key) {
        String[] result = new String[2];
        if (key == null) {
            return result;
        }
        result[0] = key;
        int d = key.indexOf(DELIM_DEFAULT);
        if (d != -1) {
            result[0] = key.substring(0, d);
            result[1] = key.substring(d + 2);
        }
        return result;
    }

    public static Boolean toBooleanObject(String value) {
        if (OptionHelper.isNullOrEmptyOrAllSpaces(value)) {
            return null;
        }
        String trimmedVal = value.trim();
        if ("true".equalsIgnoreCase(trimmedVal)) {
            return true;
        }
        if ("false".equalsIgnoreCase(trimmedVal)) {
            return false;
        }
        return null;
    }

    public static boolean toBoolean(String value, boolean defaultVal) {
        if (value == null) {
            return defaultVal;
        }
        String trimmedVal = value.trim();
        if ("true".equalsIgnoreCase(trimmedVal)) {
            return true;
        }
        if ("false".equalsIgnoreCase(trimmedVal)) {
            return false;
        }
        return defaultVal;
    }

    public static boolean containsUnicodeEscape(String value) {
        return value.contains("\\u") || value.contains("\\U");
    }

    public static boolean isEmpty(String str) {
        return OptionHelper.isNullOrEmptyOrAllSpaces(str);
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isNullOrEmptyOrAllSpaces(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static final boolean isNullOrEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static final boolean isNotEmtpy(Object[] array) {
        return !OptionHelper.isNullOrEmpty(array);
    }
}

