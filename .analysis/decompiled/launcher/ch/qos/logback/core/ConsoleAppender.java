/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core;

import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.joran.spi.ConsoleTarget;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.ReentryGuard;
import ch.qos.logback.core.util.ReentryGuardFactory;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ConsoleAppender<E>
extends OutputStreamAppender<E> {
    protected ConsoleTarget target = ConsoleTarget.SystemOut;
    protected boolean withJansi = false;
    public static final String JLINE_JANSI_ANSI_CONSOLE_CLASS_NAME = "org.jline.jansi.AnsiConsole";
    public static final String FUSESOURCE_JANSI_ANSI_CONSOLE_CLASS_NAME = "org.fusesource.jansi.AnsiConsole";
    private static final String[] ANSI_CONSOLE_CLASS_NAMES = new String[]{"org.jline.jansi.AnsiConsole", "org.fusesource.jansi.AnsiConsole"};
    protected String preferredJansiClassName = null;
    private static final String JANSI2_OUT_METHOD_NAME = "out";
    private static final String JANSI2_ERR_METHOD_NAME = "err";
    private static final String WRAP_SYSTEM_OUT_METHOD_NAME = "wrapSystemOut";
    private static final String WRAP_SYSTEM_ERR_METHOD_NAME = "wrapSystemErr";
    private static final String SYSTEM_INSTALL_METHOD_NAME = "systemInstall";
    private static final Class<?>[] ARGUMENT_TYPES = new Class[]{PrintStream.class};
    private static final String CONSOLE_APPENDER_WARNING_URL = "https://logback.qos.ch/codes.html#slowConsole";

    public void setTarget(String value) {
        ConsoleTarget t = ConsoleTarget.findByName(value.trim());
        if (t == null) {
            this.targetWarn(value);
        } else {
            this.target = t;
        }
    }

    public String getTarget() {
        return this.target.getName();
    }

    public String getPreferredJansiClassName() {
        return this.preferredJansiClassName;
    }

    public void setPreferredJansiClassName(String preferredJansiClassName) {
        this.preferredJansiClassName = preferredJansiClassName;
    }

    private boolean isValidPreferredJansiClassName(String className) {
        return JLINE_JANSI_ANSI_CONSOLE_CLASS_NAME.equals(className) || FUSESOURCE_JANSI_ANSI_CONSOLE_CLASS_NAME.equals(className);
    }

    private void preferredJansiClassNameWarn(String val) {
        WarnStatus status = new WarnStatus("[" + val + "] should be one of " + Arrays.toString(ANSI_CONSOLE_CLASS_NAMES), this);
        status.add(new WarnStatus("Ignoring preferredJansiClassName, using default probing order.", this));
        this.addStatus(status);
    }

    private void targetWarn(String val) {
        WarnStatus status = new WarnStatus("[" + val + "] should be one of " + Arrays.toString((Object[])ConsoleTarget.values()), this);
        status.add(new WarnStatus("Using previously set target, System.out by default.", this));
        this.addStatus(status);
    }

    @Override
    public void start() {
        this.addInfo("NOTE: Writing to the console can be slow. Try to avoid logging to the ");
        this.addInfo("console in production environments, especially in high volume systems.");
        this.addInfo("See also https://logback.qos.ch/codes.html#slowConsole");
        OutputStream targetStream = this.target.getStream();
        if (this.withJansi) {
            targetStream = this.wrapWithJansi(targetStream);
        }
        this.setOutputStream(targetStream);
        super.start();
    }

    @Override
    protected ReentryGuard buildReentryGuard() {
        return ReentryGuardFactory.makeGuard(ReentryGuardFactory.GuardType.THREAD_LOCAL);
    }

    private OutputStream wrapWithJansi(OutputStream targetStream) {
        try {
            this.addInfo("Enabling JANSI AnsiPrintStream for the console.");
            ClassLoader classLoader = Loader.getClassLoaderOfObject(this.context);
            Class<?> classObj = this.loadAnsiConsoleClass(classLoader);
            Method systemInstallMethod = classObj.getMethod(SYSTEM_INSTALL_METHOD_NAME, new Class[0]);
            if (systemInstallMethod != null) {
                systemInstallMethod.invoke(null, new Object[0]);
            }
            String methodNameJansi2 = this.target == ConsoleTarget.SystemOut ? JANSI2_OUT_METHOD_NAME : JANSI2_ERR_METHOD_NAME;
            Optional<Method> optOutMethod = Arrays.stream(classObj.getMethods()).filter(m -> m.getName().equals(methodNameJansi2)).filter(m -> m.getParameters().length == 0).filter(m -> Modifier.isStatic(m.getModifiers())).filter(m -> PrintStream.class.isAssignableFrom(m.getReturnType())).findAny();
            if (optOutMethod.isPresent()) {
                Method outMethod = optOutMethod.orElseThrow(() -> new NoSuchElementException("No out/err method present"));
                return (PrintStream)outMethod.invoke(null, new Object[0]);
            }
            String methodName = this.target == ConsoleTarget.SystemOut ? WRAP_SYSTEM_OUT_METHOD_NAME : WRAP_SYSTEM_ERR_METHOD_NAME;
            Method method = classObj.getMethod(methodName, ARGUMENT_TYPES);
            return (OutputStream)method.invoke(null, new PrintStream(targetStream));
        }
        catch (Exception e) {
            this.addWarn("Failed to create AnsiPrintStream. Falling back on the default stream.", e);
            return targetStream;
        }
    }

    Class<?> loadAnsiConsoleClass(ClassLoader classLoader) throws ClassNotFoundException {
        if (this.preferredJansiClassName != null) {
            if (this.isValidPreferredJansiClassName(this.preferredJansiClassName)) {
                return classLoader.loadClass(this.preferredJansiClassName);
            }
            this.preferredJansiClassNameWarn(this.preferredJansiClassName);
        }
        ClassNotFoundException lastException = null;
        for (String className : ANSI_CONSOLE_CLASS_NAMES) {
            try {
                return classLoader.loadClass(className);
            }
            catch (ClassNotFoundException e) {
                lastException = e;
            }
        }
        throw lastException;
    }

    public boolean isWithJansi() {
        return this.withJansi;
    }

    public void setWithJansi(boolean withJansi) {
        this.withJansi = withJansi;
    }
}

