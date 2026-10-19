/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.testUtil;

import ch.qos.logback.core.read.ListAppender;

public class DelayingListAppender<E>
extends ListAppender<E> {
    public int delay = 1;
    public boolean interrupted = false;

    public void setDelay(int ms) {
        this.delay = ms;
    }

    @Override
    public void append(E e) {
        try {
            Thread.sleep(this.delay);
        }
        catch (InterruptedException ie) {
            this.interrupted = true;
        }
        super.append(e);
    }
}

