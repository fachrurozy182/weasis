/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.testUtil;

import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;
import java.util.ArrayList;
import java.util.List;

public class TrivialStatusListener
implements StatusListener,
LifeCycle {
    public List<Status> list = new ArrayList<Status>();
    boolean start = false;

    @Override
    public void addStatusEvent(Status status) {
        if (!this.isStarted()) {
            return;
        }
        this.list.add(status);
    }

    @Override
    public void start() {
        this.start = true;
    }

    @Override
    public void stop() {
        this.start = false;
    }

    @Override
    public boolean isStarted() {
        return this.start;
    }
}

