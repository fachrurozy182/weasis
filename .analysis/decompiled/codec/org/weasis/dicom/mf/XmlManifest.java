/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.mf;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public interface XmlManifest {
    public String xmlManifest(String var1);

    default public String getCharsetEncoding() {
        return StandardCharsets.UTF_8.name();
    }

    public void writeManifest(Writer var1, String var2) throws IOException;
}

