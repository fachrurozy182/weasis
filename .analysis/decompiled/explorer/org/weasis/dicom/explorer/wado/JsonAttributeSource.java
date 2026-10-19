/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.json.JsonObject
 *  jakarta.json.JsonString
 *  jakarta.json.JsonValue
 *  jakarta.json.JsonValue$ValueType
 *  org.weasis.core.api.media.data.AttributeSource
 */
package org.weasis.dicom.explorer.wado;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import org.weasis.core.api.media.data.AttributeSource;

record JsonAttributeSource(JsonObject object) implements AttributeSource
{
    public String getAttribute(String name) {
        JsonValue value = (JsonValue)this.object.get((Object)name);
        if (value == null) {
            return null;
        }
        return switch (value.getValueType()) {
            case JsonValue.ValueType.STRING -> ((JsonString)value).getString();
            case JsonValue.ValueType.NUMBER -> value.toString();
            case JsonValue.ValueType.TRUE -> Boolean.TRUE.toString();
            case JsonValue.ValueType.FALSE -> Boolean.FALSE.toString();
            default -> null;
        };
    }
}

