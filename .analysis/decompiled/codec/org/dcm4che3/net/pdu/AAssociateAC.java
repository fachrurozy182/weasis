/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.pdu;

import org.dcm4che3.net.pdu.AAssociateRQAC;
import org.dcm4che3.net.pdu.UserIdentityRQ;

public class AAssociateAC
extends AAssociateRQAC {
    @Override
    public void setUserIdentityRQ(UserIdentityRQ userIdentityRQ) {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return this.promptTo(new StringBuilder(512)).toString();
    }

    StringBuilder promptTo(StringBuilder sb) {
        return this.promptTo("A-ASSOCIATE-AC[", sb);
    }
}

