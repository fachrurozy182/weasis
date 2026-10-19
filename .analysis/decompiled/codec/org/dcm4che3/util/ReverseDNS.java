/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.util;

import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReverseDNS {
    static final Logger LOG = LoggerFactory.getLogger(ReverseDNS.class);
    static final boolean DISABLED = ReverseDNS.isFalse(ReverseDNS.class.getName());

    private ReverseDNS() {
    }

    private static boolean isFalse(String name) {
        try {
            String s = System.getProperty(name);
            return s != null && s.equalsIgnoreCase("false");
        }
        catch (IllegalArgumentException | NullPointerException runtimeException) {
            return false;
        }
    }

    public static String hostNameOf(InetAddress inetAddress) {
        if (DISABLED) {
            return inetAddress.getHostAddress();
        }
        if (!LOG.isDebugEnabled()) {
            return inetAddress.getHostName();
        }
        String hostAddress = inetAddress.getHostAddress();
        LOG.debug("rDNS {} -> ...", (Object)hostAddress);
        long start = System.nanoTime();
        String hostName = inetAddress.getHostName();
        long end = System.nanoTime();
        LOG.debug("rDNS {} -> {} in {} ms", new Object[]{hostAddress, hostName, (end - start) / 1000L});
        return hostName;
    }
}

