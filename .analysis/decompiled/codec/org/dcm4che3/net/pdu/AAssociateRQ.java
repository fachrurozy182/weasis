/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.pdu;

import org.dcm4che3.net.pdu.AAssociateRQAC;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.UserIdentityAC;

public class AAssociateRQ
extends AAssociateRQAC {
    @Override
    public void setUserIdentityAC(UserIdentityAC userIdentityAC) {
        throw new UnsupportedOperationException();
    }

    public boolean containsPresentationContextFor(String as) {
        for (PresentationContext pc : this.pcs) {
            if (!as.equals(pc.getAbstractSyntax())) continue;
            return true;
        }
        return false;
    }

    public boolean containsPresentationContextFor(String as, String ts) {
        for (PresentationContext pc : this.pcs) {
            if (!as.equals(pc.getAbstractSyntax()) || !pc.containsTransferSyntax(ts)) continue;
            return true;
        }
        return false;
    }

    public boolean addPresentationContextFor(String as, String ts) {
        if (this.containsPresentationContextFor(as, ts)) {
            return false;
        }
        int pcid = this.getNumberOfPresentationContexts() * 2 + 1;
        this.addPresentationContext(new PresentationContext(pcid, as, ts));
        return true;
    }

    public String toString() {
        return this.promptTo(new StringBuilder(512)).toString();
    }

    StringBuilder promptTo(StringBuilder sb) {
        return this.promptTo("A-ASSOCIATE-RQ[", sb);
    }
}

