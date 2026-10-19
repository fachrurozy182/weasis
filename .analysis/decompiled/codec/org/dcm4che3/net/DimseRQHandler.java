/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.pdu.PresentationContext;

public interface DimseRQHandler {
    public void onDimseRQ(Association var1, PresentationContext var2, Dimse var3, Attributes var4, PDVInputStream var5) throws IOException;

    public void onClose(Association var1);
}

