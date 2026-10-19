/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.pattern.parser;

import ch.qos.logback.core.pattern.CompositeConverter;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.pattern.LiteralConverter;
import ch.qos.logback.core.pattern.parser.CompositeNode;
import ch.qos.logback.core.pattern.parser.Node;
import ch.qos.logback.core.pattern.parser.SimpleKeywordNode;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.ErrorStatus;
import java.util.Map;
import java.util.function.Supplier;

class Compiler<E>
extends ContextAwareBase {
    Converter<E> head;
    Converter<E> tail;
    final Node top;
    final Map<String, Supplier<DynamicConverter>> converterMap;

    Compiler(Node top, Map<String, Supplier<DynamicConverter>> converterMap) {
        this.top = top;
        this.converterMap = converterMap;
    }

    Converter<E> compile() {
        this.tail = null;
        this.head = null;
        Node n = this.top;
        while (n != null) {
            switch (n.type) {
                case 0: {
                    this.addToList(new LiteralConverter((String)n.getValue()));
                    break;
                }
                case 2: {
                    CompositeNode cn = (CompositeNode)n;
                    CompositeConverter<E> compositeConverter = this.createCompositeConverter(cn);
                    if (compositeConverter == null) {
                        this.addError("Failed to create converter for [%" + String.valueOf(cn.getValue()) + "] keyword");
                        this.addToList(new LiteralConverter("%PARSER_ERROR[" + String.valueOf(cn.getValue()) + "]"));
                        break;
                    }
                    compositeConverter.setFormattingInfo(cn.getFormatInfo());
                    compositeConverter.setOptionList(cn.getOptions());
                    Compiler<E> childCompiler = new Compiler<E>(cn.getChildNode(), this.converterMap);
                    childCompiler.setContext(this.context);
                    Converter<E> childConverter = childCompiler.compile();
                    compositeConverter.setChildConverter(childConverter);
                    this.addToList(compositeConverter);
                    break;
                }
                case 1: {
                    SimpleKeywordNode kn = (SimpleKeywordNode)n;
                    DynamicConverter<E> dynaConverter = this.createConverter(kn);
                    if (dynaConverter != null) {
                        dynaConverter.setFormattingInfo(kn.getFormatInfo());
                        dynaConverter.setOptionList(kn.getOptions());
                        this.addToList(dynaConverter);
                        break;
                    }
                    LiteralConverter errConveter = new LiteralConverter("%PARSER_ERROR[" + String.valueOf(kn.getValue()) + "]");
                    this.addStatus(new ErrorStatus("[" + String.valueOf(kn.getValue()) + "] is not a valid conversion word", this));
                    this.addToList(errConveter);
                }
            }
            n = n.next;
        }
        return this.head;
    }

    private void addToList(Converter<E> c) {
        if (this.head == null) {
            this.tail = c;
            this.head = this.tail;
        } else {
            this.tail.setNext(c);
            this.tail = c;
        }
    }

    DynamicConverter<E> createConverter(SimpleKeywordNode kn) {
        String keyword = (String)kn.getValue();
        Supplier<DynamicConverter> supplier = this.converterMap.get(keyword);
        if (supplier != null) {
            return supplier.get();
        }
        this.addError("There is no conversion supplier registered for conversion word [" + keyword + "]");
        return null;
    }

    CompositeConverter<E> createCompositeConverter(CompositeNode cn) {
        String keyword = (String)cn.getValue();
        Supplier<DynamicConverter> supplier = this.converterMap.get(keyword);
        if (supplier != null) {
            try {
                return (CompositeConverter)supplier.get();
            }
            catch (ClassCastException e) {
                this.addError("Failed to cast as CompositeConverter for keyword [" + keyword + "]", e);
                return null;
            }
        }
        this.addError("There is no conversion class registered for composite conversion word [" + keyword + "]");
        return null;
    }
}

