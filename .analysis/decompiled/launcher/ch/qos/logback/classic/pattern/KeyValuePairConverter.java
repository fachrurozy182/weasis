/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.List;
import org.slf4j.event.KeyValuePair;

public class KeyValuePairConverter
extends ClassicConverter {
    static final String DOUBLE_OPTION_STR = "DOUBLE";
    static final String SINGLE_OPTION_STR = "SINGLE";
    static final String NONE_OPTION_STR = "NONE";
    ValueQuoteSpecification valueQuoteSpec = ValueQuoteSpecification.DOUBLE;

    @Override
    public void start() {
        String optStr = this.getFirstOption();
        this.valueQuoteSpec = this.optionStrToSpec(optStr);
        super.start();
    }

    private ValueQuoteSpecification optionStrToSpec(String optStr) {
        if (optStr == null) {
            return ValueQuoteSpecification.DOUBLE;
        }
        if (DOUBLE_OPTION_STR.equalsIgnoreCase(optStr)) {
            return ValueQuoteSpecification.DOUBLE;
        }
        if (SINGLE_OPTION_STR.equalsIgnoreCase(optStr)) {
            return ValueQuoteSpecification.SINGLE;
        }
        if (NONE_OPTION_STR.equalsIgnoreCase(optStr)) {
            return ValueQuoteSpecification.NONE;
        }
        return ValueQuoteSpecification.DOUBLE;
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
            sb.append(String.valueOf(kvp.value));
            if (quoteChar == null) continue;
            sb.append(quoteChar);
        }
        return sb.toString();
    }

    static enum ValueQuoteSpecification {
        NONE,
        SINGLE,
        DOUBLE;


        Character asChar() {
            switch (this.ordinal()) {
                case 0: {
                    return null;
                }
                case 2: {
                    return Character.valueOf('\"');
                }
                case 1: {
                    return Character.valueOf('\'');
                }
            }
            throw new IllegalStateException();
        }
    }
}

