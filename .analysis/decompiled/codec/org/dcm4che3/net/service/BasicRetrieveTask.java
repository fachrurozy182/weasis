/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.net.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.DataWriter;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.InputStreamDataWriter;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4che3.net.service.RetrieveTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicRetrieveTask<T extends InstanceLocator>
implements RetrieveTask {
    protected static final Logger LOG = LoggerFactory.getLogger(BasicRetrieveTask.class);
    protected final Dimse rq;
    protected final Association rqas;
    protected final Association storeas;
    protected final PresentationContext pc;
    protected final Attributes rqCmd;
    protected final int msgId;
    protected final int priority;
    protected int status = 0;
    protected boolean pendingRSP;
    protected int pendingRSPInterval;
    protected boolean canceled;
    protected final List<T> insts;
    protected final List<T> completed;
    protected final List<T> warning;
    protected final List<T> failed;
    protected int outstandingRSP = 0;
    protected Object outstandingRSPLock = new Object();
    private ScheduledFuture<?> writePendingRSP;

    public BasicRetrieveTask(Dimse rq, Association rqas, PresentationContext pc, Attributes rqCmd, List<T> insts, Association storeas) {
        this.rq = rq;
        this.rqas = rqas;
        this.storeas = storeas;
        this.pc = pc;
        this.rqCmd = rqCmd;
        this.insts = insts;
        this.msgId = rqCmd.getInt(272, -1);
        this.priority = rqCmd.getInt(1792, 0);
        this.completed = new ArrayList<T>(insts.size());
        this.warning = new ArrayList<T>(insts.size());
        this.failed = new ArrayList<T>(insts.size());
    }

    public void setSendPendingRSP(boolean pendingRSP) {
        this.pendingRSP = pendingRSP;
    }

    public void setSendPendingRSPInterval(int pendingRSPInterval) {
        this.pendingRSPInterval = pendingRSPInterval;
    }

    public boolean isCMove() {
        return this.rq == Dimse.C_MOVE_RQ;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public int getStatus() {
        return this.status;
    }

    public Association getRequestAssociation() {
        return this.rqas;
    }

    public Association getStoreAssociation() {
        return this.storeas;
    }

    public List<T> getCompleted() {
        return this.completed;
    }

    public List<T> getWarning() {
        return this.warning;
    }

    public List<T> getFailed() {
        return this.failed;
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
        this.rqas.addCancelRQHandler(this.msgId, this);
        try {
            if (this.pendingRSPInterval > 0) {
                this.startWritePendingRSP();
            }
            Iterator<T> iter = this.insts.iterator();
            while (iter.hasNext()) {
                DataWriter dataWriter;
                String tsuid;
                InstanceLocator inst = (InstanceLocator)iter.next();
                if (this.canceled) {
                    this.status = 65024;
                    break;
                }
                if (this.pendingRSP) {
                    this.writePendingRSP();
                }
                try {
                    tsuid = this.selectTransferSyntaxFor(this.storeas, inst);
                    dataWriter = this.createDataWriter(inst, tsuid);
                }
                catch (Exception e) {
                    this.status = 45056;
                    LOG.info("{}: Unable to retrieve {}/{} to {}", new Object[]{this.rqas, UID.nameOf(inst.cuid), UID.nameOf(inst.tsuid), this.storeas.getRemoteAET(), e});
                    this.failed.add(inst);
                    continue;
                }
                try {
                    this.cstore(this.storeas, inst, tsuid, dataWriter);
                }
                catch (Exception e) {
                    this.status = 42754;
                    LOG.warn("{}: Unable to perform sub-operation on association to {}", new Object[]{this.rqas, this.storeas.getRemoteAET(), e});
                    this.failed.add(inst);
                    while (iter.hasNext()) {
                        this.failed.add((InstanceLocator)iter.next());
                    }
                }
            }
            this.waitForOutstandingCStoreRSP(this.storeas);
            if (this.isCMove()) {
                this.releaseStoreAssociation(this.storeas);
            }
            this.stopWritePendingRSP();
            this.writeRSP(this.status);
        }
        finally {
            this.rqas.removeCancelRQHandler(this.msgId);
            try {
                this.close();
            }
            catch (Throwable e) {
                LOG.warn("Exception thrown by {}.close()", (Object)this.getClass().getName(), (Object)e);
            }
        }
    }

    private void startWritePendingRSP() {
        this.writePendingRSP = this.rqas.getApplicationEntity().getDevice().scheduleAtFixedRate(new Runnable(){

            @Override
            public void run() {
                BasicRetrieveTask.this.writePendingRSP();
            }
        }, 0L, this.pendingRSPInterval, TimeUnit.SECONDS);
    }

    private void stopWritePendingRSP() {
        if (this.writePendingRSP != null) {
            this.writePendingRSP.cancel(false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void waitForOutstandingCStoreRSP(Association storeas) {
        try {
            Object object = this.outstandingRSPLock;
            synchronized (object) {
                while (this.outstandingRSP > 0) {
                    this.outstandingRSPLock.wait();
                }
            }
        }
        catch (InterruptedException e) {
            LOG.warn("{}: failed to wait for outstanding RSP on association to {}", new Object[]{this.rqas, storeas.getRemoteAET(), e});
        }
    }

    protected void releaseStoreAssociation(Association storeas) {
        try {
            if (storeas.isReadyForDataTransfer()) {
                storeas.release();
            }
        }
        catch (IOException e) {
            LOG.warn("{}: failed to release association to {}", new Object[]{this.rqas, storeas.getRemoteAET(), e});
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void cstore(Association storeas, T inst, String tsuid, DataWriter dataWriter) throws IOException, InterruptedException {
        CStoreRSPHandler rspHandler = new CStoreRSPHandler(this, storeas.nextMessageID(), inst);
        if (this.isCMove()) {
            storeas.cstore(((InstanceLocator)inst).cuid, ((InstanceLocator)inst).iuid, this.priority, this.rqas.getRemoteAET(), this.msgId, dataWriter, tsuid, rspHandler);
        } else {
            storeas.cstore(((InstanceLocator)inst).cuid, ((InstanceLocator)inst).iuid, this.priority, dataWriter, tsuid, rspHandler);
        }
        Object object = this.outstandingRSPLock;
        synchronized (object) {
            ++this.outstandingRSP;
        }
    }

    protected String selectTransferSyntaxFor(Association storeas, T inst) throws Exception {
        return ((InstanceLocator)inst).tsuid;
    }

    protected DataWriter createDataWriter(T inst, String tsuid) throws Exception {
        DicomInputStream in = new DicomInputStream(((InstanceLocator)inst).getFile());
        in.readFileMetaInformation();
        return new InputStreamDataWriter(in);
    }

    public void writePendingRSP() {
        this.writeRSP(65280);
    }

    private void writeRSP(int status) {
        Attributes cmd = Commands.mkRSP(this.rqCmd, status, this.rq);
        if (status == 65280 || status == 65024) {
            cmd.setInt(4128, VR.US, this.remaining());
        }
        cmd.setInt(4129, VR.US, this.completed.size());
        cmd.setInt(4130, VR.US, this.failed.size());
        cmd.setInt(4131, VR.US, this.warning.size());
        Attributes data = null;
        if (!this.failed.isEmpty() && status != 65280) {
            data = new Attributes(1);
            String[] iuids = new String[this.failed.size()];
            for (int i = 0; i < iuids.length; ++i) {
                iuids[i] = ((InstanceLocator)this.failed.get((int)i)).iuid;
            }
            data.setString(524376, VR.UI, iuids);
        }
        this.writeRSP(cmd, data);
    }

    private void writeRSP(Attributes cmd, Attributes data) {
        try {
            this.rqas.writeDimseRSP(this.pc, cmd, data);
        }
        catch (IOException e) {
            this.pendingRSP = false;
            this.stopWritePendingRSP();
            LOG.warn("{}: Unable to send C-GET or C-MOVE RSP on association to {}", new Object[]{this.rqas, this.rqas.getRemoteAET(), e});
        }
    }

    private int remaining() {
        return this.insts.size() - this.completed.size() - this.warning.size() - this.failed.size();
    }

    protected void close() {
    }

    private static final class CStoreRSPHandler
    extends DimseRSPHandler {
        private final T inst;
        final /* synthetic */ BasicRetrieveTask this$0;

        public CStoreRSPHandler(int msgId, T inst) {
            this.this$0 = var1_1;
            super(msgId);
            this.inst = inst;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
            super.onDimseRSP(as, cmd, data);
            int storeStatus = cmd.getInt(2304, -1);
            if (storeStatus == 0) {
                this.this$0.completed.add(this.inst);
            } else if ((storeStatus & 0xB000) == 45056) {
                this.this$0.warning.add(this.inst);
            } else {
                this.this$0.failed.add(this.inst);
                if (this.this$0.status == 0) {
                    this.this$0.status = 45056;
                }
            }
            Object object = this.this$0.outstandingRSPLock;
            synchronized (object) {
                if (--this.this$0.outstandingRSP == 0) {
                    this.this$0.outstandingRSPLock.notify();
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void onClose(Association as) {
            super.onClose(as);
            Object object = this.this$0.outstandingRSPLock;
            synchronized (object) {
                this.this$0.outstandingRSP = 0;
                this.this$0.outstandingRSPLock.notify();
            }
        }
    }
}

