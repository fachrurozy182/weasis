/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.service;

import java.io.IOException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.AbstractDicomService;
import org.dcm4che3.net.service.DicomServiceException;

public class BasicCStoreSCP
extends AbstractDicomService {
    public BasicCStoreSCP() {
        super("*");
    }

    public BasicCStoreSCP(String ... sopClasses) {
        super(sopClasses);
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes rq, PDVInputStream data) throws IOException {
        if (dimse != Dimse.C_STORE_RQ) {
            throw new DicomServiceException(529);
        }
        Attributes rsp = Commands.mkCStoreRSP(rq, 0);
        this.store(as, pc, rq, data, rsp);
        as.tryWriteDimseRSP(pc, rsp);
    }

    protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp) throws IOException {
    }

    @Override
    protected void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, Attributes data) throws IOException {
        throw new UnsupportedOperationException();
    }
}

