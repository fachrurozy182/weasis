/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.model;

import ch.qos.logback.core.model.ComponentModel;

public class ConversionRuleModel
extends ComponentModel {
    String conversionWord;

    @Override
    protected ConversionRuleModel makeNewInstance() {
        return new ConversionRuleModel();
    }

    public String getConversionWord() {
        return this.conversionWord;
    }

    public void setConversionWord(String conversionWord) {
        this.conversionWord = conversionWord;
    }

    public String getConverterClass() {
        return this.getClassName();
    }

    public void setConverterClass(String converterClass) {
        this.setClassName(converterClass);
    }
}

