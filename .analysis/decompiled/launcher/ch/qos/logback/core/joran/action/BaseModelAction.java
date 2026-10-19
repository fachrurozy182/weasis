/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import org.xml.sax.Attributes;

public abstract class BaseModelAction
extends Action {
    Model parentModel;
    Model currentModel;
    boolean inError = false;

    @Override
    public void begin(SaxEventInterpretationContext saxEventInterpretationContext, String name, Attributes attributes) throws ActionException {
        this.parentModel = null;
        this.inError = false;
        if (!this.validPreconditions(saxEventInterpretationContext, name, attributes)) {
            this.inError = true;
            return;
        }
        this.currentModel = this.buildCurrentModel(saxEventInterpretationContext, name, attributes);
        this.currentModel.setTag(name);
        if (!saxEventInterpretationContext.isModelStackEmpty()) {
            this.parentModel = saxEventInterpretationContext.peekModel();
        }
        int lineNumber = BaseModelAction.getLineNumber(saxEventInterpretationContext);
        this.currentModel.setLineNumber(lineNumber);
        saxEventInterpretationContext.pushModel(this.currentModel);
    }

    protected abstract Model buildCurrentModel(SaxEventInterpretationContext var1, String var2, Attributes var3);

    protected boolean validPreconditions(SaxEventInterpretationContext intercon, String name, Attributes attributes) {
        return true;
    }

    @Override
    public void body(SaxEventInterpretationContext ec, String body) throws ActionException {
        if (this.currentModel == null) {
            throw new ActionException("current model is null. Is <configuration> element missing?");
        }
        this.currentModel.addText(body);
    }

    @Override
    public void end(SaxEventInterpretationContext saxEventInterpretationContext, String name) throws ActionException {
        if (this.inError) {
            return;
        }
        Model m = saxEventInterpretationContext.peekModel();
        if (m != this.currentModel) {
            this.addWarn("The object " + String.valueOf(m) + "] at the top of the stack differs from the model [" + this.currentModel.idString() + "] pushed earlier.");
            this.addWarn("This is wholly unexpected.");
        }
        if (this.parentModel != null) {
            this.parentModel.addSubModel(this.currentModel);
            saxEventInterpretationContext.popModel();
        }
    }
}

