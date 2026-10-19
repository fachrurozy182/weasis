/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net.pdu;

import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.UIDUtils;

public class PresentationContext {
    public static final int ACCEPTANCE = 0;
    public static final int USER_REJECTION = 1;
    public static final int PROVIDER_REJECTION = 2;
    public static final int ABSTRACT_SYNTAX_NOT_SUPPORTED = 3;
    public static final int TRANSFER_SYNTAX_NOT_SUPPORTED = 4;
    private static final String[] RESULTS = new String[]{"0 - acceptance", "1 - user-rejection", "2 - no-reason (provider rejection)", "3 - abstract-syntax-not-supported (provider rejection)", "4 - transfer-syntaxes-not-supported (provider rejection)"};
    private final int pcid;
    private final int result;
    private final String as;
    private final String[] tss;

    public PresentationContext(int pcid, int result, String as, String ... tss) {
        this.pcid = pcid;
        this.result = result;
        this.as = as;
        this.tss = tss;
    }

    public PresentationContext(int pcid, String as, String ... tss) {
        this(pcid, 0, as, tss);
    }

    public PresentationContext(int pcid, int result, String ts) {
        this(pcid, result, null, ts);
    }

    private static String resultAsString(int result) {
        try {
            return RESULTS[result];
        }
        catch (IndexOutOfBoundsException e) {
            return Integer.toString(result);
        }
    }

    public final int getPCID() {
        return this.pcid;
    }

    public final int getResult() {
        return this.result;
    }

    public boolean isAccepted() {
        return this.result == 0;
    }

    public final String getAbstractSyntax() {
        return this.as;
    }

    public final String[] getTransferSyntaxes() {
        return this.tss;
    }

    public boolean containsTransferSyntax(String ts) {
        for (String ts0 : this.tss) {
            if (!ts.equals(ts0)) continue;
            return true;
        }
        return false;
    }

    public String getTransferSyntax() {
        return this.tss[0];
    }

    public int length() {
        int len = 4;
        if (this.as != null) {
            len += 4 + this.as.length();
        }
        for (String ts : this.tss) {
            len += 4 + ts.length();
        }
        return len;
    }

    public String toString() {
        return this.promptTo(new StringBuilder()).toString();
    }

    StringBuilder promptTo(StringBuilder sb) {
        sb.append("  PresentationContext[id: ").append(this.pcid).append(StringUtils.LINE_SEPARATOR);
        if (this.as != null) {
            UIDUtils.promptTo(this.as, sb.append("    as: "));
        } else {
            sb.append("    result: ").append(PresentationContext.resultAsString(this.result));
        }
        sb.append(StringUtils.LINE_SEPARATOR);
        for (String ts : this.tss) {
            UIDUtils.promptTo(ts, sb.append("    ts: ")).append(StringUtils.LINE_SEPARATOR);
        }
        return sb.append("  ]");
    }
}

