/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.net;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.AEExtension;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.AssociationMonitor;
import org.dcm4che3.net.CompatibleConnection;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.DimseRQHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.State;
import org.dcm4che3.net.StorageOptions;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.pdu.AAbort;
import org.dcm4che3.net.pdu.AAssociateAC;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.CommonExtendedNegotiation;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.RoleSelection;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationEntity
implements Serializable {
    private static final long serialVersionUID = 3883790997057469573L;
    protected static final Logger LOG = LoggerFactory.getLogger(ApplicationEntity.class);
    private Device device;
    private String aet;
    private String description;
    private byte[][] vendorData = new byte[0][];
    private String[] applicationClusters = new String[0];
    private String[] prefCalledAETs = new String[0];
    private String[] prefCallingAETs = new String[0];
    private String[] prefTransferSyntaxes = new String[0];
    private String[] supportedCharacterSets = new String[0];
    private boolean acceptor = true;
    private boolean initiator = true;
    private Boolean installed;
    private Boolean roleSelectionNegotiationLenient;
    private String shareTransferCapabilitiesFromAETitle;
    private String hl7ApplicationName;
    private final LinkedHashSet<String> acceptedCallingAETs = new LinkedHashSet();
    private final LinkedHashSet<String> otherAETs = new LinkedHashSet();
    private final LinkedHashSet<String> noAsyncModeCalledAETs = new LinkedHashSet();
    private final LinkedHashMap<String, String> masqueradeCallingAETs = new LinkedHashMap();
    private final LinkedHashMap<String, String> masqueradeCalledAETs = new LinkedHashMap();
    private final List<Connection> conns = new ArrayList<Connection>(1);
    private final LinkedHashMap<String, TransferCapability> scuTCs = new LinkedHashMap();
    private final LinkedHashMap<String, TransferCapability> scpTCs = new LinkedHashMap();
    private final LinkedHashMap<Class<? extends AEExtension>, AEExtension> extensions = new LinkedHashMap();
    private transient DimseRQHandler dimseRQHandler;

    public ApplicationEntity() {
    }

    public ApplicationEntity(String aeTitle) {
        this.setAETitle(aeTitle);
    }

    public final Device getDevice() {
        return this.device;
    }

    void setDevice(Device device) {
        if (device != null) {
            if (this.device != null) {
                throw new IllegalStateException("already owned by " + this.device.getDeviceName());
            }
            for (Connection conn : this.conns) {
                if (conn.getDevice() == device) continue;
                throw new IllegalStateException(conn + " not owned by " + device.getDeviceName());
            }
        }
        this.device = device;
    }

    public final String getAETitle() {
        return this.aet;
    }

    public void setAETitle(String aet) {
        if (aet.isEmpty()) {
            throw new IllegalArgumentException("AE title cannot be empty");
        }
        Device device = this.device;
        if (device != null) {
            device.removeApplicationEntity(this.aet);
        }
        this.aet = aet;
        if (device != null) {
            device.addApplicationEntity(this);
        }
    }

    public final String getDescription() {
        return this.description;
    }

    public final void setDescription(String description) {
        this.description = description;
    }

    public final byte[][] getVendorData() {
        return this.vendorData;
    }

    public final void setVendorData(byte[] ... vendorData) {
        this.vendorData = vendorData;
    }

    public String[] getApplicationClusters() {
        return this.applicationClusters;
    }

    public void setApplicationClusters(String ... clusters) {
        this.applicationClusters = clusters;
    }

    public String[] getPreferredCalledAETitles() {
        return this.prefCalledAETs;
    }

    public void setPreferredCalledAETitles(String ... aets) {
        this.prefCalledAETs = aets;
    }

    public String[] getPreferredCallingAETitles() {
        return this.prefCallingAETs;
    }

    public void setPreferredCallingAETitles(String ... aets) {
        this.prefCallingAETs = aets;
    }

    public String[] getPreferredTransferSyntaxes() {
        return this.prefTransferSyntaxes;
    }

    public void setPreferredTransferSyntaxes(String ... transferSyntaxes) {
        this.prefTransferSyntaxes = StringUtils.requireContainsNoEmpty(transferSyntaxes, "empty transferSyntax");
    }

    public String[] getAcceptedCallingAETitles() {
        return this.acceptedCallingAETs.toArray(new String[this.acceptedCallingAETs.size()]);
    }

    public void setAcceptedCallingAETitles(String ... aets) {
        this.acceptedCallingAETs.clear();
        for (String name : aets) {
            this.acceptedCallingAETs.add(name);
        }
    }

    public boolean isAcceptedCallingAETitle(String aet) {
        return this.acceptedCallingAETs.isEmpty() || this.acceptedCallingAETs.contains(aet);
    }

    public String[] getOtherAETitles() {
        return this.otherAETs.toArray(new String[this.otherAETs.size()]);
    }

    public void setOtherAETitles(String ... aets) {
        this.otherAETs.clear();
        for (String name : aets) {
            this.otherAETs.add(name);
        }
    }

    public boolean isOtherAETitle(String aet) {
        return this.otherAETs.contains(aet);
    }

    public String[] getNoAsyncModeCalledAETitles() {
        return this.noAsyncModeCalledAETs.toArray(new String[this.noAsyncModeCalledAETs.size()]);
    }

    public void setNoAsyncModeCalledAETitles(String ... aets) {
        this.noAsyncModeCalledAETs.clear();
        for (String aet : aets) {
            this.noAsyncModeCalledAETs.add(aet);
        }
    }

    public boolean isNoAsyncModeCalledAETitle(String calledAET) {
        return this.noAsyncModeCalledAETs.contains(calledAET);
    }

    public String[] getMasqueradeCallingAETitles() {
        String[] aets = new String[this.masqueradeCallingAETs.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : this.masqueradeCallingAETs.entrySet()) {
            aets[i++] = entry.getKey().equals("*") ? entry.getValue() : '[' + entry.getKey() + ']' + entry.getValue();
        }
        return aets;
    }

    public void setMasqueradeCallingAETitles(String ... aets) {
        this.masqueradeCallingAETs.clear();
        for (String aet : aets) {
            if (aet.charAt(0) == '[') {
                int end = aet.indexOf(93);
                if (end <= 0) continue;
                this.masqueradeCallingAETs.put(aet.substring(1, end), aet.substring(end + 1));
                continue;
            }
            this.masqueradeCallingAETs.put("*", aet);
        }
    }

    public String[] getMasqueradeCalledAETitles() {
        String[] aets = new String[this.masqueradeCalledAETs.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : this.masqueradeCalledAETs.entrySet()) {
            aets[i++] = entry.getKey() + ':' + entry.getValue();
        }
        return aets;
    }

    public void setMasqueradeCalledAETitles(String ... aets) {
        this.masqueradeCalledAETs.clear();
        for (String aet : aets) {
            int index = aet.indexOf(58);
            if (index <= 0) continue;
            this.masqueradeCalledAETs.put(aet.substring(0, index), aet.substring(index + 1));
        }
    }

    public String getCallingAETitle(String calledAET) {
        String callingAET = this.masqueradeCallingAETs.get(calledAET);
        if (callingAET == null && (callingAET = this.masqueradeCallingAETs.get("*")) == null) {
            callingAET = this.aet;
        }
        return callingAET;
    }

    public boolean isMasqueradeCallingAETitle(String calledAET) {
        return this.masqueradeCallingAETs.containsKey(calledAET) || this.masqueradeCallingAETs.containsKey("*");
    }

    public String masqueradeCalledAETitle(String calledAET) {
        return this.masqueradeCalledAETs.getOrDefault(calledAET, calledAET);
    }

    public String[] getSupportedCharacterSets() {
        return this.supportedCharacterSets;
    }

    public void setSupportedCharacterSets(String ... characterSets) {
        this.supportedCharacterSets = characterSets;
    }

    public final boolean isAssociationAcceptor() {
        return this.acceptor;
    }

    public final void setAssociationAcceptor(boolean acceptor) {
        this.acceptor = acceptor;
    }

    public final boolean isAssociationInitiator() {
        return this.initiator;
    }

    public final void setAssociationInitiator(boolean initiator) {
        this.initiator = initiator;
    }

    public boolean isInstalled() {
        return this.device != null && this.device.isInstalled() && (this.installed == null || this.installed != false);
    }

    public final Boolean getInstalled() {
        return this.installed;
    }

    public void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    public boolean isRoleSelectionNegotiationLenient() {
        return this.roleSelectionNegotiationLenient != null ? this.roleSelectionNegotiationLenient : this.device != null && this.device.isRoleSelectionNegotiationLenient();
    }

    public final Boolean getRoleSelectionNegotiationLenient() {
        return this.roleSelectionNegotiationLenient;
    }

    public void setRoleSelectionNegotiationLenient(Boolean roleSelectionNegotiationLenient) {
        this.roleSelectionNegotiationLenient = roleSelectionNegotiationLenient;
    }

    public String getShareTransferCapabilitiesFromAETitle() {
        return this.shareTransferCapabilitiesFromAETitle;
    }

    public void setShareTransferCapabilitiesFromAETitle(String shareTransferCapabilitiesFromAETitle) {
        this.shareTransferCapabilitiesFromAETitle = shareTransferCapabilitiesFromAETitle;
    }

    public ApplicationEntity transferCapabilitiesAE() {
        return this.shareTransferCapabilitiesFromAETitle != null ? this.device.getApplicationEntity(this.shareTransferCapabilitiesFromAETitle) : this;
    }

    public String getHl7ApplicationName() {
        return this.hl7ApplicationName;
    }

    public void setHl7ApplicationName(String hl7ApplicationName) {
        this.hl7ApplicationName = hl7ApplicationName;
    }

    public DimseRQHandler getDimseRQHandler() {
        DimseRQHandler handler = this.dimseRQHandler;
        if (handler != null) {
            return handler;
        }
        Device device = this.device;
        return device != null ? device.getDimseRQHandler() : null;
    }

    public final void setDimseRQHandler(DimseRQHandler dimseRQHandler) {
        this.dimseRQHandler = dimseRQHandler;
    }

    private void checkInstalled() {
        if (!this.isInstalled()) {
            throw new IllegalStateException("Not installed");
        }
    }

    private void checkDevice() {
        if (this.device == null) {
            throw new IllegalStateException("Not attached to Device");
        }
    }

    void onDimseRQ(Association as, PresentationContext pc, Dimse cmd, Attributes cmdAttrs, PDVInputStream data) throws IOException {
        DimseRQHandler tmp = this.getDimseRQHandler();
        if (tmp == null) {
            LOG.error("DimseRQHandler not initalized");
            throw new AAbort();
        }
        tmp.onDimseRQ(as, pc, cmd, cmdAttrs, data);
    }

    public void addConnection(Connection conn) {
        if (conn.getProtocol() != Connection.Protocol.DICOM) {
            throw new IllegalArgumentException("protocol != DICOM - " + (Object)((Object)conn.getProtocol()));
        }
        if (this.device != null && this.device != conn.getDevice()) {
            throw new IllegalStateException(conn + " not contained by Device: " + this.device.getDeviceName());
        }
        this.conns.add(conn);
    }

    public boolean removeConnection(Connection conn) {
        return this.conns.remove(conn);
    }

    public List<Connection> getConnections() {
        return this.conns;
    }

    public TransferCapability addTransferCapability(TransferCapability tc) {
        tc.setApplicationEntity(this);
        TransferCapability prev = (tc.getRole() == TransferCapability.Role.SCU ? this.scuTCs : this.scpTCs).put(tc.getSopClass(), tc);
        if (prev != null && prev != tc) {
            prev.setApplicationEntity(null);
        }
        return prev;
    }

    public TransferCapability removeTransferCapabilityFor(String sopClass, TransferCapability.Role role) {
        TransferCapability tc = (TransferCapability)(role == TransferCapability.Role.SCU ? this.scuTCs : this.scpTCs).remove(sopClass);
        if (tc != null) {
            tc.setApplicationEntity(null);
        }
        return tc;
    }

    public Collection<TransferCapability> getTransferCapabilities() {
        ArrayList<TransferCapability> tcs = new ArrayList<TransferCapability>(this.scuTCs.size() + this.scpTCs.size());
        tcs.addAll(this.scpTCs.values());
        tcs.addAll(this.scuTCs.values());
        return tcs;
    }

    public Collection<TransferCapability> getTransferCapabilitiesWithRole(TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? this.scuTCs : this.scpTCs).values();
    }

    public TransferCapability getTransferCapabilityFor(String sopClass, TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? this.scuTCs : this.scpTCs).get(sopClass);
    }

    public boolean hasTransferCapabilityFor(String sopClass, TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? this.scuTCs : this.scpTCs).containsKey(sopClass);
    }

    protected PresentationContext negotiate(AAssociateRQ rq, AAssociateAC ac, PresentationContext rqpc) {
        String as = rqpc.getAbstractSyntax();
        TransferCapability tc = this.roleSelection(rq, ac, as);
        int pcid = rqpc.getPCID();
        if (tc == null) {
            return new PresentationContext(pcid, 3, rqpc.getTransferSyntax());
        }
        String ts = tc.selectTransferSyntax(rqpc.getTransferSyntaxes());
        if (ts == null) {
            return new PresentationContext(pcid, 4, rqpc.getTransferSyntax());
        }
        byte[] info = this.negotiate(rq.getExtNegotiationFor(as), tc);
        if (info != null) {
            ac.addExtendedNegotiation(new ExtendedNegotiation(as, info));
        }
        return new PresentationContext(pcid, 0, ts);
    }

    private TransferCapability roleSelection(AAssociateRQ rq, AAssociateAC ac, String asuid) {
        RoleSelection rqrs = rq.getRoleSelectionFor(asuid);
        if (rqrs == null) {
            return this.getTC(this.scpTCs, asuid, rq);
        }
        RoleSelection acrs = ac.getRoleSelectionFor(asuid);
        if (acrs != null) {
            return this.getTC(acrs.isSCU() ? this.scpTCs : this.scuTCs, asuid, rq);
        }
        TransferCapability tcscu = null;
        TransferCapability tcscp = null;
        boolean scu = rqrs.isSCU() && (tcscp = this.getTC(this.scpTCs, asuid, rq)) != null;
        boolean scp = rqrs.isSCP() && (tcscu = this.getTC(this.scuTCs, asuid, rq)) != null;
        ac.addRoleSelection(new RoleSelection(asuid, scu, scp));
        return scu ? tcscp : tcscu;
    }

    private TransferCapability getTC(HashMap<String, TransferCapability> tcs, String asuid, AAssociateRQ rq) {
        TransferCapability tc = tcs.get(asuid);
        if (tc != null) {
            return tc;
        }
        CommonExtendedNegotiation commonExtNeg = rq.getCommonExtendedNegotiationFor(asuid);
        if (commonExtNeg != null) {
            for (String cuid : commonExtNeg.getRelatedGeneralSOPClassUIDs()) {
                tc = tcs.get(cuid);
                if (tc == null) continue;
                return tc;
            }
            tc = tcs.get(commonExtNeg.getServiceClassUID());
            if (tc != null) {
                return tc;
            }
        }
        return tcs.get("*");
    }

    private byte[] negotiate(ExtendedNegotiation exneg, TransferCapability tc) {
        if (exneg == null) {
            return null;
        }
        StorageOptions storageOptions = tc.getStorageOptions();
        if (storageOptions != null) {
            return storageOptions.toExtendedNegotiationInformation();
        }
        EnumSet<QueryOption> queryOptions = tc.getQueryOptions();
        if (queryOptions != null) {
            EnumSet<QueryOption> commonOpts = QueryOption.toOptions(exneg);
            commonOpts.retainAll(queryOptions);
            return QueryOption.toExtendedNegotiationInformation(commonOpts);
        }
        return null;
    }

    public Association connect(Connection local, Connection remote, AAssociateRQ rq) throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        this.checkDevice();
        this.checkInstalled();
        if (rq.getCallingAET() == null) {
            rq.setCallingAET(this.getCallingAETitle(rq.getCalledAET()));
        }
        if (!this.isNoAsyncModeCalledAETitle(rq.getCalledAET())) {
            rq.setMaxOpsInvoked(local.getMaxOpsInvoked());
            rq.setMaxOpsPerformed(local.getMaxOpsPerformed());
        }
        rq.setMaxPDULength(local.getReceivePDULength());
        Socket sock = local.connect(remote);
        AssociationMonitor monitor = this.device.getAssociationMonitor();
        Association as = null;
        try {
            as = new Association(this, local, sock);
            as.write(rq);
            as.waitForLeaving(State.Sta5);
            if (monitor != null) {
                monitor.onAssociationEstablished(as);
            }
            return as;
        }
        catch (IOException | InterruptedException e) {
            SafeClose.close(sock);
            if (as != null && monitor != null) {
                monitor.onAssociationFailed(as, e);
            }
            throw e;
        }
    }

    public Association connect(Connection remote, AAssociateRQ rq) throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        return this.connect(this.findCompatibleConnection(remote), remote, rq);
    }

    public Connection findCompatibleConnection(Connection remoteConn) throws IncompatibleConnectionException {
        for (Connection conn : this.conns) {
            if (!conn.isInstalled() || !conn.isCompatible(remoteConn)) continue;
            return conn;
        }
        throw new IncompatibleConnectionException("No compatible connection to " + remoteConn + " available on " + this.aet);
    }

    public CompatibleConnection findCompatibleConnection(ApplicationEntity remote) throws IncompatibleConnectionException {
        for (Connection remoteConn : remote.conns) {
            if (!remoteConn.isInstalled() || !remoteConn.isServer()) continue;
            for (Connection conn : this.conns) {
                if (!conn.isInstalled() || !conn.isCompatible(remoteConn)) continue;
                return new CompatibleConnection(conn, remoteConn);
            }
        }
        throw new IncompatibleConnectionException("No compatible connection to " + remote.getAETitle() + " available on " + this.aet);
    }

    public Association connect(ApplicationEntity remote, AAssociateRQ rq) throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        CompatibleConnection cc = this.findCompatibleConnection(remote);
        if (rq.getCalledAET() == null) {
            rq.setCalledAET(this.masqueradeCalledAETitle(remote.getAETitle()));
        }
        return this.connect(cc.getLocalConnection(), cc.getRemoteConnection(), rq);
    }

    public String toString() {
        return this.promptTo(new StringBuilder(512), "").toString();
    }

    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + "  ";
        StringUtils.appendLine(sb, indent, "ApplicationEntity[title: ", this.aet);
        StringUtils.appendLine(sb, indent2, "desc: ", this.description);
        StringUtils.appendLine(sb, indent2, "acceptor: ", this.acceptor);
        StringUtils.appendLine(sb, indent2, "initiator: ", this.initiator);
        StringUtils.appendLine(sb, indent2, "installed: ", this.getInstalled());
        for (Connection conn : this.conns) {
            conn.promptTo(sb, indent2).append(StringUtils.LINE_SEPARATOR);
        }
        for (TransferCapability tc : this.getTransferCapabilities()) {
            tc.promptTo(sb, indent2).append(StringUtils.LINE_SEPARATOR);
        }
        return sb.append(indent).append(']');
    }

    void reconfigure(ApplicationEntity src) {
        this.setApplicationEntityAttributes(src);
        this.device.reconfigureConnections(this.conns, src.conns);
        this.reconfigureTransferCapabilities(src);
        this.reconfigureAEExtensions(src);
    }

    private void reconfigureTransferCapabilities(ApplicationEntity src) {
        this.scuTCs.clear();
        this.scuTCs.putAll(src.scuTCs);
        this.scpTCs.clear();
        this.scpTCs.putAll(src.scpTCs);
    }

    private void reconfigureAEExtensions(ApplicationEntity from) {
        Iterator<Class<? extends AEExtension>> it = this.extensions.keySet().iterator();
        while (it.hasNext()) {
            if (from.extensions.containsKey(it.next())) continue;
            it.remove();
        }
        for (AEExtension src : from.extensions.values()) {
            Class<?> clazz = src.getClass();
            AEExtension ext = this.extensions.get(clazz);
            if (ext == null) {
                try {
                    ext = (AEExtension)clazz.newInstance();
                    this.addAEExtension(ext);
                }
                catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
                }
            }
            ext.reconfigure(src);
        }
    }

    protected void setApplicationEntityAttributes(ApplicationEntity from) {
        this.description = from.description;
        this.vendorData = from.vendorData;
        this.applicationClusters = from.applicationClusters;
        this.prefCalledAETs = from.prefCalledAETs;
        this.prefCallingAETs = from.prefCallingAETs;
        this.acceptedCallingAETs.clear();
        this.acceptedCallingAETs.addAll(from.acceptedCallingAETs);
        this.otherAETs.clear();
        this.otherAETs.addAll(from.otherAETs);
        this.noAsyncModeCalledAETs.clear();
        this.noAsyncModeCalledAETs.addAll(from.noAsyncModeCalledAETs);
        this.masqueradeCallingAETs.clear();
        this.masqueradeCallingAETs.putAll(from.masqueradeCallingAETs);
        this.supportedCharacterSets = from.supportedCharacterSets;
        this.prefTransferSyntaxes = from.prefTransferSyntaxes;
        this.shareTransferCapabilitiesFromAETitle = from.shareTransferCapabilitiesFromAETitle;
        this.hl7ApplicationName = from.hl7ApplicationName;
        this.acceptor = from.acceptor;
        this.initiator = from.initiator;
        this.installed = from.installed;
        this.roleSelectionNegotiationLenient = from.roleSelectionNegotiationLenient;
    }

    public void addAEExtension(AEExtension ext) {
        Class<?> clazz = ext.getClass();
        if (this.extensions.containsKey(clazz)) {
            throw new IllegalStateException("already contains AE Extension:" + clazz);
        }
        ext.setApplicationEntity(this);
        this.extensions.put(clazz, ext);
    }

    public boolean removeAEExtension(AEExtension ext) {
        if (this.extensions.remove(ext.getClass()) == null) {
            return false;
        }
        ext.setApplicationEntity(null);
        return true;
    }

    public Collection<AEExtension> listAEExtensions() {
        return this.extensions.values();
    }

    public <T extends AEExtension> T getAEExtension(Class<T> clazz) {
        return (T)this.extensions.get(clazz);
    }

    public <T extends AEExtension> T getAEExtensionNotNull(Class<T> clazz) {
        T aeExt = this.getAEExtension(clazz);
        if (aeExt == null) {
            throw new IllegalStateException("No " + clazz.getName() + " configured for AE: " + this.aet);
        }
        return aeExt;
    }
}

