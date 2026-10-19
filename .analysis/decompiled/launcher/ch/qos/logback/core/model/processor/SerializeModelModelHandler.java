/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.SerializeModelModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class SerializeModelModelHandler
extends ModelHandlerBase {
    public SerializeModelModelHandler(Context context) {
        super(context);
    }

    public static ModelHandlerBase makeInstance(Context context, ModelInterpretationContext mic) {
        return new SerializeModelModelHandler(context);
    }

    @Override
    public void handle(ModelInterpretationContext modelInterpretationContext, Model model) throws ModelHandlerException {
        Object configuratorHint = modelInterpretationContext.getConfiguratorHint();
        if (configuratorHint != null && configuratorHint.getClass().getName().equals("ch.qos.logback.classic.joran.SerializedModelConfigurator")) {
            this.addInfo("Skipping model serialization as calling configurator is already model based.");
            return;
        }
        if (!(model instanceof SerializeModelModel)) {
            this.addWarn("Model parameter is not of type SerializeModelModel. Skipping serialization of model structure");
            return;
        }
        SerializeModelModel serializeModelModel = (SerializeModelModel)model;
        Model topModel = modelInterpretationContext.getTopModel();
        if (topModel == null) {
            this.addWarn("Could not find top most model. Skipping serialization of model structure.");
            return;
        }
        Object fileStr = serializeModelModel.getFile();
        if (fileStr == null) {
            DateTimeFormatter dft = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmm");
            Instant now = Instant.now();
            String timestamp = dft.format(now);
            fileStr = "logback-" + timestamp + ".scmo";
            this.addInfo("For model serialization, using default file destination [" + (String)fileStr + "]");
        } else {
            fileStr = modelInterpretationContext.subst((String)fileStr);
        }
        this.writeModel((String)fileStr, topModel);
    }

    private void writeModel(String fileStr, Model firstModel) {
        this.addInfo("Serializing model to file [" + fileStr + "]");
        try (FileOutputStream fos = new FileOutputStream(fileStr);){
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(firstModel);
            oos.flush();
            oos.close();
        }
        catch (IOException e) {
            this.addError("IO failure while serializing Model [" + fileStr + "]");
        }
    }
}

