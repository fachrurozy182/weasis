/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.PropertyModel;
import org.xml.sax.Attributes;

public class PropertyAction
extends BaseModelAction {
    static final String RESOURCE_ATTRIBUTE = "resource";

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext interpretationContext, String localName, Attributes attributes) {
        if ("substitutionProperty".equals(localName)) {
            this.addWarn("[substitutionProperty] element has been deprecated. Please use the [variable] element instead.");
        }
        return true;
    }

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        PropertyModel propertyModel = new PropertyModel();
        propertyModel.setName(attributes.getValue("name"));
        propertyModel.setValue(attributes.getValue("value"));
        propertyModel.setFile(attributes.getValue("file"));
        propertyModel.setResource(attributes.getValue(RESOURCE_ATTRIBUTE));
        propertyModel.setScopeStr(attributes.getValue("scope"));
        return propertyModel;
    }
}

