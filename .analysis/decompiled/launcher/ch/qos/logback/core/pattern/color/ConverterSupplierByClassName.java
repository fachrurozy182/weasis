/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.pattern.color;

import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.OptionHelper;
import java.util.function.Supplier;

public class ConverterSupplierByClassName
extends ContextAwareBase
implements Supplier<DynamicConverter> {
    String conversionWord;
    String converterClassStr;

    public ConverterSupplierByClassName(String conversionWord, String converterClassStr) {
        this.conversionWord = conversionWord;
        this.converterClassStr = converterClassStr;
    }

    @Override
    public DynamicConverter get() {
        try {
            return (DynamicConverter)OptionHelper.instantiateByClassName(this.converterClassStr, DynamicConverter.class, this.context);
        }
        catch (Exception e) {
            this.addError("Failed to instantiate converter class [" + this.converterClassStr + "] for conversion word [" + this.conversionWord + "]", e);
            return null;
        }
    }
}

