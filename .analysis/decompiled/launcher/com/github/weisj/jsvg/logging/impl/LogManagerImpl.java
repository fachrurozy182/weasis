/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  aQute.bnd.annotation.spi.ServiceConsumer
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.logging.impl;

import aQute.bnd.annotation.spi.ServiceConsumer;
import com.github.weisj.jsvg.logging.LogManager;
import com.github.weisj.jsvg.logging.Logger;
import com.github.weisj.jsvg.logging.impl.CallerFinder;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ServiceConsumer(value=LogManager.class)
public final class LogManagerImpl {
    @NotNull
    private static final LogManager logManager;

    private LogManagerImpl() {
    }

    @NotNull
    public static LogManager logManager() {
        return logManager;
    }

    static {
        Iterator<LogManager> managers = ServiceLoader.load(LogManager.class).iterator();
        logManager = managers.hasNext() ? managers.next() : new DefaultLogManager();
    }

    private static class DefaultLogManager
    implements LogManager {
        private DefaultLogManager() {
        }

        @Override
        @NotNull
        public Logger createLogger(@NotNull String name) {
            return new JavaUtilLoggerWrapper(java.util.logging.Logger.getLogger(name));
        }
    }

    private static final class WrapperLogRecord
    extends LogRecord {
        private static final String DISPATCHER_CLASS_NAME = JavaUtilLoggerWrapper.class.getName();
        private boolean needToInferCaller = true;

        public WrapperLogRecord(Level level, String msg) {
            super(level, msg);
        }

        @Override
        public String getSourceClassName() {
            if (this.needToInferCaller) {
                this.customInferCaller();
            }
            return super.getSourceClassName();
        }

        @Override
        public String getSourceMethodName() {
            if (this.needToInferCaller) {
                this.customInferCaller();
            }
            return super.getSourceMethodName();
        }

        private void customInferCaller() {
            this.needToInferCaller = false;
            new CallerFinder(DISPATCHER_CLASS_NAME).findCaller().ifPresent(st -> {
                this.setSourceClassName(st.getClassName());
                this.setSourceMethodName(st.getMethodName());
            });
        }
    }

    private static final class JavaUtilLoggerWrapper
    implements Logger {
        @NotNull
        private final java.util.logging.Logger logger;

        private JavaUtilLoggerWrapper(@NotNull java.util.logging.Logger logger) {
            this.logger = logger;
        }

        @Override
        public void log(Logger.Level level, @Nullable String message) {
            this.logger.log(new WrapperLogRecord(JavaUtilLoggerWrapper.toJavaUtilLevel(level), message));
        }

        @Override
        public void log(Logger.Level level, @Nullable String message, @NotNull Throwable e) {
            WrapperLogRecord logRecord = new WrapperLogRecord(JavaUtilLoggerWrapper.toJavaUtilLevel(level), message);
            logRecord.setThrown(e);
            this.logger.log(logRecord);
        }

        @Override
        public void log(Logger.Level level, @NotNull Supplier<@Nullable String> messageSupplier) {
            Level javaUtilLevel = JavaUtilLoggerWrapper.toJavaUtilLevel(level);
            if (!this.logger.isLoggable(javaUtilLevel)) {
                return;
            }
            this.logger.log(new WrapperLogRecord(javaUtilLevel, messageSupplier.get()));
        }

        static Level toJavaUtilLevel(@NotNull Logger.Level level) {
            switch (level) {
                case DEBUG: {
                    return Level.FINE;
                }
                case INFO: {
                    return Level.INFO;
                }
                case WARNING: {
                    return Level.WARNING;
                }
                case ERROR: {
                    return Level.SEVERE;
                }
            }
            throw new IllegalArgumentException("Unknown log level: " + (Object)((Object)level));
        }
    }
}

