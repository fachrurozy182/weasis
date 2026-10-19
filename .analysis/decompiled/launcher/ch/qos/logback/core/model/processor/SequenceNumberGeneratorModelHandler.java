/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.SequenceNumberGeneratorModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.SequenceNumberGenerator;
import ch.qos.logback.core.util.OptionHelper;

public class SequenceNumberGeneratorModelHandler
extends ModelHandlerBase {
    SequenceNumberGenerator sequenceNumberGenerator;
    private boolean inError;

    public SequenceNumberGeneratorModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new SequenceNumberGeneratorModelHandler(context);
    }

    protected Class<SequenceNumberGeneratorModel> getSupportedModelClass() {
        return SequenceNumberGeneratorModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        SequenceNumberGeneratorModel sequenceNumberGeneratorModel = (SequenceNumberGeneratorModel)model;
        String className = sequenceNumberGeneratorModel.getClassName();
        if (OptionHelper.isNullOrEmptyOrAllSpaces(className)) {
            this.addWarn("Missing className. This should have been caught earlier.");
            this.inError = true;
            return;
        }
        className = mic.getImport(className);
        try {
            this.addInfo("About to instantiate SequenceNumberGenerator of type [" + className + "]");
            this.sequenceNumberGenerator = (SequenceNumberGenerator)OptionHelper.instantiateByClassName(className, SequenceNumberGenerator.class, this.context);
            this.sequenceNumberGenerator.setContext(this.context);
            mic.pushObject(this.sequenceNumberGenerator);
        }
        catch (Exception e) {
            this.inError = true;
            this.addError("Could not create a SequenceNumberGenerator of type [" + className + "].", e);
            throw new ModelHandlerException(e);
        }
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        if (this.inError) {
            return;
        }
        Object o = mic.peekObject();
        if (o != this.sequenceNumberGenerator) {
            this.addWarn("The object at the of the stack is not the hook pushed earlier.");
        } else {
            mic.popObject();
            this.addInfo("Registering " + String.valueOf(o) + " with context.");
            this.context.setSequenceNumberGenerator(this.sequenceNumberGenerator);
        }
    }
}

