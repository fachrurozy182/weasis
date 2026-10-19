/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.pdu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import org.dcm4che3.data.Implementation;
import org.dcm4che3.net.pdu.CommonExtendedNegotiation;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.RoleSelection;
import org.dcm4che3.net.pdu.UserIdentityAC;
import org.dcm4che3.net.pdu.UserIdentityRQ;
import org.dcm4che3.util.IntHashMap;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.UIDUtils;

public abstract class AAssociateRQAC {
    protected byte[] reservedBytes = new byte[32];
    protected int protocolVersion = 1;
    protected int maxPDULength = 16378;
    protected int maxOpsInvoked = 1;
    protected int maxOpsPerformed = 1;
    protected String calledAET;
    protected String callingAET;
    protected String applicationContext = "1.2.840.10008.3.1.1.1";
    protected String implClassUID = Implementation.getClassUID();
    protected String implVersionName = Implementation.getVersionName();
    protected UserIdentityRQ userIdentityRQ;
    protected UserIdentityAC userIdentityAC;
    protected final ArrayList<PresentationContext> pcs = new ArrayList();
    protected final IntHashMap<PresentationContext> pcidMap = new IntHashMap();
    protected final LinkedHashMap<String, RoleSelection> roleSelMap = new LinkedHashMap();
    protected final LinkedHashMap<String, ExtendedNegotiation> extNegMap = new LinkedHashMap();
    protected final LinkedHashMap<String, CommonExtendedNegotiation> commonExtNegMap = new LinkedHashMap();

    public void checkCallingAET() {
        if (this.callingAET == null) {
            throw new IllegalStateException("Calling AET not initalized");
        }
    }

    public void checkCalledAET() {
        if (this.calledAET == null) {
            throw new IllegalStateException("Called AET not initalized");
        }
    }

    public final int getProtocolVersion() {
        return this.protocolVersion;
    }

    public final void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public final byte[] getReservedBytes() {
        return (byte[])this.reservedBytes.clone();
    }

    public final void setReservedBytes(byte[] reservedBytes) {
        if (reservedBytes.length != 32) {
            throw new IllegalArgumentException("reservedBytes.length: " + reservedBytes.length);
        }
        System.arraycopy(reservedBytes, 0, this.reservedBytes, 0, 32);
    }

    public final String getCalledAET() {
        return this.calledAET;
    }

    public final void setCalledAET(String calledAET) {
        if (calledAET.length() > 16) {
            throw new IllegalArgumentException("calledAET: " + calledAET);
        }
        this.calledAET = calledAET;
    }

    public final String getCallingAET() {
        return this.callingAET;
    }

    public final void setCallingAET(String callingAET) {
        if (callingAET.length() > 16) {
            throw new IllegalArgumentException("callingAET: " + callingAET);
        }
        this.callingAET = callingAET;
    }

    public final String getApplicationContext() {
        return this.applicationContext;
    }

    public final void setApplicationContext(String applicationContext) {
        if (applicationContext == null) {
            throw new NullPointerException();
        }
        this.applicationContext = applicationContext;
    }

    public final int getMaxPDULength() {
        return this.maxPDULength;
    }

    public final void setMaxPDULength(int maxPDULength) {
        this.maxPDULength = maxPDULength;
    }

    public final int getMaxOpsInvoked() {
        return this.maxOpsInvoked;
    }

    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        this.maxOpsInvoked = maxOpsInvoked;
    }

    public final int getMaxOpsPerformed() {
        return this.maxOpsPerformed;
    }

    public final void setMaxOpsPerformed(int maxOpsPerformed) {
        this.maxOpsPerformed = maxOpsPerformed;
    }

    public final boolean isAsyncOps() {
        return this.maxOpsInvoked != 1 || this.maxOpsPerformed != 1;
    }

    public final String getImplClassUID() {
        return this.implClassUID;
    }

    public final void setImplClassUID(String implClassUID) {
        if (implClassUID == null) {
            throw new NullPointerException();
        }
        this.implClassUID = implClassUID;
    }

    public final String getImplVersionName() {
        return this.implVersionName;
    }

    public final void setImplVersionName(String implVersionName) {
        this.implVersionName = implVersionName;
    }

    public final UserIdentityRQ getUserIdentityRQ() {
        return this.userIdentityRQ;
    }

    public void setUserIdentityRQ(UserIdentityRQ userIdentityRQ) {
        this.userIdentityRQ = userIdentityRQ;
    }

    public final UserIdentityAC getUserIdentityAC() {
        return this.userIdentityAC;
    }

    public void setUserIdentityAC(UserIdentityAC userIdentityAC) {
        this.userIdentityAC = userIdentityAC;
    }

    public List<PresentationContext> getPresentationContexts() {
        return Collections.unmodifiableList(this.pcs);
    }

    public int getNumberOfPresentationContexts() {
        return this.pcs.size();
    }

    public PresentationContext getPresentationContext(int pcid) {
        return this.pcidMap.get(pcid);
    }

    public void addPresentationContext(PresentationContext pc) {
        int pcid = pc.getPCID();
        if (this.pcidMap.containsKey(pcid)) {
            throw new IllegalStateException("Already contains Presentation Context with pid: " + pcid);
        }
        this.pcidMap.put(pcid, pc);
        this.pcs.add(pc);
    }

    public boolean removePresentationContext(PresentationContext pc) {
        if (!this.pcs.remove(pc)) {
            return false;
        }
        this.pcidMap.remove(pc.getPCID());
        return true;
    }

    public Collection<RoleSelection> getRoleSelections() {
        return Collections.unmodifiableCollection(this.roleSelMap.values());
    }

    public RoleSelection getRoleSelectionFor(String cuid) {
        return this.roleSelMap.get(cuid);
    }

    public RoleSelection addRoleSelection(RoleSelection rs) {
        return this.roleSelMap.put(rs.getSOPClassUID(), rs);
    }

    public RoleSelection removeRoleSelectionFor(String cuid) {
        return (RoleSelection)this.roleSelMap.remove(cuid);
    }

    public Collection<ExtendedNegotiation> getExtendedNegotiations() {
        return Collections.unmodifiableCollection(this.extNegMap.values());
    }

    public ExtendedNegotiation getExtNegotiationFor(String cuid) {
        return this.extNegMap.get(cuid);
    }

    public ExtendedNegotiation addExtendedNegotiation(ExtendedNegotiation extNeg) {
        return this.extNegMap.put(extNeg.getSOPClassUID(), extNeg);
    }

    public ExtendedNegotiation removeExtendedNegotiationFor(String cuid) {
        return (ExtendedNegotiation)this.extNegMap.remove(cuid);
    }

    public Collection<CommonExtendedNegotiation> getCommonExtendedNegotiations() {
        return Collections.unmodifiableCollection(this.commonExtNegMap.values());
    }

    public CommonExtendedNegotiation getCommonExtendedNegotiationFor(String cuid) {
        return this.commonExtNegMap.get(cuid);
    }

    public CommonExtendedNegotiation addCommonExtendedNegotiation(CommonExtendedNegotiation extNeg) {
        return this.commonExtNegMap.put(extNeg.getSOPClassUID(), extNeg);
    }

    public CommonExtendedNegotiation removeCommonExtendedNegotiationFor(String cuid) {
        return (CommonExtendedNegotiation)this.commonExtNegMap.remove(cuid);
    }

    public int length() {
        int len = 68;
        len += 4 + this.applicationContext.length();
        for (PresentationContext pc : this.pcs) {
            len += 4 + pc.length();
        }
        return len += 4 + this.userInfoLength();
    }

    public int userInfoLength() {
        int len = 8;
        len += 4 + this.implClassUID.length();
        if (this.isAsyncOps()) {
            len += 8;
        }
        for (RoleSelection rs : this.roleSelMap.values()) {
            len += 4 + rs.length();
        }
        if (this.implVersionName != null) {
            len += 4 + this.implVersionName.length();
        }
        for (ExtendedNegotiation en : this.extNegMap.values()) {
            len += 4 + en.length();
        }
        for (CommonExtendedNegotiation cen : this.commonExtNegMap.values()) {
            len += 4 + cen.length();
        }
        if (this.userIdentityRQ != null) {
            len += 4 + this.userIdentityRQ.length();
        }
        if (this.userIdentityAC != null) {
            len += 4 + this.userIdentityAC.length();
        }
        return len;
    }

    protected StringBuilder promptTo(String header, StringBuilder sb) {
        sb.append(header).append(StringUtils.LINE_SEPARATOR).append("  calledAET: ").append(this.calledAET).append(StringUtils.LINE_SEPARATOR).append("  callingAET: ").append(this.callingAET).append(StringUtils.LINE_SEPARATOR).append("  applicationContext: ");
        UIDUtils.promptTo(this.applicationContext, sb).append(StringUtils.LINE_SEPARATOR).append("  implClassUID: ").append(this.implClassUID).append(StringUtils.LINE_SEPARATOR).append("  implVersionName: ").append(this.implVersionName).append(StringUtils.LINE_SEPARATOR).append("  maxPDULength: ").append(this.maxPDULength).append(StringUtils.LINE_SEPARATOR).append("  maxOpsInvoked/maxOpsPerformed: ").append(this.maxOpsInvoked).append("/").append(this.maxOpsPerformed).append(StringUtils.LINE_SEPARATOR);
        if (this.userIdentityRQ != null) {
            this.userIdentityRQ.promptTo(sb).append(StringUtils.LINE_SEPARATOR);
        }
        if (this.userIdentityAC != null) {
            this.userIdentityAC.promptTo(sb).append(StringUtils.LINE_SEPARATOR);
        }
        for (PresentationContext presentationContext : this.pcs) {
            presentationContext.promptTo(sb).append(StringUtils.LINE_SEPARATOR);
        }
        for (RoleSelection roleSelection : this.roleSelMap.values()) {
            roleSelection.promptTo(sb).append(StringUtils.LINE_SEPARATOR);
        }
        for (ExtendedNegotiation extendedNegotiation : this.extNegMap.values()) {
            extendedNegotiation.promptTo(sb).append(StringUtils.LINE_SEPARATOR);
        }
        for (CommonExtendedNegotiation commonExtendedNegotiation : this.commonExtNegMap.values()) {
            commonExtendedNegotiation.promptTo(sb).append(StringUtils.LINE_SEPARATOR);
        }
        return sb.append("]");
    }
}

