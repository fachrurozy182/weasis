/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.Timeout;
import org.dcm4che3.net.pdu.PresentationContext;

public class DimseRSPHandler {
    private final int msgId;
    private PresentationContext pc;
    private volatile Timeout timeout;
    private volatile boolean stopOnPending;
    private volatile boolean canceled;

    public DimseRSPHandler(int msgId) {
        this.msgId = msgId;
    }

    final void setPC(PresentationContext pc) {
        this.pc = pc;
    }

    public final int getMessageID() {
        return this.msgId;
    }

    final void setTimeout(Timeout timeout, boolean stopOnPending) {
        this.timeout = timeout;
        this.stopOnPending = stopOnPending;
    }

    boolean isStopOnPending() {
        return this.stopOnPending;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public void cancel(Association as) throws IOException {
        as.cancel(this.pc, this.msgId);
        this.canceled = true;
    }

    public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
        if (this.stopOnPending || !Status.isPending(cmd.getInt(2304, -1))) {
            this.stopTimeout(as);
        }
    }

    public void onClose(Association as) {
        this.stopTimeout(as);
    }

    void stopTimeout(Association as) {
        if (this.timeout != null) {
            this.timeout.stop();
            this.timeout = null;
        }
    }
}

