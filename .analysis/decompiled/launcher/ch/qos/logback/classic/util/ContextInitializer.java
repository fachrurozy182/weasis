/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ConfiguratorRank;
import ch.qos.logback.classic.util.ClassicEnvUtil;
import ch.qos.logback.classic.util.ClassicVersionUtil;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareImpl;
import ch.qos.logback.core.util.CoreVersionUtil;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.StatusListenerConfigHelper;
import ch.qos.logback.core.util.VersionUtil;
import java.util.Comparator;
import java.util.List;

public class ContextInitializer {
    public static final String AUTOCONFIG_FILE = "logback.xml";
    public static final String TEST_AUTOCONFIG_FILE = "logback-test.xml";
    public static final String CONFIG_FILE_PROPERTY = "logback.configurationFile";
    String[] INTERNAL_CONFIGURATOR_CLASSNAME_LIST = new String[]{"ch.qos.logback.classic.util.DefaultJoranConfigurator", "ch.qos.logback.classic.BasicConfigurator"};
    final LoggerContext loggerContext;
    final ContextAware contextAware;
    Comparator<Configurator> rankComparator = new Comparator<Configurator>(){

        @Override
        public int compare(Configurator c1, Configurator c2) {
            ConfiguratorRank r1 = c1.getClass().getAnnotation(ConfiguratorRank.class);
            ConfiguratorRank r2 = c2.getClass().getAnnotation(ConfiguratorRank.class);
            int value1 = r1 == null ? 20 : r1.value();
            int value2 = r2 == null ? 20 : r2.value();
            int result = ContextInitializer.this.compareRankValue(value1, value2);
            return -result;
        }
    };

    public ContextInitializer(LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
        this.contextAware = new ContextAwareImpl(loggerContext, this);
    }

    public void autoConfig() throws JoranException {
        this.autoConfig(Configurator.class.getClassLoader());
    }

    public void autoConfig(ClassLoader classLoader) throws JoranException {
        classLoader = Loader.systemClassloaderIfNull(classLoader);
        this.checkVersions();
        StatusListenerConfigHelper.installIfAsked(this.loggerContext);
        List<Configurator> configuratorList = ClassicEnvUtil.loadFromServiceLoader(Configurator.class, classLoader);
        configuratorList.sort(this.rankComparator);
        if (configuratorList.isEmpty()) {
            this.contextAware.addInfo("No custom configurators were discovered as a service.");
        } else {
            this.printConfiguratorOrder(configuratorList);
        }
        int authConfiguratorCount = this.authConfiguratorCount(configuratorList);
        if (authConfiguratorCount > 1) {
            this.contextAware.addError("Multiple custom configurators of rank AUTHENTICATING were discovered as a service. This is not allowed.");
            this.contextAware.addError("Ensure that at most one AUTHENTICATING configurator exists on the classpath.");
            return;
        }
        for (Configurator c : configuratorList) {
            if (this.invokeConfigure(c) != Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY) continue;
            return;
        }
        for (String configuratorClassName : this.INTERNAL_CONFIGURATOR_CLASSNAME_LIST) {
            this.contextAware.addInfo("Trying to configure with " + configuratorClassName);
            Configurator c = this.instantiateConfiguratorByClassName(configuratorClassName, classLoader);
            if (c == null || this.invokeConfigure(c) != Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY) continue;
            return;
        }
    }

    private int authConfiguratorCount(List<Configurator> configuratorList) {
        int count = 0;
        for (Configurator c : configuratorList) {
            if (c.getClass().getAnnotation(ConfiguratorRank.class) == null || c.getClass().getAnnotation(ConfiguratorRank.class).value() != 100) continue;
            this.contextAware.addInfo("Configurator of rank AUTHENTICATING discovered: " + c.getClass().getName());
            ++count;
        }
        return count;
    }

    private void checkVersions() {
        try {
            String coreVersion = CoreVersionUtil.getCoreVersionBySelfDeclaredProperties();
            String classicVersion = ClassicVersionUtil.getVersionBySelfDeclaredProperties();
            VersionUtil.checkForVersionEquality(this.loggerContext, coreVersion, classicVersion, "logback-core", "logback-classic");
        }
        catch (NoClassDefFoundError e) {
            this.contextAware.addWarn("Missing ch.logback.core.util.VersionUtil class on classpath. The version of logback-core is probably older than 1.5.26.");
        }
        catch (NoSuchMethodError e) {
            this.contextAware.addWarn(e.toString());
            this.contextAware.addWarn("The version of logback-core is probably older than 1.5.26.");
        }
    }

    private Configurator instantiateConfiguratorByClassName(String configuratorClassName, ClassLoader classLoader) {
        try {
            Class<?> classObj = classLoader.loadClass(configuratorClassName);
            return (Configurator)classObj.getConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (ReflectiveOperationException e) {
            this.contextAware.addInfo("Instantiation failure: " + e.toString());
            return null;
        }
    }

    private Configurator.ExecutionStatus invokeConfigure(Configurator configurator) {
        try {
            long start = System.currentTimeMillis();
            this.contextAware.addInfo("Constructed configurator of type " + String.valueOf(configurator.getClass()));
            configurator.setContext(this.loggerContext);
            Configurator.ExecutionStatus status = configurator.configure(this.loggerContext);
            this.printDuration(start, configurator, status);
            return status;
        }
        catch (Exception e) {
            throw new LogbackException(String.format("Failed to initialize or to run Configurator: %s", configurator != null ? configurator.getClass().getCanonicalName() : "null"), e);
        }
    }

    private void printConfiguratorOrder(List<Configurator> configuratorList) {
        this.contextAware.addInfo("Here is a list of configurators discovered as a service, by rank: ");
        for (Configurator c : configuratorList) {
            this.contextAware.addInfo("  " + c.getClass().getName());
        }
        this.contextAware.addInfo("They will be invoked in order until ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY is returned.");
    }

    private void printDuration(long start, Configurator configurator, Configurator.ExecutionStatus executionStatus) {
        long end = System.currentTimeMillis();
        long diff = end - start;
        this.contextAware.addInfo(configurator.getClass().getName() + ".configure() call lasted " + diff + " milliseconds. ExecutionStatus=" + String.valueOf((Object)executionStatus));
    }

    private Configurator.ExecutionStatus attemptConfigurationUsingJoranUsingReflexion(ClassLoader classLoader) {
        try {
            Class<?> djcClass = classLoader.loadClass("ch.qos.logback.classic.util.DefaultJoranConfigurator");
            Configurator c = (Configurator)djcClass.newInstance();
            c.setContext(this.loggerContext);
            return c.configure(this.loggerContext);
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            this.contextAware.addError("unexpected exception while instantiating DefaultJoranConfigurator", e);
            return Configurator.ExecutionStatus.INVOKE_NEXT_IF_ANY;
        }
    }

    private int compareRankValue(int value1, int value2) {
        if (value1 > value2) {
            return 1;
        }
        if (value1 == value2) {
            return 0;
        }
        return -1;
    }
}

