/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.PDVInputStream;
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
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;

class PDUDecoder
extends PDVInputStream {
    private static final String UNRECOGNIZED_PDU = "{}: unrecognized PDU[type={}, len={}]";
    private static final String INVALID_PDU_LENGTH = "{}: invalid length of PDU[type={}, len={}]";
    private static final String INVALID_COMMON_EXTENDED_NEGOTIATION = "{}: invalid Common Extended Negotiation sub-item in PDU[type={}, len={}]";
    private static final String INVALID_USER_IDENTITY = "{}: invalid User Identity sub-item in PDU[type={}, len={}]";
    private static final String INVALID_PDV = "{}: invalid PDV in PDU[type={}, len={}]";
    private static final String UNEXPECTED_PDV_TYPE = "{}: unexpected PDV type in PDU[type={}, len={}]";
    private static final String UNEXPECTED_PDV_PCID = "{}: unexpected pcid in PDV in PDU[type={}, len={}]";
    private static final int MAX_PDU_LEN = 0x1000000;
    private final Association as;
    private final InputStream in;
    private final Thread th;
    private byte[] buf = new byte[16384];
    private int pos;
    private int pdutype;
    private int pdulen;
    private int pcid = -1;
    private int pdvmch;
    private int pdvend;

    public PDUDecoder(Association as, InputStream in) {
        this.as = as;
        this.in = in;
        this.th = Thread.currentThread();
    }

    private int remaining() {
        return this.pdulen + 6 - this.pos;
    }

    private boolean hasRemaining() {
        return this.pos < this.pdulen + 6;
    }

    private int get() {
        if (!this.hasRemaining()) {
            throw new IndexOutOfBoundsException();
        }
        return this.buf[this.pos++] & 0xFF;
    }

    private void get(byte[] b, int off, int len) {
        if (len > this.remaining()) {
            throw new IndexOutOfBoundsException();
        }
        System.arraycopy(this.buf, this.pos, b, off, len);
        this.pos += len;
    }

    private void skip(int len) {
        if (len > this.remaining()) {
            throw new IndexOutOfBoundsException();
        }
        this.pos += len;
    }

    private int getUnsignedShort() {
        int val = ByteUtils.bytesToUShortBE(this.buf, this.pos);
        this.pos += 2;
        return val;
    }

    private int getInt() {
        int val = ByteUtils.bytesToIntBE(this.buf, this.pos);
        this.pos += 4;
        return val;
    }

    private byte[] getBytes(int len) {
        byte[] bs = new byte[len];
        this.get(bs, 0, len);
        return bs;
    }

    private byte[] decodeBytes() {
        return this.getBytes(this.getUnsignedShort());
    }

    public void nextPDU() throws IOException {
        this.checkThread();
        Association.LOG.trace("{}: waiting for PDU", (Object)this.as);
        this.readFully(0, 10);
        this.pos = 0;
        this.pdutype = this.get();
        this.get();
        this.pdulen = this.getInt();
        Association.LOG.trace("{} >> PDU[type={}, len={}]", new Object[]{this.as, this.pdutype, (long)this.pdulen & 0xFFFFFFFFL});
        switch (this.pdutype) {
            case 1: {
                this.readPDU();
                this.as.onAAssociateRQ((AAssociateRQ)this.decode(new AAssociateRQ()));
                return;
            }
            case 2: {
                this.readPDU();
                this.as.onAAssociateAC((AAssociateAC)this.decode(new AAssociateAC()));
                return;
            }
            case 4: {
                this.readPDU();
                this.as.onPDataTF();
                return;
            }
            case 3: {
                this.checkPDULength(4);
                this.get();
                this.as.onAAssociateRJ(new AAssociateRJ(this.get(), this.get(), this.get()));
                break;
            }
            case 5: {
                this.checkPDULength(4);
                this.as.onAReleaseRQ();
                break;
            }
            case 6: {
                this.checkPDULength(4);
                this.as.onAReleaseRP();
                break;
            }
            case 7: {
                this.checkPDULength(4);
                this.get();
                this.get();
                this.as.onAAbort(new AAbort(this.get(), this.get()));
                break;
            }
            default: {
                this.abort(1, UNRECOGNIZED_PDU);
            }
        }
    }

    private void checkThread() {
        if (this.th != Thread.currentThread()) {
            throw new IllegalStateException("Entered by wrong thread");
        }
    }

    private void checkPDULength(int len) throws AAbort {
        if (this.pdulen != len) {
            this.abort(6, INVALID_PDU_LENGTH);
        }
    }

    private void readPDU() throws IOException {
        if (this.pdulen < 4 || this.pdulen > 0x1000000) {
            this.abort(6, INVALID_PDU_LENGTH);
        }
        if (6 + this.pdulen > this.buf.length) {
            this.buf = Arrays.copyOf(this.buf, 6 + this.pdulen);
        }
        this.readFully(10, this.pdulen - 4);
    }

    private void readFully(int off, int len) throws IOException {
        StreamUtils.readFully(this.in, this.buf, off, len);
    }

    private void abort(int reason, String logmsg) throws AAbort {
        Association.LOG.warn(logmsg, new Object[]{this.as, this.pdutype, (long)this.pdulen & 0xFFFFFFFFL});
        throw new AAbort(2, reason);
    }

    private String getString(int len) {
        int len0;
        if (this.pos + len > this.pdulen + 6) {
            throw new IndexOutOfBoundsException();
        }
        for (len0 = len; len0 > 0 && this.buf[this.pos + len0 - 1] == 0; --len0) {
        }
        String s = new String(this.buf, 0, this.pos, len0);
        this.pos += len;
        return s;
    }

    private String decodeString() {
        return this.getString(this.getUnsignedShort());
    }

    private AAssociateRQAC decode(AAssociateRQAC rqac) throws AAbort {
        try {
            rqac.setImplVersionName(null);
            rqac.setProtocolVersion(this.getUnsignedShort());
            this.get();
            this.get();
            rqac.setCalledAET(this.getString(16).trim());
            rqac.setCallingAET(this.getString(16).trim());
            rqac.setReservedBytes(this.getBytes(32));
            while (this.pos < this.pdulen) {
                this.decodeItem(rqac);
            }
            this.checkPDULength(this.pos - 6);
        }
        catch (IndexOutOfBoundsException e) {
            this.abort(6, INVALID_PDU_LENGTH);
        }
        return rqac;
    }

    private void decodeItem(AAssociateRQAC rqac) throws AAbort {
        int itemType = this.get();
        this.get();
        int itemLen = this.getUnsignedShort();
        switch (itemType) {
            case 16: {
                rqac.setApplicationContext(this.getString(itemLen));
                break;
            }
            case 32: 
            case 33: {
                rqac.addPresentationContext(this.decodePC(itemLen));
                break;
            }
            case 80: {
                this.decodeUserInfo(itemLen, rqac);
                break;
            }
            default: {
                this.skip(itemLen);
            }
        }
    }

    private PresentationContext decodePC(int itemLen) {
        int pcid = this.get();
        this.get();
        int result = this.get();
        this.get();
        String as = null;
        ArrayList<String> tss = new ArrayList<String>(1);
        int endpos = this.pos + itemLen - 4;
        block4: while (this.pos < endpos) {
            int subItemType = this.get() & 0xFF;
            this.get();
            int subItemLen = this.getUnsignedShort();
            switch (subItemType) {
                case 48: {
                    as = this.getString(subItemLen);
                    continue block4;
                }
                case 64: {
                    tss.add(this.getString(subItemLen));
                    continue block4;
                }
            }
            this.skip(subItemLen);
        }
        return new PresentationContext(pcid, result, as, tss.toArray(new String[tss.size()]));
    }

    private void decodeUserInfo(int itemLength, AAssociateRQAC rqac) throws AAbort {
        int endpos = this.pos + itemLength;
        while (this.pos < endpos) {
            this.decodeUserInfoSubItem(rqac);
        }
    }

    private void decodeUserInfoSubItem(AAssociateRQAC rqac) throws AAbort {
        int itemType = this.get();
        this.get();
        int itemLen = this.getUnsignedShort();
        switch (itemType) {
            case 81: {
                rqac.setMaxPDULength(this.getInt());
                break;
            }
            case 82: {
                rqac.setImplClassUID(this.getString(itemLen));
                break;
            }
            case 83: {
                rqac.setMaxOpsInvoked(this.getUnsignedShort());
                rqac.setMaxOpsPerformed(this.getUnsignedShort());
                break;
            }
            case 84: {
                rqac.addRoleSelection(this.decodeRoleSelection(itemLen));
                break;
            }
            case 85: {
                rqac.setImplVersionName(this.getString(itemLen));
                break;
            }
            case 86: {
                rqac.addExtendedNegotiation(this.decodeExtNeg(itemLen));
                break;
            }
            case 87: {
                rqac.addCommonExtendedNegotiation(this.decodeCommonExtNeg(itemLen));
                break;
            }
            case 88: {
                rqac.setUserIdentityRQ(this.decodeUserIdentityRQ(itemLen));
                break;
            }
            case 89: {
                rqac.setUserIdentityAC(this.decodeUserIdentityAC(itemLen));
                break;
            }
            default: {
                this.skip(itemLen);
            }
        }
    }

    private RoleSelection decodeRoleSelection(int itemLen) {
        String cuid = this.decodeString();
        boolean scu = this.get() != 0;
        boolean scp = this.get() != 0;
        return new RoleSelection(cuid, scu, scp);
    }

    private ExtendedNegotiation decodeExtNeg(int itemLen) {
        int uidLength = this.getUnsignedShort();
        String cuid = this.getString(uidLength);
        byte[] info = this.getBytes(itemLen - uidLength - 2);
        return new ExtendedNegotiation(cuid, info);
    }

    private CommonExtendedNegotiation decodeCommonExtNeg(int itemLen) throws AAbort {
        int endPos = this.pos + itemLen;
        String sopCUID = this.getString(this.getUnsignedShort());
        String serviceCUID = this.getString(this.getUnsignedShort());
        ArrayList<String> relSopCUIDs = new ArrayList<String>(1);
        int relSopCUIDsLen = this.getUnsignedShort();
        int endRelSopCUIDs = this.pos + relSopCUIDsLen;
        while (this.pos < endRelSopCUIDs) {
            relSopCUIDs.add(this.decodeString());
        }
        if (this.pos != endRelSopCUIDs || this.pos > endPos) {
            this.abort(6, INVALID_COMMON_EXTENDED_NEGOTIATION);
        }
        this.skip(endPos - this.pos);
        return new CommonExtendedNegotiation(sopCUID, serviceCUID, relSopCUIDs.toArray(new String[relSopCUIDs.size()]));
    }

    private UserIdentityRQ decodeUserIdentityRQ(int itemLen) throws AAbort {
        int endPos = this.pos + itemLen;
        int type = this.get() & 0xFF;
        boolean rspReq = this.get() != 0;
        byte[] primaryField = this.decodeBytes();
        byte[] secondaryField = this.decodeBytes();
        if (this.pos != endPos) {
            this.abort(6, INVALID_USER_IDENTITY);
        }
        return new UserIdentityRQ(type, rspReq, primaryField, secondaryField);
    }

    private UserIdentityAC decodeUserIdentityAC(int itemLen) throws AAbort {
        int endPos = this.pos + itemLen;
        byte[] serverResponse = this.decodeBytes();
        if (this.pos != endPos) {
            this.abort(6, INVALID_USER_IDENTITY);
        }
        return new UserIdentityAC(serverResponse);
    }

    public void decodeDIMSE() throws IOException {
        this.checkThread();
        if (this.pcid != -1) {
            return;
        }
        this.nextPDV(1, -1);
        PresentationContext pc = this.as.getPresentationContext(this.pcid);
        if (pc == null) {
            Association.LOG.warn("{}: No Presentation Context with given ID - {}", (Object)this.as, (Object)this.pcid);
            throw new AAbort();
        }
        if (!pc.isAccepted()) {
            Association.LOG.warn("{}: No accepted Presentation Context with given ID - {}", (Object)this.as, (Object)this.pcid);
            throw new AAbort();
        }
        Attributes cmd = this.readCommand();
        Dimse dimse = this.dimseOf(cmd);
        String tsuid = pc.getTransferSyntax();
        if (Dimse.LOG.isInfoEnabled()) {
            Dimse.LOG.info("{} >> {}", (Object)this.as, (Object)dimse.toString(cmd, this.pcid, tsuid));
            if (Dimse.LOG.isDebugEnabled()) {
                Dimse.LOG.debug("{} >> {} Command:\n{}", new Object[]{this.as, dimse.toString(cmd), cmd});
            }
        }
        if (dimse == Dimse.C_CANCEL_RQ) {
            this.as.onCancelRQ(cmd);
        } else if (Commands.hasDataset(cmd)) {
            this.nextPDV(0, this.pcid);
            if (dimse.isRSP()) {
                Attributes data = this.readDataset(tsuid);
                if (Dimse.LOG.isDebugEnabled()) {
                    Dimse.LOG.debug("{} >> {} Dataset:\n{}", new Object[]{this.as, dimse.toString(cmd), data});
                }
                this.as.onDimseRSP(dimse, cmd, data);
            } else {
                if (Dimse.LOG.isDebugEnabled()) {
                    Dimse.LOG.debug("{} >> {} Dataset receiving...", (Object)this.as, (Object)dimse.toString(cmd));
                }
                this.as.onDimseRQ(pc, dimse, cmd, this);
                long skipped = this.skipAll();
                if (skipped > 0L) {
                    Association.LOG.debug("{}: Service User did not consume {} bytes of DIMSE data.", (Object)this.as, (Object)skipped);
                }
            }
        } else if (dimse.isRSP()) {
            this.as.onDimseRSP(dimse, cmd, null);
        } else {
            this.as.onDimseRQ(pc, dimse, cmd, null);
        }
        this.pcid = -1;
    }

    private Dimse dimseOf(Attributes cmd) throws AAbort {
        try {
            return Dimse.valueOf(cmd.getInt(256, 0));
        }
        catch (IllegalArgumentException e) {
            Dimse.LOG.info("{}: illegal DIMSE:", (Object)this.as);
            Dimse.LOG.info("\n{}", (Object)cmd);
            throw new AAbort();
        }
    }

    private Attributes readCommand() throws IOException {
        DicomInputStream in = new DicomInputStream((InputStream)this, "1.2.840.10008.1.2");
        try {
            Attributes attributes = in.readCommand();
            return attributes;
        }
        finally {
            SafeClose.close(in);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Attributes readDataset(String tsuid) throws IOException {
        DicomInputStream in = new DicomInputStream((InputStream)this, tsuid);
        try {
            Attributes attributes = in.readDataset();
            return attributes;
        }
        finally {
            SafeClose.close(in);
        }
    }

    private void nextPDV(int expectedPDVType, int expectedPCID) throws IOException {
        if (!this.hasRemaining()) {
            this.nextPDU();
            if (this.pdutype != 4) {
                Association.LOG.info("{}: Expected P-DATA-TF PDU but received PDU[type={}]", (Object)this.as, (Object)this.pdutype);
                throw new EOFException();
            }
        }
        if (this.remaining() < 6) {
            this.abort(6, INVALID_PDV);
        }
        int pdvlen = this.getInt();
        this.pdvend = this.pos + pdvlen;
        if (pdvlen < 2 || pdvlen > this.remaining()) {
            this.abort(6, INVALID_PDV);
        }
        this.pcid = this.get();
        this.pdvmch = this.get();
        Association.LOG.trace("{} >> PDV[len={}, pcid={}, mch={}]", new Object[]{this.as, pdvlen, this.pcid, this.pdvmch});
        if ((this.pdvmch & 1) != expectedPDVType) {
            this.abort(5, UNEXPECTED_PDV_TYPE);
        }
        if (expectedPCID != -1 && this.pcid != expectedPCID) {
            this.abort(5, UNEXPECTED_PDV_PCID);
        }
    }

    private boolean isLastPDV() throws IOException {
        while (this.pos == this.pdvend) {
            if ((this.pdvmch & 2) != 0) {
                return true;
            }
            this.nextPDV(this.pdvmch & 1, this.pcid);
        }
        return false;
    }

    boolean isPendingPDV() {
        return this.pcid != -1 && (this.pdvmch & 2) == 0;
    }

    @Override
    public int read() throws IOException {
        if (this.th != Thread.currentThread()) {
            throw new IllegalStateException("Entered by wrong thread");
        }
        if (this.isLastPDV()) {
            return -1;
        }
        return this.get();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.th != Thread.currentThread()) {
            throw new IllegalStateException("Entered by wrong thread");
        }
        if (this.isLastPDV()) {
            return -1;
        }
        int read = Math.min(len, this.pdvend - this.pos);
        this.get(b, off, read);
        return read;
    }

    @Override
    public final int available() {
        return this.pdvend - this.pos;
    }

    @Override
    public long skip(long n) throws IOException {
        if (this.th != Thread.currentThread()) {
            throw new IllegalStateException("Entered by wrong thread");
        }
        if (n <= 0L || this.isLastPDV()) {
            return 0L;
        }
        int skipped = (int)Math.min(n, (long)(this.pdvend - this.pos));
        this.skip(skipped);
        return skipped;
    }

    @Override
    public void close() throws IOException {
        if (this.th != Thread.currentThread()) {
            throw new IllegalStateException("Entered by wrong thread");
        }
        this.skipAll();
    }

    @Override
    public long skipAll() throws IOException {
        if (this.th != Thread.currentThread()) {
            throw new IllegalStateException("Entered by wrong thread");
        }
        long n = 0L;
        while (!this.isLastPDV()) {
            n += (long)(this.pdvend - this.pos);
            this.pos = this.pdvend;
        }
        return n;
    }

    @Override
    public void copyTo(OutputStream out, int length) throws IOException {
        if (this.th != Thread.currentThread()) {
            throw new IllegalStateException("Entered by wrong thread");
        }
        int remaining = length;
        while (remaining > 0) {
            if (this.isLastPDV()) {
                throw new EOFException("remaining: " + remaining);
            }
            int read = Math.min(remaining, this.pdvend - this.pos);
            out.write(this.buf, this.pos, read);
            remaining -= read;
            this.pos += read;
        }
    }

    @Override
    public void copyTo(OutputStream out) throws IOException {
        if (this.th != Thread.currentThread()) {
            throw new IllegalStateException("Entered by wrong thread");
        }
        while (!this.isLastPDV()) {
            out.write(this.buf, this.pos, this.pdvend - this.pos);
            this.pos = this.pdvend;
        }
    }
}

