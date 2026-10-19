/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.DataWriter;
import org.dcm4che3.net.DataWriterAdapter;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.PDVOutputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.Timeout;
import org.dcm4che3.net.pdu.AAbort;
import org.dcm4che3.net.pdu.AAssociateAC;
import org.dcm4che3.net.pdu.AAssociateRJ;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.AAssociateRQAC;
import org.dcm4che3.net.pdu.CommonExtendedNegotiation;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.RoleSelection;
import org.dcm4che3.net.pdu.UserIdentityAC;
import org.dcm4che3.net.pdu.UserIdentityRQ;

class PDUEncoder
extends PDVOutputStream {
    private Association as;
    private OutputStream out;
    private byte[] buf = new byte[16384];
    private int pos;
    private int pdvpcid;
    private int pdvcmd;
    private int pdvpos;
    private int maxpdulen;
    private Thread th;
    private Object dimseLock = new Object();
    private Lock writeLock = new ReentrantLock(true);

    public PDUEncoder(Association as, OutputStream out) {
        this.as = as;
        this.out = out;
    }

    public void write(AAssociateRQ rq) throws IOException {
        this.encode(rq, 1, 32);
        this.writePDU(this.pos - 6);
    }

    public void write(AAssociateAC ac) throws IOException {
        this.encode(ac, 2, 33);
        this.writePDU(this.pos - 6);
    }

    public void write(AAssociateRJ rj) throws IOException {
        this.write(3, rj.getResult(), rj.getSource(), rj.getReason(), true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeAReleaseRQ() throws IOException {
        Object object = this.dimseLock;
        synchronized (object) {
            this.write(5, 0, 0, 0, true);
        }
    }

    public void writeAReleaseRP() {
        try {
            this.write(6, 0, 0, 0, false);
        }
        catch (IOException e) {
            Association.LOG.info("{} << A-RELEASE-RP failed: {}", (Object)this.as, (Object)e.getMessage());
        }
    }

    public void write(AAbort aa) {
        try {
            this.write(7, 0, aa.getSource(), aa.getReason(), false);
        }
        catch (IOException e) {
            Association.LOG.info("{} << {} failed: {}", new Object[]{this.as, aa, e.getMessage()});
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void write(int pdutype, int result, int source, int reason, boolean blocking) throws IOException {
        if (blocking) {
            this.writeLock.lock();
        } else {
            block8: {
                try {
                    int timeout = this.as.getConnection().getAbortTimeout();
                    Timeout.LOG.debug("{}: start A-ABORT timeout of {}ms", (Object)this.as, (Object)timeout);
                    if (!this.writeLock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                        Timeout.LOG.info("{}: A-ABORT timeout expired", (Object)this.as);
                        return;
                    }
                }
                catch (InterruptedException e) {
                    if (this.writeLock.tryLock()) break block8;
                    Timeout.LOG.info("{}: A-ABORT timeout interrupted: {}", (Object)this.as, (Object)e.getMessage());
                    return;
                }
            }
            Timeout.LOG.debug("{}: stop A-ABORT timeout", (Object)this.as);
        }
        byte[] b = new byte[]{(byte)pdutype, 0, 0, 0, 0, 4, 0, (byte)result, (byte)source, (byte)reason};
        try {
            this.out.write(b);
            this.out.flush();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    private void writePDU(int pdulen) throws IOException {
        this.writeLock.lock();
        try {
            this.out.write(this.buf, 0, 6 + pdulen);
            this.out.flush();
        }
        catch (IOException e) {
            this.as.onIOException(e);
            throw e;
        }
        finally {
            this.writeLock.unlock();
        }
        this.pdvpos = 6;
        this.pos = 12;
    }

    private void encode(AAssociateRQAC rqac, int pduType, int pcItemType) {
        rqac.checkCallingAET();
        rqac.checkCalledAET();
        int pdulen = rqac.length();
        if (this.buf.length < 6 + pdulen) {
            this.buf = new byte[6 + pdulen];
        }
        this.pos = 0;
        this.put(pduType);
        this.put(0);
        this.putInt(pdulen);
        this.putShort(rqac.getProtocolVersion());
        this.put(0);
        this.put(0);
        this.encodeAET(rqac.getCalledAET());
        this.encodeAET(rqac.getCallingAET());
        this.put(rqac.getReservedBytes(), 0, 32);
        this.encodeStringItem(16, rqac.getApplicationContext());
        for (PresentationContext pc : rqac.getPresentationContexts()) {
            this.encode(pc, pcItemType);
        }
        this.encodeUserInfo(rqac);
    }

    private void put(int ch) {
        this.buf[this.pos++] = (byte)ch;
    }

    private void put(byte[] b) {
        this.put(b, 0, b.length);
    }

    private void put(byte[] b, int off, int len) {
        System.arraycopy(b, off, this.buf, this.pos, len);
        this.pos += len;
    }

    private void putShort(int v) {
        this.buf[this.pos++] = (byte)(v >> 8);
        this.buf[this.pos++] = (byte)v;
    }

    private void putInt(int v) {
        this.buf[this.pos++] = (byte)(v >> 24);
        this.buf[this.pos++] = (byte)(v >> 16);
        this.buf[this.pos++] = (byte)(v >> 8);
        this.buf[this.pos++] = (byte)v;
    }

    private void putString(String s) {
        int len = s.length();
        s.getBytes(0, len, this.buf, this.pos);
        this.pos += len;
    }

    private void encode(byte[] b) {
        this.putShort(b.length);
        this.put(b, 0, b.length);
    }

    private void encode(String s) {
        this.putShort(s.length());
        this.putString(s);
    }

    private void encodeAET(String aet) {
        int endpos = this.pos + 16;
        this.putString(aet);
        while (this.pos < endpos) {
            this.put(32);
        }
    }

    private void encodeItemHeader(int type, int len) {
        this.put(type);
        this.put(0);
        this.putShort(len);
    }

    private void encodeStringItem(int type, String s) {
        if (s == null) {
            return;
        }
        this.encodeItemHeader(type, s.length());
        this.putString(s);
    }

    private void encode(PresentationContext pc, int pcItemType) {
        this.encodeItemHeader(pcItemType, pc.length());
        this.put(pc.getPCID());
        this.put(0);
        this.put(pc.getResult());
        this.put(0);
        this.encodeStringItem(48, pc.getAbstractSyntax());
        for (String ts : pc.getTransferSyntaxes()) {
            this.encodeStringItem(64, ts);
        }
    }

    private void encodeUserInfo(AAssociateRQAC rqac) {
        this.encodeItemHeader(80, rqac.userInfoLength());
        this.encodeMaxPDULength(rqac.getMaxPDULength());
        this.encodeStringItem(82, rqac.getImplClassUID());
        if (rqac.isAsyncOps()) {
            this.encodeAsyncOpsWindow(rqac);
        }
        for (RoleSelection roleSelection : rqac.getRoleSelections()) {
            this.encode(roleSelection);
        }
        this.encodeStringItem(85, rqac.getImplVersionName());
        for (ExtendedNegotiation extendedNegotiation : rqac.getExtendedNegotiations()) {
            this.encode(extendedNegotiation);
        }
        for (CommonExtendedNegotiation commonExtendedNegotiation : rqac.getCommonExtendedNegotiations()) {
            this.encode(commonExtendedNegotiation);
        }
        this.encode(rqac.getUserIdentityRQ());
        this.encode(rqac.getUserIdentityAC());
    }

    private void encodeMaxPDULength(int maxPDULength) {
        this.encodeItemHeader(81, 4);
        this.putInt(maxPDULength);
    }

    private void encodeAsyncOpsWindow(AAssociateRQAC rqac) {
        this.encodeItemHeader(83, 4);
        this.putShort(rqac.getMaxOpsInvoked());
        this.putShort(rqac.getMaxOpsPerformed());
    }

    private void encode(RoleSelection rs) {
        this.encodeItemHeader(84, rs.length());
        this.encode(rs.getSOPClassUID());
        this.put(rs.isSCU() ? 1 : 0);
        this.put(rs.isSCP() ? 1 : 0);
    }

    private void encode(ExtendedNegotiation extNeg) {
        this.encodeItemHeader(86, extNeg.length());
        this.encode(extNeg.getSOPClassUID());
        this.put(extNeg.getInformation());
    }

    private void encode(CommonExtendedNegotiation extNeg) {
        this.encodeItemHeader(87, extNeg.length());
        this.encode(extNeg.getSOPClassUID());
        this.encode(extNeg.getServiceClassUID());
        this.putShort(extNeg.getRelatedGeneralSOPClassUIDsLength());
        for (String cuid : extNeg.getRelatedGeneralSOPClassUIDs()) {
            this.encode(cuid);
        }
    }

    private void encode(UserIdentityRQ userIdentity) {
        if (userIdentity == null) {
            return;
        }
        this.encodeItemHeader(88, userIdentity.length());
        this.put(userIdentity.getType());
        this.put(userIdentity.isPositiveResponseRequested() ? 1 : 0);
        this.encode(userIdentity.getPrimaryField());
        this.encode(userIdentity.getSecondaryField());
    }

    private void encode(UserIdentityAC userIdentity) {
        if (userIdentity == null) {
            return;
        }
        this.encodeItemHeader(89, userIdentity.length());
        this.encode(userIdentity.getServerResponse());
    }

    @Override
    public void write(int b) throws IOException {
        this.checkThread();
        this.flushPDataTF();
        this.put(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        int write;
        this.checkThread();
        int pos = off;
        for (int remaining = len; remaining > 0; remaining -= write) {
            this.flushPDataTF();
            write = Math.min(remaining, this.free());
            this.put(b, pos, write);
            pos += write;
        }
    }

    @Override
    public void close() {
        this.checkThread();
        this.encodePDVHeader(2);
    }

    @Override
    public void copyFrom(InputStream in, int len) throws IOException {
        int copy;
        this.checkThread();
        for (int remaining = len; remaining > 0; remaining -= copy) {
            this.flushPDataTF();
            copy = in.read(this.buf, this.pos, Math.min(remaining, this.free()));
            if (copy == -1) {
                throw new EOFException();
            }
            this.pos += copy;
        }
    }

    @Override
    public void copyFrom(InputStream in) throws IOException {
        this.checkThread();
        while (true) {
            this.flushPDataTF();
            int copy = in.read(this.buf, this.pos, this.free());
            if (copy == -1) {
                return;
            }
            this.pos += copy;
        }
    }

    private void checkThread() {
        if (this.th != Thread.currentThread()) {
            throw new IllegalStateException("Entered by wrong thread");
        }
    }

    private int free() {
        return this.maxpdulen + 6 - this.pos;
    }

    private void flushPDataTF() throws IOException {
        if (this.free() > 0) {
            return;
        }
        this.encodePDVHeader(0);
        this.as.writePDataTF();
    }

    private void encodePDVHeader(int last) {
        int endpos = this.pos;
        int pdvlen = endpos - this.pdvpos - 4;
        this.pos = this.pdvpos;
        this.putInt(pdvlen);
        this.put(this.pdvpcid);
        this.put(this.pdvcmd | last);
        this.pos = endpos;
        Association.LOG.trace("{} << PDV[len={}, pcid={}, mch={}]", new Object[]{this.as, pdvlen, this.pdvpcid, this.pdvcmd | last});
    }

    public void writePDataTF() throws IOException {
        int pdulen = this.pos - 6;
        this.pos = 0;
        this.put(4);
        this.put(0);
        this.putInt(pdulen);
        Association.LOG.trace("{} << P-DATA-TF[len={}]", new Object[]{this.as, pdulen});
        this.writePDU(pdulen);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeDIMSE(PresentationContext pc, Attributes cmd, DataWriter dataWriter) throws IOException {
        Object object = this.dimseLock;
        synchronized (object) {
            int pcid = pc.getPCID();
            String tsuid = pc.getTransferSyntax();
            Dimse dimse = Dimse.valueOf(cmd.getInt(256, -1));
            if (!dimse.isRSP() || !Status.isPending(cmd.getInt(2304, -1))) {
                this.as.incSentCount(dimse);
            }
            if (Dimse.LOG.isInfoEnabled()) {
                Dimse.LOG.info("{} << {}", (Object)this.as, (Object)dimse.toString(cmd, pcid, tsuid));
                if (Dimse.LOG.isDebugEnabled()) {
                    Dimse.LOG.debug("{} << {} Command:\n{}", new Object[]{this.as, dimse.toString(cmd), cmd});
                }
            }
            this.th = Thread.currentThread();
            this.maxpdulen = this.as.getMaxPDULengthSend();
            if (this.buf.length < this.maxpdulen + 6) {
                this.buf = new byte[this.maxpdulen + 6];
            }
            this.pdvpcid = pcid;
            this.pdvcmd = 1;
            DicomOutputStream cmdout = new DicomOutputStream(this, "1.2.840.10008.1.2");
            cmdout.writeCommand(cmd);
            cmdout.close();
            if (dataWriter != null) {
                if (!this.as.isPackPDV()) {
                    this.as.writePDataTF();
                } else {
                    this.pdvpos = this.pos;
                    this.pos += 6;
                }
                this.pdvcmd = 0;
                if (Dimse.LOG.isDebugEnabled()) {
                    if (dataWriter instanceof DataWriterAdapter) {
                        Dimse.LOG.debug("{} << {} Dataset:\n{}", new Object[]{this.as, dimse.toString(cmd), ((DataWriterAdapter)dataWriter).getDataset()});
                    } else {
                        Dimse.LOG.debug("{} << {} Dataset sending...", (Object)this.as, (Object)dimse.toString(cmd));
                    }
                }
                dataWriter.writeTo(this, tsuid);
                if (Dimse.LOG.isDebugEnabled() && !(dataWriter instanceof DataWriterAdapter)) {
                    Dimse.LOG.debug("{} << {} Dataset sent", (Object)this.as, (Object)dimse.toString(cmd));
                }
                this.close();
            }
            this.as.writePDataTF();
            this.th = null;
        }
    }
}

