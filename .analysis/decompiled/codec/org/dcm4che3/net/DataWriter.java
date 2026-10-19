/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import org.dcm4che3.net.PDVOutputStream;

public interface DataWriter {
    public void writeTo(PDVOutputStream var1, String var2) throws IOException;
}

