/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.spi;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.Util;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

public class LogbackServiceProvider
implements SLF4JServiceProvider {
    static final String NULL_CS_URL = "https://logback.qos.ch/codes.html#null_CS";
    public static String REQUESTED_API_VERSION = "2.0.99";
    private LoggerContext defaultLoggerContext = new LoggerContext();
    private IMarkerFactory markerFactory = new BasicMarkerFactory();
    private LogbackMDCAdapter mdcAdapter = new LogbackMDCAdapter();

    @Override
    public void initialize() {
        this.defaultLoggerContext.setName("default");
        this.defaultLoggerContext.setMDCAdapter(this.mdcAdapter);
        this.initializeLoggerContext();
        this.defaultLoggerContext.start();
    }

    private void initializeLoggerContext() {
        try {
            try {
                new ContextInitializer(this.defaultLoggerContext).autoConfig();
            }
            catch (JoranException je) {
                Util.report("Failed to auto configure default logger context", je);
            }
            if (!StatusUtil.contextHasStatusListener(this.defaultLoggerContext)) {
                StatusPrinter.printInCaseOfErrorsOrWarnings(this.defaultLoggerContext);
            }
        }
        catch (Exception t) {
            Util.report("Failed to instantiate [" + LoggerContext.class.getName() + "]", t);
        }
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return this.defaultLoggerContext;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return this.markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return this.mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return REQUESTED_API_VERSION;
    }
}

