/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.action;

import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.action.PreconditionValidator;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.ConversionRuleModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.util.OptionHelper;
import org.xml.sax.Attributes;

public class ConversionRuleAction
extends BaseModelAction {
    public static String CONVERTER_CLASS_ATTRIBUTE = "converterClass";

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext seic, String name, Attributes attributes) {
        boolean missingClass;
        boolean multipleClassAttributes;
        PreconditionValidator pv = new PreconditionValidator(this, seic, name, attributes);
        pv.validateGivenAttribute("conversionWord");
        if (!pv.isValid()) {
            return false;
        }
        boolean validConverterClassAttribute = pv.isValidAttribute(CONVERTER_CLASS_ATTRIBUTE);
        boolean validClassAttribute = pv.isValidAttribute("class");
        boolean bl = multipleClassAttributes = validClassAttribute && validConverterClassAttribute;
        if (validConverterClassAttribute && !multipleClassAttributes) {
            pv.addWarn("[" + CONVERTER_CLASS_ATTRIBUTE + "] attribute is deprecated and replaced by [class]. " + pv.getLocationSuffix());
        }
        boolean bl2 = missingClass = !validClassAttribute && !validConverterClassAttribute;
        if (missingClass) {
            pv.addMissingAttributeError("class");
            return false;
        }
        if (multipleClassAttributes) {
            String classAttr;
            String converterClass = attributes.getValue(CONVERTER_CLASS_ATTRIBUTE);
            if (!converterClass.equals(classAttr = attributes.getValue("class"))) {
                pv.addWarn("Both [" + CONVERTER_CLASS_ATTRIBUTE + "] and [class] attributes are specified but have different values.");
            } else {
                pv.addInfo("Both [" + CONVERTER_CLASS_ATTRIBUTE + "] attribute and [class] attribute specified. ");
            }
            pv.addInfo("[class] attribute will be used. ");
        }
        pv.validateGivenAttribute("conversionWord");
        return pv.isValid();
    }

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name, Attributes attributes) {
        String classStr;
        ConversionRuleModel conversionRuleModel = new ConversionRuleModel();
        conversionRuleModel.setConversionWord(attributes.getValue("conversionWord"));
        String converterClassStr = attributes.getValue(CONVERTER_CLASS_ATTRIBUTE);
        if (!OptionHelper.isNullOrEmpty(converterClassStr)) {
            conversionRuleModel.setClassName(converterClassStr);
        }
        if (!OptionHelper.isNullOrEmpty(classStr = attributes.getValue("class"))) {
            conversionRuleModel.setClassName(classStr);
        }
        return conversionRuleModel;
    }
}

