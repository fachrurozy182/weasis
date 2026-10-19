/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.joran.PropertiesConfigurator;
import ch.qos.logback.classic.model.PropertiesConfiguratorModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ResourceModel;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.model.processor.ResourceHandlerBase;
import ch.qos.logback.core.spi.ContextAwarePropertyContainer;
import ch.qos.logback.core.util.OptionHelper;
import java.io.InputStream;
import java.net.URL;

public class PropertiesConfiguratorModelHandler
extends ResourceHandlerBase {
    boolean inError = false;
    static final boolean CREATE_CWL_IF_NOT_ALREADY_CREATED = true;

    public PropertiesConfiguratorModelHandler(Context context) {
        super(context);
    }

    public static PropertiesConfiguratorModelHandler makeInstance(Context context, ModelInterpretationContext mic) {
        return new PropertiesConfiguratorModelHandler(context);
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        Boolean topScanBoolean = mic.getTopScanBoolean();
        this.detachedHandle(mic, model, topScanBoolean);
    }

    public void detachedHandle(ContextAwarePropertyContainer capc, Model model, Boolean topScanBoolean) throws ModelHandlerException {
        InputStream in;
        PropertiesConfiguratorModel propertyConfiguratorModel = (PropertiesConfiguratorModel)model;
        this.optional = OptionHelper.toBoolean(propertyConfiguratorModel.getOptional(), false);
        if (!this.checkAttributes(propertyConfiguratorModel)) {
            this.inError = true;
            return;
        }
        URL inputURL = this.getInputURL(capc, propertyConfiguratorModel);
        if (inputURL == null) {
            this.inError = true;
            return;
        }
        Boolean localScan = OptionHelper.toBooleanObject(propertyConfiguratorModel.getScanStr());
        if (localScan == Boolean.TRUE || topScanBoolean == Boolean.TRUE) {
            if (topScanBoolean != Boolean.TRUE) {
                ConfigurationWatchListUtil.registerNewConfigurationWatchListWithContext(this.context);
            }
            ConfigurationWatchListUtil.addToWatchList(this.context, inputURL, true);
        }
        if ((in = this.openURL(inputURL)) == null) {
            this.inError = true;
            return;
        }
        this.addInfo("Reading configuration from [" + this.getAttribureInUse() + "]");
        PropertiesConfigurator propertiesConfigurator = new PropertiesConfigurator();
        propertiesConfigurator.setContext(capc.getContext());
        try {
            propertiesConfigurator.doConfigure(in);
        }
        catch (JoranException e) {
            this.addError("Could not configure from " + this.getAttribureInUse());
            throw new ModelHandlerException(e);
        }
    }

    protected InputStream getInputStream(ContextAwarePropertyContainer capc, ResourceModel resourceModel) {
        URL inputURL = this.getInputURL(capc, resourceModel);
        if (inputURL == null) {
            return null;
        }
        ConfigurationWatchListUtil.addToWatchList(this.context, inputURL, true);
        return this.openURL(inputURL);
    }
}

