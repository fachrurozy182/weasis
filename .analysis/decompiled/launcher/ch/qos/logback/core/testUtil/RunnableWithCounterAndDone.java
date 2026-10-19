/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.testUtil;

public abstract class RunnableWithCounterAndDone
implements Runnable {
    protected boolean done = false;
    protected long counter = 0L;

    public long getCounter() {
        return this.counter;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isDone() {
        return this.done;
    }
}

