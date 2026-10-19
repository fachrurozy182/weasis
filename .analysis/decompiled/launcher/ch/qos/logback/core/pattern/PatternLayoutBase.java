/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.pattern;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.ConverterUtil;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.pattern.PostCompileProcessor;
import ch.qos.logback.core.pattern.color.ConverterSupplierByClassName;
import ch.qos.logback.core.pattern.parser.Node;
import ch.qos.logback.core.pattern.parser.Parser;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.StatusManager;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class PatternLayoutBase<E>
extends LayoutBase<E> {
    static final int INTIAL_STRING_BUILDER_SIZE = 256;
    Converter<E> head;
    String pattern;
    protected PostCompileProcessor<E> postCompileProcessor;
    Map<String, Supplier<DynamicConverter>> instanceConverterMap = new HashMap<String, Supplier<DynamicConverter>>();
    protected boolean outputPatternAsHeader = false;

    protected abstract Map<String, Supplier<DynamicConverter>> getDefaultConverterSupplierMap();

    public Map<String, Supplier<DynamicConverter>> getEffectiveConverterMap() {
        Map contextMap;
        HashMap<String, Supplier<DynamicConverter>> effectiveMap = new HashMap<String, Supplier<DynamicConverter>>();
        Map<String, Supplier<DynamicConverter>> defaultConverterSupplierMap = this.getDefaultConverterSupplierMap();
        if (defaultConverterSupplierMap != null) {
            effectiveMap.putAll(defaultConverterSupplierMap);
        }
        this.caterForLegacyConverterMaps(effectiveMap);
        Context context = this.getContext();
        if (context != null && (contextMap = (Map)context.getObject("PATTERN_RULE_REGISTRY_FOR_SUPPLIERS")) != null) {
            effectiveMap.putAll(contextMap);
        }
        effectiveMap.putAll(this.instanceConverterMap);
        return effectiveMap;
    }

    private void caterForLegacyConverterMaps(Map<String, Supplier<DynamicConverter>> effectiveMap) {
        Map mapFromContext = (Map)this.context.getObject("PATTERN_RULE_REGISTRY");
        this.migrateFromStringMapToSupplierMap(mapFromContext, effectiveMap);
    }

    private void migrateFromStringMapToSupplierMap(Map<String, String> legacyMap, Map<String, Supplier<DynamicConverter>> targetSupplierMap) {
        if (legacyMap == null) {
            return;
        }
        for (Map.Entry<String, String> entry : legacyMap.entrySet()) {
            String key = entry.getKey();
            String converterClassName = entry.getValue();
            ConverterSupplierByClassName converterSupplierByClassName = new ConverterSupplierByClassName(key, converterClassName);
            converterSupplierByClassName.setContext(this.getContext());
            targetSupplierMap.put(key, converterSupplierByClassName);
        }
    }

    @Override
    public void start() {
        if (this.pattern == null || this.pattern.length() == 0) {
            this.addError("Empty or null pattern.");
            return;
        }
        try {
            Parser p = new Parser(this.pattern);
            if (this.getContext() != null) {
                p.setContext(this.getContext());
            }
            Node t = p.parse();
            this.head = p.compile(t, this.getEffectiveConverterMap());
            if (this.postCompileProcessor != null) {
                this.postCompileProcessor.process(this.context, this.head);
            }
            ConverterUtil.setContextForConverters(this.getContext(), this.head);
            ConverterUtil.startConverters(this.head);
            super.start();
        }
        catch (ScanException sce) {
            StatusManager sm = this.getContext().getStatusManager();
            sm.add(new ErrorStatus("Failed to parse pattern \"" + this.getPattern() + "\".", this, sce));
        }
    }

    public void setPostCompileProcessor(PostCompileProcessor<E> postCompileProcessor) {
        this.postCompileProcessor = postCompileProcessor;
    }

    protected String writeLoopOnConverters(E event) {
        StringBuilder strBuilder = new StringBuilder(256);
        for (Converter<E> c = this.head; c != null; c = c.getNext()) {
            c.write(strBuilder, event);
        }
        return strBuilder.toString();
    }

    public String getPattern() {
        return this.pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String toString() {
        return this.getClass().getName() + "(\"" + this.getPattern() + "\")";
    }

    public Map<String, Supplier<DynamicConverter>> getInstanceConverterMap() {
        return this.instanceConverterMap;
    }

    protected String getPresentationHeaderPrefix() {
        return "";
    }

    public boolean isOutputPatternAsHeader() {
        return this.outputPatternAsHeader;
    }

    public void setOutputPatternAsHeader(boolean outputPatternAsHeader) {
        this.outputPatternAsHeader = outputPatternAsHeader;
    }

    @Override
    public String getPresentationHeader() {
        if (this.outputPatternAsHeader) {
            return this.getPresentationHeaderPrefix() + this.pattern;
        }
        return super.getPresentationHeader();
    }
}

