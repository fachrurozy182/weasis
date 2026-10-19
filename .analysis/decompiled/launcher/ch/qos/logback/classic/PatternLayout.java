/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.classic;

import ch.qos.logback.classic.pattern.CallerDataConverter;
import ch.qos.logback.classic.pattern.ClassOfCallerConverter;
import ch.qos.logback.classic.pattern.ContextNameConverter;
import ch.qos.logback.classic.pattern.DateConverter;
import ch.qos.logback.classic.pattern.EnsureExceptionHandling;
import ch.qos.logback.classic.pattern.EpochConverter;
import ch.qos.logback.classic.pattern.ExtendedThrowableProxyConverter;
import ch.qos.logback.classic.pattern.FileOfCallerConverter;
import ch.qos.logback.classic.pattern.KeyValuePairConverter;
import ch.qos.logback.classic.pattern.LevelConverter;
import ch.qos.logback.classic.pattern.LineOfCallerConverter;
import ch.qos.logback.classic.pattern.LineSeparatorConverter;
import ch.qos.logback.classic.pattern.LocalSequenceNumberConverter;
import ch.qos.logback.classic.pattern.LoggerConverter;
import ch.qos.logback.classic.pattern.MDCConverter;
import ch.qos.logback.classic.pattern.MarkerConverter;
import ch.qos.logback.classic.pattern.MaskedKeyValuePairConverter;
import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.pattern.MethodOfCallerConverter;
import ch.qos.logback.classic.pattern.MicrosecondConverter;
import ch.qos.logback.classic.pattern.NopThrowableInformationConverter;
import ch.qos.logback.classic.pattern.PrefixCompositeConverter;
import ch.qos.logback.classic.pattern.PropertyConverter;
import ch.qos.logback.classic.pattern.RelativeTimeConverter;
import ch.qos.logback.classic.pattern.RootCauseFirstThrowableProxyConverter;
import ch.qos.logback.classic.pattern.SequenceNumberConverter;
import ch.qos.logback.classic.pattern.ThreadConverter;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.pattern.color.HighlightingCompositeConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import ch.qos.logback.core.pattern.color.BlackCompositeConverter;
import ch.qos.logback.core.pattern.color.BlueCompositeConverter;
import ch.qos.logback.core.pattern.color.BoldBlueCompositeConverter;
import ch.qos.logback.core.pattern.color.BoldCyanCompositeConverter;
import ch.qos.logback.core.pattern.color.BoldGreenCompositeConverter;
import ch.qos.logback.core.pattern.color.BoldMagentaCompositeConverter;
import ch.qos.logback.core.pattern.color.BoldRedCompositeConverter;
import ch.qos.logback.core.pattern.color.BoldWhiteCompositeConverter;
import ch.qos.logback.core.pattern.color.BoldYellowCompositeConverter;
import ch.qos.logback.core.pattern.color.CyanCompositeConverter;
import ch.qos.logback.core.pattern.color.GrayCompositeConverter;
import ch.qos.logback.core.pattern.color.GreenCompositeConverter;
import ch.qos.logback.core.pattern.color.MagentaCompositeConverter;
import ch.qos.logback.core.pattern.color.RedCompositeConverter;
import ch.qos.logback.core.pattern.color.WhiteCompositeConverter;
import ch.qos.logback.core.pattern.color.YellowCompositeConverter;
import ch.qos.logback.core.pattern.parser.Parser;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PatternLayout
extends PatternLayoutBase<ILoggingEvent> {
    public static final Map<String, Supplier<DynamicConverter>> DEFAULT_CONVERTER_SUPPLIER_MAP = new HashMap<String, Supplier<DynamicConverter>>();
    public static final Map<String, String> CONVERTER_CLASS_TO_KEY_MAP = new HashMap<String, String>();
    public static final String HEADER_PREFIX = "#logback.classic pattern: ";

    public PatternLayout() {
        this.postCompileProcessor = new EnsureExceptionHandling();
    }

    @Override
    public Map<String, Supplier<DynamicConverter>> getDefaultConverterSupplierMap() {
        return DEFAULT_CONVERTER_SUPPLIER_MAP;
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        if (!this.isStarted()) {
            return "";
        }
        return this.writeLoopOnConverters(event);
    }

    @Override
    protected String getPresentationHeaderPrefix() {
        return HEADER_PREFIX;
    }

    static {
        DEFAULT_CONVERTER_SUPPLIER_MAP.putAll(Parser.DEFAULT_COMPOSITE_CONVERTER_MAP);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("d", DateConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("date", DateConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(DateConverter.class.getName(), "date");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("ms", MicrosecondConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("micros", MicrosecondConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(MicrosecondConverter.class.getName(), "micros");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("ep", EpochConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("epoch", EpochConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(EpochConverter.class.getName(), "epoch");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("r", RelativeTimeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("relative", RelativeTimeConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(RelativeTimeConverter.class.getName(), "relative");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("level", LevelConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("le", LevelConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("p", LevelConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(LevelConverter.class.getName(), "level");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("t", ThreadConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("thread", ThreadConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(ThreadConverter.class.getName(), "thread");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("lo", LoggerConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("logger", LoggerConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("c", LoggerConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(LoggerConverter.class.getName(), "logger");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("m", MessageConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("msg", MessageConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("message", MessageConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(MessageConverter.class.getName(), "message");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("C", ClassOfCallerConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("class", ClassOfCallerConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(ClassOfCallerConverter.class.getName(), "class");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("M", MethodOfCallerConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("method", MethodOfCallerConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(MethodOfCallerConverter.class.getName(), "method");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("L", LineOfCallerConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("line", LineOfCallerConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(LineOfCallerConverter.class.getName(), "line");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("F", FileOfCallerConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("file", FileOfCallerConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(FileOfCallerConverter.class.getName(), "file");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("X", MDCConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("mdc", MDCConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("ex", ThrowableProxyConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("exception", ThrowableProxyConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("rEx", RootCauseFirstThrowableProxyConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("rootException", RootCauseFirstThrowableProxyConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("throwable", ThrowableProxyConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("xEx", ExtendedThrowableProxyConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("xException", ExtendedThrowableProxyConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("xThrowable", ExtendedThrowableProxyConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("nopex", NopThrowableInformationConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("nopexception", NopThrowableInformationConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("cn", ContextNameConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("contextName", ContextNameConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(ContextNameConverter.class.getName(), "contextName");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("caller", CallerDataConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(CallerDataConverter.class.getName(), "caller");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("marker", MarkerConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(MarkerConverter.class.getName(), "marker");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("kvp", KeyValuePairConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(KeyValuePairConverter.class.getName(), "kvp");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("maskedKvp", MaskedKeyValuePairConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(MaskedKeyValuePairConverter.class.getName(), "maskedKvp");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("property", PropertyConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("n", LineSeparatorConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("black", BlackCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("red", RedCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("green", GreenCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("yellow", YellowCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("blue", BlueCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("magenta", MagentaCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("cyan", CyanCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("white", WhiteCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("gray", GrayCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("boldRed", BoldRedCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("boldGreen", BoldGreenCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("boldYellow", BoldYellowCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("boldBlue", BoldBlueCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("boldMagenta", BoldMagentaCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("boldCyan", BoldCyanCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("boldWhite", BoldWhiteCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("highlight", HighlightingCompositeConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("lsn", LocalSequenceNumberConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(LocalSequenceNumberConverter.class.getName(), "lsn");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("sn", SequenceNumberConverter::new);
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("sequenceNumber", SequenceNumberConverter::new);
        CONVERTER_CLASS_TO_KEY_MAP.put(SequenceNumberConverter.class.getName(), "sequenceNumber");
        DEFAULT_CONVERTER_SUPPLIER_MAP.put("prefix", PrefixCompositeConverter::new);
    }
}

