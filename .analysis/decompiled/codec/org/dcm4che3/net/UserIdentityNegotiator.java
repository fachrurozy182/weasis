/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import org.dcm4che3.net.Association;
import org.dcm4che3.net.pdu.AAssociateRJ;
import org.dcm4che3.net.pdu.UserIdentityAC;
import org.dcm4che3.net.pdu.UserIdentityRQ;
import org.dcm4che3.util.ByteUtils;

public interface UserIdentityNegotiator {
    default public UserIdentityAC negotiate(Association as, UserIdentityRQ userIdentity) throws AAssociateRJ {
        return userIdentity != null && userIdentity.isPositiveResponseRequested() ? new UserIdentityAC(userIdentity.getType() > 2 ? userIdentity.getPrimaryField() : ByteUtils.EMPTY_BYTES) : null;
    }
}

