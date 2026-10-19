/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.joran;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.model.util.VariableSubstitutionsHelper;
import ch.qos.logback.core.spi.ContextAwareBase;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class PropertiesConfigurator
extends ContextAwareBase {
    static Comparator<String> LENGTH_COMPARATOR = new Comparator<String>(){

        @Override
        public int compare(String o1, String o2) {
            int len1;
            int len2 = o2 == null ? 0 : o2.length();
            int diff = len2 - (len1 = o1 == null ? 0 : o1.length());
            if (diff != 0) {
                return diff;
            }
            return o2.compareTo(o1);
        }
    };
    static final String LOGBACK_PREFIX = "logback";
    static final String LOGBACK_ROOT_LOGGER_PREFIX = "logback.root";
    static final int LOGBACK_ROOT_LOGGER_PREFIX_LENGTH = "logback.root".length();
    public static final String LOGBACK_LOGGER_PREFIX = "logback.logger.";
    static final int LOGBACK_LOGGER_PREFIX_LENGTH = "logback.logger.".length();
    VariableSubstitutionsHelper variableSubstitutionsHelper;

    LoggerContext getLoggerContext() {
        return (LoggerContext)this.getContext();
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
    }

    public void doConfigure(URL url) throws JoranException {
        try {
            URLConnection urlConnection = url.openConnection();
            urlConnection.setUseCaches(false);
            InputStream in = urlConnection.getInputStream();
            this.doConfigure(in);
        }
        catch (IOException ioe) {
            String errMsg = "Could not open URL [" + String.valueOf(url) + "].";
            this.addError(errMsg, ioe);
            throw new JoranException(errMsg, ioe);
        }
    }

    public void doConfigure(File file) throws JoranException {
        try (FileInputStream fileInputStream = new FileInputStream(file);){
            this.doConfigure(fileInputStream);
        }
        catch (IOException e) {
            throw new JoranException("Failed to load file " + String.valueOf(file), e);
        }
    }

    public void doConfigure(String filename) throws JoranException {
        this.doConfigure(new File(filename));
    }

    public void doConfigure(InputStream inputStream) throws JoranException {
        Properties props = new Properties();
        try {
            props.load(inputStream);
        }
        catch (IOException e) {
            throw new JoranException("Failed to load from input stream", e);
        }
        finally {
            this.close(inputStream);
        }
        this.doConfigure(props);
    }

    private void close(InputStream inputStream) throws JoranException {
        if (inputStream != null) {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                throw new JoranException("failed to close stream", e);
            }
        }
    }

    void doConfigure(Properties properties) {
        Map<String, String> variablesMap = this.extractVariablesMap(properties);
        Map<String, String> instructionMap = this.extractLogbackInstructionMap(properties);
        this.variableSubstitutionsHelper = new VariableSubstitutionsHelper(this.context, variablesMap);
        this.configureLoggers(instructionMap);
        this.configureRootLogger(instructionMap);
    }

    void configureRootLogger(Map<String, String> instructionMap) {
        String val = this.subst(instructionMap.get(LOGBACK_ROOT_LOGGER_PREFIX));
        if (val != null) {
            this.setLevel("ROOT", val);
        }
    }

    void configureLoggers(Map<String, String> instructionMap) {
        for (String key : instructionMap.keySet()) {
            if (!key.startsWith(LOGBACK_LOGGER_PREFIX)) continue;
            String loggerName = key.substring(LOGBACK_LOGGER_PREFIX_LENGTH);
            String value = this.subst(instructionMap.get(key));
            this.setLevel(loggerName, value);
        }
    }

    private void setLevel(String loggerName, String levelStr) {
        Logger logger = this.getLoggerContext().getLogger(loggerName);
        if ("INHERITED".equalsIgnoreCase(levelStr) || JoranConstants.NULL.equalsIgnoreCase(levelStr)) {
            if ("ROOT".equalsIgnoreCase(loggerName)) {
                this.addError("The level for the ROOT logger cannot be set to NULL or INHERITED. Ignoring.");
            } else {
                this.addInfo("Setting level of logger [" + loggerName + "] to null, i.e. INHERITED");
                logger.setLevel(null);
            }
        } else {
            Level level = Level.toLevel(levelStr);
            logger.setLevel(level);
        }
    }

    private Map<String, String> extractVariablesMap(Properties properties) {
        HashMap<String, String> variablesMap = new HashMap<String, String>();
        for (String key : properties.stringPropertyNames()) {
            if (key == null || key.startsWith(LOGBACK_PREFIX)) continue;
            variablesMap.put(key, properties.getProperty(key));
        }
        return variablesMap;
    }

    private Map<String, String> extractLogbackInstructionMap(Properties properties) {
        TreeMap<String, String> instructionMap = new TreeMap<String, String>(LENGTH_COMPARATOR);
        for (String key : properties.stringPropertyNames()) {
            if (key == null || !key.startsWith(LOGBACK_PREFIX)) continue;
            instructionMap.put(key, properties.getProperty(key));
        }
        return instructionMap;
    }

    public String subst(String ref) {
        String substituted = this.variableSubstitutionsHelper.subst(ref);
        if (ref != null && !ref.equals(substituted)) {
            String sanitized = this.variableSubstitutionsHelper.sanitizeIfConfidential(ref, substituted);
            this.addInfo("value \"" + sanitized + "\" substituted for \"" + ref + "\"");
        }
        return substituted;
    }
}

