/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.media.data;

import javax.xml.stream.XMLStreamReader;
import org.weasis.core.api.media.data.AttributeSource;

public record XmlAttributeSource(XMLStreamReader reader) implements AttributeSource
{
    @Override
    public String getAttribute(String name) {
        return this.reader.getAttributeValue(null, name);
    }
}

