/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.ImportModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

public class ImportModelHandler
extends ModelHandlerBase {
    public ImportModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new ImportModelHandler(context);
    }

    protected Class<ImportModel> getSupportedModelClass() {
        return ImportModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext intercon, Model model) throws ModelHandlerException {
        ImportModel importModel = (ImportModel)model;
        String className = importModel.getClassName();
        if (OptionHelper.isNullOrEmptyOrAllSpaces(className)) {
            this.addWarn("Empty className not allowed");
            return;
        }
        String stem = this.extractStem(className);
        if (stem == null) {
            this.addWarn("[" + className + "] could not be imported due to incorrect format");
            return;
        }
        intercon.addImport(stem, className);
    }

    String extractStem(String className) {
        if (className == null) {
            return null;
        }
        int lastDotIndex = className.lastIndexOf(46);
        if (lastDotIndex == -1) {
            return null;
        }
        if (lastDotIndex + 1 == className.length()) {
            return null;
        }
        return className.substring(lastDotIndex + 1);
    }
}

