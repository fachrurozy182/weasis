/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.sift;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.SiftModel;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.sift.AppenderFactoryUsingSiftModel;
import ch.qos.logback.core.sift.SiftingAppenderBase;
import java.util.stream.Stream;

public class SiftModelHandler
extends ModelHandlerBase {
    static final String ONE_AND_ONLY_ONE_URL = "https://logback.qos.ch/codes.html#1andOnly1";

    public SiftModelHandler(Context context) {
        super(context);
    }

    public static SiftModelHandler makeInstance(Context context, ModelInterpretationContext ic) {
        return new SiftModelHandler(context);
    }

    protected Class<SiftModel> getSupportedModelClass() {
        return SiftModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        SiftModel siftModel = (SiftModel)model;
        siftModel.markAsSkipped();
        long appenderModelCount = this.computeAppenderModelCount(siftModel);
        if (appenderModelCount == 0L) {
            String errMsg = "No nested appenders found within the <sift> element in SiftingAppender.";
            this.addError(errMsg);
            return;
        }
        if (appenderModelCount > 1L) {
            String errMsg = "Only and only one appender can be nested the <sift> element in SiftingAppender. See also https://logback.qos.ch/codes.html#1andOnly1";
            this.addError(errMsg);
            return;
        }
        Object o = mic.peekObject();
        if (o instanceof SiftingAppenderBase) {
            SiftingAppenderBase sa = (SiftingAppenderBase)o;
            String key = sa.getDiscriminatorKey();
            AppenderFactoryUsingSiftModel afusm = new AppenderFactoryUsingSiftModel(mic, siftModel, key);
            sa.setAppenderFactory(afusm);
        } else {
            this.addError("Unexpected object " + String.valueOf(o));
        }
    }

    private long computeAppenderModelCount(SiftModel siftModel) {
        Stream stream = siftModel.getSubModels().stream();
        long count = stream.filter(m -> m instanceof AppenderModel).count();
        return count;
    }
}

