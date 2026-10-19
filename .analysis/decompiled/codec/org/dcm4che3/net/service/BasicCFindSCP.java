/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.service;

import java.io.IOException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.AbstractDicomService;
import org.dcm4che3.net.service.BasicQueryTask;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.QueryTask;

public class BasicCFindSCP
extends AbstractDicomService {
    public BasicCFindSCP(String ... sopClasses) {
        super(sopClasses);
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes rq, Attributes keys) throws IOException {
        if (dimse != Dimse.C_FIND_RQ) {
            throw new DicomServiceException(529);
        }
        QueryTask queryTask = this.calculateMatches(as, pc, rq, keys);
        as.getApplicationEntity().getDevice().execute(queryTask);
    }

    protected QueryTask calculateMatches(Association as, PresentationContext pc, Attributes rq, Attributes keys) throws DicomServiceException {
        return new BasicQueryTask(as, pc, rq, keys);
    }
}

