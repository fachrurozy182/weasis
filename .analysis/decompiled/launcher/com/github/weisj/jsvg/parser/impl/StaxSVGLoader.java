/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser.impl;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.logging.Logger;
import com.github.weisj.jsvg.logging.impl.LogFactory;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.XMLInput;
import com.github.weisj.jsvg.parser.impl.InputStreamXMLInput;
import com.github.weisj.jsvg.parser.impl.NodeSupplier;
import com.github.weisj.jsvg.parser.impl.SVGDocumentBuilder;
import com.github.weisj.jsvg.util.supplier.LazySupplier;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.function.Supplier;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StaxSVGLoader {
    private static final Logger LOGGER = LogFactory.createLogger(StaxSVGLoader.class);
    private static final String SVG_NAMESPACE_URI = "http://www.w3.org/2000/svg";
    private static final String XLINK_NAMESPACE_URI = "http://www.w3.org/1999/xlink";
    @NotNull
    private static final NodeSupplier NODE_SUPPLIER = new NodeSupplier();
    @NotNull
    private final @NotNull Supplier<@NotNull XMLInputFactory> xmlInputFactory = new LazySupplier<XMLInputFactory>(() -> {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty("javax.xml.stream.supportDTD", false);
        factory.setProperty("javax.xml.stream.isReplacingEntityReferences", false);
        factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
        return factory;
    });

    @Nullable
    SVGDocumentBuilder parse(@NotNull InputStream inputStream, @Nullable URI xmlBase, @NotNull LoaderContext loaderContext) throws XMLStreamException {
        return this.parse(this.createXMLInput(inputStream), xmlBase, loaderContext);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Nullable
    SVGDocumentBuilder parse(@NotNull XMLInput xmlInput, @Nullable URI xmlBase, @NotNull LoaderContext loaderContext) throws XMLStreamException {
        try (XMLEventReader reader = null;){
            reader = xmlInput.createReader();
            SVGDocumentBuilder builder = new SVGDocumentBuilder(xmlBase, loaderContext, NODE_SUPPLIER);
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                switch (event.getEventType()) {
                    case 7: {
                        builder.startDocument();
                        break;
                    }
                    case 8: {
                        builder.endDocument();
                        break;
                    }
                    case 1: {
                        StartElement element = event.asStartElement();
                        String uri = element.getName().getNamespaceURI();
                        if (uri != null && !uri.isEmpty() && !SVG_NAMESPACE_URI.equals(uri)) {
                            StaxSVGLoader.skipElement(reader);
                            break;
                        }
                        HashMap<String, String> attributes = new HashMap<String, String>();
                        Iterator<Attribute> attrs = element.getAttributes();
                        while (attrs.hasNext()) {
                            Attribute attr = attrs.next();
                            attributes.put(StaxSVGLoader.qualifiedName(attr.getName()), attr.getValue().trim());
                        }
                        if (builder.startElement(StaxSVGLoader.qualifiedName(element.getName(), MakeLowerCase.YES), attributes)) break;
                        StaxSVGLoader.skipElement(reader);
                        break;
                    }
                    case 2: {
                        builder.endElement(StaxSVGLoader.qualifiedName(event.asEndElement().getName(), MakeLowerCase.YES));
                        break;
                    }
                    case 4: 
                    case 12: {
                        char[] data = event.asCharacters().getData().toCharArray();
                        builder.addTextContent(data, 0, data.length);
                        break;
                    }
                }
            }
            SVGDocumentBuilder sVGDocumentBuilder = builder;
            return sVGDocumentBuilder;
        }
        return null;
    }

    @Nullable
    public SVGDocument load(@NotNull XMLInput xmlInput, @Nullable URI xmlBase, @NotNull LoaderContext loaderContext) throws XMLStreamException {
        SVGDocumentBuilder builder = this.parse(xmlInput, xmlBase, loaderContext);
        if (builder == null) {
            return null;
        }
        return builder.build();
    }

    private static void skipElement(@NotNull XMLEventReader reader) throws XMLStreamException {
        int elementCount = 1;
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                ++elementCount;
            } else if (event.isEndElement()) {
                --elementCount;
            }
            if (elementCount != 0) continue;
            return;
        }
    }

    @NotNull
    public XMLInput createXMLInput(@NotNull InputStream inputStream) {
        return new InputStreamXMLInput(this.xmlInputFactory.get(), inputStream);
    }

    @NotNull
    private static String qualifiedName(@NotNull QName name, MakeLowerCase makeLowerCase) {
        String qName = StaxSVGLoader.qualifiedNameImpl(name);
        if (makeLowerCase == MakeLowerCase.YES) {
            return qName.toLowerCase(Locale.ROOT);
        }
        return qName;
    }

    @NotNull
    private static String qualifiedName(@NotNull QName name) {
        return StaxSVGLoader.qualifiedName(name, MakeLowerCase.NO);
    }

    @NotNull
    private static String qualifiedNameImpl(@NotNull QName name) {
        String prefix = name.getPrefix();
        String localName = name.getLocalPart();
        if (prefix == null) {
            return localName;
        }
        if (prefix.isEmpty()) {
            return localName;
        }
        if (SVG_NAMESPACE_URI.equals(name.getNamespaceURI())) {
            return localName;
        }
        if (XLINK_NAMESPACE_URI.equals(name.getNamespaceURI())) {
            return "xlink:" + localName;
        }
        return prefix + ":" + localName;
    }

    private static enum MakeLowerCase {
        YES,
        NO;

    }
}

