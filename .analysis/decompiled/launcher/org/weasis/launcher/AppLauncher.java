/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.launcher;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.launcher.PlatformCertificateLoader;
import org.weasis.launcher.Singleton;
import org.weasis.launcher.WeasisLauncher;
import org.weasis.pref.ConfigData;

public class AppLauncher
extends WeasisLauncher
implements Singleton.SingletonApp {
    private static final Logger LOGGER;

    public AppLauncher(ConfigData configData) {
        super(configData);
    }

    public static void main(String[] argv) throws Exception {
        LOGGER.info("*PERF* JVM start, type:INIT time:{}", (Object)(System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()));
        PlatformCertificateLoader.startAsyncSetup();
        WeasisLauncher.Type launchType = WeasisLauncher.Type.NATIVE;
        long startTime = System.currentTimeMillis();
        ConfigData configData = new ConfigData(argv);
        LOGGER.info("*PERF* Loading config, type:INIT time:{}", (Object)(System.currentTimeMillis() - startTime));
        if (!Singleton.invoke(configData)) {
            AppLauncher instance = new AppLauncher(configData);
            Singleton.start(instance, configData.getSourceID());
            instance.launch(launchType);
        }
    }

    @Override
    public void newActivation(List<String> arguments) {
        this.waitWhenStarted();
        if (this.mTracker != null) {
            this.executeCommands(arguments, null);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void waitWhenStarted() {
        AppLauncher appLauncher = this;
        synchronized (appLauncher) {
            int loop = 0;
            boolean runLoop = true;
            while (runLoop && !this.frameworkLoaded) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100L);
                    if (++loop <= 300) continue;
                    runLoop = false;
                }
                catch (InterruptedException e) {
                    runLoop = false;
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public boolean canStartNewActivation(Properties prop) {
        boolean sameUser = this.configData.isPropertyValueSimilar("weasis.user", prop.getProperty("weasis.user"));
        boolean sameConfig = this.configData.isPropertyValueSimilar("weasis.config.hash", prop.getProperty("weasis.config.hash"));
        return sameUser && sameConfig;
    }

    @Override
    protected void stopSingletonServer() {
        Singleton.stop();
    }

    static {
        String home = System.getProperty("user.home", "");
        File bootLog = new File(home + File.separator + ".weasis" + File.separator + "log");
        bootLog.mkdirs();
        LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
        loggerContext.reset();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{dd.MM.yyyy HH:mm:ss.SSS} *%-5level* %msg%n");
        encoder.start();
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<ILoggingEvent>();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setName("CONSOLE");
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();
        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
        rollingFileAppender.setContext(loggerContext);
        rollingFileAppender.setName("BOOT_ROLLING_FILE");
        rollingFileAppender.setEncoder(encoder);
        rollingFileAppender.setFile(bootLog.getPath() + "/boot.log");
        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setFileNamePattern(bootLog.getPath() + "/boot.%i.log.zip");
        rollingPolicy.setMinIndex(1);
        rollingPolicy.setMaxIndex(3);
        rollingPolicy.start();
        SizeBasedTriggeringPolicy triggeringPolicy = new SizeBasedTriggeringPolicy();
        triggeringPolicy.setMaxFileSize(FileSize.valueOf("3MB"));
        triggeringPolicy.start();
        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setTriggeringPolicy(triggeringPolicy);
        rollingFileAppender.start();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger("ROOT");
        logger.setLevel(Level.DEBUG);
        logger.setAdditive(false);
        logger.addAppender((Appender<ILoggingEvent>)consoleAppender);
        logger.addAppender((Appender<ILoggingEvent>)rollingFileAppender);
        LOGGER = LoggerFactory.getLogger(AppLauncher.class);
    }
}

