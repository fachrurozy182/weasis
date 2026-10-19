/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran;

import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.event.SaxEventRecorder;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.spi.ElementPath;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;
import ch.qos.logback.core.joran.spi.SimpleRuleStore;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.ConfigurationEvent;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.StatusUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.xml.sax.InputSource;

public abstract class GenericXMLConfigurator
extends ContextAwareBase {
    protected SaxEventInterpreter saxEventInterpreter;
    protected ModelInterpretationContext modelInterpretationContext;
    private RuleStore ruleStore;
    URL topURL;

    public ModelInterpretationContext getModelInterpretationContext() {
        return this.modelInterpretationContext;
    }

    public URL getTopURL() {
        return this.topURL;
    }

    public void setTopURL(URL topURL) {
        this.topURL = topURL;
    }

    public final void doConfigure(URL url) throws JoranException {
        InputStream in = null;
        try {
            this.topURL = url;
            URLConnection urlConnection = url.openConnection();
            urlConnection.setUseCaches(false);
            in = urlConnection.getInputStream();
            this.doConfigure(in, url.toExternalForm());
        }
        catch (IOException ioe) {
            String errMsg = "Could not open URL [" + String.valueOf(url) + "].";
            this.addError(errMsg, ioe);
            throw new JoranException(errMsg, ioe);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ioe) {
                    String errMsg = "Could not close input stream";
                    this.addError(errMsg, ioe);
                    throw new JoranException(errMsg, ioe);
                }
            }
        }
    }

    public final void doConfigure(String filename) throws JoranException {
        this.doConfigure(new File(filename));
    }

    public final void doConfigure(File file) throws JoranException {
        FileInputStream fis = null;
        try {
            URL url;
            this.topURL = url = file.toURI().toURL();
            fis = new FileInputStream(file);
            this.doConfigure(fis, url.toExternalForm());
        }
        catch (IOException ioe) {
            String errMsg = "Could not open [" + file.getPath() + "].";
            this.addError(errMsg, ioe);
            throw new JoranException(errMsg, ioe);
        }
        finally {
            if (fis != null) {
                try {
                    fis.close();
                }
                catch (IOException ioe) {
                    String errMsg = "Could not close [" + file.getName() + "].";
                    this.addError(errMsg, ioe);
                    throw new JoranException(errMsg, ioe);
                }
            }
        }
    }

    public final void doConfigure(InputStream inputStream) throws JoranException {
        this.doConfigure(new InputSource(inputStream));
    }

    public final void doConfigure(InputStream inputStream, String systemId) throws JoranException {
        InputSource inputSource = new InputSource(inputStream);
        inputSource.setSystemId(systemId);
        this.doConfigure(inputSource);
    }

    protected abstract void addElementSelectorAndActionAssociations(RuleStore var1);

    protected abstract void setImplicitRuleSupplier(SaxEventInterpreter var1);

    protected void addDefaultNestedComponentRegistryRules(DefaultNestedComponentRegistry registry) {
    }

    protected ElementPath initialElementPath() {
        return new ElementPath();
    }

    protected void buildSaxEventInterpreter(List<SaxEvent> saxEvents) {
        RuleStore rs = this.getRuleStore();
        this.addElementSelectorAndActionAssociations(rs);
        this.saxEventInterpreter = new SaxEventInterpreter(this.context, rs, this.initialElementPath(), saxEvents);
        SaxEventInterpretationContext interpretationContext = this.saxEventInterpreter.getSaxEventInterpretationContext();
        interpretationContext.setContext(this.context);
        this.setImplicitRuleSupplier(this.saxEventInterpreter);
    }

    public RuleStore getRuleStore() {
        if (this.ruleStore == null) {
            this.ruleStore = new SimpleRuleStore(this.context);
        }
        return this.ruleStore;
    }

    protected void buildModelInterpretationContext() {
        this.modelInterpretationContext = new ModelInterpretationContext(this.context);
        this.modelInterpretationContext.setTopURL(this.topURL);
        this.addDefaultNestedComponentRegistryRules(this.modelInterpretationContext.getDefaultNestedComponentRegistry());
    }

    public final void doConfigure(InputSource inputSource) throws JoranException {
        this.context.fireConfigurationEvent(ConfigurationEvent.newConfigurationStartedEvent(this));
        long threshold = System.currentTimeMillis();
        SaxEventRecorder recorder = this.populateSaxEventRecorder(inputSource);
        List<SaxEvent> saxEvents = recorder.getSaxEventList();
        if (saxEvents.isEmpty()) {
            this.addWarn("Empty sax event list");
            return;
        }
        Model top = this.buildModelFromSaxEventList(recorder.getSaxEventList());
        if (top == null) {
            this.addError("Could not find valid configuration instructions. Exiting.");
            return;
        }
        this.sanityCheck(top);
        this.processModel(top);
        StatusUtil statusUtil = new StatusUtil(this.context);
        if (statusUtil.noXMLParsingErrorsOccurred(threshold)) {
            this.addInfo("Registering current configuration as safe fallback point");
            this.registerSafeConfiguration(top);
            this.context.fireConfigurationEvent(ConfigurationEvent.newConfigurationEndedSuccessfullyEvent(this));
        } else {
            this.context.fireConfigurationEvent(ConfigurationEvent.newConfigurationEndedWithXMLParsingErrorsEvent(this));
        }
    }

    public SaxEventRecorder populateSaxEventRecorder(InputSource inputSource) throws JoranException {
        SaxEventRecorder recorder = new SaxEventRecorder(this.context);
        recorder.recordEvents(inputSource);
        return recorder;
    }

    public Model buildModelFromSaxEventList(List<SaxEvent> saxEvents) throws JoranException {
        this.buildSaxEventInterpreter(saxEvents);
        this.playSaxEvents();
        Model top = this.saxEventInterpreter.getSaxEventInterpretationContext().peekModel();
        return top;
    }

    private void playSaxEvents() throws JoranException {
        this.saxEventInterpreter.getEventPlayer().play();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void processModel(Model model) {
        this.buildModelInterpretationContext();
        this.modelInterpretationContext.setTopModel(model);
        this.modelInterpretationContext.setConfiguratorHint(this);
        DefaultProcessor defaultProcessor = new DefaultProcessor(this.context, this.modelInterpretationContext);
        this.addModelHandlerAssociations(defaultProcessor);
        ReentrantLock configurationLock = this.context.getConfigurationLock();
        try {
            configurationLock.lock();
            defaultProcessor.process(model);
        }
        finally {
            configurationLock.unlock();
        }
    }

    protected void sanityCheck(Model topModel) {
    }

    protected void addModelHandlerAssociations(DefaultProcessor defaultProcessor) {
    }

    public void registerSafeConfiguration(Model top) {
        this.context.putObject("SAFE_JORAN_CONFIGURATION", top);
    }

    public Model recallSafeConfiguration() {
        return (Model)this.context.getObject("SAFE_JORAN_CONFIGURATION");
    }
}

