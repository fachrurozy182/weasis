/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import java.util.Objects;
import org.dcm4che3.data.Implementation;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.DimseRQHandler;
import org.dcm4che3.net.UserIdentityNegotiator;
import org.dcm4che3.net.pdu.AAssociateAC;
import org.dcm4che3.net.pdu.AAssociateRJ;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.UserIdentityAC;

public class AssociationHandler {
    private UserIdentityNegotiator userIdNegotiator = new UserIdentityNegotiator(){};

    public UserIdentityNegotiator getUserIdNegotiator() {
        return this.userIdNegotiator;
    }

    public void setUserIdNegotiator(UserIdentityNegotiator userIdNegotiator) {
        this.userIdNegotiator = Objects.requireNonNull(userIdNegotiator);
    }

    protected AAssociateAC negotiate(Association as, AAssociateRQ rq) throws IOException {
        if ((rq.getProtocolVersion() & 1) == 0) {
            throw new AAssociateRJ(1, 2, 2);
        }
        if (!rq.getApplicationContext().equals("1.2.840.10008.3.1.1.1")) {
            throw new AAssociateRJ(1, 1, 2);
        }
        ApplicationEntity ae = as.getApplicationEntity();
        if (!(ae != null && ae.getConnections().contains(as.getConnection()) && ae.isInstalled() && ae.isAssociationAcceptor())) {
            throw new AAssociateRJ(1, 1, 7);
        }
        if (!ae.isAcceptedCallingAETitle(rq.getCallingAET())) {
            throw new AAssociateRJ(1, 1, 3);
        }
        UserIdentityAC userIdentity = this.getUserIdNegotiator().negotiate(as, rq.getUserIdentityRQ());
        if (ae.getDevice().isLimitOfAssociationsExceeded(rq)) {
            throw new AAssociateRJ(2, 3, 2);
        }
        return this.makeAAssociateAC(as, rq, userIdentity);
    }

    protected AAssociateAC makeAAssociateAC(Association as, AAssociateRQ rq, UserIdentityAC userIdentity) throws IOException {
        AAssociateAC ac = new AAssociateAC();
        ac.setImplVersionName(Implementation.getVersionName());
        ac.setCalledAET(rq.getCalledAET());
        ac.setCallingAET(rq.getCallingAET());
        Connection conn = as.getConnection();
        ac.setMaxPDULength(conn.getReceivePDULength());
        ac.setMaxOpsInvoked(Association.minZeroAsMax(rq.getMaxOpsInvoked(), conn.getMaxOpsPerformed()));
        ac.setMaxOpsPerformed(Association.minZeroAsMax(rq.getMaxOpsPerformed(), conn.getMaxOpsInvoked()));
        ac.setUserIdentityAC(userIdentity);
        ApplicationEntity ae = as.getApplicationEntity().transferCapabilitiesAE();
        for (PresentationContext rqpc : rq.getPresentationContexts()) {
            ac.addPresentationContext(ae != null ? ae.negotiate(rq, ac, rqpc) : new PresentationContext(rqpc.getPCID(), 3, rqpc.getTransferSyntax()));
        }
        return ac;
    }

    protected void onClose(Association as) {
        DimseRQHandler tmp = as.getApplicationEntity().getDimseRQHandler();
        if (tmp != null) {
            tmp.onClose(as);
        }
    }
}

