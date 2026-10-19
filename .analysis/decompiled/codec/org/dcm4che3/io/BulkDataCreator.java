/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.io;

import java.io.IOException;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.io.DicomInputStream;

public interface BulkDataCreator {
    public BulkData createBulkData(DicomInputStream var1) throws IOException;
}

