/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.pdu;

import java.io.IOException;
import org.dcm4che3.util.StringUtils;

public class AAbort
extends IOException {
    private static final long serialVersionUID = -1825815767517748111L;
    public static final int UL_SERIVE_USER = 0;
    public static final int UL_SERIVE_PROVIDER = 2;
    public static final int REASON_NOT_SPECIFIED = 0;
    public static final int UNRECOGNIZED_PDU = 1;
    public static final int UNEXPECTED_PDU = 2;
    public static final int UNRECOGNIZED_PDU_PARAMETER = 4;
    public static final int UNEXPECTED_PDU_PARAMETER = 5;
    public static final int INVALID_PDU_PARAMETER_VALUE = 6;
    private static final String[] SOURCES = new String[]{"0 - service-user", "1", "2 - service-provider"};
    private static final String[] SERVICE_USER_REASONS = new String[]{"0"};
    private static final String[] SERVICE_PROVIDER_REASONS = new String[]{"0 - reason-not-specified", "1 - unrecognized-PDU", "2 - unexpected-PDU", "3", "4 - unrecognized-PDU-parameter", "5 - unexpected-PDU-parameter", "6 - invalid-PDU-parameter-value"};
    private static final String[][] REASONS = new String[][]{SERVICE_USER_REASONS, StringUtils.EMPTY_STRING, SERVICE_PROVIDER_REASONS};
    private final int source;
    private final int reason;

    public AAbort(int source, int reason) {
        super("A-ABORT[source: " + AAbort.toString(SOURCES, source) + ", reason: " + AAbort.toReason(source, reason) + ']');
        this.source = source;
        this.reason = reason;
    }

    public AAbort() {
        this(0, 0);
    }

    private static String toString(String[] ss, int i) {
        try {
            return ss[i];
        }
        catch (IndexOutOfBoundsException e) {
            return Integer.toString(i);
        }
    }

    private static String toReason(int source, int reason) {
        try {
            return AAbort.toString(REASONS[source], reason);
        }
        catch (IndexOutOfBoundsException e) {
            return Integer.toString(reason);
        }
    }

    public final int getReason() {
        return this.reason;
    }

    public final int getSource() {
        return this.source;
    }

    @Override
    public String toString() {
        return this.getMessage();
    }
}

