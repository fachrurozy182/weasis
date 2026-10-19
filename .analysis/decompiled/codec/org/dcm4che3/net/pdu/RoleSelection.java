/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.pdu;

import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.UIDUtils;

public class RoleSelection {
    private final String cuid;
    private final boolean scu;
    private final boolean scp;

    public RoleSelection(String cuid, boolean scu, boolean scp) {
        if (cuid == null) {
            throw new NullPointerException();
        }
        this.cuid = cuid;
        this.scu = scu;
        this.scp = scp;
    }

    public final String getSOPClassUID() {
        return this.cuid;
    }

    public final boolean isSCU() {
        return this.scu;
    }

    public final boolean isSCP() {
        return this.scp;
    }

    public int length() {
        return this.cuid.length() + 4;
    }

    public String toString() {
        return this.promptTo(new StringBuilder()).toString();
    }

    StringBuilder promptTo(StringBuilder sb) {
        sb.append("  RoleSelection[").append(StringUtils.LINE_SEPARATOR).append("    sopClass: ");
        return UIDUtils.promptTo(this.cuid, sb).append(StringUtils.LINE_SEPARATOR).append("    scu: ").append(this.scu).append(StringUtils.LINE_SEPARATOR).append("    scp: ").append(this.scp).append(StringUtils.LINE_SEPARATOR).append("  ]");
    }
}

