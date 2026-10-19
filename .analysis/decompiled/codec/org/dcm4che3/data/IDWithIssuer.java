/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.HL7Separator;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.StringUtils;

public class IDWithIssuer {
    public static final IDWithIssuer[] EMPTY = new IDWithIssuer[0];
    private final String id;
    private String typeOfPatientID;
    private String identifierTypeCode;
    private Issuer issuer;

    public IDWithIssuer(String id, Issuer issuer) {
        if (id.isEmpty()) {
            throw new IllegalArgumentException("empty id");
        }
        this.id = id;
        this.setIssuer(issuer);
    }

    public IDWithIssuer(String id, String issuer) {
        this.id = id;
        this.setIssuer(issuer != null ? new Issuer(issuer, '&') : null);
    }

    public IDWithIssuer(String cx) {
        String[] ss = StringUtils.split(cx, '^');
        this.id = HL7Separator.unescapeAll(ss[0]);
        if (ss.length > 3) {
            if (!ss[3].isEmpty()) {
                this.setIssuer(new Issuer(ss[3], '&'));
            }
            if (ss.length > 4 && !ss[4].isEmpty()) {
                this.setIdentifierTypeCode(HL7Separator.unescapeAll(ss[4]));
            }
        }
    }

    public IDWithIssuer withoutIssuer() {
        return this.issuer == null ? this : new IDWithIssuer(this.id, (Issuer)null);
    }

    public final String getID() {
        return this.id;
    }

    public String getTypeOfPatientID() {
        return this.typeOfPatientID;
    }

    public void setTypeOfPatientID(String typeOfPatientID) {
        this.typeOfPatientID = typeOfPatientID;
    }

    public final String getIdentifierTypeCode() {
        return this.identifierTypeCode;
    }

    public final void setIdentifierTypeCode(String identifierTypeCode) {
        this.identifierTypeCode = identifierTypeCode;
    }

    public final Issuer getIssuer() {
        return this.issuer;
    }

    public final void setIssuer(Issuer issuer) {
        this.issuer = issuer;
    }

    public String toString() {
        if (this.issuer == null && this.identifierTypeCode == null) {
            return HL7Separator.escapeAll(this.id);
        }
        StringBuilder sb = new StringBuilder(HL7Separator.escapeAll(this.id));
        sb.append("^^^");
        if (this.issuer != null) {
            sb.append(this.issuer.toString('&'));
        }
        if (this.identifierTypeCode != null) {
            sb.append('^').append(HL7Separator.escapeAll(this.identifierTypeCode));
        }
        return sb.toString();
    }

    public int hashCode() {
        int result = this.id.hashCode();
        if (this.typeOfPatientID != null) {
            result += this.typeOfPatientID.hashCode() * 31;
        }
        if (this.identifierTypeCode != null) {
            result += this.identifierTypeCode.hashCode() * 31;
        }
        if (this.issuer != null) {
            result += this.issuer.hashCode() * 31;
        }
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IDWithIssuer)) {
            return false;
        }
        IDWithIssuer other = (IDWithIssuer)obj;
        return Objects.equals(this.id, other.getID()) && Objects.equals(this.typeOfPatientID, other.getTypeOfPatientID()) && Objects.equals(this.identifierTypeCode, other.getIdentifierTypeCode()) && Objects.equals(this.issuer, other.issuer);
    }

    public boolean matches(IDWithIssuer other) {
        return this.matches(other, false, false);
    }

    public boolean matches(IDWithIssuer other, boolean matchNoIssuer, boolean matchOnNoMismatch) {
        return this.id.equals(other.id) && (this.issuer == null ? other.issuer == null || matchNoIssuer : this.issuer.matches(other.issuer, matchNoIssuer, matchOnNoMismatch));
    }

    public Attributes exportPatientIDWithIssuer(Attributes attrs) {
        if (attrs == null) {
            attrs = new Attributes(3);
        }
        attrs.setString(0x100020, VR.LO, this.id);
        if (this.typeOfPatientID != null) {
            attrs.setString(0x100022, VR.CS, this.typeOfPatientID);
        }
        if (this.issuer == null && this.identifierTypeCode == null) {
            return attrs;
        }
        if (this.issuer != null) {
            this.issuer.toIssuerOfPatientID(attrs);
        }
        if (this.identifierTypeCode != null) {
            Attributes item = attrs.getNestedDataset(1048612);
            if (item == null) {
                item = new Attributes(1);
                attrs.newSequence(1048612, 1).add(item);
            }
            item.setString(4194357, VR.CS, this.identifierTypeCode);
        }
        return attrs;
    }

    public static IDWithIssuer valueOf(Attributes attrs, int idTag, int issuerSeqTag) {
        String id = attrs.getString(idTag);
        if (id == null) {
            return null;
        }
        return new IDWithIssuer(id, Issuer.valueOf(attrs.getNestedDataset(issuerSeqTag)));
    }

    public static IDWithIssuer pidOf(Attributes attrs) {
        String id = attrs.getString(0x100020);
        if (id == null) {
            return null;
        }
        IDWithIssuer result = new IDWithIssuer(id, Issuer.fromIssuerOfPatientID(attrs));
        result.setTypeOfPatientID(attrs.getString(0x100022));
        result.setIdentifierTypeCode(IDWithIssuer.identifierTypeCodeOf(attrs));
        return result;
    }

    private static String identifierTypeCodeOf(Attributes attrs) {
        Attributes qualifiers = attrs.getNestedDataset(1048612);
        return qualifiers != null ? qualifiers.getString(4194357) : null;
    }

    public static Set<IDWithIssuer> pidsOf(Attributes attrs) {
        IDWithIssuer pid = IDWithIssuer.pidOf(attrs);
        Sequence opidseq = attrs.getSequence(0x101002);
        if (opidseq == null || opidseq.isEmpty()) {
            if (pid == null) {
                return Collections.emptySet();
            }
            return Collections.singleton(pid);
        }
        LinkedHashSet<IDWithIssuer> pids = new LinkedHashSet<IDWithIssuer>(1 + opidseq.size() << 1);
        if (pid != null) {
            pids.add(pid);
        }
        for (Attributes item : opidseq) {
            IDWithIssuer.addTo(IDWithIssuer.pidOf(item), pids);
        }
        return pids;
    }

    public static Set<IDWithIssuer> opidsOf(Attributes attrs) {
        Sequence opidseq = attrs.getSequence(0x101002);
        if (opidseq == null || opidseq.isEmpty()) {
            return Collections.emptySet();
        }
        LinkedHashSet<IDWithIssuer> pids = new LinkedHashSet<IDWithIssuer>(opidseq.size() << 1);
        for (Attributes item : opidseq) {
            IDWithIssuer.addTo(IDWithIssuer.pidOf(item), pids);
        }
        return pids;
    }

    private static void addTo(IDWithIssuer pid, Set<IDWithIssuer> pids) {
        if (pid == null) {
            return;
        }
        Iterator<IDWithIssuer> itr = pids.iterator();
        while (itr.hasNext()) {
            IDWithIssuer next = itr.next();
            if (!next.matches(pid, true, false)) continue;
            if (pid.issuer != null && (next.issuer == null || next.issuer.isLesserQualifiedThan(pid.issuer))) {
                itr.remove();
                continue;
            }
            return;
        }
        pids.add(pid);
    }
}

