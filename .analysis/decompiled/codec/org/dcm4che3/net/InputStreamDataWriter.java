/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import java.io.InputStream;
import org.dcm4che3.net.DataWriter;
import org.dcm4che3.net.PDVOutputStream;

public class InputStreamDataWriter
implements DataWriter {
    private final InputStream in;

    public InputStreamDataWriter(InputStream in) {
        if (in == null) {
            throw new NullPointerException();
        }
        this.in = in;
    }

    @Override
    public void writeTo(PDVOutputStream out, String tsuid) throws IOException {
        out.copyFrom(this.in);
        this.in.close();
    }
}

