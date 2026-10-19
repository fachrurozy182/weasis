/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.pdu;

import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.UIDUtils;

public class ExtendedNegotiation {
    private final String cuid;
    private final byte[] info;

    public ExtendedNegotiation(String cuid, byte[] info) {
        if (cuid == null) {
            throw new NullPointerException();
        }
        this.cuid = cuid;
        this.info = (byte[])info.clone();
    }

    public final String getSOPClassUID() {
        return this.cuid;
    }

    public final byte[] getInformation() {
        return (byte[])this.info.clone();
    }

    public final byte getField(int index, byte def) {
        return index < this.info.length ? this.info[index] : def;
    }

    public int length() {
        return this.cuid.length() + this.info.length + 2;
    }

    public String toString() {
        return this.promptTo(new StringBuilder()).toString();
    }

    StringBuilder promptTo(StringBuilder sb) {
        sb.append("  ExtendedNegotiation[").append(StringUtils.LINE_SEPARATOR).append("    sopClass: ");
        UIDUtils.promptTo(this.cuid, sb).append(StringUtils.LINE_SEPARATOR).append("    info: [");
        for (byte b : this.info) {
            sb.append(b).append(", ");
        }
        return sb.append(']').append(StringUtils.LINE_SEPARATOR).append("  ]");
    }
}

