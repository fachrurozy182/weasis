/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.io.IOException;
import org.dcm4che3.util.UIDUtils;

public class NoPresentationContextException
extends IOException {
    private static final long serialVersionUID = 2724320651716449165L;

    public NoPresentationContextException(String cuid) {
        super(NoPresentationContextException.toMessage(cuid));
    }

    public NoPresentationContextException(String cuid, String tsuid) {
        super(NoPresentationContextException.toMessage(cuid, tsuid));
    }

    private static String toMessage(String cuid) {
        StringBuilder sb = new StringBuilder();
        sb.append("No Presentation Context for Abstract Syntax: ");
        UIDUtils.promptTo(cuid, sb);
        sb.append(" negotiated");
        return sb.toString();
    }

    private static String toMessage(String cuid, String tsuid) {
        StringBuilder sb = new StringBuilder();
        sb.append("No Presentation Context for Abstract Syntax: ");
        UIDUtils.promptTo(cuid, sb);
        sb.append(" with Transfer Syntax: ");
        UIDUtils.promptTo(tsuid, sb);
        sb.append(" negotiated");
        return sb.toString();
    }
}

