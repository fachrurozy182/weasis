/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.sanity;

import ch.qos.logback.core.joran.sanity.Pair;
import ch.qos.logback.core.joran.sanity.SanityChecker;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ContextAwareBase;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppenderWithinAppenderSanityChecker
extends ContextAwareBase
implements SanityChecker {
    public static String NESTED_APPENDERS_WARNING = "As of logback version 1.3, nested appenders are not allowed.";

    @Override
    public void check(Model model) {
        if (model == null) {
            return;
        }
        ArrayList<Model> appenderModels = new ArrayList<Model>();
        this.deepFindAllModelsOfType(AppenderModel.class, appenderModels, model);
        List<Pair<Model, Model>> nestedPairs = this.deepFindNestedSubModelsOfType(AppenderModel.class, appenderModels);
        List filteredNestedPairs = nestedPairs.stream().filter(pair -> !this.isSiftingAppender((Model)pair.first)).collect(Collectors.toList());
        if (filteredNestedPairs.isEmpty()) {
            return;
        }
        this.addWarn(NESTED_APPENDERS_WARNING);
        for (Pair pair2 : filteredNestedPairs) {
            this.addWarn("Appender at line " + ((Model)pair2.first).getLineNumber() + " contains a nested appender at line " + ((Model)pair2.second).getLineNumber());
        }
    }

    private boolean isSiftingAppender(Model first) {
        if (first instanceof AppenderModel) {
            AppenderModel appenderModel = (AppenderModel)first;
            String classname = appenderModel.getClassName();
            if (classname == null) {
                return false;
            }
            return appenderModel.getClassName().contains("SiftingAppender");
        }
        return false;
    }
}

