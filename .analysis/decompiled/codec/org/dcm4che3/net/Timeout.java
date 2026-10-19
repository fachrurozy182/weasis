/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.net;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.dcm4che3.net.Association;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Timeout
implements Runnable {
    public static Logger LOG = LoggerFactory.getLogger(Timeout.class);
    private final Association as;
    private final String expiredMsg;
    private final String cancelMsg;
    private final ScheduledFuture<?> future;

    private Timeout(Association as, String expiredMsg, String cancelMsg, int timeout) {
        this.as = as;
        this.expiredMsg = expiredMsg;
        this.cancelMsg = cancelMsg;
        this.future = as.getDevice().schedule(this, timeout, TimeUnit.MILLISECONDS);
    }

    public static Timeout start(Association as, String startMsg, String expiredMsg, String cancelMsg, int timeout) {
        LOG.debug(startMsg, (Object)as, (Object)timeout);
        return new Timeout(as, expiredMsg, cancelMsg, timeout);
    }

    public void stop() {
        LOG.debug(this.cancelMsg, (Object)this.as);
        this.future.cancel(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        Association association = this.as;
        synchronized (association) {
            LOG.info(this.expiredMsg, (Object)this.as);
            this.as.abort();
        }
    }
}

