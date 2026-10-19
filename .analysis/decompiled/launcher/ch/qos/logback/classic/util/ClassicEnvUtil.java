/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.util;

import ch.qos.logback.core.util.EnvUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ClassicEnvUtil {
    public static boolean isGroovyAvailable() {
        return EnvUtil.isClassAvailable(ClassicEnvUtil.class, "groovy.lang.Binding");
    }

    public static <T> List<T> loadFromServiceLoader(Class<T> c, ClassLoader classLoader) {
        ServiceLoader<T> loader = ServiceLoader.load(c, classLoader);
        ArrayList<T> listOfT = new ArrayList<T>();
        for (T t : loader) {
            listOfT.add(t);
        }
        return listOfT;
    }
}

