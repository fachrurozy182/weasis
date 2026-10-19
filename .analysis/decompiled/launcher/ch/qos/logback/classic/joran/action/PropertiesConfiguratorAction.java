/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.joran.action;

import ch.qos.logback.classic.model.PropertiesConfiguratorModel;
import ch.qos.logback.core.joran.action.ResourceAction;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import org.xml.sax.Attributes;

public class PropertiesConfiguratorAction
extends ResourceAction {
    @Override
    protected PropertiesConfiguratorModel makeNewResourceModel() {
        return new PropertiesConfiguratorModel();
    }

    @Override
    public Model buildCurrentModel(SaxEventInterpretationContext saxEventInterpretationContext, String localName, Attributes attributes) {
        Model model = super.buildCurrentModel(saxEventInterpretationContext, localName, attributes);
        if (model instanceof PropertiesConfiguratorModel) {
            PropertiesConfiguratorModel propertiesConfiguratorModel = (PropertiesConfiguratorModel)model;
            String scanAttribute = attributes.getValue("scan");
            propertiesConfiguratorModel.setScanStr(scanAttribute);
            return propertiesConfiguratorModel;
        }
        throw new IllegalStateException("Model is not of type PropertiesConfiguratorModel");
    }
}

