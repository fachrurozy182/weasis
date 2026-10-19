/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.pdu;

import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.UIDUtils;

public class CommonExtendedNegotiation {
    private final String sopCUID;
    private final String serviceCUID;
    private final String[] relSopCUIDs;

    public CommonExtendedNegotiation(String sopCUID, String serviceCUID, String ... relSopCUIDs) {
        if (sopCUID == null) {
            throw new NullPointerException("sopCUID");
        }
        if (serviceCUID == null) {
            throw new NullPointerException("serviceCUID");
        }
        this.sopCUID = sopCUID;
        this.serviceCUID = serviceCUID;
        this.relSopCUIDs = relSopCUIDs;
    }

    public final String getSOPClassUID() {
        return this.sopCUID;
    }

    public final String getServiceClassUID() {
        return this.serviceCUID;
    }

    public String[] getRelatedGeneralSOPClassUIDs() {
        return this.relSopCUIDs;
    }

    public int length() {
        return 6 + this.sopCUID.length() + this.serviceCUID.length() + this.getRelatedGeneralSOPClassUIDsLength();
    }

    public String toString() {
        return this.promptTo(new StringBuilder()).toString();
    }

    StringBuilder promptTo(StringBuilder sb) {
        sb.append("  CommonExtendedNegotiation[").append(StringUtils.LINE_SEPARATOR).append("    sopClass: ");
        UIDUtils.promptTo(this.sopCUID, sb).append(StringUtils.LINE_SEPARATOR).append("    serviceClass: ");
        UIDUtils.promptTo(this.serviceCUID, sb).append(StringUtils.LINE_SEPARATOR);
        if (this.relSopCUIDs.length != 0) {
            sb.append("    relatedSOPClasses:").append(StringUtils.LINE_SEPARATOR);
            for (String uid : this.relSopCUIDs) {
                UIDUtils.promptTo(uid, sb.append("      ")).append(StringUtils.LINE_SEPARATOR);
            }
        }
        return sb.append("  ]");
    }

    public int getRelatedGeneralSOPClassUIDsLength() {
        int len = 0;
        for (String cuid : this.relSopCUIDs) {
            len += 2 + cuid.length();
        }
        return len;
    }
}

