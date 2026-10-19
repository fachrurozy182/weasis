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
import org.dcm4che3.net.service.RetrieveTask;

public class BasicCMoveSCP
extends AbstractDicomService {
    public BasicCMoveSCP(String ... sopClasses) {
        super(sopClasses);
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, Attributes keys) throws IOException {
        if (dimse != Dimse.C_MOVE_RQ) {
            throw new DicomServiceException(529);
        }
        RetrieveTask retrieveTask = this.calculateMatches(as, pc, cmd, keys);
        if (retrieveTask != null) {
            as.getApplicationEntity().getDevice().execute(retrieveTask);
        } else {
            as.tryWriteDimseRSP(pc, Commands.mkCMoveRSP(cmd, 0));
        }
    }

    protected RetrieveTask calculateMatches(Association as, PresentationContext pc, Attributes rq, Attributes keys) throws DicomServiceException {
        return null;
    }
}

