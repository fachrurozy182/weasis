/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.DataWriter;
import org.dcm4che3.net.PDVOutputStream;

public class DataWriterAdapter
implements DataWriter {
    private final Attributes data;

    public DataWriterAdapter(Attributes data) {
        if (data == null) {
            throw new NullPointerException();
        }
        this.data = data;
    }

    public static DataWriterAdapter forAttributes(Attributes data) {
        return data != null ? new DataWriterAdapter(data) : null;
    }

    @Override
    public void writeTo(PDVOutputStream out, String tsuid) throws IOException {
        DicomOutputStream dos = new DicomOutputStream(out, tsuid);
        dos.writeDataset(null, this.data);
        dos.finish();
    }

    public final Attributes getDataset() {
        return this.data;
    }
}

