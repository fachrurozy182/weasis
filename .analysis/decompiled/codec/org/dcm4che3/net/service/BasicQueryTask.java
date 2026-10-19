/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.service;

import java.io.IOException;
import java.util.NoSuchElementException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.QueryTask;

public class BasicQueryTask
implements QueryTask {
    protected final Association as;
    protected final PresentationContext pc;
    protected final Attributes rq;
    protected final Attributes keys;
    protected volatile boolean canceled;
    protected boolean optionalKeysNotSupported = false;

    public BasicQueryTask(Association as, PresentationContext pc, Attributes rq, Attributes keys) {
        this.as = as;
        this.pc = pc;
        this.rq = rq;
        this.keys = keys;
    }

    public boolean isOptionalKeysNotSupported() {
        return this.optionalKeysNotSupported;
    }

    public void setOptionalKeysNotSupported(boolean optionalKeysNotSupported) {
        this.optionalKeysNotSupported = optionalKeysNotSupported;
    }

    @Override
    public void onCancelRQ(Association as) {
        this.canceled = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        try {
            int msgId = this.rq.getInt(272, -1);
            this.as.addCancelRQHandler(msgId, this);
            try {
                while (!this.canceled && this.hasMoreMatches()) {
                    Attributes match = this.adjust(this.nextMatch());
                    if (match == null) continue;
                    int status = this.optionalKeysNotSupported ? 65281 : 65280;
                    this.as.writeDimseRSP(this.pc, Commands.mkCFindRSP(this.rq, status), match);
                }
                int status = this.canceled ? 65024 : 0;
                this.as.writeDimseRSP(this.pc, Commands.mkCFindRSP(this.rq, status));
            }
            catch (DicomServiceException e) {
                Attributes rsp = e.mkRSP(32800, msgId);
                this.as.writeDimseRSP(this.pc, rsp, e.getDataset());
            }
            finally {
                this.as.removeCancelRQHandler(msgId);
                this.close();
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    protected void close() {
    }

    protected Attributes nextMatch() throws DicomServiceException {
        throw new NoSuchElementException();
    }

    protected boolean hasMoreMatches() throws DicomServiceException {
        return false;
    }

    protected Attributes adjust(Attributes match) throws DicomServiceException {
        String[] ss;
        if (match == null) {
            return null;
        }
        Attributes filtered = new Attributes(match.size());
        if (!this.keys.contains(524293) && (ss = match.getStrings(524293)) != null) {
            filtered.setString(524293, VR.CS, ss);
        }
        filtered.addSelected(match, this.keys);
        filtered.supplementEmpty(this.keys);
        return filtered;
    }
}

