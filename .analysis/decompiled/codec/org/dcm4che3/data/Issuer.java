/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.data;

import java.io.Serializable;
import java.util.Objects;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.HL7Separator;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.StringUtils;

public class Issuer
implements Serializable {
    private static final long serialVersionUID = 5350502680059507981L;
    private String localNamespaceEntityID;
    private String universalEntityID;
    private String universalEntityIDType;

    public Issuer(String localNamespaceEntityID, String universalEntityID, String universalEntityIDType) {
        this.localNamespaceEntityID = localNamespaceEntityID;
        this.universalEntityID = universalEntityID;
        this.universalEntityIDType = universalEntityIDType;
        this.validate();
    }

    public Issuer(String s) {
        this(s, '&');
    }

    public Issuer(String s, char delim) {
        String[] ss = StringUtils.split(s, delim);
        if (ss.length > 3) {
            throw new IllegalArgumentException(s);
        }
        this.localNamespaceEntityID = Issuer.unescapeHL7Separators(ss[0]);
        this.universalEntityID = ss.length > 1 ? Issuer.unescapeHL7Separators(ss[1]) : null;
        this.universalEntityIDType = ss.length > 2 ? Issuer.unescapeHL7Separators(ss[2]) : null;
        this.validate();
    }

    public Issuer(String issuerOfPatientID, Attributes qualifiers) {
        this(issuerOfPatientID, qualifiers != null ? qualifiers.getString(4194354) : null, qualifiers != null ? qualifiers.getString(0x400033) : null);
    }

    public Issuer(Attributes issuerItem) {
        this(issuerItem.getString(4194353), issuerItem.getString(4194354), issuerItem.getString(0x400033));
    }

    public Issuer(Issuer other) {
        this(other.getLocalNamespaceEntityID(), other.getUniversalEntityID(), other.getUniversalEntityIDType());
    }

    protected Issuer() {
    }

    public static Issuer fromIssuerOfPatientID(Attributes attrs) {
        String issuerOfPatientID = attrs.getString(0x100021);
        Attributes qualifiers = attrs.getNestedDataset(1048612);
        if (qualifiers != null) {
            String universalEntityID = qualifiers.getString(4194354);
            String universalEntityIDType = qualifiers.getString(0x400033);
            if (universalEntityID != null && universalEntityIDType != null) {
                return new Issuer(issuerOfPatientID, universalEntityID, universalEntityIDType);
            }
        }
        return issuerOfPatientID != null ? new Issuer(issuerOfPatientID, null, null) : null;
    }

    public static Issuer valueOf(Attributes issuerItem) {
        if (issuerItem == null) {
            return null;
        }
        String localNamespaceEntityID = issuerItem.getString(4194353);
        String universalEntityID = issuerItem.getString(4194354);
        String universalEntityIDType = issuerItem.getString(0x400033);
        return universalEntityID != null && universalEntityIDType != null ? new Issuer(localNamespaceEntityID, universalEntityID, universalEntityIDType) : (localNamespaceEntityID != null ? new Issuer(localNamespaceEntityID, null, null) : null);
    }

    private void validate() {
        if (this.localNamespaceEntityID == null && this.universalEntityID == null) {
            throw new IllegalArgumentException("Missing Local Namespace Entity ID or Universal Entity ID");
        }
        if (this.universalEntityID != null && this.universalEntityIDType == null) {
            throw new IllegalArgumentException("Missing Universal Entity ID Type");
        }
    }

    private static String unescapeHL7Separators(String s) {
        return s.isEmpty() ? null : HL7Separator.unescapeAll(s);
    }

    public final String getLocalNamespaceEntityID() {
        return this.localNamespaceEntityID;
    }

    public final String getUniversalEntityID() {
        return this.universalEntityID;
    }

    public final String getUniversalEntityIDType() {
        return this.universalEntityIDType;
    }

    public boolean merge(Issuer other) {
        boolean mergeUniversal;
        if (!this.matches(other, true, true)) {
            throw new IllegalArgumentException("other=" + other);
        }
        boolean mergeLocalNamespace = this.localNamespaceEntityID == null && other.localNamespaceEntityID != null;
        if (mergeLocalNamespace) {
            this.localNamespaceEntityID = other.localNamespaceEntityID;
        }
        if (mergeUniversal = this.universalEntityID == null && other.universalEntityID != null) {
            this.universalEntityID = other.universalEntityID;
            this.universalEntityIDType = other.universalEntityIDType;
        }
        return mergeLocalNamespace || mergeUniversal;
    }

    public int hashCode() {
        return 37 * (37 * this.hashCode(this.localNamespaceEntityID) + this.hashCode(this.universalEntityID)) + this.hashCode(this.universalEntityIDType);
    }

    private int hashCode(String s) {
        return s == null ? 0 : s.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Issuer)) {
            return false;
        }
        Issuer other = (Issuer)o;
        return this.equals(this.localNamespaceEntityID, other.getLocalNamespaceEntityID()) && this.equals(this.universalEntityID, other.getUniversalEntityID()) && this.equals(this.universalEntityIDType, other.getUniversalEntityIDType());
    }

    private boolean equals(String s1, String s2) {
        return Objects.equals(s1, s2);
    }

    public boolean matches(Issuer other) {
        return this.matches(other, true, false);
    }

    public boolean matches(Issuer other, boolean matchNoIssuer, boolean matchOnNoMismatch) {
        boolean matchUniversal;
        if (this == other) {
            return true;
        }
        if (other == null) {
            return matchNoIssuer;
        }
        boolean matchLocal = this.localNamespaceEntityID != null && other.getLocalNamespaceEntityID() != null;
        boolean bl = matchUniversal = this.universalEntityID != null && other.getUniversalEntityID() != null;
        return !matchLocal && !matchUniversal ? matchOnNoMismatch : (!matchLocal || this.localNamespaceEntityID.equals(other.getLocalNamespaceEntityID())) && (!matchUniversal || this.universalEntityID.equals(other.getUniversalEntityID()) && this.universalEntityIDType.equals(other.getUniversalEntityIDType()));
    }

    public String toString() {
        return this.toString('&');
    }

    public String toString(char delim) {
        if (this.universalEntityID == null) {
            return HL7Separator.escapeAll(this.localNamespaceEntityID);
        }
        StringBuilder sb = new StringBuilder();
        if (this.localNamespaceEntityID != null) {
            sb.append(HL7Separator.escapeAll(this.localNamespaceEntityID));
        }
        sb.append(delim);
        sb.append(HL7Separator.escapeAll(this.universalEntityID));
        sb.append(delim);
        sb.append(HL7Separator.escapeAll(this.universalEntityIDType));
        return sb.toString();
    }

    public Attributes toItem() {
        int size = 0;
        if (this.localNamespaceEntityID != null) {
            ++size;
        }
        if (this.universalEntityID != null) {
            ++size;
        }
        if (this.universalEntityIDType != null) {
            ++size;
        }
        Attributes item = new Attributes(size);
        if (this.localNamespaceEntityID != null) {
            item.setString(4194353, VR.UT, this.localNamespaceEntityID);
        }
        if (this.universalEntityID != null) {
            item.setString(4194354, VR.UT, this.universalEntityID);
        }
        if (this.universalEntityIDType != null) {
            item.setString(0x400033, VR.CS, this.universalEntityIDType);
        }
        return item;
    }

    public Attributes toIssuerOfPatientID(Attributes attrs) {
        if (attrs == null) {
            attrs = new Attributes(2);
        }
        if (this.localNamespaceEntityID != null) {
            attrs.setString(0x100021, VR.LO, this.localNamespaceEntityID);
        }
        if (this.universalEntityID != null) {
            Attributes item = new Attributes(2);
            item.setString(4194354, VR.UT, this.universalEntityID);
            item.setString(0x400033, VR.CS, this.universalEntityIDType);
            attrs.newSequence(1048612, 1).add(item);
        }
        return attrs;
    }

    public boolean isLesserQualifiedThan(Issuer other) {
        return other.universalEntityID != null && (this.universalEntityID == null || other.localNamespaceEntityID != null && this.localNamespaceEntityID == null);
    }
}

