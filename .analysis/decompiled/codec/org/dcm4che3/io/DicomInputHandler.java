/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

import java.io.IOException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.io.DicomInputStream;

public interface DicomInputHandler {
    public void readValue(DicomInputStream var1, Attributes var2) throws IOException;

    public void readValue(DicomInputStream var1, Sequence var2) throws IOException;

    public void readValue(DicomInputStream var1, Fragments var2) throws IOException;

    public void startDataset(DicomInputStream var1) throws IOException;

    public void endDataset(DicomInputStream var1) throws IOException;
}

