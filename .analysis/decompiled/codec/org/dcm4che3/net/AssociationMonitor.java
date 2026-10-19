/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import org.dcm4che3.net.Association;
import org.dcm4che3.net.pdu.AAssociateRJ;

public interface AssociationMonitor {
    public void onAssociationEstablished(Association var1);

    public void onAssociationFailed(Association var1, Throwable var2);

    public void onAssociationRejected(Association var1, AAssociateRJ var2);

    public void onAssociationAccepted(Association var1);
}

