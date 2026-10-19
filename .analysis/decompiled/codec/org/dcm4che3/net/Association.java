/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.AssociationListener;
import org.dcm4che3.net.AssociationMonitor;
import org.dcm4che3.net.CancelRQHandler;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.DataWriter;
import org.dcm4che3.net.DataWriterAdapter;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.DimseRSP;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.FutureDimseRSP;
import org.dcm4che3.net.NoPresentationContextException;
import org.dcm4che3.net.NoRoleSelectionException;
import org.dcm4che3.net.PDUDecoder;
import org.dcm4che3.net.PDUEncoder;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.State;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.Timeout;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.pdu.AAbort;
import org.dcm4che3.net.pdu.AAssociateAC;
import org.dcm4che3.net.pdu.AAssociateRJ;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.CommonExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.RoleSelection;
import org.dcm4che3.util.IntHashMap;
import org.dcm4che3.util.ReverseDNS;
import org.dcm4che3.util.SafeClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Association {
    public static final Logger LOG = LoggerFactory.getLogger(Association.class);
    private static final AtomicInteger prevSerialNo = new AtomicInteger();
    private final AtomicInteger messageID = new AtomicInteger();
    private final AtomicIntegerArray dimseCounters = new AtomicIntegerArray(46);
    private final long connectTime;
    private final int serialNo;
    private final boolean requestor;
    private String name;
    private ApplicationEntity ae;
    private final Device device;
    private final AssociationMonitor monitor;
    private final Connection conn;
    private final Socket sock;
    private final InputStream in;
    private final OutputStream out;
    private final PDUEncoder encoder;
    private PDUDecoder decoder;
    private volatile State state;
    private AAssociateRQ rq;
    private AAssociateAC ac;
    private IOException ex;
    private HashMap<String, Object> properties;
    private int maxOpsInvoked;
    private int maxPDULength;
    private int performing;
    private Timeout timeout;
    private final IntHashMap<DimseRSPHandler> rspHandlerForMsgId = new IntHashMap();
    private final IntHashMap<CancelRQHandler> cancelHandlerForMsgId = new IntHashMap();
    private final HashMap<String, HashMap<String, PresentationContext>> pcMap = new HashMap();
    private final LinkedList<AssociationListener> listeners = new LinkedList();

    Association(ApplicationEntity ae, Connection local, Socket sock) throws IOException {
        this.connectTime = System.currentTimeMillis();
        this.serialNo = prevSerialNo.incrementAndGet();
        this.ae = ae;
        this.requestor = ae != null;
        this.name = "" + sock.getLocalSocketAddress() + this.delim() + sock.getRemoteSocketAddress() + '(' + this.serialNo + ')';
        this.conn = local;
        this.device = local.getDevice();
        this.monitor = this.device.getAssociationMonitor();
        this.sock = sock;
        this.in = sock.getInputStream();
        this.out = sock.getOutputStream();
        this.encoder = new PDUEncoder(this, this.out);
        if (this.requestor) {
            this.enterState(State.Sta4);
        } else {
            this.enterState(State.Sta2);
            this.startRequestTimeout();
        }
        this.activate();
    }

    public long getConnectTimeInMillis() {
        return this.connectTime;
    }

    public int getSerialNo() {
        return this.serialNo;
    }

    public Device getDevice() {
        return this.device;
    }

    public int nextMessageID() {
        return this.messageID.incrementAndGet() & 0xFFFF;
    }

    private String delim() {
        return this.requestor ? "->" : "<-";
    }

    public int getNumberOfSent(Dimse dimse) {
        return this.dimseCounters.get(dimse.ordinal());
    }

    public int getNumberOfReceived(Dimse dimse) {
        return this.dimseCounters.get(23 + dimse.ordinal());
    }

    void incSentCount(Dimse dimse) {
        this.dimseCounters.getAndIncrement(dimse.ordinal());
    }

    void incReceivedCount(Dimse dimse) {
        this.dimseCounters.getAndIncrement(23 + dimse.ordinal());
    }

    public String toString() {
        return this.name;
    }

    public final Socket getSocket() {
        return this.sock;
    }

    public String getLocalHostName() {
        return ReverseDNS.hostNameOf(this.sock.getLocalAddress());
    }

    public String getRemoteHostName() {
        return ReverseDNS.hostNameOf(this.sock.getInetAddress());
    }

    public final Connection getConnection() {
        return this.conn;
    }

    public final AAssociateRQ getAAssociateRQ() {
        return this.rq;
    }

    public final AAssociateAC getAAssociateAC() {
        return this.ac;
    }

    public final IOException getException() {
        return this.ex;
    }

    public final ApplicationEntity getApplicationEntity() {
        return this.ae;
    }

    public Set<String> getPropertyNames() {
        return this.properties != null ? this.properties.keySet() : Collections.emptySet();
    }

    public Object getProperty(String key) {
        return this.properties != null ? this.properties.get(key) : null;
    }

    public <T> T getProperty(Class<T> clazz) {
        return (T)this.getProperty(clazz.getName());
    }

    public <T> void setProperty(Class<T> clazz, Object value) {
        this.setProperty(clazz.getName(), value);
    }

    public boolean containsProperty(String key) {
        return this.properties != null && this.properties.containsKey(key);
    }

    public Object setProperty(String key, Object value) {
        if (this.properties == null) {
            this.properties = new HashMap();
        }
        return this.properties.put(key, value);
    }

    public Object clearProperty(String key) {
        return this.properties != null ? this.properties.remove(key) : null;
    }

    public void addAssociationListener(AssociationListener listener) {
        this.listeners.add(listener);
    }

    public void removeAssociationListener(AssociationListener listener) {
        this.listeners.remove(listener);
    }

    public final boolean isRequestor() {
        return this.requestor;
    }

    public boolean isReadyForDataTransfer() {
        return this.state == State.Sta6;
    }

    private void checkIsSCP(String cuid) throws NoRoleSelectionException {
        if (!this.isSCPFor(cuid)) {
            NoRoleSelectionException ex = new NoRoleSelectionException(cuid, TransferCapability.Role.SCP);
            if (this.ae.isRoleSelectionNegotiationLenient() && this.ac.getRoleSelectionFor(cuid) == null) {
                LOG.info("{}: {}", (Object)this, (Object)ex.getMessage());
            } else {
                throw ex;
            }
        }
    }

    public boolean isSCPFor(String cuid) {
        RoleSelection rolsel = this.ac.getRoleSelectionFor(cuid);
        if (rolsel == null) {
            return !this.requestor;
        }
        return this.requestor ? rolsel.isSCP() : rolsel.isSCU();
    }

    private void checkIsSCU(String cuid) throws NoRoleSelectionException {
        if (!this.isSCUFor(cuid)) {
            NoRoleSelectionException ex = new NoRoleSelectionException(cuid, TransferCapability.Role.SCU);
            if (this.ae.isRoleSelectionNegotiationLenient() && this.ac.getRoleSelectionFor(cuid) == null) {
                LOG.info("{}: {}", (Object)this, (Object)ex.getMessage());
            } else {
                throw ex;
            }
        }
    }

    public boolean isSCUFor(String cuid) {
        RoleSelection rolsel = this.ac.getRoleSelectionFor(cuid);
        if (rolsel == null) {
            return this.requestor;
        }
        return this.requestor ? rolsel.isSCU() : rolsel.isSCP();
    }

    public String getCallingAET() {
        return this.rq != null ? this.rq.getCallingAET() : null;
    }

    public String getCalledAET() {
        return this.rq != null ? this.rq.getCalledAET() : null;
    }

    public String getRemoteAET() {
        return this.requestor ? this.getCalledAET() : this.getCallingAET();
    }

    public String getLocalAET() {
        return this.requestor ? this.getCallingAET() : this.getCalledAET();
    }

    public String getRemoteImplVersionName() {
        return (this.requestor ? this.ac : this.rq).getImplVersionName();
    }

    public String getRemoteImplClassUID() {
        return (this.requestor ? this.ac : this.rq).getImplClassUID();
    }

    public String getLocalImplVersionName() {
        return (this.requestor ? this.rq : this.ac).getImplVersionName();
    }

    public String getLocalImplClassUID() {
        return (this.requestor ? this.rq : this.ac).getImplClassUID();
    }

    public String getAbstractSyntax(int pcid) {
        PresentationContext rqpc = this.rq.getPresentationContext(pcid);
        return rqpc != null ? rqpc.getAbstractSyntax() : null;
    }

    final int getMaxPDULengthSend() {
        return this.maxPDULength;
    }

    boolean isPackPDV() {
        return this.conn.isPackPDV();
    }

    public void release() throws IOException {
        this.state.writeAReleaseRQ(this);
    }

    public void abort() {
        this.abort(new AAbort());
    }

    void abort(AAbort aa) {
        this.state.write(this, aa);
    }

    private synchronized void closeSocket() {
        this.state.closeSocket(this);
    }

    void doCloseSocket() {
        LOG.info("{}: close {}", (Object)this.name, (Object)this.sock);
        SafeClose.close(this.sock);
        this.enterState(State.Sta1);
    }

    private synchronized void closeSocketDelayed() {
        this.state.closeSocketDelayed(this);
    }

    void doCloseSocketDelayed() {
        this.enterState(State.Sta13);
        int delay = this.conn.getSocketCloseDelay();
        if (delay > 0) {
            this.device.schedule(new Runnable(){

                @Override
                public void run() {
                    Association.this.closeSocket();
                }
            }, delay, TimeUnit.MILLISECONDS);
            LOG.debug("{}: closing {} in {} ms", new Object[]{this.name, this.sock, delay});
        } else {
            this.closeSocket();
        }
    }

    synchronized void onIOException(IOException e) {
        if (this.ex != null) {
            return;
        }
        this.ex = e;
        LOG.info("{}: i/o exception: {} in State: {}", new Object[]{this.name, e, this.state});
        this.closeSocket();
    }

    void write(AAbort aa) {
        LOG.info("{} << {}", (Object)this.name, (Object)aa.toString());
        this.encoder.write(aa);
        this.ex = aa;
        this.closeSocketDelayed();
    }

    void writeAReleaseRQ() throws IOException {
        LOG.info("{} << A-RELEASE-RQ", (Object)this.name);
        this.enterState(State.Sta7);
        this.stopTimeout();
        this.encoder.writeAReleaseRQ();
        this.startReleaseTimeout();
    }

    private void startRequestTimeout() {
        this.startTimeout("{}: start A-ASSOCIATE-RQ timeout of {}ms", "{}: A-ASSOCIATE-RQ timeout expired", "{}: stop A-ASSOCIATE-RQ timeout", this.conn.getRequestTimeout(), State.Sta2);
    }

    private void startAcceptTimeout() {
        this.startTimeout("{}: start A-ASSOCIATE-AC timeout of {}ms", "{}: A-ASSOCIATE-AC timeout expired", "{}: stop A-ASSOCIATE-AC timeout", this.conn.getAcceptTimeout(), State.Sta5);
    }

    private void startReleaseTimeout() {
        this.startTimeout("{}: start A-RELEASE-RP timeout of {}ms", "{}: A-RELEASE-RP timeout expired", "{}: stop A-RELEASE-RP timeout", this.conn.getReleaseTimeout(), State.Sta7);
    }

    private void startIdleTimeout() {
        this.startTimeout("{}: start idle timeout of {}ms", "{}: idle timeout expired", "{}: stop idle timeout", this.conn.getIdleTimeout(), State.Sta6);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void startSendTimeout(int timeout) {
        if (timeout > 0) {
            Association association = this;
            synchronized (association) {
                this.stopTimeout();
                this.timeout = Timeout.start(this, "{}: start send timeout of {}ms", "{}: send timeout expired", "{}: stop send timeout", timeout);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void startTimeout(String startMsg, String expiredMsg, String cancelMsg, int timeout, State state) {
        if (timeout > 0 && this.performing == 0 && this.rspHandlerForMsgId.isEmpty()) {
            Association association = this;
            synchronized (association) {
                if (this.state == state) {
                    this.stopTimeout();
                    this.timeout = Timeout.start(this, startMsg, expiredMsg, cancelMsg, timeout);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void startTimeout(int msgID, int timeout, boolean stopOnPending) {
        if (timeout > 0) {
            IntHashMap<DimseRSPHandler> intHashMap = this.rspHandlerForMsgId;
            synchronized (intHashMap) {
                DimseRSPHandler rspHandler = this.rspHandlerForMsgId.get(msgID);
                if (rspHandler != null) {
                    rspHandler.setTimeout(Timeout.start(this, "{}: start " + msgID + ":DIMSE-RSP timeout of {}ms", "{}: " + msgID + ":DIMSE-RSP timeout expired", "{}: stop " + msgID + ":DIMSE-RSP timeout", timeout), stopOnPending);
                }
            }
        }
    }

    private synchronized void stopTimeout() {
        if (this.timeout != null) {
            this.timeout.stop();
            this.timeout = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitForOutstandingRSP() throws InterruptedException {
        IntHashMap<DimseRSPHandler> intHashMap = this.rspHandlerForMsgId;
        synchronized (intHashMap) {
            while (!this.rspHandlerForMsgId.isEmpty()) {
                this.rspHandlerForMsgId.wait();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitForNonBlockingInvoke() throws InterruptedException {
        if (this.maxOpsInvoked > 0) {
            IntHashMap<DimseRSPHandler> intHashMap = this.rspHandlerForMsgId;
            synchronized (intHashMap) {
                while (this.rspHandlerForMsgId.size() >= this.maxOpsInvoked) {
                    this.rspHandlerForMsgId.wait();
                }
            }
        }
    }

    void write(AAssociateRQ rq) throws IOException {
        this.name = rq.getCallingAET() + this.delim() + rq.getCalledAET() + '(' + this.serialNo + ')';
        this.rq = rq;
        LOG.info("{} << A-ASSOCIATE-RQ", (Object)this.name);
        LOG.debug("{}", (Object)rq);
        this.enterState(State.Sta5);
        this.encoder.write(rq);
        this.startAcceptTimeout();
    }

    private void write(AAssociateAC ac) throws IOException {
        LOG.info("{} << A-ASSOCIATE-AC", (Object)this.name);
        LOG.debug("{}", (Object)ac);
        this.enterState(State.Sta6);
        this.encoder.write(ac);
        this.startIdleTimeout();
    }

    private void write(AAssociateRJ rj) throws IOException {
        LOG.info("{} << {}", (Object)this.name, (Object)rj.toString());
        this.encoder.write(rj);
        this.closeSocketDelayed();
    }

    private void checkException() throws IOException {
        if (this.ex != null) {
            throw this.ex;
        }
    }

    private synchronized void enterState(State newState) {
        LOG.debug("{}: enter state: {}", (Object)this.name, (Object)newState);
        this.state = newState;
        this.notifyAll();
    }

    public final State getState() {
        return this.state;
    }

    synchronized void waitForLeaving(State state) throws InterruptedException, IOException {
        while (this.state == state) {
            this.wait();
        }
        this.checkException();
    }

    synchronized void waitForEntering(State state) throws InterruptedException, IOException {
        while (this.state != state) {
            this.wait();
        }
        this.checkException();
    }

    public void waitForSocketClose() throws InterruptedException, IOException {
        this.waitForEntering(State.Sta1);
    }

    private void activate() {
        this.device.execute(new Runnable(){

            @Override
            public void run() {
                try {
                    Association.this.decoder = new PDUDecoder(Association.this, Association.this.in);
                    Association.this.device.addAssociation(Association.this);
                    while (Association.this.state != State.Sta1 && Association.this.state != State.Sta13) {
                        Association.this.decoder.nextPDU();
                    }
                }
                catch (AAbort aa) {
                    Association.this.abort(aa);
                }
                catch (IOException e) {
                    Association.this.onIOException(e);
                }
                catch (Exception e) {
                    Association.this.onIOException(new IOException("Unexpected Error", e));
                }
                finally {
                    Association.this.device.removeAssociation(Association.this);
                    Association.this.onClose();
                }
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void onClose() {
        this.stopTimeout();
        IntHashMap<DimseRSPHandler> intHashMap = this.rspHandlerForMsgId;
        synchronized (intHashMap) {
            IntHashMap.Visitor<DimseRSPHandler> visitor = new IntHashMap.Visitor<DimseRSPHandler>(){

                @Override
                public boolean visit(int key, DimseRSPHandler value) {
                    value.onClose(Association.this);
                    return true;
                }
            };
            this.rspHandlerForMsgId.accept(visitor);
            this.rspHandlerForMsgId.clear();
            this.rspHandlerForMsgId.notifyAll();
        }
        if (this.ae != null) {
            this.ae.getDevice().getAssociationHandler().onClose(this);
        }
        for (AssociationListener listener : this.listeners) {
            listener.onClose(this);
        }
    }

    void onAAssociateRQ(AAssociateRQ rq) throws IOException {
        this.name = rq.getCalledAET() + this.delim() + rq.getCallingAET() + '(' + this.serialNo + ')';
        LOG.info("{} >> A-ASSOCIATE-RQ", (Object)this.name);
        LOG.debug("{}", (Object)rq);
        this.stopTimeout();
        this.state.onAAssociateRQ(this, rq);
    }

    void handle(AAssociateRQ rq) throws IOException {
        block3: {
            this.rq = rq;
            this.enterState(State.Sta3);
            try {
                this.ae = this.device.getApplicationEntity(rq.getCalledAET(), true);
                this.ac = this.device.getAssociationHandler().negotiate(this, rq);
                this.initPCMap();
                this.maxOpsInvoked = this.ac.getMaxOpsPerformed();
                this.maxPDULength = Association.minZeroAsMax(rq.getMaxPDULength(), this.conn.getSendPDULength());
                this.write(this.ac);
                if (this.monitor != null) {
                    this.monitor.onAssociationAccepted(this);
                }
            }
            catch (AAssociateRJ e) {
                this.write(e);
                if (this.monitor == null) break block3;
                this.monitor.onAssociationRejected(this, e);
            }
        }
    }

    void onAAssociateAC(AAssociateAC ac) throws IOException {
        LOG.info("{} >> A-ASSOCIATE-AC", (Object)this.name);
        LOG.debug("{}", (Object)ac);
        this.stopTimeout();
        this.state.onAAssociateAC(this, ac);
    }

    void handle(AAssociateAC ac) throws IOException {
        this.ac = ac;
        this.initPCMap();
        this.maxOpsInvoked = ac.getMaxOpsInvoked();
        this.maxPDULength = Association.minZeroAsMax(ac.getMaxPDULength(), this.conn.getSendPDULength());
        this.enterState(State.Sta6);
        this.startIdleTimeout();
    }

    void onAAssociateRJ(AAssociateRJ rj) throws IOException {
        LOG.info("{} >> {}", (Object)this.name, (Object)rj.toString());
        this.state.onAAssociateRJ(this, rj);
    }

    void handle(AAssociateRJ rq) {
        this.ex = rq;
        this.closeSocket();
    }

    void onAReleaseRQ() throws IOException {
        LOG.info("{} >> A-RELEASE-RQ", (Object)this.name);
        this.stopTimeout();
        this.state.onAReleaseRQ(this);
    }

    void handleAReleaseRQ() {
        if (this.decoder.isPendingPDV()) {
            LOG.info("{}: unexpected A-RELEASE-RQ after P-DATA-TF with pending PDV", (Object)this);
            this.abort();
            return;
        }
        this.enterState(State.Sta8);
        this.waitForPerformingOps();
        LOG.info("{} << A-RELEASE-RP", (Object)this.name);
        this.encoder.writeAReleaseRP();
        this.closeSocketDelayed();
    }

    private synchronized void waitForPerformingOps() {
        while (this.performing > 0 && this.state == State.Sta8) {
            try {
                this.wait();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    void handleAReleaseRQCollision() {
        if (this.isRequestor()) {
            this.enterState(State.Sta9);
            LOG.info("{} << A-RELEASE-RP", (Object)this.name);
            this.encoder.writeAReleaseRP();
            this.enterState(State.Sta11);
        } else {
            this.enterState(State.Sta10);
        }
    }

    void onAReleaseRP() throws IOException {
        LOG.info("{} >> A-RELEASE-RP", (Object)this.name);
        this.stopTimeout();
        this.state.onAReleaseRP(this);
    }

    void handleAReleaseRP() {
        this.closeSocket();
    }

    void handleAReleaseRPCollision() {
        this.enterState(State.Sta12);
        LOG.info("{} << A-RELEASE-RP", (Object)this.name);
        this.encoder.writeAReleaseRP();
        this.closeSocketDelayed();
    }

    void onAAbort(AAbort aa) {
        LOG.info("{} >> {}", (Object)this.name, (Object)aa.toString());
        this.stopTimeout();
        this.ex = aa;
        this.closeSocket();
    }

    void unexpectedPDU(String pdu) throws AAbort {
        LOG.warn("{} >> unexpected {} in state: {}", new Object[]{this.name, pdu, this.state});
        throw new AAbort(2, 2);
    }

    void onPDataTF() throws IOException {
        this.state.onPDataTF(this);
    }

    void handlePDataTF() throws IOException {
        this.decoder.decodeDIMSE();
    }

    void writePDataTF() throws IOException {
        this.checkException();
        this.state.writePDataTF(this);
    }

    void doWritePDataTF() throws IOException {
        this.encoder.writePDataTF();
    }

    void onDimseRQ(PresentationContext pc, Dimse dimse, Attributes cmd, PDVInputStream data) throws IOException {
        this.stopTimeout();
        this.incPerforming();
        this.incReceivedCount(dimse);
        this.ae.onDimseRQ(this, pc, dimse, cmd, data);
    }

    private synchronized void incPerforming() {
        ++this.performing;
    }

    private synchronized void decPerforming() {
        --this.performing;
        this.notifyAll();
    }

    void onDimseRSP(Dimse dimse, Attributes cmd, Attributes data) throws AAbort {
        int msgId = cmd.getInt(288, -1);
        int status = cmd.getInt(2304, 0);
        boolean pending = Status.isPending(status);
        DimseRSPHandler rspHandler = this.getDimseRSPHandler(msgId);
        if (rspHandler == null) {
            Dimse.LOG.info("{}: unexpected message ID in DIMSE RSP:", (Object)this.name);
            Dimse.LOG.info("\n{}", (Object)cmd);
            throw new AAbort();
        }
        rspHandler.onDimseRSP(this, cmd, data);
        if (pending) {
            if (rspHandler.isStopOnPending()) {
                this.startTimeout(msgId, this.conn.getRetrieveTimeout(), true);
            }
        } else {
            this.incReceivedCount(dimse);
            this.removeDimseRSPHandler(msgId);
            if (this.rspHandlerForMsgId.isEmpty() && this.performing == 0) {
                this.startIdleOrReleaseTimeout();
            }
        }
    }

    private synchronized void startIdleOrReleaseTimeout() {
        if (this.state == State.Sta6) {
            this.startIdleTimeout();
        } else if (this.state == State.Sta7) {
            this.startReleaseTimeout();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void addDimseRSPHandler(DimseRSPHandler rspHandler) throws InterruptedException {
        IntHashMap<DimseRSPHandler> intHashMap = this.rspHandlerForMsgId;
        synchronized (intHashMap) {
            while (this.maxOpsInvoked > 0 && this.rspHandlerForMsgId.size() >= this.maxOpsInvoked) {
                this.rspHandlerForMsgId.wait();
            }
            this.rspHandlerForMsgId.put(rspHandler.getMessageID(), rspHandler);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private DimseRSPHandler getDimseRSPHandler(int msgId) {
        IntHashMap<DimseRSPHandler> intHashMap = this.rspHandlerForMsgId;
        synchronized (intHashMap) {
            return this.rspHandlerForMsgId.get(msgId);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private DimseRSPHandler removeDimseRSPHandler(int msgId) {
        IntHashMap<DimseRSPHandler> intHashMap = this.rspHandlerForMsgId;
        synchronized (intHashMap) {
            DimseRSPHandler tmp = this.rspHandlerForMsgId.remove(msgId);
            if (tmp != null) {
                tmp.stopTimeout(this);
            }
            this.rspHandlerForMsgId.notifyAll();
            return tmp;
        }
    }

    void cancel(PresentationContext pc, int msgId) throws IOException {
        Attributes cmd = Commands.mkCCancelRQ(msgId);
        this.encoder.writeDIMSE(pc, cmd, null);
    }

    public boolean tryWriteDimseRSP(PresentationContext pc, Attributes cmd) {
        return this.tryWriteDimseRSP(pc, cmd, null);
    }

    public boolean tryWriteDimseRSP(PresentationContext pc, Attributes cmd, Attributes data) {
        try {
            this.writeDimseRSP(pc, cmd, data);
            return true;
        }
        catch (IOException e) {
            LOG.warn("{} << {} failed: {}", new Object[]{this, Dimse.valueOf(cmd.getInt(256, 0)), e.getMessage()});
            return false;
        }
    }

    public void writeDimseRSP(PresentationContext pc, Attributes cmd) throws IOException {
        this.writeDimseRSP(pc, cmd, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeDimseRSP(PresentationContext pc, Attributes cmd, Attributes data) throws IOException {
        DataWriterAdapter writer = null;
        int datasetType = 257;
        if (data != null) {
            writer = new DataWriterAdapter(data);
            datasetType = Commands.getWithDatasetType();
        }
        cmd.setInt(2048, VR.US, datasetType);
        try {
            this.encoder.writeDIMSE(pc, cmd, writer);
        }
        finally {
            if (!Status.isPending(cmd.getInt(2304, 0))) {
                this.decPerforming();
                this.startIdleTimeout();
            }
        }
    }

    void onCancelRQ(Attributes cmd) throws IOException {
        this.incReceivedCount(Dimse.C_CANCEL_RQ);
        int msgId = cmd.getInt(288, -1);
        CancelRQHandler handler = this.removeCancelRQHandler(msgId);
        if (handler != null) {
            handler.onCancelRQ(this);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addCancelRQHandler(int msgId, CancelRQHandler handler) {
        IntHashMap<CancelRQHandler> intHashMap = this.cancelHandlerForMsgId;
        synchronized (intHashMap) {
            this.cancelHandlerForMsgId.put(msgId, handler);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public CancelRQHandler removeCancelRQHandler(int msgId) {
        IntHashMap<CancelRQHandler> intHashMap = this.cancelHandlerForMsgId;
        synchronized (intHashMap) {
            return this.cancelHandlerForMsgId.remove(msgId);
        }
    }

    private void initPCMap() {
        for (PresentationContext pc : this.ac.getPresentationContexts()) {
            if (!pc.isAccepted()) continue;
            PresentationContext rqpc = this.rq.getPresentationContext(pc.getPCID());
            if (rqpc != null) {
                this.initTSMap(rqpc.getAbstractSyntax()).put(pc.getTransferSyntax(), pc);
                continue;
            }
            LOG.info("{}: Ignore unexpected {} in A-ASSOCIATE-AC", (Object)this.name, (Object)pc);
        }
    }

    private HashMap<String, PresentationContext> initTSMap(String as) {
        HashMap<String, PresentationContext> tsMap = this.pcMap.get(as);
        if (tsMap == null) {
            tsMap = new HashMap();
            this.pcMap.put(as, tsMap);
        }
        return tsMap;
    }

    public PresentationContext pcFor(String cuid, String tsuid) throws NoPresentationContextException {
        HashMap<String, PresentationContext> tsMap = this.pcMap.get(cuid);
        if (tsMap == null) {
            throw new NoPresentationContextException(cuid);
        }
        if (tsuid == null) {
            return tsMap.values().iterator().next();
        }
        PresentationContext pc = tsMap.get(tsuid);
        if (pc == null) {
            throw new NoPresentationContextException(cuid, tsuid);
        }
        return pc;
    }

    public Set<String> getTransferSyntaxesFor(String cuid) {
        HashMap<String, PresentationContext> tsMap = this.pcMap.get(cuid);
        if (tsMap == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(tsMap.keySet());
    }

    PresentationContext getPresentationContext(int pcid) {
        return this.ac.getPresentationContext(pcid);
    }

    public CommonExtendedNegotiation getCommonExtendedNegotiationFor(String cuid) {
        return this.ac.getCommonExtendedNegotiationFor(cuid);
    }

    public void cstore(String cuid, String iuid, int priority, DataWriter data, String tsuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = this.pcFor(cuid, tsuid);
        this.checkIsSCU(cuid);
        Attributes cstorerq = Commands.mkCStoreRQ(rspHandler.getMessageID(), cuid, iuid, priority);
        this.invoke(pc, cstorerq, data, rspHandler, this.conn.getStoreTimeout(), this.conn.getResponseTimeout());
    }

    public DimseRSP cstore(String cuid, String iuid, int priority, DataWriter data, String tsuid) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(this.nextMessageID());
        this.cstore(cuid, iuid, priority, data, tsuid, rsp);
        return rsp;
    }

    public void cstore(String cuid, String iuid, int priority, String moveOriginatorAET, int moveOriginatorMsgId, DataWriter data, String tsuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = this.pcFor(cuid, tsuid);
        Attributes cstorerq = Commands.mkCStoreRQ(rspHandler.getMessageID(), cuid, iuid, priority, moveOriginatorAET, moveOriginatorMsgId);
        this.invoke(pc, cstorerq, data, rspHandler, this.conn.getStoreTimeout(), this.conn.getResponseTimeout());
    }

    public DimseRSP cstore(String cuid, String iuid, int priority, String moveOriginatorAET, int moveOriginatorMsgId, DataWriter data, String tsuid) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(this.nextMessageID());
        this.cstore(cuid, iuid, priority, moveOriginatorAET, moveOriginatorMsgId, data, tsuid, rsp);
        return rsp;
    }

    public void cfind(String cuid, int priority, Attributes data, String tsuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = this.pcFor(cuid, tsuid);
        this.checkIsSCU(cuid);
        Attributes cfindrq = Commands.mkCFindRQ(rspHandler.getMessageID(), cuid, priority);
        this.invoke(pc, cfindrq, new DataWriterAdapter(data), rspHandler, this.conn.getSendTimeout(), this.conn.getResponseTimeout());
    }

    public DimseRSP cfind(String cuid, int priority, Attributes data, String tsuid, int autoCancel) throws IOException, InterruptedException {
        return this.cfind(cuid, priority, data, tsuid, autoCancel, Integer.MAX_VALUE);
    }

    public DimseRSP cfind(String cuid, int priority, Attributes data, String tsuid, int autoCancel, int capacity) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(this.nextMessageID());
        rsp.setAutoCancel(autoCancel);
        rsp.setCapacity(capacity);
        this.cfind(cuid, priority, data, tsuid, rsp);
        return rsp;
    }

    public void cget(String cuid, int priority, Attributes data, String tsuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = this.pcFor(cuid, tsuid);
        this.checkIsSCU(cuid);
        Attributes cgetrq = Commands.mkCGetRQ(rspHandler.getMessageID(), cuid, priority);
        this.invoke(pc, cgetrq, new DataWriterAdapter(data), rspHandler, this.conn.getSendTimeout(), this.conn.getRetrieveTimeout(), !this.conn.isRetrieveTimeoutTotal());
    }

    public DimseRSP cget(String cuid, int priority, Attributes data, String tsuid) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(this.nextMessageID());
        this.cget(cuid, priority, data, tsuid, rsp);
        return rsp;
    }

    public void cmove(String cuid, int priority, Attributes data, String tsuid, String destination, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = this.pcFor(cuid, tsuid);
        this.checkIsSCU(cuid);
        Attributes cmoverq = Commands.mkCMoveRQ(rspHandler.getMessageID(), cuid, priority, destination);
        this.invoke(pc, cmoverq, new DataWriterAdapter(data), rspHandler, this.conn.getSendTimeout(), this.conn.getRetrieveTimeout(), !this.conn.isRetrieveTimeoutTotal());
    }

    public DimseRSP cmove(String cuid, int priority, Attributes data, String tsuid, String destination) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(this.nextMessageID());
        this.cmove(cuid, priority, data, tsuid, destination, rsp);
        return rsp;
    }

    public DimseRSP cecho() throws IOException, InterruptedException {
        return this.cecho("1.2.840.10008.1.1");
    }

    public DimseRSP cecho(String cuid) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(this.nextMessageID());
        PresentationContext pc = this.pcFor(cuid, null);
        this.checkIsSCU(cuid);
        Attributes cechorq = Commands.mkCEchoRQ(rsp.getMessageID(), cuid);
        this.invoke(pc, cechorq, null, rsp, this.conn.getSendTimeout(), this.conn.getResponseTimeout());
        return rsp;
    }

    public void neventReport(String cuid, String iuid, int eventTypeId, Attributes data, String tsuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        this.neventReport(cuid, cuid, iuid, eventTypeId, data, tsuid, rspHandler);
    }

    public void neventReport(String asuid, String cuid, String iuid, int eventTypeId, Attributes data, String tsuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = this.pcFor(asuid, tsuid);
        this.checkIsSCP(asuid);
        Attributes neventrq = Commands.mkNEventReportRQ(rspHandler.getMessageID(), cuid, iuid, eventTypeId, data);
        this.invoke(pc, neventrq, DataWriterAdapter.forAttributes(data), rspHandler, this.conn.getSendTimeout(), this.conn.getResponseTimeout());
    }

    public DimseRSP neventReport(String cuid, String iuid, int eventTypeId, Attributes data, String tsuid) throws IOException, InterruptedException {
        return this.neventReport(cuid, cuid, iuid, eventTypeId, data, tsuid);
    }

    public DimseRSP neventReport(String asuid, String cuid, String iuid, int eventTypeId, Attributes data, String tsuid) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(this.nextMessageID());
        this.neventReport(asuid, cuid, iuid, eventTypeId, data, tsuid, rsp);
        return rsp;
    }

    public void nget(String cuid, String iuid, int[] tags, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        this.nget(cuid, cuid, iuid, tags, rspHandler);
    }

    public void nget(String asuid, String cuid, String iuid, int[] tags, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = this.pcFor(asuid, null);
        this.checkIsSCU(asuid);
        Attributes ngetrq = Commands.mkNGetRQ(rspHandler.getMessageID(), cuid, iuid, tags);
        this.invoke(pc, ngetrq, null, rspHandler, this.conn.getSendTimeout(), this.conn.getResponseTimeout());
    }

    public DimseRSP nget(String cuid, String iuid, int[] tags) throws IOException, InterruptedException {
        return this.nget(cuid, cuid, iuid, tags);
    }

    public DimseRSP nget(String asuid, String cuid, String iuid, int[] tags) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(this.nextMessageID());
        this.nget(asuid, cuid, iuid, tags, rsp);
        return rsp;
    }

    public void nset(String cuid, String iuid, Attributes data, String tsuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        this.nset(cuid, cuid, iuid, new DataWriterAdapter(data), tsuid, rspHandler);
    }

    public void nset(String asuid, String cuid, String iuid, Attributes data, String tsuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        this.nset(asuid, cuid, iuid, new DataWriterAdapter(data), tsuid, rspHandler);
    }

    public DimseRSP nset(String cuid, String iuid, Attributes data, String tsuid) throws IOException, InterruptedException {
        return this.nset(cuid, cuid, iuid, new DataWriterAdapter(data), tsuid);
    }

    public DimseRSP nset(String asuid, String cuid, String iuid, Attributes data, String tsuid) throws IOException, InterruptedException {
        return this.nset(asuid, cuid, iuid, new DataWriterAdapter(data), tsuid);
    }

    public void nset(String cuid, String iuid, DataWriter data, String tsuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        this.nset(cuid, cuid, iuid, data, tsuid, rspHandler);
    }

    public void nset(String asuid, String cuid, String iuid, DataWriter data, String tsuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = this.pcFor(asuid, tsuid);
        this.checkIsSCU(asuid);
        Attributes nsetrq = Commands.mkNSetRQ(rspHandler.getMessageID(), cuid, iuid);
        this.invoke(pc, nsetrq, data, rspHandler, this.conn.getSendTimeout(), this.conn.getResponseTimeout());
    }

    public DimseRSP nset(String cuid, String iuid, DataWriter data, String tsuid) throws IOException, InterruptedException {
        return this.nset(cuid, cuid, iuid, data, tsuid);
    }

    public DimseRSP nset(String asuid, String cuid, String iuid, DataWriter data, String tsuid) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(this.nextMessageID());
        this.nset(asuid, cuid, iuid, data, tsuid, (DimseRSPHandler)rsp);
        return rsp;
    }

    public void naction(String cuid, String iuid, int actionTypeId, Attributes data, String tsuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        this.naction(cuid, cuid, iuid, actionTypeId, data, tsuid, rspHandler);
    }

    public void naction(String asuid, String cuid, String iuid, int actionTypeId, Attributes data, String tsuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = this.pcFor(asuid, tsuid);
        this.checkIsSCU(asuid);
        Attributes nactionrq = Commands.mkNActionRQ(rspHandler.getMessageID(), cuid, iuid, actionTypeId, data);
        this.invoke(pc, nactionrq, DataWriterAdapter.forAttributes(data), rspHandler, this.conn.getSendTimeout(), this.conn.getResponseTimeout());
    }

    public DimseRSP naction(String cuid, String iuid, int actionTypeId, Attributes data, String tsuid) throws IOException, InterruptedException {
        return this.naction(cuid, cuid, iuid, actionTypeId, data, tsuid);
    }

    public DimseRSP naction(String asuid, String cuid, String iuid, int actionTypeId, Attributes data, String tsuid) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(this.nextMessageID());
        this.naction(asuid, cuid, iuid, actionTypeId, data, tsuid, rsp);
        return rsp;
    }

    public void ncreate(String cuid, String iuid, Attributes data, String tsuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        this.ncreate(cuid, cuid, iuid, data, tsuid, rspHandler);
    }

    public void ncreate(String asuid, String cuid, String iuid, Attributes data, String tsuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = this.pcFor(asuid, tsuid);
        this.checkIsSCU(asuid);
        Attributes ncreaterq = Commands.mkNCreateRQ(rspHandler.getMessageID(), cuid, iuid);
        this.invoke(pc, ncreaterq, DataWriterAdapter.forAttributes(data), rspHandler, this.conn.getSendTimeout(), this.conn.getResponseTimeout());
    }

    public DimseRSP ncreate(String cuid, String iuid, Attributes data, String tsuid) throws IOException, InterruptedException {
        return this.ncreate(cuid, cuid, iuid, data, tsuid);
    }

    public DimseRSP ncreate(String asuid, String cuid, String iuid, Attributes data, String tsuid) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(this.nextMessageID());
        this.ncreate(asuid, cuid, iuid, data, tsuid, rsp);
        return rsp;
    }

    public void ndelete(String cuid, String iuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        this.ndelete(cuid, cuid, iuid, rspHandler);
    }

    public void ndelete(String asuid, String cuid, String iuid, DimseRSPHandler rspHandler) throws IOException, InterruptedException {
        PresentationContext pc = this.pcFor(asuid, null);
        this.checkIsSCU(asuid);
        Attributes ndeleterq = Commands.mkNDeleteRQ(rspHandler.getMessageID(), cuid, iuid);
        this.invoke(pc, ndeleterq, null, rspHandler, this.conn.getSendTimeout(), this.conn.getResponseTimeout());
    }

    public DimseRSP ndelete(String cuid, String iuid) throws IOException, InterruptedException {
        return this.ndelete(cuid, cuid, iuid);
    }

    public DimseRSP ndelete(String asuid, String cuid, String iuid) throws IOException, InterruptedException {
        FutureDimseRSP rsp = new FutureDimseRSP(this.nextMessageID());
        this.ndelete(asuid, cuid, iuid, rsp);
        return rsp;
    }

    public void invoke(PresentationContext pc, Attributes cmd, DataWriter data, DimseRSPHandler rspHandler, int sendTimeout, int rspTimeout) throws IOException, InterruptedException {
        this.invoke(pc, cmd, data, rspHandler, sendTimeout, rspTimeout, true);
    }

    public void invoke(PresentationContext pc, Attributes cmd, DataWriter data, DimseRSPHandler rspHandler, int sendTimeout, int rspTimeout, boolean stopOnPending) throws IOException, InterruptedException {
        this.stopTimeout();
        this.checkException();
        rspHandler.setPC(pc);
        this.addDimseRSPHandler(rspHandler);
        this.startSendTimeout(sendTimeout);
        try {
            this.encoder.writeDIMSE(pc, cmd, data);
            this.stopTimeout();
            this.startTimeout(rspHandler.getMessageID(), rspTimeout, stopOnPending);
        }
        catch (IOException | RuntimeException e) {
            this.removeDimseRSPHandler(rspHandler.getMessageID());
            throw e;
        }
    }

    static int minZeroAsMax(int i1, int i2) {
        return i1 == 0 ? i2 : (i2 == 0 ? i1 : Math.min(i1, i2));
    }

    public Attributes createFileMetaInformation(String iuid, String cuid, String tsuid) {
        Attributes fmi = new Attributes(7);
        fmi.setBytes(131073, VR.OB, new byte[]{0, 1});
        fmi.setString(131074, VR.UI, cuid);
        fmi.setString(131075, VR.UI, iuid);
        fmi.setString(131088, VR.UI, tsuid);
        fmi.setString(131090, VR.UI, this.getRemoteImplClassUID());
        String versionName = this.getRemoteImplVersionName();
        if (versionName != null) {
            fmi.setString(131091, VR.SH, versionName);
        }
        fmi.setString(131094, VR.AE, this.getRemoteAET());
        return fmi;
    }

    public EnumSet<QueryOption> getQueryOptionsFor(String cuid) {
        return QueryOption.toOptions(this.ac.getExtNegotiationFor(cuid));
    }

    public EnumSet<QueryOption> getRequestedQueryOptionsFor(String cuid) {
        return QueryOption.toOptions(this.rq.getExtNegotiationFor(cuid));
    }

    public int getPerformingOperationCount() {
        return this.performing;
    }
}

