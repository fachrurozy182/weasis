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

public class BasicMPPSSCP
extends AbstractDicomService {
    public BasicMPPSSCP() {
        super("1.2.840.10008.3.1.2.3.3");
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes rq, Attributes rqAttrs) throws IOException {
        switch (dimse) {
            case N_CREATE_RQ: {
                this.onNCreateRQ(as, pc, rq, rqAttrs);
                break;
            }
            case N_SET_RQ: {
                this.onNSetRQ(as, pc, rq, rqAttrs);
                break;
            }
            default: {
                throw new DicomServiceException(529);
            }
        }
    }

    protected void onNCreateRQ(Association as, PresentationContext pc, Attributes rq, Attributes rqAttrs) throws IOException {
        Attributes rsp = Commands.mkNCreateRSP(rq, 0);
        Attributes rspAttrs = this.create(as, rq, rqAttrs, rsp);
        as.tryWriteDimseRSP(pc, rsp, rspAttrs);
    }

    protected Attributes create(Association as, Attributes rq, Attributes rqAttrs, Attributes rsp) throws DicomServiceException {
        return null;
    }

    protected void onNSetRQ(Association as, PresentationContext pc, Attributes rq, Attributes rqAttrs) throws IOException {
        Attributes rsp = Commands.mkNSetRSP(rq, 0);
        Attributes rspAttrs = this.set(as, rq, rqAttrs, rsp);
        as.tryWriteDimseRSP(pc, rsp, rspAttrs);
    }

    protected Attributes set(Association as, Attributes rq, Attributes rqAttrs, Attributes rsp) throws DicomServiceException {
        return null;
    }

    public static void mayNoLongerBeUpdated() throws DicomServiceException {
        throw new DicomServiceException(272, "Performed Procedure Step Object may no longer be updated").setErrorID(42768);
    }
}

