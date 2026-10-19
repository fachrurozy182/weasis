/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.pdu;

import org.dcm4che3.util.StringUtils;

public class UserIdentityAC {
    private final byte[] serverResponse;

    public UserIdentityAC(byte[] serverResponse) {
        this.serverResponse = (byte[])serverResponse.clone();
    }

    public final byte[] getServerResponse() {
        return (byte[])this.serverResponse.clone();
    }

    public int length() {
        return 2 + this.serverResponse.length;
    }

    public String toString() {
        return this.promptTo(new StringBuilder()).toString();
    }

    StringBuilder promptTo(StringBuilder sb) {
        return sb.append("  UserIdentity[").append(StringUtils.LINE_SEPARATOR).append("    serverResponse: byte[").append(this.serverResponse.length).append(']').append(StringUtils.LINE_SEPARATOR).append("  ]");
    }
}

