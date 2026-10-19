/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.testUtil;

import ch.qos.logback.core.testUtil.RunnableWithCounterAndDone;

public abstract class AbstractMultiThreadedHarness {
    public abstract void waitUntilEndCondition() throws InterruptedException;

    public void execute(RunnableWithCounterAndDone[] runnableArray) throws InterruptedException {
        Thread[] threadArray = new Thread[runnableArray.length];
        for (int i = 0; i < runnableArray.length; ++i) {
            System.out.println("Starting " + String.valueOf(runnableArray[i]));
            threadArray[i] = new Thread((Runnable)runnableArray[i], "Harness[" + i + "]");
        }
        for (Thread thread : threadArray) {
            thread.start();
        }
        this.waitUntilEndCondition();
        for (Runnable runnable : runnableArray) {
            ((RunnableWithCounterAndDone)runnable).setDone(true);
        }
        for (Runnable runnable : threadArray) {
            ((Thread)runnable).join();
        }
    }
}

