/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.ConversionRuleModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.pattern.color.ConverterSupplierByClassName;
import ch.qos.logback.core.util.OptionHelper;
import java.util.HashMap;

public class ConversionRuleModelHandler
extends ModelHandlerBase {
    private boolean inError;

    public ConversionRuleModelHandler(Context context) {
        super(context);
    }

    public static ConversionRuleModelHandler makeInstance(Context context, ModelInterpretationContext mic) {
        return new ConversionRuleModelHandler(context);
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        ConversionRuleModel conversionRuleModel = (ConversionRuleModel)model;
        String converterClass = conversionRuleModel.getClassName();
        if (OptionHelper.isNullOrEmptyOrAllSpaces(converterClass)) {
            this.addWarn("Missing className. This should have been caught earlier.");
            this.inError = true;
            return;
        }
        converterClass = mic.getImport(converterClass);
        String conversionWord = conversionRuleModel.getConversionWord();
        try {
            HashMap<String, ConverterSupplierByClassName> ruleRegistry = (HashMap<String, ConverterSupplierByClassName>)this.context.getObject("PATTERN_RULE_REGISTRY_FOR_SUPPLIERS");
            if (ruleRegistry == null) {
                ruleRegistry = new HashMap<String, ConverterSupplierByClassName>();
                this.context.putObject("PATTERN_RULE_REGISTRY_FOR_SUPPLIERS", ruleRegistry);
            }
            this.addInfo("registering conversion word " + conversionWord + " with class [" + converterClass + "]");
            ConverterSupplierByClassName converterSupplierByClassName = new ConverterSupplierByClassName(conversionWord, converterClass);
            converterSupplierByClassName.setContext(this.getContext());
            ruleRegistry.put(conversionWord, converterSupplierByClassName);
        }
        catch (Exception oops) {
            this.inError = true;
            String errorMsg = "Could not add conversion rule to PatternLayout.";
            this.addError(errorMsg);
        }
    }
}

