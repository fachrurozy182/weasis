/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.pattern.KeyValuePairConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.event.KeyValuePair;

public class MaskedKeyValuePairConverter
extends ClassicConverter {
    public static final String MASK = "XXX";
    List<String> optionList;
    List<String> maskList = new ArrayList<String>();
    KeyValuePairConverter.ValueQuoteSpecification valueQuoteSpec = KeyValuePairConverter.ValueQuoteSpecification.DOUBLE;

    @Override
    public void start() {
        this.optionList = this.getOptionList();
        KeyValuePairConverter.ValueQuoteSpecification extractedSpec = this.extractSpec(this.optionList);
        if (extractedSpec == null) {
            this.maskList = this.optionList;
        } else {
            this.valueQuoteSpec = extractedSpec;
            this.maskList = this.optionList.subList(1, this.optionList.size());
        }
        this.checkMaskListForExtraQuoteSpecs(this.maskList);
        super.start();
    }

    private void checkMaskListForExtraQuoteSpecs(List<String> maskList) {
        if (maskList == null || maskList.isEmpty()) {
            return;
        }
        if (maskList.contains("DOUBLE")) {
            this.addWarn("quote spec DOUBLE found in the wrong order");
        }
        if (maskList.contains("SINGLE")) {
            this.addWarn("extra quote spec SINGLE found in the wrong order");
        }
        if (maskList.contains("NONE")) {
            this.addWarn("extra quote spec NONE found in the wrong order");
        }
    }

    KeyValuePairConverter.ValueQuoteSpecification extractSpec(List<String> optionList) {
        if (optionList == null || optionList.isEmpty()) {
            return null;
        }
        String firstOption = optionList.get(0);
        if ("DOUBLE".equalsIgnoreCase(firstOption)) {
            return KeyValuePairConverter.ValueQuoteSpecification.DOUBLE;
        }
        if ("SINGLE".equalsIgnoreCase(firstOption)) {
            return KeyValuePairConverter.ValueQuoteSpecification.SINGLE;
        }
        if ("NONE".equalsIgnoreCase(firstOption)) {
            return KeyValuePairConverter.ValueQuoteSpecification.NONE;
        }
        return null;
    }

    @Override
    public String convert(ILoggingEvent event) {
        List<KeyValuePair> kvpList = event.getKeyValuePairs();
        if (kvpList == null || kvpList.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < kvpList.size(); ++i) {
            KeyValuePair kvp = kvpList.get(i);
            if (i != 0) {
                sb.append(' ');
            }
            sb.append(String.valueOf(kvp.key));
            sb.append('=');
            Character quoteChar = this.valueQuoteSpec.asChar();
            if (quoteChar != null) {
                sb.append(quoteChar);
            }
            if (this.maskList.contains(kvp.key)) {
                sb.append(MASK);
            } else {
                sb.append(String.valueOf(kvp.value));
            }
            if (quoteChar == null) continue;
            sb.append(quoteChar);
        }
        return sb.toString();
    }
}

