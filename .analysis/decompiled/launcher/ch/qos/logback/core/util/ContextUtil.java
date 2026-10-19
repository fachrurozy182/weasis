/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.hook.ShutdownHook;
import ch.qos.logback.core.spi.ContextAwareBase;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ContextUtil
extends ContextAwareBase {
    static final String GROOVY_RUNTIME_PACKAGE = "org.codehaus.groovy.runtime";

    public ContextUtil(Context context) {
        this.setContext(context);
    }

    public void addProperties(Properties props) {
        if (props == null) {
            return;
        }
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            String key = (String)e.getKey();
            this.context.putProperty(key, (String)e.getValue());
        }
    }

    public void addGroovyPackages(List<String> frameworkPackages) {
        this.addFrameworkPackage(frameworkPackages, GROOVY_RUNTIME_PACKAGE);
    }

    public void addFrameworkPackage(List<String> frameworkPackages, String packageName) {
        if (!frameworkPackages.contains(packageName)) {
            frameworkPackages.add(packageName);
        }
    }

    public void addOrReplaceShutdownHook(ShutdownHook hook) {
        Runtime runtime = Runtime.getRuntime();
        Thread oldShutdownHookThread = (Thread)this.context.getObject("SHUTDOWN_HOOK");
        if (oldShutdownHookThread != null) {
            this.addInfo("Removing old shutdown hook from JVM runtime");
            runtime.removeShutdownHook(oldShutdownHookThread);
        }
        Thread hookThread = new Thread((Runnable)hook, "Logback shutdown hook [" + this.context.getName() + "]");
        this.addInfo("Registering shutdown hook with JVM runtime.");
        this.context.putObject("SHUTDOWN_HOOK", hookThread);
        runtime.addShutdownHook(hookThread);
    }
}

