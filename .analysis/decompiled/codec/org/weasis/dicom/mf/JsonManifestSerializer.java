/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.json.Json
 *  jakarta.json.stream.JsonGenerator
 *  org.weasis.core.util.StringUtil
 */
package org.weasis.dicom.mf;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.mf.ManifestSerializer;

public class JsonManifestSerializer
implements ManifestSerializer {
    private final JsonGenerator generator;
    private final Deque<Boolean> arrayContext = new ArrayDeque<Boolean>();

    public JsonManifestSerializer(Writer writer) {
        this.generator = Json.createGenerator((Writer)writer);
    }

    @Override
    public void beginDocument() {
        this.generator.writeStartObject();
        this.arrayContext.push(Boolean.FALSE);
    }

    @Override
    public void endDocument() {
        this.generator.writeEnd();
        this.arrayContext.pop();
        this.generator.flush();
    }

    @Override
    public void beginObject(String name) {
        this.startObject(name);
    }

    @Override
    public void beginLeaf(String name) {
        this.startObject(name);
    }

    @Override
    public void attribute(String name, String value) {
        if (StringUtil.hasText((CharSequence)name) && StringUtil.hasText((CharSequence)value)) {
            this.generator.write(name, value);
        }
    }

    @Override
    public void attribute(String name, boolean value) {
        if (StringUtil.hasText((CharSequence)name)) {
            this.generator.write(name, value);
        }
    }

    @Override
    public void schema(String namespace) {
    }

    @Override
    public void beginArray(String name) {
        this.generator.writeStartArray(name);
        this.arrayContext.push(Boolean.TRUE);
    }

    @Override
    public void endArray() {
        this.endElement();
    }

    @Override
    public void endObject() {
        this.endElement();
    }

    private void endElement() {
        this.generator.writeEnd();
        this.arrayContext.pop();
    }

    @Override
    public void endLeaf() {
        this.endObject();
    }

    private void startObject(String name) {
        if (Boolean.TRUE.equals(this.arrayContext.peek())) {
            this.generator.writeStartObject();
        } else {
            this.generator.writeStartObject(name);
        }
        this.arrayContext.push(Boolean.FALSE);
    }
}

