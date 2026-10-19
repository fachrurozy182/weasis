/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.sanity;

import ch.qos.logback.core.joran.sanity.Pair;
import ch.qos.logback.core.model.Model;
import java.util.ArrayList;
import java.util.List;

public interface SanityChecker {
    public void check(Model var1);

    default public void deepFindAllModelsOfType(Class<? extends Model> modelClass, List<Model> modelList, Model model) {
        if (modelClass.isInstance(model)) {
            modelList.add(model);
        }
        for (Model m : model.getSubModels()) {
            this.deepFindAllModelsOfType(modelClass, modelList, m);
        }
    }

    default public List<Pair<Model, Model>> deepFindNestedSubModelsOfType(Class<? extends Model> modelClass, List<? extends Model> parentList) {
        ArrayList<Pair<Model, Model>> nestingPairs = new ArrayList<Pair<Model, Model>>();
        for (Model model : parentList) {
            ArrayList nestedElements = new ArrayList();
            model.getSubModels().stream().forEach(m -> this.deepFindAllModelsOfType(modelClass, nestedElements, (Model)m));
            nestedElements.forEach(n -> nestingPairs.add(new Pair<Model, Model>(parent, (Model)n)));
        }
        return nestingPairs;
    }
}

