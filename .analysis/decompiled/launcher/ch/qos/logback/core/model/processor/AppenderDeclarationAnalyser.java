/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.model.processor.PhaseIndicator;
import ch.qos.logback.core.model.processor.ProcessingPhase;
import java.util.HashSet;
import java.util.Set;

@PhaseIndicator(phase=ProcessingPhase.DEPENDENCY_ANALYSIS)
public class AppenderDeclarationAnalyser
extends ModelHandlerBase {
    static final String DECLARED_APPENDER_NAME_SET_KEY = "DECLARED_APPENDER_NAME_SET";

    public AppenderDeclarationAnalyser(Context context) {
        super(context);
    }

    protected Class<AppenderModel> getSupportedModelClass() {
        return AppenderModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        AppenderModel appenderModel = (AppenderModel)model;
        String originalAppenderName = appenderModel.getName();
        String substAppenderName = mic.subst(originalAppenderName);
        AppenderDeclarationAnalyser.addAppenderDeclaration(mic, substAppenderName);
    }

    public static Set<String> getAppenderNameSet(ModelInterpretationContext mic) {
        HashSet set = (HashSet)mic.getObjectMap().get(DECLARED_APPENDER_NAME_SET_KEY);
        if (set == null) {
            set = new HashSet();
            mic.getObjectMap().put(DECLARED_APPENDER_NAME_SET_KEY, set);
        }
        return set;
    }

    public static void addAppenderDeclaration(ModelInterpretationContext mic, String appenderName) {
        Set<String> set = AppenderDeclarationAnalyser.getAppenderNameSet(mic);
        set.add(appenderName);
    }

    public static boolean isAppenderDeclared(ModelInterpretationContext mic, String appenderName) {
        Set<String> set = AppenderDeclarationAnalyser.getAppenderNameSet(mic);
        return set.contains(appenderName);
    }
}

