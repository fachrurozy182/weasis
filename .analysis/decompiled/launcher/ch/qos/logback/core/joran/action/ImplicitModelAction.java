/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.Model;
import java.util.Stack;
import org.xml.sax.Attributes;

public class ImplicitModelAction
extends Action {
    Stack<ImplicitModel> currentImplicitModelStack = new Stack();

    @Override
    public void begin(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) throws ActionException {
        ImplicitModel currentImplicitModel = new ImplicitModel();
        currentImplicitModel.setTag(name);
        String className = attributes.getValue("class");
        currentImplicitModel.setClassName(className);
        this.currentImplicitModelStack.push(currentImplicitModel);
        interpretationContext.pushModel(currentImplicitModel);
    }

    @Override
    public void body(SaxEventInterpretationContext ec, String body) {
        ImplicitModel implicitModel = this.currentImplicitModelStack.peek();
        implicitModel.addText(body);
    }

    @Override
    public void end(SaxEventInterpretationContext interpretationContext, String name) throws ActionException {
        Model otherImplicitModel;
        ImplicitModel implicitModel = this.currentImplicitModelStack.peek();
        if (implicitModel != (otherImplicitModel = interpretationContext.popModel())) {
            this.addError(String.valueOf(implicitModel) + " does not match " + String.valueOf(otherImplicitModel));
            return;
        }
        Model parentModel = interpretationContext.peekModel();
        if (parentModel != null) {
            parentModel.addSubModel(implicitModel);
        } else {
            this.addWarn("Could not find parent model.");
            this.addWarn(" Will not add current implicit model as subModel.");
        }
        this.currentImplicitModelStack.pop();
    }
}

