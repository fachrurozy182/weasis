/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.SiftModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.model.processor.PhaseIndicator;
import ch.qos.logback.core.model.processor.ProcessingPhase;
import ch.qos.logback.core.rolling.RollingFileAppender;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PhaseIndicator(phase=ProcessingPhase.DEPENDENCY_ANALYSIS)
public class FileCollisionAnalyser
extends ModelHandlerBase {
    public static final String COLLISION_DETECTED = "Collision detected. Skipping initialization of appender named [%s]";
    public static final String COLLISION_MESSAGE = "In appender [%s] option '%s' has the same value '%s' as that set for appender [%s] defined earlier";
    public static final String SIFT_COLLISION_MESSAGE_0 = "The nested appender of SiftingAppender [%s] does not reference the discriminator key [%s] in its 'file'/'fileNamePattern'. ";
    public static final String SIFT_COLLISION_MESSAGE_1 = "Every child appender will therefore write to the same file [%s] causing collisions. ";
    public static final String SIFT_COLLISION_MESSAGE_2 = "Consider embedding ${%s} in the file path.";

    public FileCollisionAnalyser(Context context) {
        super(context);
    }

    protected Class<AppenderModel> getSupportedModelClass() {
        return AppenderModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        AppenderModel appenderModel = (AppenderModel)model;
        String originalClassName = appenderModel.getClassName();
        String className = mic.getImport(originalClassName);
        String appenderName = appenderModel.getName();
        Model siftModel = this.firstSubModelOfClass(appenderModel, SiftModel.class);
        if (siftModel != null) {
            this.checkSiftingAppenderForCollision(mic, appenderModel, siftModel, appenderName);
            return;
        }
        if (!FileCollisionAnalyser.fileAppenderOrRollingFileAppender(className)) {
            return;
        }
        String tagName0 = "file";
        this.checkForCollisions(mic, MapKey.FILE_COLLISION_MAP_KEY, appenderModel, appenderName, tagName0);
        String tagName1 = "fileNamePattern";
        this.checkForCollisions(mic, MapKey.RFA_FILENAME_COLLISION_MAP, appenderModel, appenderName, tagName1);
    }

    private static boolean fileAppenderOrRollingFileAppender(String className) {
        return FileAppender.class.getName().equals(className) || RollingFileAppender.class.getName().equals(className);
    }

    boolean tagPredicate(Model model, String tagName) {
        return model instanceof ImplicitModel && tagName.equals(model.getTag());
    }

    private void checkForCollisions(ModelInterpretationContext mic, MapKey mapKey, AppenderModel appenderModel, String appenderName, String tagName) {
        Stream streamLevel2;
        Stream streamLevel1 = appenderModel.getSubModels().stream();
        List matchingModels = Stream.concat(streamLevel1, streamLevel2 = appenderModel.getSubModels().stream().flatMap(child -> child.getSubModels().stream())).filter(m -> this.tagPredicate((Model)m, tagName)).collect(Collectors.toList());
        if (!matchingModels.isEmpty()) {
            ImplicitModel implicitModel = (ImplicitModel)matchingModels.get(0);
            String bodyValue = mic.subst(implicitModel.getBodyText());
            Map<String, String> collisionMap = this.getCollisionMapByKey(mic, mapKey);
            Optional<Map.Entry> collision = collisionMap.entrySet().stream().filter(entry -> bodyValue.equals(entry.getValue())).findFirst();
            if (collision.isPresent()) {
                this.addErrorForCollision(tagName, appenderName, (String)collision.get().getKey(), bodyValue);
                appenderModel.markAsHandled();
                appenderModel.deepMarkAsSkipped();
            } else {
                collisionMap.put(appenderName, bodyValue);
            }
        }
    }

    private Map<String, String> getCollisionMapByKey(ModelInterpretationContext mic, MapKey mapKey) {
        HashMap map = (HashMap)mic.getObjectMap().get(mapKey.name());
        if (map == null) {
            map = new HashMap();
            mic.getObjectMap().put(mapKey.name(), map);
        }
        return map;
    }

    private void addErrorForCollision(String optionName, String appenderName, String previousAppenderName, String optionValue) {
        this.addError(String.format(COLLISION_DETECTED, appenderName));
        this.addError(String.format(COLLISION_MESSAGE, appenderName, optionName, optionValue, previousAppenderName));
    }

    private void checkSiftingAppenderForCollision(ModelInterpretationContext mic, AppenderModel appenderModel, Model siftModel, String appenderName) {
        String discriminatorKey = this.findDiscriminatorKey(appenderModel);
        if (discriminatorKey == null || discriminatorKey.isEmpty()) {
            return;
        }
        Model nestedAppenderModel = this.firstSubModelOfClass(siftModel, AppenderModel.class);
        if (nestedAppenderModel == null) {
            return;
        }
        ArrayList<String> fileValues = new ArrayList<String>();
        this.collectBodyText(nestedAppenderModel, "file", fileValues);
        this.collectBodyText(nestedAppenderModel, "fileNamePattern", fileValues);
        if (fileValues.isEmpty()) {
            return;
        }
        String keyReference = "${" + discriminatorKey;
        boolean referencesKey = fileValues.stream().anyMatch(v -> v.contains(keyReference));
        if (!referencesKey) {
            String resolvedTarget = mic.subst((String)fileValues.get(0));
            this.addWarn(String.format(SIFT_COLLISION_MESSAGE_0, appenderName, discriminatorKey));
            this.addWarn(String.format(SIFT_COLLISION_MESSAGE_1, resolvedTarget));
            this.addWarn(String.format(SIFT_COLLISION_MESSAGE_2, discriminatorKey));
        }
    }

    private Model firstSubModelOfClass(Model parent, Class<? extends Model> modelClass) {
        return parent.getSubModels().stream().filter(modelClass::isInstance).findFirst().orElse(null);
    }

    private String findDiscriminatorKey(AppenderModel appenderModel) {
        Optional<Model> discriminator = appenderModel.getSubModels().stream().filter(m -> "discriminator".equalsIgnoreCase(m.getTag())).findFirst();
        if (discriminator.isEmpty()) {
            return null;
        }
        for (Model child : discriminator.get().getSubModels()) {
            String bodyText;
            if (!"key".equalsIgnoreCase(child.getTag()) || (bodyText = child.getBodyText()) == null) continue;
            return bodyText.trim();
        }
        return null;
    }

    private void collectBodyText(Model appenderModel, String tagName, List<String> out) {
        for (Model child : appenderModel.getSubModels()) {
            this.addBodyTextIfMatching(child, tagName, out);
            for (Model grandChild : child.getSubModels()) {
                this.addBodyTextIfMatching(grandChild, tagName, out);
            }
        }
    }

    private void addBodyTextIfMatching(Model model, String tagName, List<String> out) {
        if (!this.tagPredicate(model, tagName)) {
            return;
        }
        String bodyText = model.getBodyText();
        if (bodyText != null) {
            out.add(bodyText);
        }
    }

    static enum MapKey {
        FILE_COLLISION_MAP_KEY,
        RFA_FILENAME_COLLISION_MAP;

    }
}

