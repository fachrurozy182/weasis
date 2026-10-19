/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.rolling;

import ch.qos.logback.core.rolling.SizeAndTimeBasedFileNamingAndTriggeringPolicy;

public class SizeAndTimeBasedFNATP<E>
extends SizeAndTimeBasedFileNamingAndTriggeringPolicy<E> {
    @Override
    public void start() {
        this.addWarn("SizeAndTimeBasedFNATP class was renamed as SizeAndTimeBasedFileNamingAndTriggeringPolicy.");
        super.start();
    }
}

