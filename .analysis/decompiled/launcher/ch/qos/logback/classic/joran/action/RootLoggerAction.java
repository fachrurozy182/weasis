/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.joran.action;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.model.RootLoggerModel;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import org.xml.sax.Attributes;

public class RootLoggerAction
extends BaseModelAction {
    Logger root;
    boolean inError = false;

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext interpcont, String name, Attributes attributes) {
        String levelStr = attributes.getValue("level");
        if (JoranConstants.NULL.equalsIgnoreCase(levelStr) || "INHERITED".equalsIgnoreCase(levelStr)) {
            this.addError("The level for the ROOT logger cannot be set to NULL or INHERITED. Ignoring.");
            return false;
        }
        return true;
    }

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        RootLoggerModel rootLoggerModel = new RootLoggerModel();
        String levelStr = attributes.getValue("level");
        rootLoggerModel.setLevel(levelStr);
        return rootLoggerModel;
    }
}

