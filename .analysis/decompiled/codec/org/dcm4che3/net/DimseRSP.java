/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;

public interface DimseRSP {
    public boolean next() throws IOException, InterruptedException;

    public Attributes getCommand();

    public Attributes getDataset();

    public void cancel(Association var1) throws IOException;
}

