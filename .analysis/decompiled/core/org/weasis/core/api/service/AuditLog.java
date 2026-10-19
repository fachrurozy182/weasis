/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  ch.qos.logback.classic.Level
 *  ch.qos.logback.classic.Logger
 *  ch.qos.logback.classic.LoggerContext
 *  ch.qos.logback.classic.encoder.PatternLayoutEncoder
 *  ch.qos.logback.classic.spi.ILoggingEvent
 *  ch.qos.logback.core.Appender
 *  ch.qos.logback.core.ConsoleAppender
 *  ch.qos.logback.core.Context
 *  ch.qos.logback.core.encoder.Encoder
 *  ch.qos.logback.core.rolling.FixedWindowRollingPolicy
 *  ch.qos.logback.core.rolling.RollingFileAppender
 *  ch.qos.logback.core.rolling.RollingPolicy
 *  ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy
 *  ch.qos.logback.core.rolling.TriggeringPolicy
 *  ch.qos.logback.core.util.FileSize
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.core.api.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TriggeringPolicy;
import ch.qos.logback.core.util.FileSize;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.service.WProperties;
import org.weasis.core.util.StringUtil;

public class AuditLog {
    public static final Logger LOGGER = LoggerFactory.getLogger(AuditLog.class);
    public static final String NAME_CONSOLE = "CONSOLE";
    public static final String NAME_ROLLING_FILES = "ROLLING_FILES";
    public static final String NAME_AUDIT = "AUDIT";
    public static final String LOG_LEVEL = "org.apache.sling.commons.log.level";
    public static final String LOG_STACKTRACE_LIMIT = "org.apache.sling.commons.log.stack.limit";
    public static final String LOG_FILE_ACTIVATION = "org.apache.sling.commons.log.file.activate";
    public static final String LOG_FILE = "org.apache.sling.commons.log.file";
    public static final String LOG_FILE_NUMBER = "org.apache.sling.commons.log.file.number";
    public static final String LOG_FILE_SIZE = "org.apache.sling.commons.log.file.size";
    public static final String LOG_PATTERN = "org.apache.sling.commons.log.pattern";
    public static final String DEFAULT_LOG_PATTERN = "%d{dd.MM.yyyy HH:mm:ss.SSS} *%-5level* [%thread] %logger{36}: %msg%ex{3}%n";
    public static final String LOG_LOGGERS = "org.apache.sling.commons.log.names";
    public static final String MARKER_PERF = "*PERF*";
    public static final String LOG_CLASSES = "org.apache.sling.commons.log.classes";
    public static final String LOG_FILE_PATTERN = "org.apache.sling.commons.log.file.pattern";
    public static final String LOG_FOLDER_PATH = String.valueOf(AppProperties.WEASIS_PATH) + File.separator + "log" + File.separator;

    public static WProperties getAuditProperties() {
        WProperties p = new WProperties();
        p.putBooleanProperty(LOG_FILE_ACTIVATION, true);
        p.setProperty(LOG_FILE, LOG_FOLDER_PATH + "audit-" + AppProperties.WEASIS_USER + ".log");
        p.setProperty(LOG_FILE_NUMBER, "10");
        p.setProperty(LOG_FILE_SIZE, "20MB");
        p.setProperty(LOG_CLASSES, "org.weasis.core.api.service.AuditLog");
        p.setProperty(LOG_FILE_PATTERN, LOG_FOLDER_PATH + "audit-" + AppProperties.WEASIS_USER + ".%i.log.zip");
        return p;
    }

    public static void applyConfig(WProperties prefs, LoggerContext loggerContext) {
        String pattern = prefs.getProperty(LOG_PATTERN);
        String limit = prefs.getProperty(LOG_STACKTRACE_LIMIT);
        PatternLayoutEncoder encoder = AuditLog.getPatternLayoutEncoder(loggerContext, pattern, limit);
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger((String)"ROOT");
        AuditLog.createOrUpdateConsoleAppender(loggerContext, logger, encoder);
        if (StringUtil.hasText((CharSequence)prefs.getProperty(LOG_FILE))) {
            prefs.setProperty(LOG_FILE_PATTERN, "default.%i.log.zip");
            RollingFileAppender<ILoggingEvent> rollingFileAppender = AuditLog.getRollingFilesAppender(logger, NAME_ROLLING_FILES);
            AuditLog.updateRollingFilesAppender(rollingFileAppender, loggerContext, prefs, encoder);
        } else {
            logger.detachAppender(NAME_ROLLING_FILES);
        }
    }

    public static void createOrUpdateConsoleAppender(LoggerContext loggerContext, ch.qos.logback.classic.Logger logger, PatternLayoutEncoder encoder) {
        Appender appender = logger.getAppender(NAME_CONSOLE);
        if (appender instanceof ConsoleAppender) {
            ConsoleAppender consoleAppender = (ConsoleAppender)appender;
            consoleAppender.setEncoder((Encoder)encoder);
        } else {
            ConsoleAppender appender2 = new ConsoleAppender();
            appender2.setContext((Context)loggerContext);
            appender2.setName(NAME_CONSOLE);
            appender2.setEncoder((Encoder)encoder);
            appender2.start();
            logger.addAppender((Appender)appender2);
        }
    }

    public static RollingFileAppender<ILoggingEvent> getRollingFilesAppender(ch.qos.logback.classic.Logger logger, String name) {
        Appender appender = logger.getAppender(name);
        if (appender instanceof RollingFileAppender) {
            RollingFileAppender rollingAppender = (RollingFileAppender)appender;
            rollingAppender.stop();
            return rollingAppender;
        }
        RollingFileAppender appender2 = new RollingFileAppender();
        appender2.setName(name);
        return appender2;
    }

    public static void updateRollingFilesAppender(RollingFileAppender<ILoggingEvent> appender, LoggerContext loggerContext, WProperties prefs, PatternLayoutEncoder encoder) {
        String logActivation = prefs.getProperty(LOG_FILE);
        if (StringUtil.hasText((CharSequence)logActivation)) {
            Level level = Level.toLevel((String)prefs.getProperty(LOG_LEVEL), (Level)Level.INFO);
            int nbFiles = prefs.getIntProperty(LOG_FILE_NUMBER, 10);
            FileSize fileSize = FileSize.valueOf((String)prefs.getProperty(LOG_FILE_SIZE, "10MB"));
            String classes = prefs.getProperty(LOG_CLASSES, "ROOT");
            String filePattern = LOG_FOLDER_PATH + prefs.getProperty(LOG_FILE_PATTERN, "default.%i.log.zip");
            appender.setContext((Context)loggerContext);
            appender.setEncoder((Encoder)encoder);
            appender.setFile(logActivation);
            FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
            rollingPolicy.setContext((Context)loggerContext);
            rollingPolicy.setParent(appender);
            rollingPolicy.setFileNamePattern(filePattern);
            rollingPolicy.setMinIndex(1);
            rollingPolicy.setMaxIndex(nbFiles);
            rollingPolicy.start();
            SizeBasedTriggeringPolicy triggeringPolicy = new SizeBasedTriggeringPolicy();
            triggeringPolicy.setMaxFileSize(fileSize);
            triggeringPolicy.start();
            appender.setRollingPolicy((RollingPolicy)rollingPolicy);
            appender.setTriggeringPolicy((TriggeringPolicy)triggeringPolicy);
            appender.start();
            ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger((String)classes);
            logger.setLevel(level);
            logger.addAppender(appender);
        }
    }

    public static PatternLayoutEncoder getPatternLayoutEncoder(LoggerContext loggerContext, String pattern, String stackLimit) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext((Context)loggerContext);
        String limit = StringUtil.hasText((CharSequence)stackLimit) && !"-1".equals(stackLimit) ? stackLimit : "full";
        String layout = StringUtil.hasText((CharSequence)pattern) ? pattern : DEFAULT_LOG_PATTERN;
        String str = layout.replaceAll("ex\\{\\d+}", "ex{" + limit + "}");
        encoder.setPattern(str);
        encoder.start();
        return encoder;
    }

    public static PatternLayoutEncoder getPatternLayoutEncoder(LoggerContext loggerContext, String pattern) {
        return AuditLog.getPatternLayoutEncoder(loggerContext, pattern, "");
    }

    public static void logError(Logger log, Throwable t, String message) {
        if (log.isDebugEnabled()) {
            log.error(message, t);
        } else {
            log.error(t.getMessage());
        }
    }

    public static enum LEVEL {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR;


        public static LEVEL getLevel(String level) {
            try {
                return LEVEL.valueOf(level);
            }
            catch (Exception exception) {
                return INFO;
            }
        }
    }
}

