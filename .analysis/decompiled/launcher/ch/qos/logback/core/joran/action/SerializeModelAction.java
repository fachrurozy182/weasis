/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.SerializeModelModel;
import org.xml.sax.Attributes;

public class SerializeModelAction
extends BaseModelAction {
    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        SerializeModelModel serializeModelModel = new SerializeModelModel();
        serializeModelModel.setFile(attributes.getValue("file"));
        return serializeModelModel;
    }
}

