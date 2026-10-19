/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.weasis.core.util.EscapeChars
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.mf;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import org.weasis.core.util.EscapeChars;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.mf.ManifestSerializer;

public class XmlManifestSerializer
implements ManifestSerializer {
    private final Writer writer;
    private final String charsetEncoding;
    private final Deque<Element> stack = new ArrayDeque<Element>();

    public XmlManifestSerializer(Writer writer) {
        this(writer, StandardCharsets.UTF_8.name());
    }

    public XmlManifestSerializer(Writer writer, String charsetEncoding) {
        this.writer = writer;
        this.charsetEncoding = charsetEncoding;
    }

    @Override
    public void beginDocument() throws IOException {
        this.writer.append("<?xml version=\"1.0\" encoding=\"").append(this.charsetEncoding).append("\" ?>");
    }

    @Override
    public void endDocument() throws IOException {
        this.writer.append("\n");
    }

    @Override
    public void beginObject(String name) throws IOException {
        this.startElement(name);
    }

    @Override
    public void beginLeaf(String name) throws IOException {
        this.startElement(name);
    }

    @Override
    public void attribute(String name, String value) throws IOException {
        if (StringUtil.hasText((CharSequence)name) && StringUtil.hasText((CharSequence)value)) {
            this.writer.append(name).append("=\"").append(EscapeChars.forXML((String)value)).append("\" ");
        }
    }

    @Override
    public void attribute(String name, boolean value) throws IOException {
        if (StringUtil.hasText((CharSequence)name)) {
            this.writer.append(name).append("=\"").append(Boolean.toString(value)).append("\" ");
        }
    }

    @Override
    public void schema(String namespace) throws IOException {
        if (StringUtil.hasText((CharSequence)namespace)) {
            this.writer.append(namespace);
        }
    }

    @Override
    public void beginArray(String name) {
    }

    @Override
    public void endArray() {
    }

    @Override
    public void endObject() throws IOException {
        Element element = this.stack.pop();
        if (element.startTagOpen) {
            this.writer.append(">");
        }
        this.writer.append("\n</").append(element.name).append(">");
    }

    @Override
    public void endLeaf() throws IOException {
        this.stack.pop();
        this.writer.append("/>");
    }

    private void startElement(String name) throws IOException {
        this.closeParentStartTag();
        this.writer.append("\n<").append(name).append(" ");
        this.stack.push(new Element(name));
    }

    private void closeParentStartTag() throws IOException {
        Element parent = this.stack.peek();
        if (parent != null && parent.startTagOpen) {
            this.writer.append(">");
            parent.startTagOpen = false;
        }
    }

    private static final class Element {
        private final String name;
        private boolean startTagOpen = true;

        private Element(String name) {
            this.name = name;
        }
    }
}

