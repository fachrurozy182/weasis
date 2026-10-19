/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.util;

import ch.qos.logback.core.util.ReentryGuard;

public class ReentryGuardFactory {
    public static ReentryGuard makeGuard(GuardType guardType) {
        switch (guardType.ordinal()) {
            case 0: {
                return new ReentryGuard.ReentryGuardImpl();
            }
            case 1: {
                return new ReentryGuard.NOPRentryGuard();
            }
        }
        throw new IllegalArgumentException("Unknown GuardType: " + String.valueOf((Object)guardType));
    }

    public static enum GuardType {
        THREAD_LOCAL,
        NOP;

    }
}

