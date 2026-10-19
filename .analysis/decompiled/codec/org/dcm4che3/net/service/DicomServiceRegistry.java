/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.service;

import java.io.IOException;
import java.util.HashMap;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.DimseRQHandler;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.pdu.CommonExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.DicomService;
import org.dcm4che3.net.service.DicomServiceApplicationException;
import org.dcm4che3.net.service.DicomServiceException;

public class DicomServiceRegistry
implements DimseRQHandler {
    private final HashMap<String, DimseRQHandler> services = new HashMap();

    public void addDicomService(DicomService service) {
        this.addDimseRQHandler(service, service.getSOPClasses());
    }

    public synchronized void addDimseRQHandler(DimseRQHandler service, String ... sopClasses) {
        for (String uid : sopClasses) {
            this.services.put(uid, service);
        }
    }

    public void removeDicomService(DicomService service) {
        this.removeDimseRQHandler(service.getSOPClasses());
    }

    public synchronized void removeDimseRQHandler(String ... sopClasses) {
        for (String uid : sopClasses) {
            this.services.remove(uid);
        }
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, PDVInputStream data) throws IOException {
        try {
            this.lookupService(as, dimse, cmd).onDimseRQ(as, pc, dimse, cmd, data);
        }
        catch (DicomServiceApplicationException e) {
            Association.LOG.info("{}: processing {} failed. Caused by: {}", new Object[]{as, dimse.toString(cmd, pc.getPCID(), pc.getTransferSyntax()), e.getLocalizedMessage()});
            this.rspForDimseRQException(as, pc, dimse, cmd, e);
        }
        catch (DicomServiceException e) {
            Association.LOG.info("{}: processing {} failed. Caused by:\t", new Object[]{as, dimse.toString(cmd, pc.getPCID(), pc.getTransferSyntax()), e});
            this.rspForDimseRQException(as, pc, dimse, cmd, e);
        }
    }

    private void rspForDimseRQException(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, DicomServiceException e) {
        Attributes rsp = e.mkRSP(dimse.commandFieldOfRSP(), cmd.getInt(272, 0));
        as.tryWriteDimseRSP(pc, rsp, e.getDataset());
    }

    private DimseRQHandler lookupService(Association as, Dimse dimse, Attributes cmd) throws DicomServiceException {
        String cuid = cmd.getString(dimse.tagOfSOPClassUID());
        if (cuid == null) {
            throw new DicomServiceException(530);
        }
        DimseRQHandler service = this.services.get(cuid);
        if (service != null) {
            return service;
        }
        if (dimse == Dimse.C_STORE_RQ) {
            CommonExtendedNegotiation commonExtNeg = as.getCommonExtendedNegotiationFor(cuid);
            if (commonExtNeg != null) {
                for (String uid : commonExtNeg.getRelatedGeneralSOPClassUIDs()) {
                    service = this.services.get(uid);
                    if (service == null) continue;
                    return service;
                }
                service = this.services.get(commonExtNeg.getServiceClassUID());
                if (service != null) {
                    return service;
                }
            }
            if ((service = this.services.get("*")) != null) {
                return service;
            }
        }
        throw new DicomServiceException(dimse.isCService() ? 290 : 280);
    }

    @Override
    public void onClose(Association as) {
        for (DimseRQHandler service : this.services.values()) {
            service.onClose(as);
        }
    }
}

