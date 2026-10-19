/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.dicom.mf;

import java.io.IOException;

public interface ManifestSerializer {
    public void beginDocument() throws IOException;

    public void endDocument() throws IOException;

    public void beginObject(String var1) throws IOException;

    public void endObject() throws IOException;

    public void beginLeaf(String var1) throws IOException;

    public void endLeaf() throws IOException;

    public void attribute(String var1, String var2) throws IOException;

    public void attribute(String var1, boolean var2) throws IOException;

    public void schema(String var1) throws IOException;

    public void beginArray(String var1) throws IOException;

    public void endArray() throws IOException;
}

