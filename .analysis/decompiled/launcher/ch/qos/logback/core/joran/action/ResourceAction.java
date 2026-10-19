/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.action.PreconditionValidator;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ResourceModel;
import org.xml.sax.Attributes;

public abstract class ResourceAction
extends BaseModelAction {
    private static final String FILE_ATTR = "file";
    private static final String URL_ATTR = "url";
    private static final String RESOURCE_ATTR = "resource";
    private static final String OPTIONAL_ATTR = "optional";

    protected abstract ResourceModel makeNewResourceModel();

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext intercon, String name, Attributes attributes) {
        PreconditionValidator pv = new PreconditionValidator(this, intercon, name, attributes);
        pv.validateOneAndOnlyOneAttributeProvided(FILE_ATTR, URL_ATTR, RESOURCE_ATTR);
        return pv.isValid();
    }

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String localName, Attributes attributes) {
        ResourceModel resourceModel = this.makeNewResourceModel();
        this.fillInIncludeModelAttributes(resourceModel, localName, attributes);
        return resourceModel;
    }

    private void fillInIncludeModelAttributes(ResourceModel resourceModel, String tagName, Attributes attributes) {
        resourceModel.setTag(tagName);
        String fileAttribute = attributes.getValue(FILE_ATTR);
        String urlAttribute = attributes.getValue(URL_ATTR);
        String resourceAttribute = attributes.getValue(RESOURCE_ATTR);
        String optionalAttribute = attributes.getValue(OPTIONAL_ATTR);
        resourceModel.setFile(fileAttribute);
        resourceModel.setUrl(urlAttribute);
        resourceModel.setResource(resourceAttribute);
        resourceModel.setOptional(optionalAttribute);
    }
}

