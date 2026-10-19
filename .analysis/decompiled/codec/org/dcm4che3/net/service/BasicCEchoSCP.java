/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.service;

import java.io.IOException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.AbstractDicomService;
import org.dcm4che3.net.service.DicomServiceException;

public class BasicCEchoSCP
extends AbstractDicomService {
    public BasicCEchoSCP() {
        super("1.2.840.10008.1.1");
    }

    public BasicCEchoSCP(String ... sopClasses) {
        super(sopClasses);
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, Attributes data) throws IOException {
        if (dimse != Dimse.C_ECHO_RQ) {
            throw new DicomServiceException(529);
        }
        as.tryWriteDimseRSP(pc, Commands.mkEchoRSP(cmd, 0));
    }
}

