/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.gui.task;

public class TaskInterruptionException
extends RuntimeException {
    public TaskInterruptionException() {
    }

    public TaskInterruptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskInterruptionException(String message) {
        super(message);
    }

    public TaskInterruptionException(Throwable cause) {
        super(cause);
    }
}

