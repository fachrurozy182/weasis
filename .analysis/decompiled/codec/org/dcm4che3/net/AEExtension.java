/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.Serializable;
import org.dcm4che3.net.ApplicationEntity;

public class AEExtension
implements Serializable {
    private static final long serialVersionUID = -8287458555186708798L;
    protected ApplicationEntity ae;

    public final ApplicationEntity getApplicationEntity() {
        return this.ae;
    }

    void setApplicationEntity(ApplicationEntity ae) {
        if (ae != null && this.ae != null) {
            throw new IllegalStateException("already owned by AE: " + ae.getAETitle());
        }
        this.ae = ae;
    }

    public void reconfigure(AEExtension from) {
    }
}

