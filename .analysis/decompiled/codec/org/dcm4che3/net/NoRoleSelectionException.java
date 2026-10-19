/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.util.UIDUtils;

public class NoRoleSelectionException
extends IOException {
    private static final long serialVersionUID = -8725680403033100521L;

    public NoRoleSelectionException(String cuid, TransferCapability.Role role) {
        super(NoRoleSelectionException.toMessage(cuid, role));
    }

    private static String toMessage(String cuid, TransferCapability.Role role) {
        StringBuilder sb = new StringBuilder();
        sb.append("No Role Selection for SOP Class ");
        UIDUtils.promptTo(cuid, sb);
        sb.append(" as ").append((Object)role).append(" negotiated");
        return sb.toString();
    }
}

