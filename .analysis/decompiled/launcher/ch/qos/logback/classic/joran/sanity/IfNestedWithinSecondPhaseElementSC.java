/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.joran.sanity;

import ch.qos.logback.classic.model.LoggerModel;
import ch.qos.logback.classic.model.RootLoggerModel;
import ch.qos.logback.core.joran.sanity.Pair;
import ch.qos.logback.core.joran.sanity.SanityChecker;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.conditional.IfModel;
import ch.qos.logback.core.spi.ContextAwareBase;
import java.util.ArrayList;
import java.util.List;

public class IfNestedWithinSecondPhaseElementSC
extends ContextAwareBase
implements SanityChecker {
    public static final String NESTED_IF_WARNING_URL = "https://logback.qos.ch/codes.html#nested_if_element";

    @Override
    public void check(Model model) {
        if (model == null) {
            return;
        }
        ArrayList<Model> secondPhaseModels = new ArrayList<Model>();
        this.deepFindAllModelsOfType(AppenderModel.class, secondPhaseModels, model);
        this.deepFindAllModelsOfType(LoggerModel.class, secondPhaseModels, model);
        this.deepFindAllModelsOfType(RootLoggerModel.class, secondPhaseModels, model);
        List<Pair<Model, Model>> nestedPairs = this.deepFindNestedSubModelsOfType(IfModel.class, secondPhaseModels);
        if (nestedPairs.isEmpty()) {
            return;
        }
        this.addWarn("<if> elements cannot be nested within an <appender>, <logger> or <root> element");
        this.addWarn("See also https://logback.qos.ch/codes.html#nested_if_element");
        for (Pair<Model, Model> pair : nestedPairs) {
            Model p = (Model)pair.first;
            int pLine = p.getLineNumber();
            Model s = (Model)pair.second;
            int sLine = s.getLineNumber();
            this.addWarn("Element <" + p.getTag() + "> at line " + pLine + " contains a nested <" + s.getTag() + "> element at line " + sLine);
        }
    }

    public String toString() {
        return "IfNestedWithinSecondPhaseElementSC";
    }
}

