/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.StorageOptions;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.UIDUtils;

public class TransferCapability
implements Serializable {
    private static final long serialVersionUID = 6386251434418693778L;
    private ApplicationEntity ae;
    private String commonName;
    private String sopClass;
    private Role role;
    private String[] transferSyntaxes;
    private String[] prefTransferSyntaxes = new String[0];
    private EnumSet<QueryOption> queryOptions;
    private StorageOptions storageOptions;

    public TransferCapability() {
        this(null, "1.2.840.10008.1.1", Role.SCU, "1.2.840.10008.1.2");
    }

    public TransferCapability(String commonName, String sopClass, Role role, String ... transferSyntaxes) {
        this.setCommonName(commonName);
        this.setSopClass(sopClass);
        this.setRole(role);
        this.setTransferSyntaxes(transferSyntaxes);
    }

    public void setApplicationEntity(ApplicationEntity ae) {
        if (ae != null && this.ae != null) {
            throw new IllegalStateException("already owned by AE " + this.ae.getAETitle());
        }
        this.ae = ae;
    }

    public String getCommonName() {
        return this.commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
        if (role == null) {
            throw new NullPointerException();
        }
        if (this.role == role) {
            return;
        }
        ApplicationEntity ae = this.ae;
        if (ae != null) {
            ae.removeTransferCapabilityFor(this.sopClass, this.role);
        }
        this.role = role;
        if (ae != null) {
            ae.addTransferCapability(this);
        }
    }

    public String getSopClass() {
        return this.sopClass;
    }

    public void setSopClass(String sopClass) {
        if (sopClass.isEmpty()) {
            throw new IllegalArgumentException("empty sopClass");
        }
        if (sopClass.equals(this.sopClass)) {
            return;
        }
        ApplicationEntity ae = this.ae;
        if (ae != null) {
            ae.removeTransferCapabilityFor(sopClass, this.role);
        }
        this.sopClass = sopClass;
        if (ae != null) {
            ae.addTransferCapability(this);
        }
    }

    public String[] getTransferSyntaxes() {
        return this.transferSyntaxes;
    }

    public void setTransferSyntaxes(String ... transferSyntaxes) {
        this.transferSyntaxes = StringUtils.requireContainsNoEmpty(StringUtils.requireNotEmpty(transferSyntaxes, "missing transferSyntax"), "empty transferSyntax");
    }

    public String[] getPreferredTransferSyntaxes() {
        return this.prefTransferSyntaxes;
    }

    public void setPreferredTransferSyntaxes(String ... transferSyntaxes) {
        this.prefTransferSyntaxes = StringUtils.requireContainsNoEmpty(transferSyntaxes, "empty transferSyntax");
    }

    public String[] preferredTransferSyntaxes() {
        return this.prefTransferSyntaxes.length > 0 ? this.prefTransferSyntaxes : this.ae.getPreferredTransferSyntaxes();
    }

    public boolean containsTransferSyntax(String ts) {
        return "*".equals(this.transferSyntaxes[0]) || StringUtils.contains(this.transferSyntaxes, ts);
    }

    public String selectTransferSyntax(String ... transferSyntaxes) {
        if (transferSyntaxes.length == 1) {
            return this.containsTransferSyntax(transferSyntaxes[0]) ? transferSyntaxes[0] : null;
        }
        List<String> acceptable = this.retainAcceptable(transferSyntaxes);
        if (acceptable.isEmpty()) {
            return null;
        }
        for (String prefTransferSyntax : this.preferredTransferSyntaxes()) {
            if (!acceptable.contains(prefTransferSyntax)) continue;
            return prefTransferSyntax;
        }
        return acceptable.get(0);
    }

    private List<String> retainAcceptable(String[] transferSyntaxes) {
        ArrayList<String> acceptable = new ArrayList<String>(transferSyntaxes.length);
        for (String transferSyntax : transferSyntaxes) {
            if (!this.containsTransferSyntax(transferSyntax)) continue;
            acceptable.add(transferSyntax);
        }
        return acceptable;
    }

    public void setQueryOptions(EnumSet<QueryOption> queryOptions) {
        this.queryOptions = queryOptions;
    }

    public EnumSet<QueryOption> getQueryOptions() {
        return this.queryOptions;
    }

    public void setStorageOptions(StorageOptions storageOptions) {
        this.storageOptions = storageOptions;
    }

    public StorageOptions getStorageOptions() {
        return this.storageOptions;
    }

    public String toString() {
        return this.promptTo(new StringBuilder(512), "").toString();
    }

    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + "  ";
        StringUtils.appendLine(sb, indent, "TransferCapability[cn: ", this.commonName);
        StringUtils.appendLine(sb, new Object[]{indent2, "role: ", this.role});
        sb.append(indent2).append("as: ");
        UIDUtils.promptTo(this.sopClass, sb).append(StringUtils.LINE_SEPARATOR);
        for (String ts : this.transferSyntaxes) {
            sb.append(indent2).append("ts: ");
            UIDUtils.promptTo(ts, sb).append(StringUtils.LINE_SEPARATOR);
        }
        if (this.queryOptions != null) {
            sb.append(indent2).append("QueryOptions").append(this.queryOptions).append(StringUtils.LINE_SEPARATOR);
        }
        if (this.storageOptions != null) {
            sb.append(indent2).append(this.storageOptions).append(StringUtils.LINE_SEPARATOR);
        }
        return sb.append(indent).append(']');
    }

    public static enum Role {
        SCU,
        SCP;

    }
}

