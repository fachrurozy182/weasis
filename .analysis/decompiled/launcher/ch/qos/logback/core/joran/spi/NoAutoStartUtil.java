/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.spi;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import ch.qos.logback.core.spi.LifeCycle;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class NoAutoStartUtil {
    public static boolean notMarkedWithNoAutoStart(Object o) {
        if (o == null) {
            return false;
        }
        Class<?> clazz = o.getClass();
        NoAutoStart a = NoAutoStartUtil.findAnnotation(clazz, NoAutoStart.class);
        return a == null;
    }

    private static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
        return NoAutoStartUtil.findAnnotation(clazz, annotationType, new HashSet<Annotation>());
    }

    private static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType, Set<Annotation> visited) {
        A foundAnnotation = clazz.getAnnotation(annotationType);
        if (foundAnnotation != null) {
            return foundAnnotation;
        }
        for (Class<?> ifc : clazz.getInterfaces()) {
            A annotation = NoAutoStartUtil.findAnnotation(ifc, annotationType, visited);
            if (annotation == null) continue;
            return annotation;
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass == null || Object.class == superclass) {
            return null;
        }
        return NoAutoStartUtil.findAnnotation(superclass, annotationType, visited);
    }

    public static boolean shouldBeStarted(Object o) {
        if (o instanceof LifeCycle) {
            return NoAutoStartUtil.notMarkedWithNoAutoStart(o);
        }
        return false;
    }
}

