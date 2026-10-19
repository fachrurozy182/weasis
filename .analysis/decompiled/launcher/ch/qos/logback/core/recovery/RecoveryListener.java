/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.recovery;

import java.io.IOException;

public interface RecoveryListener {
    public void newFailure(IOException var1);

    public void recoveryOccured();
}

