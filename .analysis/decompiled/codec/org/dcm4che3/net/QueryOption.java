/*
 * Decompiled with CFR 0.152.
 */
package org.dcm4che3.net;

import java.util.EnumSet;
import org.dcm4che3.net.pdu.ExtendedNegotiation;

public enum QueryOption {
    RELATIONAL,
    DATETIME,
    FUZZY,
    TIMEZONE;


    public static byte[] toExtendedNegotiationInformation(EnumSet<QueryOption> opts) {
        byte[] info = new byte[opts.contains((Object)TIMEZONE) ? 4 : (opts.contains((Object)FUZZY) || opts.contains((Object)DATETIME) ? 3 : 1)];
        for (QueryOption query : opts) {
            info[query.ordinal()] = 1;
        }
        return info;
    }

    public static EnumSet<QueryOption> toOptions(ExtendedNegotiation extNeg) {
        EnumSet<QueryOption> opts = EnumSet.noneOf(QueryOption.class);
        if (extNeg != null) {
            QueryOption.toOption(extNeg, RELATIONAL, opts);
            QueryOption.toOption(extNeg, DATETIME, opts);
            QueryOption.toOption(extNeg, FUZZY, opts);
            QueryOption.toOption(extNeg, TIMEZONE, opts);
        }
        return opts;
    }

    private static void toOption(ExtendedNegotiation extNeg, QueryOption opt, EnumSet<QueryOption> opts) {
        if (extNeg.getField(opt.ordinal(), (byte)0) == 1) {
            opts.add(opt);
        }
    }
}

