/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.dcm4che3.io.SAXParserFactoryHolder;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXReader {
    public static Attributes parse(String uri, Attributes attrs) throws ParserConfigurationException, SAXException, IOException {
        if (attrs == null) {
            attrs = new Attributes();
        }
        SAXParser parser = SAXParserFactoryHolder.factory.newSAXParser();
        parser.parse(uri, (DefaultHandler)new ContentHandlerAdapter(attrs));
        return attrs;
    }

    public static Attributes parse(InputStream is, Attributes attrs) throws ParserConfigurationException, SAXException, IOException {
        if (attrs == null) {
            attrs = new Attributes();
        }
        SAXParser parser = SAXParserFactoryHolder.factory.newSAXParser();
        parser.parse(is, (DefaultHandler)new ContentHandlerAdapter(attrs));
        return attrs;
    }

    public static Attributes parse(String uri) throws ParserConfigurationException, SAXException, IOException {
        return SAXReader.parse(uri, null);
    }

    public static Attributes parse(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        return SAXReader.parse(is, null);
    }
}

