/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package org.dcm4che3.net;

import java.io.IOException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.DimseRSP;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FutureDimseRSP
extends DimseRSPHandler
implements DimseRSP {
    static final Logger LOG = LoggerFactory.getLogger(FutureDimseRSP.class);
    private Entry entry = new Entry(null, null);
    private boolean finished;
    private int autoCancel;
    private int remainingCapacity = Integer.MAX_VALUE;
    private IOException ex;

    public FutureDimseRSP(int msgID) {
        super(msgID);
    }

    @Override
    public synchronized void onDimseRSP(Association as, Attributes cmd, Attributes data) {
        super.onDimseRSP(as, cmd, data);
        Entry last = this.entry;
        while (last.next != null) {
            last = last.next;
        }
        last.next = new Entry(cmd, data);
        if (Status.isPending(cmd.getInt(2304, 0))) {
            if (this.autoCancel > 0 && --this.autoCancel == 0) {
                try {
                    super.cancel(as);
                }
                catch (IOException e) {
                    this.ex = e;
                }
            }
        } else {
            this.finished = true;
        }
        this.notifyAll();
        if (!this.finished && --this.remainingCapacity == 0) {
            try {
                LOG.debug("Wait for consuming DIMSE RSP");
                while (this.ex != null && this.remainingCapacity == 0) {
                    this.wait();
                }
                LOG.debug("Stop waiting for consuming DIMSE RSP");
            }
            catch (InterruptedException e) {
                LOG.warn("Failed to wait for consuming DIMSE RSP", (Throwable)e);
            }
        }
    }

    @Override
    public synchronized void onClose(Association as) {
        super.onClose(as);
        if (!this.finished) {
            this.ex = as.getException();
            if (this.ex == null) {
                this.ex = new IOException("Association to " + as.getRemoteAET() + " released before receive of outstanding DIMSE RSP");
            }
            this.notifyAll();
        }
    }

    public synchronized void setAutoCancel(int autoCancel) {
        this.autoCancel = autoCancel;
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity: " + capacity);
        }
        this.remainingCapacity = capacity;
    }

    @Override
    public void cancel(Association a) throws IOException {
        if (this.ex != null) {
            throw this.ex;
        }
        if (!this.finished) {
            super.cancel(a);
        }
    }

    @Override
    public final Attributes getCommand() {
        return this.entry.command;
    }

    @Override
    public final Attributes getDataset() {
        return this.entry.dataset;
    }

    @Override
    public synchronized boolean next() throws IOException, InterruptedException {
        if (this.entry.next == null) {
            if (this.finished) {
                return false;
            }
            if (this.entry.next == null && this.ex == null) {
                LOG.debug("Wait for next DIMSE RSP");
                while (this.entry.next == null && this.ex == null) {
                    this.wait();
                }
                LOG.debug("Stop waiting for next DIMSE RSP");
            }
            if (this.ex != null) {
                throw this.ex;
            }
        }
        this.entry = this.entry.next;
        if (this.remainingCapacity++ == 0) {
            this.notifyAll();
        }
        return true;
    }

    private static class Entry {
        final Attributes command;
        final Attributes dataset;
        Entry next;

        public Entry(Attributes command, Attributes dataset) {
            this.command = command;
            this.dataset = dataset;
        }
    }
}

