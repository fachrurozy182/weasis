/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.service;

import java.io.IOException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.DicomService;

public abstract class AbstractDicomService
implements DicomService {
    private final String[] sopClasses;

    protected AbstractDicomService(String ... sopClasses) {
        this.sopClasses = (String[])sopClasses.clone();
    }

    @Override
    public String[] getSOPClasses() {
        return this.sopClasses;
    }

    @Override
    public void onClose(Association as) {
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, PDVInputStream data) throws IOException {
        this.onDimseRQ(as, pc, dimse, cmd, this.readDataset(pc, data));
    }

    private Attributes readDataset(PresentationContext pc, PDVInputStream data) throws IOException {
        if (data == null) {
            return null;
        }
        Attributes dataset = data.readDataset(pc.getTransferSyntax());
        Dimse.LOG.debug("Dataset:\n{}", (Object)dataset);
        return dataset;
    }

    protected abstract void onDimseRQ(Association var1, PresentationContext var2, Dimse var3, Attributes var4, Attributes var5) throws IOException;
}

