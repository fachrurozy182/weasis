/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import org.dcm4che3.data.BulkData;

public class BulkDataWithPrefix
extends BulkData {
    private final byte[] prefix;

    public BulkDataWithPrefix(String uri, long offset, int length, boolean bigEndian, byte ... prefix) {
        super(uri, offset, length + prefix.length, bigEndian);
        this.prefix = (byte[])prefix.clone();
    }

    @Override
    public InputStream openStream() throws IOException {
        return new SequenceInputStream(new ByteArrayInputStream(this.prefix), super.openStream());
    }
}

