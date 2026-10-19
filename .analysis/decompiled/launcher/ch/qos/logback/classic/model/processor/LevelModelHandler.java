/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.model.LevelModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;

public class LevelModelHandler
extends ModelHandlerBase {
    boolean inError = false;

    public LevelModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext ic) {
        return new LevelModelHandler(context);
    }

    protected Class<? extends LevelModel> getSupportedModelClass() {
        return LevelModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        Object o = mic.peekObject();
        if (!(o instanceof Logger)) {
            this.inError = true;
            this.addError("For element <level>, could not find a logger at the top of execution stack.");
            return;
        }
        Logger l = (Logger)o;
        String loggerName = l.getName();
        LevelModel levelModel = (LevelModel)model;
        String levelStr = mic.subst(levelModel.getValue());
        if ("INHERITED".equalsIgnoreCase(levelStr) || JoranConstants.NULL.equalsIgnoreCase(levelStr)) {
            if ("ROOT".equalsIgnoreCase(loggerName)) {
                this.addError("The level for the ROOT logger cannot be set to NULL or INHERITED. Ignoring.");
            } else {
                l.setLevel(null);
            }
        } else {
            l.setLevel(Level.toLevel(levelStr, Level.DEBUG));
        }
        this.addInfo(loggerName + " level set to " + String.valueOf(l.getLevel()));
    }
}

