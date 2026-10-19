/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.util;

import java.net.URL;

public class ResourceLocator {
    public static String resourceURL(String name) {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        URL url = tcl.getResource(name);
        return url != null ? url.toString() : null;
    }

    public static String getResource(String resource, Object source) {
        Class<?> c = source == null ? null : source.getClass();
        return ResourceLocator.getResource(resource, c);
    }

    public static String getResource(String resource, Class<?> c) {
        URL url = ResourceLocator.getResourceURL(resource, c);
        return url != null ? url.toString() : null;
    }

    public static URL getResourceURL(String resource, Object source) {
        Class<?> c = source == null ? null : source.getClass();
        return ResourceLocator.getResourceURL(resource, c);
    }

    public static URL getResourceURL(String resource, Class<?> c) {
        ClassLoader classLoader;
        if (c != null && (classLoader = c.getClassLoader()) != null) {
            return classLoader.getResource(resource);
        }
        classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            return classLoader.getResource(resource);
        }
        return ClassLoader.getSystemResource(resource);
    }
}

