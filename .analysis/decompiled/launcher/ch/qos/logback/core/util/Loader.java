/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.util.OptionHelper;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class Loader {
    static final String TSTR = "Caught Exception while in Loader.getResource. This may be innocuous.";
    private static boolean ignoreTCL = false;
    public static final String IGNORE_TCL_PROPERTY_NAME = "logback.ignoreTCL";

    public static ClassLoader systemClassloaderIfNull(ClassLoader cl) {
        if (cl == null) {
            return ClassLoader.getSystemClassLoader();
        }
        return cl;
    }

    public static Set<URL> getResources(String resource, ClassLoader classLoader) throws IOException {
        HashSet<URL> urlSet = new HashSet<URL>();
        Enumeration<URL> urlEnum = classLoader.getResources(resource);
        while (urlEnum.hasMoreElements()) {
            URL url = urlEnum.nextElement();
            urlSet.add(url);
        }
        return urlSet;
    }

    public static URL getResource(String resource, ClassLoader classLoader) {
        try {
            return classLoader.getResource(resource);
        }
        catch (Throwable t) {
            return null;
        }
    }

    public static URL getResourceBySelfClassLoader(String resource) {
        return Loader.getResource(resource, Loader.getClassLoaderOfClass(Loader.class));
    }

    public static ClassLoader getTCL() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static Class<?> loadClass(String clazz, Context context) throws ClassNotFoundException {
        ClassLoader cl = Loader.getClassLoaderOfObject(context);
        return cl.loadClass(clazz);
    }

    public static ClassLoader getClassLoaderOfObject(Object o) {
        if (o == null) {
            throw new NullPointerException("Argument cannot be null");
        }
        return Loader.getClassLoaderOfClass(o.getClass());
    }

    public static boolean isClassLoadable(String className, Context context) {
        try {
            Class<?> aClass = Loader.loadClass(className, context);
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static ClassLoader getClassLoaderOfClass(Class<?> clazz) {
        ClassLoader cl = clazz.getClassLoader();
        return Loader.systemClassloaderIfNull(cl);
    }

    public static Class<?> loadClass(String clazz) throws ClassNotFoundException {
        if (ignoreTCL) {
            return Class.forName(clazz);
        }
        try {
            return Loader.getTCL().loadClass(clazz);
        }
        catch (Throwable e) {
            return Class.forName(clazz);
        }
    }

    static {
        String ignoreTCLProp = OptionHelper.getSystemProperty(IGNORE_TCL_PROPERTY_NAME, null);
        if (ignoreTCLProp != null) {
            ignoreTCL = OptionHelper.toBoolean(ignoreTCLProp, true);
        }
    }
}

